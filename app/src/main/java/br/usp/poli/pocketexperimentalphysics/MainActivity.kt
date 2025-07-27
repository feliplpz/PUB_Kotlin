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

    private lateinit var connectButton: MaterialButton
    private lateinit var statusTextView: TextView
    private var isSendingData = false
    private lateinit var startStopButton: MaterialButton

    private lateinit var toggleAccelerometerButton: SwitchMaterial
    private lateinit var toggleGyroscopeButton: SwitchMaterial
    private lateinit var toggleMagnetometerButton: SwitchMaterial

    private lateinit var infoButton: FloatingActionButton

    private var infoDialog: Dialog? = null

    private var accelerometerEnabled = true // Accelerometer enabled by default
    private var gyroscopeEnabled = false   // Gyroscope disabled by default
    private var magnetometerEnabled = false // Magnetometer disabled by default

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initializeViews()
        setupInitialState()
        setupListeners()
        setupSensorHandlers()
        setupBluetoothManager()
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
            Log.d("MainActivity", "Accelerometer ${if (isChecked) "enabled" else "disabled"}")
        }

        toggleGyroscopeButton.setOnCheckedChangeListener { _, isChecked ->
            gyroscopeEnabled = isChecked
            updateSensorSwitches()
            Log.d("MainActivity", "Gyroscope ${if (isChecked) "enabled" else "disabled"}")
        }

        toggleMagnetometerButton.setOnCheckedChangeListener { _, isChecked ->
            magnetometerEnabled = isChecked
            updateSensorSwitches()
            Log.d("MainActivity", "Magnetometer ${if (isChecked) "enabled" else "disabled"}")
        }

        infoButton.setOnClickListener {
            showInfoDialog()
        }

        connectButton.setOnClickListener {
            if (bluetoothManager.isConnected()) {
                bluetoothManager.disconnect()
            } else {
                bluetoothManager.setupBluetoothConnection { socket ->
                    Log.d("MainActivity", "Bluetooth connection successful")
                }
            }
        }
    }

    private fun setupSensorHandlers() {
        sensorHandler = SensorHandler(this)

        sensorHandler.setupAccelerometer(object : SensorDataListener<AccelerometerData> {
            override fun onDataReceived(data: AccelerometerData) {
                if (bluetoothManager.isConnected() && isSendingData && accelerometerEnabled) {
                    try {
                        Log.d(
                            "MainActivity",
                            "Sending accelerometer data: x=${data.x}, y=${data.y}, z=${data.z}"
                        )
                        bluetoothManager.sendSensorData(data)
                    } catch (e: Exception) {
                        Log.e("MainActivity", "Error sending accelerometer data", e)
                    }
                }
            }
        })

        sensorHandler.setupGyroscope(object : SensorDataListener<GyroscopeData> {
            override fun onDataReceived(data: GyroscopeData) {
                if (bluetoothManager.isConnected() && isSendingData && gyroscopeEnabled) {
                    try {
                        Log.d(
                            "MainActivity",
                            "Sending gyroscope data: x=${data.x}, y=${data.y}, z=${data.z}"
                        )
                        bluetoothManager.sendSensorData(data)
                    } catch (e: Exception) {
                        Log.e("MainActivity", "Error sending gyroscope data", e)
                    }
                }
            }
        })
        sensorHandler.setupMagnetometer(object : SensorDataListener<MagnetometerData> {
            override fun onDataReceived(data: MagnetometerData) {
                if (bluetoothManager.isConnected() && isSendingData && magnetometerEnabled) {
                    try {
                        Log.d("MainActivity", "Sending magnetometer data: x=${data.x}, y=${data.y}, z=${data.z}")
                        bluetoothManager.sendSensorData(data)
                    } catch (e: Exception) {
                        Log.e("MainActivity", "Error sending magnetometer data", e)
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
        val hasActiveSensors = accelerometerEnabled || gyroscopeEnabled || magnetometerEnabled

        if (hasActiveSensors && !isSendingData) {
            startStopButton.setIconResource(R.drawable.ic_play)
        } else if (isSendingData) {
            startStopButton.setIconResource(R.drawable.ic_pause)
        }

        if (bluetoothManager.isConnected()) {
            startStopButton.isEnabled = hasActiveSensors
        }
    }

    private fun toggleDataTransmission() {
        isSendingData = !isSendingData

        if (isSendingData) {
            startStopButton.text = getString(R.string.stop_transmission)
            startStopButton.setIconResource(R.drawable.ic_pause)
            Toast.makeText(this, "Data transmission started", Toast.LENGTH_SHORT).show()
        } else {
            startStopButton.text = getString(R.string.start_transmission)
            startStopButton.setIconResource(R.drawable.ic_play)
            Toast.makeText(this, "Data transmission stopped", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Shows information dialog about how to use the application.
     */
    private fun showInfoDialog() {
        if (infoDialog?.isShowing == true) {
            return
        }

        infoDialog = Dialog(this)
        infoDialog?.requestWindowFeature(Window.FEATURE_NO_TITLE)

        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_info, null)
        infoDialog?.setContentView(dialogView)

        infoDialog?.setCancelable(true)
        infoDialog?.setCanceledOnTouchOutside(true)

        val closeButton = dialogView.findViewById<ImageButton>(R.id.closeButton)
        val gotItButton = dialogView.findViewById<MaterialButton>(R.id.gotItButton)

        closeButton.setOnClickListener {
            dismissInfoDialog()
        }

        gotItButton.setOnClickListener {
            dismissInfoDialog()
        }

        infoDialog?.window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.9).toInt(),
            android.view.ViewGroup.LayoutParams.WRAP_CONTENT
        )

        infoDialog?.show()

        Log.d("MainActivity", "Information dialog displayed")
    }

    private fun dismissInfoDialog() {
        infoDialog?.dismiss()
        infoDialog = null
        Log.d("MainActivity", "Information dialog closed")
    }

    @SuppressLint("MissingPermission")
    override fun onDevicesAvailable(devices: List<BluetoothDevice>) {
        if (devices.isEmpty()) {
            Toast.makeText(
                this, "No paired Bluetooth devices found", Toast.LENGTH_LONG
            ).show()
            return
        }

        val deviceNames = devices.map { it.name ?: "Unnamed device (${it.address})" }
        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, deviceNames)

        val deviceListView = ListView(this)
        deviceListView.adapter = adapter

        val dialog =
            AlertDialog.Builder(this)
                .setTitle("Select a device")
                .setView(deviceListView)
                .setNegativeButton("Cancel") { dialog, _ ->
                    dialog.dismiss()
                }.create()

        deviceListView.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
            val selectedDevice = devices[position]
            dialog.dismiss()

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

            val hasActiveSensors = accelerometerEnabled || gyroscopeEnabled || magnetometerEnabled
            startStopButton.isEnabled = hasActiveSensors

            if (isSendingData) {
                toggleDataTransmission()
            }
        } else {
            statusTextView.text = getString(R.string.disconnected)
            connectButton.text = getString(R.string.connect)
            connectButton.setIconResource(R.drawable.ic_bluetooth)
            startStopButton.isEnabled = false

            if (isSendingData) {
                toggleDataTransmission()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        dismissInfoDialog()

        sensorHandler.cleanup()
        bluetoothManager.close()
        Log.d("MainActivity", "Resources cleaned up")
    }

    override fun onPause() {
        super.onPause()
        dismissInfoDialog()
    }
}