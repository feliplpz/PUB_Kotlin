package br.usp.poli.pocketexperimentalphysics

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.util.Log
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresPermission
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.sample
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity(), SensorEventListener,
    BluetoothConnectionManager.DeviceSelectionListener {

    private lateinit var bluetoothManager: BluetoothConnectionManager
    private lateinit var sensorManager: SensorManager

    // Elementos da UI (Interface de Usuário)
    private lateinit var connectButton: Button
    private lateinit var statusTextView: TextView
    private var isSendingData = false
    private lateinit var startStopButton: Button

    // Cria um CoroutineScope para operações no background
    private val coroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    // Cria um flow com regulagem (throttling) para controlar os dados do sensor
    private val sensorDataFlow = MutableSharedFlow<Triple<Double, Double, Double>>()

    // Constantes para a regulagem (throttling) e taxa de amostragem
    private val samplingPeriodUs =
        TimeUnit.MILLISECONDS.toMicros(50) // Taxa de amostragem: intervalo de 50ms para o sensor
    private val throttleIntervalMs = 100L // Envia dados a cada 100ms

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Inicializa os componentes da UI
        connectButton = findViewById(R.id.connectButton)
        statusTextView = findViewById(R.id.statusTextView)
        startStopButton = findViewById(R.id.startStopButton)

        // Configura estado inicial da UI
        startStopButton.isEnabled = false
        startStopButton.setOnClickListener {
            toggleDataTransmission()
        }

        // Initializa o administrador do sensor
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager

        // Registra o acelerômetro com a taxa de amostragem customizável
        val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        if (accelerometer != null) {
            sensorManager.registerListener(
                this, accelerometer, samplingPeriodUs.toInt()
            )
            Log.d("MainActivity", "Accelerometer registered with ${samplingPeriodUs}μs sampling")
        } else {
            Log.e("MainActivity", "No accelerometer found on device")
            Toast.makeText(this, "Este dispositivo não possui acelerômetro", Toast.LENGTH_LONG)
                .show()
        }

        // Faz o setup do sensorData
        setupSensorDataCollection()

        // Inicializa o BluetoothManager com seleção de dispositivo
        bluetoothManager = BluetoothConnectionManager(this)
        bluetoothManager.setDeviceSelectionListener(this)

        // Configura o botão de conexão
        connectButton.setOnClickListener {
            if (bluetoothManager.isConnected()) {
                bluetoothManager.disconnect()
            } else {
                bluetoothManager.setupBluetoothConnection { socket ->
                    Log.d("MainActivity", "Bluetooth connected successfully")
                }
            }
        }
    }

    private fun toggleDataTransmission() {
        isSendingData = !isSendingData

        if (isSendingData) {
            startStopButton.text = R.string.start_transmission.toString()
            Toast.makeText(this, "Iniciando transmissão de dados", Toast.LENGTH_SHORT).show()
        } else {
            startStopButton.text = R.string.stop_transmission.toString()
            Toast.makeText(this, "Transmissão de dados interrompida", Toast.LENGTH_SHORT).show()
        }
    }

    @OptIn(FlowPreview::class)
    private fun setupSensorDataCollection() {
        coroutineScope.launch {
            sensorDataFlow.sample(throttleIntervalMs) //
                .collect { (x, y, z) ->
                    // Apenas envia dados se está conectado e a transmissão está habilitada
                    if (bluetoothManager.isConnected() && isSendingData) {
                        try {
                            Log.d("MainActivity", "Enviando dado: x=$x, y=$y, z=$z")
                            bluetoothManager.sendMessage(
                                BluetoothConnectionManager.Message(x, y, z)
                            )
                        } catch (e: Exception) {
                            Log.e("MainActivity", "Error ao enviar mensagem!", e)
                        }
                    }
                }
        }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event?.let {
            if (it.sensor.type == Sensor.TYPE_ACCELEROMETER) {
                val x = it.values[0].toDouble()
                val y = it.values[1].toDouble()
                val z = it.values[2].toDouble()

                // Emite dados do sensor de para o flow (non-blocking)
                coroutineScope.launch {
                    sensorDataFlow.emit(Triple(x, y, z))
                }
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        Log.d("MainActivity", "Acurácia do sensor mudou: ${sensor?.name}, acurácia: $accuracy")
    }

    // Implementação da interface DeviceSelectionListener
    @SuppressLint("MissingPermission")
    override fun onDevicesAvailable(devices: List<BluetoothDevice>) {
        if (devices.isEmpty()) {
            Toast.makeText(
                this, "Nenhum dispositivo Bluetooth pareado encontrado", Toast.LENGTH_LONG
            ).show()
            return
        }

        // Criar adapter para lista de dispositivos
        val deviceNames = devices.map { it.name ?: "Dispositivo sem nome (${it.address})" }
        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, deviceNames)

        // Criar dialog com lista de dispositivos
        val deviceListView = ListView(this)
        deviceListView.adapter = adapter

        val dialog =
            AlertDialog.Builder(this).setTitle("Selecione um dispositivo").setView(deviceListView)
                .setNegativeButton("Cancelar") { dialog, _ ->
                    dialog.dismiss()
                }.create()

        // Configurar seleção de item
        deviceListView.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
            val selectedDevice = devices[position]
            dialog.dismiss()

            // Conectar ao dispositivo selecionado
            bluetoothManager.connectToSelectedDevice(selectedDevice)
            statusTextView.text = "Conectando a ${selectedDevice.name}..."
        }

        dialog.show()
    }

    override fun onConnectionStateChanged(connected: Boolean, deviceName: String?) {
        if (connected) {
            statusTextView.text = "Conectado a: $deviceName"
            connectButton.text = R.string.disconnect.toString()
            startStopButton.isEnabled = true

            // Se já estava transmitindo, interrompe
            if (isSendingData) {
                toggleDataTransmission()
            }
        } else {
            statusTextView.text = R.string.disconnected.toString()
            connectButton.text = R.string.connect.toString()
            startStopButton.isEnabled = false

            // Se estava transmitindo, interrompe
            if (isSendingData) {
                toggleDataTransmission()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Limpa os recursos
        sensorManager.unregisterListener(this)
        bluetoothManager.close()
        coroutineScope.cancel() // Cancela as Coroutines
        Log.d("MainActivity", "Recursos limpos")
    }
}