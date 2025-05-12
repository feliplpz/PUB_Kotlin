package br.usp.poli.pocketexperimentalphysics

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
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
import br.usp.poli.pocketexperimentalphysics.connection.BluetoothConnectionManager
import br.usp.poli.pocketexperimentalphysics.sensors.AccelerometerData
import br.usp.poli.pocketexperimentalphysics.sensors.GyroscopeData
import br.usp.poli.pocketexperimentalphysics.sensors.SensorHandler
import br.usp.poli.pocketexperimentalphysics.sensors.interfaces.SensorDataListener


class MainActivity : AppCompatActivity(), BluetoothConnectionManager.DeviceSelectionListener {

    private lateinit var bluetoothManager: BluetoothConnectionManager
    private lateinit var sensorHandler: SensorHandler

    // Elementos da UI (Interface de Usuário)
    private lateinit var connectButton: Button
    private lateinit var statusTextView: TextView
    private var isSendingData = false
    private lateinit var startStopButton: Button

    // Botões para controle dos sensores individuais
    private lateinit var toggleAccelerometerButton: Button
    private lateinit var toggleGyroscopeButton: Button

    // Estado de cada sensor
    private var accelerometerEnabled = false
    private var gyroscopeEnabled = false

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Inicializa os componentes da UI
        connectButton = findViewById(R.id.connectButton)
        statusTextView = findViewById(R.id.statusTextView)
        startStopButton = findViewById(R.id.startStopButton)
        toggleAccelerometerButton = findViewById(R.id.toggleAccelerometerButton)
        toggleGyroscopeButton = findViewById(R.id.toggleGyroscopeButton)

        // Configura estado inicial da UI
        startStopButton.isEnabled = false
        startStopButton.setOnClickListener {
            toggleDataTransmission()
        }

        toggleAccelerometerButton.setOnClickListener {
            accelerometerEnabled = !accelerometerEnabled
            updateSensorToggleButtons()
        }

        toggleGyroscopeButton.setOnClickListener {
            gyroscopeEnabled = !gyroscopeEnabled
            updateSensorToggleButtons()
        }

        // Inicializa o gerenciador de sensores
        sensorHandler = SensorHandler(this)

        // Configura o acelerômetro com listener
        sensorHandler.setupAccelerometer(object : SensorDataListener<AccelerometerData> {
            override fun onDataReceived(data: AccelerometerData) {
                if (bluetoothManager.isConnected() && isSendingData && accelerometerEnabled) {
                    try {
                        Log.d("MainActivity", "Enviando dado do acelerômetro: x=${data.x}, y=${data.y}, z=${data.z}")
                        bluetoothManager.sendSensorData(data)
                    } catch (e: Exception) {
                        Log.e("MainActivity", "Erro ao enviar dados do acelerômetro!", e)
                    }
                }
            }
        })

        // Configura o giroscópio com listener
        sensorHandler.setupGyroscope(object : SensorDataListener<GyroscopeData> {
            override fun onDataReceived(data: GyroscopeData) {
                if (bluetoothManager.isConnected() && isSendingData && gyroscopeEnabled) {
                    try {
                        Log.d("MainActivity", "Enviando dado do giroscópio: x=${data.x}, y=${data.y}, z=${data.z}")
                        bluetoothManager.sendSensorData(data)
                    } catch (e: Exception) {
                        Log.e("MainActivity", "Erro ao enviar dados do giroscópio!", e)
                    }
                }
            }
        })

        // Inicializa o BluetoothManager com seleção de dispositivo
        bluetoothManager = BluetoothConnectionManager(this)
        bluetoothManager.setDeviceSelectionListener(this)

        // Configura o botão de conexão
        connectButton.setOnClickListener {
            if (bluetoothManager.isConnected()) {
                bluetoothManager.disconnect()
            } else {
                bluetoothManager.setupBluetoothConnection { socket ->
                    Log.d("MainActivity", "Conexão de Bluetooth bem-sucedida")
                }
            }
        }

        // Atualiza o estado inicial dos botões
        updateSensorToggleButtons()
    }

    private fun updateSensorToggleButtons() {
        toggleAccelerometerButton.text = getString(R.string.enable_accelerometer)
        toggleGyroscopeButton.text = getString(R.string.enable_gyroscope)
    }

    private fun toggleDataTransmission() {
        isSendingData = !isSendingData

        if (isSendingData) {
            startStopButton.text = getString(R.string.stop_transmission)
            Toast.makeText(this, "Transmissão de dados iniciada", Toast.LENGTH_SHORT).show()
        } else {
            startStopButton.text = getString(R.string.start_transmission)
            Toast.makeText(this, "Transmissão de dados interrompida", Toast.LENGTH_SHORT).show()
        }
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
            statusTextView.text = getString(R.string.connecting_at, selectedDevice.name)
        }

        dialog.show()
    }

    override fun onConnectionStateChanged(connected: Boolean, deviceName: String?) {
        if (connected) {
            statusTextView.text = getString(R.string.connected_to, deviceName)
            connectButton.text = getString(R.string.disconnect)
            startStopButton.isEnabled = true

            // Se já estava transmitindo, interrompe
            if (isSendingData) {
                toggleDataTransmission()
            }
        } else {
            statusTextView.text = getString(R.string.disconnected)
            connectButton.text = getString(R.string.connect)
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
        sensorHandler.cleanup()
        bluetoothManager.close()
        Log.d("MainActivity", "Recursos limpos")
    }
}
