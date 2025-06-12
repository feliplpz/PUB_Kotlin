package br.usp.poli.pocketexperimentalphysics

import android.Manifest
import android.annotation.SuppressLint
import android.app.Dialog
import android.bluetooth.BluetoothDevice
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Window
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ImageButton
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresPermission
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import br.usp.poli.pocketexperimentalphysics.connection.BluetoothConnectionManager
import br.usp.poli.pocketexperimentalphysics.sensors.AccelerometerData
import br.usp.poli.pocketexperimentalphysics.sensors.GyroscopeData
import br.usp.poli.pocketexperimentalphysics.sensors.MagnetometerData
import br.usp.poli.pocketexperimentalphysics.sensors.SensorHandler
import br.usp.poli.pocketexperimentalphysics.sensors.interfaces.SensorDataListener
import com.google.android.material.button.MaterialButton
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.switchmaterial.SwitchMaterial


class MainActivity : AppCompatActivity(), BluetoothConnectionManager.DeviceSelectionListener {

    private lateinit var bluetoothManager: BluetoothConnectionManager
    private lateinit var sensorHandler: SensorHandler

    // Elementos da UI (Interface de Usuário)
    private lateinit var connectButton: MaterialButton
    private lateinit var statusTextView: TextView
    private var isSendingData = false
    private lateinit var startStopButton: MaterialButton

    // Switches para controle dos sensores individuais
    private lateinit var toggleAccelerometerButton: SwitchMaterial
    private lateinit var toggleGyroscopeButton: SwitchMaterial
    private lateinit var toggleMagnetometerButton: SwitchMaterial

    // Botão de informações
    private lateinit var infoButton: FloatingActionButton

    // Dialog de informações
    private var infoDialog: Dialog? = null

    // Estado de cada sensor
    private var accelerometerEnabled = true // Acelerômetro ativado por padrão
    private var gyroscopeEnabled = false   // Giroscópio desativado por padrão
    private var magnetometerEnabled = false // Giroscópio desativado por padrão

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Inicializa os componentes da UI
        initializeViews()

        // Configura estado inicial da UI
        setupInitialState()

        // Configura listeners
        setupListeners()

        // Inicializa o gerenciador de sensores
        setupSensorHandlers()

        // Inicializa o BluetoothManager
        setupBluetoothManager()

        // Atualiza o estado inicial dos switches
        updateSensorSwitches()
    }

    private fun initializeViews() {
        connectButton = findViewById(R.id.connectButton)
        statusTextView = findViewById(R.id.statusTextView)
        startStopButton = findViewById(R.id.startStopButton)
        toggleAccelerometerButton = findViewById(R.id.toggleAccelerometerButton)
        toggleGyroscopeButton = findViewById(R.id.toggleGyroscopeButton)
        toggleMagnetometerButton = findViewById(R.id.toggleMagnetometerButton)
        infoButton = findViewById(R.id.infoButton)
    }

    private fun setupInitialState() {
        startStopButton.isEnabled = false

        // Configura o estado inicial dos switches
        toggleAccelerometerButton.isChecked = accelerometerEnabled
        toggleGyroscopeButton.isChecked = gyroscopeEnabled
        toggleMagnetometerButton.isChecked = magnetometerEnabled

    }

    @SuppressLint("MissingPermission")
    private fun setupListeners() {
        startStopButton.setOnClickListener {
            toggleDataTransmission()
        }

        toggleAccelerometerButton.setOnCheckedChangeListener { _, isChecked ->
            accelerometerEnabled = isChecked
            updateSensorSwitches()
            Log.d("MainActivity", "Acelerômetro ${if (isChecked) "ativado" else "desativado"}")
        }

        toggleGyroscopeButton.setOnCheckedChangeListener { _, isChecked ->
            gyroscopeEnabled = isChecked
            updateSensorSwitches()
            Log.d("MainActivity", "Giroscópio ${if (isChecked) "ativado" else "desativado"}")
        }

        toggleMagnetometerButton.setOnCheckedChangeListener { _, isChecked ->
            magnetometerEnabled = isChecked
            updateSensorSwitches()
            Log.d("MainActivity", "Magnetômetro ${if (isChecked) "ativado" else "desativado"}")
        }

        // Configura o botão de informações
        infoButton.setOnClickListener {
            showInfoDialog()
        }

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
    }

    private fun setupSensorHandlers() {
        sensorHandler = SensorHandler(this)

        // Configura o acelerômetro com listener
        sensorHandler.setupAccelerometer(object : SensorDataListener<AccelerometerData> {
            override fun onDataReceived(data: AccelerometerData) {
                if (bluetoothManager.isConnected() && isSendingData && accelerometerEnabled) {
                    try {
                        Log.d(
                            "MainActivity",
                            "Enviando dado do acelerômetro: x=${data.x}, y=${data.y}, z=${data.z}"
                        )
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
                        Log.d(
                            "MainActivity",
                            "Enviando dado do giroscópio: x=${data.x}, y=${data.y}, z=${data.z}"
                        )
                        bluetoothManager.sendSensorData(data)
                    } catch (e: Exception) {
                        Log.e("MainActivity", "Erro ao enviar dados do giroscópio!", e)
                    }
                }
            }
        })
        sensorHandler.setupMagnetometer(object : SensorDataListener<MagnetometerData> {
            override fun onDataReceived(data: MagnetometerData) {
                if (bluetoothManager.isConnected() && isSendingData && magnetometerEnabled) {
                    try {
                        Log.d("MainActivity", "Enviando dado do magnetômetro: x=${data.x}, y=${data.y}, z=${data.z}")
                        bluetoothManager.sendSensorData(data)
                    } catch (e: Exception) {
                        Log.e("MainActivity", "Erro ao enviar dados do magnetômetro!", e)
                    }
                }
            }
        })
    }

    private fun setupBluetoothManager() {
        bluetoothManager = BluetoothConnectionManager(this)
        bluetoothManager.setDeviceSelectionListener(this)
    }

    private fun updateSensorSwitches() {
        // Atualiza o ícone do botão de start/stop baseado nos sensores ativos
        val hasActiveSensors = accelerometerEnabled || gyroscopeEnabled || magnetometerEnabled

        if (hasActiveSensors && !isSendingData) {
            startStopButton.setIconResource(R.drawable.ic_play)
        } else if (isSendingData) {
            startStopButton.setIconResource(R.drawable.ic_pause)
        }

        // Opcional: Desabilitar o botão se nenhum sensor estiver ativo
        if (bluetoothManager.isConnected()) {
            startStopButton.isEnabled = hasActiveSensors
        }
    }

    private fun toggleDataTransmission() {
        isSendingData = !isSendingData

        if (isSendingData) {
            startStopButton.text = getString(R.string.stop_transmission)
            startStopButton.setIconResource(R.drawable.ic_pause)
            Toast.makeText(this, "Transmissão de dados iniciada", Toast.LENGTH_SHORT).show()
        } else {
            startStopButton.text = getString(R.string.start_transmission)
            startStopButton.setIconResource(R.drawable.ic_play)
            Toast.makeText(this, "Transmissão de dados interrompida", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Mostra o dialog de informações sobre como usar o aplicativo
     */
    private fun showInfoDialog() {
        // Se o dialog já está sendo exibido, não cria outro
        if (infoDialog?.isShowing == true) {
            return
        }

        // Cria o dialog
        infoDialog = Dialog(this)
        infoDialog?.requestWindowFeature(Window.FEATURE_NO_TITLE)

        // Infla o layout customizado
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_info, null)
        infoDialog?.setContentView(dialogView)

        // Configura o dialog para ser cancelável
        infoDialog?.setCancelable(true)
        infoDialog?.setCanceledOnTouchOutside(true)

        // Configura os botões do dialog
        val closeButton = dialogView.findViewById<ImageButton>(R.id.closeButton)
        val gotItButton = dialogView.findViewById<MaterialButton>(R.id.gotItButton)

        closeButton.setOnClickListener {
            dismissInfoDialog()
        }

        gotItButton.setOnClickListener {
            dismissInfoDialog()
        }

        // Configura o tamanho do dialog
        infoDialog?.window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.9).toInt(),
            android.view.ViewGroup.LayoutParams.WRAP_CONTENT
        )

        // Mostra o dialog
        infoDialog?.show()

        Log.d("MainActivity", "Dialog de informações exibido")
    }

    /**
     * Fecha o dialog de informações
     */
    private fun dismissInfoDialog() {
        infoDialog?.dismiss()
        infoDialog = null
        Log.d("MainActivity", "Dialog de informações fechado")
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
            AlertDialog.Builder(this)
                .setTitle("Selecione um dispositivo")
                .setView(deviceListView)
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
            connectButton.setIconResource(R.drawable.ic_bluetooth)

            // Habilita o botão apenas se há sensores ativos
            val hasActiveSensors = accelerometerEnabled || gyroscopeEnabled || magnetometerEnabled
            startStopButton.isEnabled = hasActiveSensors

            // Se já estava transmitindo, interrompe
            if (isSendingData) {
                toggleDataTransmission()
            }
        } else {
            statusTextView.text = getString(R.string.disconnected)
            connectButton.text = getString(R.string.connect)
            connectButton.setIconResource(R.drawable.ic_bluetooth)
            startStopButton.isEnabled = false

            // Se estava transmitindo, interrompe
            if (isSendingData) {
                toggleDataTransmission()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        // Fecha o dialog se estiver aberto
        dismissInfoDialog()

        // Limpa os recursos
        sensorHandler.cleanup()
        bluetoothManager.close()
        Log.d("MainActivity", "Recursos limpos")
    }

    override fun onPause() {
        super.onPause()
        // Opcional: Fechar o dialog ao pausar a activity
        dismissInfoDialog()
    }
}