package br.usp.poli.pocketexperimentalphysics.connection

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresPermission
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import br.usp.poli.pocketexperimentalphysics.sensors.AccelerometerData
import br.usp.poli.pocketexperimentalphysics.sensors.SensorData
import java.io.DataOutputStream
import java.io.IOException
import java.io.OutputStream
import kotlin.concurrent.thread

/**
 * Manages Bluetooth messaging with support for multiple sensor types.
 */
class BluetoothConnectionManager(private val activity: AppCompatActivity) : AutoCloseable {

    private var bluetoothSocket: BluetoothSocket? = null
    private var connectionCallback: ((BluetoothSocket) -> Unit)? = null

    /**
     * Interface for notifying UI about available devices and connection state changes.
     */
    interface DeviceSelectionListener {
        fun onDevicesAvailable(devices: List<BluetoothDevice>)
        fun onConnectionStateChanged(connected: Boolean, deviceName: String?)
    }

    private var deviceSelectionListener: DeviceSelectionListener? = null

    fun isConnected(): Boolean {
        return bluetoothSocket?.isConnected == true
    }

    fun setDeviceSelectionListener(listener: DeviceSelectionListener) {
        deviceSelectionListener = listener
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    fun listPairedDevices(): List<BluetoothDevice> {
        val bluetoothManager = activity.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        val adapter = bluetoothManager.adapter

        if (!adapter.isEnabled) {
            Log.e("BluetoothManager", "Bluetooth not enabled")
            return emptyList()
        }

        return adapter.bondedDevices.toList()
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    fun setupBluetoothConnection(onConnected: (BluetoothSocket) -> Unit) {
        connectionCallback = onConnected
        val bluetoothManager = activity.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothManager.adapter

        if (hasBluetoothPermission()) {
            Log.d("BluetoothManager", "Bluetooth permission granted")

            val pairedDevices = listPairedDevices()
            deviceSelectionListener?.onDevicesAvailable(pairedDevices)

        } else {
            Log.d("BluetoothManager", "Requesting Bluetooth permission")
            requestBluetoothPermission()
        }
    }

    @SuppressLint("MissingPermission")
    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    fun connectToSelectedDevice(device: BluetoothDevice) {
        val bluetoothManager = activity.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        val adapter = bluetoothManager.adapter

        thread @RequiresPermission(Manifest.permission.BLUETOOTH_SCAN) {
            adapter.cancelDiscovery()

            try {
                val socket = device.javaClass.getMethod(
                    "createRfcommSocket", *arrayOf(Int::class.java)
                ).invoke(device, 1) as BluetoothSocket

                socket.connect()
                bluetoothSocket = socket

                activity.runOnUiThread {
                    deviceSelectionListener?.onConnectionStateChanged(true, device.name)
                    connectionCallback?.invoke(socket)
                }

                Log.d("BluetoothManager", "Connected to ${device.name}")
            } catch (e: Exception) {
                Log.e("BluetoothManager", "Connection failed", e)
                activity.runOnUiThread {
                    deviceSelectionListener?.onConnectionStateChanged(false, device.name)
                }
            }
        }
    }

    private fun hasBluetoothPermission(): Boolean {
        return ActivityCompat.checkSelfPermission(
            activity, Manifest.permission.BLUETOOTH_SCAN
        ) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
            activity, Manifest.permission.BLUETOOTH_CONNECT
        ) == PackageManager.PERMISSION_GRANTED
    }

    @SuppressLint("MissingPermission")
    private val permissionLauncher =
        activity.registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) @RequiresPermission(
            Manifest.permission.BLUETOOTH_CONNECT
        ) { permissions ->
            if (permissions.entries.all { it.value }) {
                Log.d("BluetoothManager", "Permissions granted")

                val pairedDevices = listPairedDevices()
                deviceSelectionListener?.onDevicesAvailable(pairedDevices)

            } else {
                Log.d("BluetoothManager", "Permissions denied")
            }
        }

    private fun requestBluetoothPermission() {
        permissionLauncher.launch(
            arrayOf(
                Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT
            )
        )
    }

    fun sendSensorData(data: SensorData) {
        val socket = bluetoothSocket ?: run {
            Log.e("BluetoothManager", "No active Bluetooth connection")
            return
        }

        try {
            serializeData(data, socket.outputStream)
            Log.d("BluetoothManager", "Data sent successfully: ${data.javaClass.simpleName}")
        } catch (e: IOException) {
            Log.e("BluetoothManager", "Error sending data", e)

            activity.runOnUiThread {
                deviceSelectionListener?.onConnectionStateChanged(false, null)
            }
        }
    }

    fun sendMessage(message: Message) {
        sendSensorData(AccelerometerData(message.x, message.y, message.z))
    }

    data class Message(
        val x: Double, val y: Double, val z: Double
    )

    private fun serializeData(data: SensorData, stream: OutputStream): Boolean {
        val jsonObject = data.toJson()
        val bytes = jsonObject.toString().toByteArray(Charsets.UTF_8)
        val dataStream = DataOutputStream(stream)

        return if (bytes.size >= 4) {
            try {
                dataStream.writeInt(bytes.size)
                dataStream.write(bytes)
                dataStream.flush()
                true
            } catch (e: IOException) {
                e.printStackTrace()
                Log.e("BluetoothManager", "Data send error")
                Thread.sleep(100)
                false
            }
        } else {
            Log.e("BluetoothManager", "Invalid data length: ${bytes.size} bytes")
            false
        }
    }

    fun disconnect() {
        try {
            bluetoothSocket?.close()
            bluetoothSocket = null

            activity.runOnUiThread {
                deviceSelectionListener?.onConnectionStateChanged(false, null)
            }

            Log.d("BluetoothManager", "Disconnected")
        } catch (e: IOException) {
            Log.e("BluetoothManager", "Error disconnecting", e)
        }
    }

    override fun close() {
        disconnect()
    }
}