package br.usp.poli.pocketexperimentalphysics

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
import org.json.JSONObject
import java.io.DataOutputStream
import java.io.IOException
import java.io.OutputStream
import kotlin.concurrent.thread


class BluetoothConnectionManager(private val activity: AppCompatActivity) : AutoCloseable {

    data class Message(
        val x: Double, val y: Double, val z: Double
    )

    private var bluetoothSocket: BluetoothSocket? = null
    private var connectionCallback: ((BluetoothSocket) -> Unit)? = null

    // Interface para notificar a UI sobre dispositivos disponíveis
    interface DeviceSelectionListener {
        fun onDevicesAvailable(devices: List<BluetoothDevice>)
        fun onConnectionStateChanged(connected: Boolean, deviceName: String?)
    }

    private var deviceSelectionListener: DeviceSelectionListener? = null

    fun isConnected(): Boolean {
        return bluetoothSocket?.isConnected == true
    }

    //  Define o listener de seleção de dispositivos
    fun setDeviceSelectionListener(listener: DeviceSelectionListener) {
        deviceSelectionListener = listener
    }

    //  Lista os dispositivos pareados
    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    fun listPairedDevices(): List<BluetoothDevice> {
        val bluetoothManager =
            activity.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        val adapter = bluetoothManager.adapter

        if (!adapter.isEnabled) {
            Log.e("BluetoothManager", "Bluetooth not enabled")
            return emptyList()
        }

        return adapter.bondedDevices.toList()
    }

    //  Inicia processo de busca e conexão
    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    fun setupBluetoothConnection(onConnected: (BluetoothSocket) -> Unit) {
        connectionCallback = onConnected
        val bluetoothManager =
            activity.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothManager.adapter

        if (hasBluetoothPermission()) {
            Log.d("BluetoothManager", "Bluetooth permission granted")

            // Em vez de conectar diretamente, notificamos a UI sobre dispositivos disponíveis
            val pairedDevices = listPairedDevices()
            deviceSelectionListener?.onDevicesAvailable(pairedDevices)

        } else {
            Log.d("BluetoothManager", "Requesting Bluetooth permission")
            requestBluetoothPermission()
        }
    }

    //  Conecta ao dispositivo selecionado pelo usuário
    @SuppressLint("MissingPermission")
    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    fun connectToSelectedDevice(device: BluetoothDevice) {
        val bluetoothManager =
            activity.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        val adapter = bluetoothManager.adapter

        thread @RequiresPermission(Manifest.permission.BLUETOOTH_SCAN) {
            adapter.cancelDiscovery()

            try {
                val socket = device.javaClass.getMethod(
                    "createRfcommSocket", *arrayOf(Int::class.java)
                ).invoke(device, 1) as BluetoothSocket

                socket.connect()
                bluetoothSocket = socket

                // Notifica sobre mudança de estado de conexão
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

    // Lida com validações de permissão
    private fun hasBluetoothPermission(): Boolean {
        return ActivityCompat.checkSelfPermission(
            activity, Manifest.permission.BLUETOOTH_SCAN
        ) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
            activity, Manifest.permission.BLUETOOTH_CONNECT
        ) == PackageManager.PERMISSION_GRANTED
    }

    // Requisita permissões de Bluetooth
    @SuppressLint("MissingPermission")
    private val permissionLauncher =
        activity.registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) @RequiresPermission(
            Manifest.permission.BLUETOOTH_CONNECT
        ) { permissions ->
            if (permissions.entries.all { it.value }) {
                Log.d("BluetoothManager", "Permissions granted")

                // Notificar sobre dispositivos disponíveis após obter permissões
                val pairedDevices = listPairedDevices()
                deviceSelectionListener?.onDevicesAvailable(pairedDevices)

            } else {
                Log.d("BluetoothManager", "Permissions denied")
            }
        }

    // Requisita permissões de Bluetooth
    private fun requestBluetoothPermission() {
        permissionLauncher.launch(
            arrayOf(
                Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT
            )
        )
    }

    // Envia mensagem serializada via Bluetooth
    fun sendMessage(message: Message) {
        val socket = bluetoothSocket ?: run {
            Log.e("BluetoothManager", "No active Bluetooth connection")
            return
        }

        try {
            serializeMessage(message, socket.outputStream)
            Log.d("BluetoothManager", "Message sent successfully")
        } catch (e: IOException) {
            Log.e("BluetoothManager", "Error sending message", e)

            // Notifica sobre desconexão
            activity.runOnUiThread {
                deviceSelectionListener?.onConnectionStateChanged(false, null)
            }
        }
    }

    // Serializa a mensagem para JSON e envia para o output stream
    private fun serializeMessage(message: Message, stream: OutputStream): Boolean {
        val result = JSONObject().apply {
            put("x", message.x)
            put("y", message.y)
            put("z", message.z)
        }

        val bytes = result.toString().toByteArray(Charsets.UTF_8)
        val dataStream = DataOutputStream(stream)

        return if (bytes.size >= 4) {
            try {
                dataStream.writeInt(bytes.size)
                dataStream.write(bytes)
                dataStream.flush()
                true
            } catch (e: IOException) {
                e.printStackTrace()
                Log.e("BluetoothManager", "Message send error")
                Thread.sleep(100)
                false
            }
        } else {
            Log.e("BluetoothManager", "Invalid message length: ${bytes.size} bytes")
            false
        }
    }

    // Desconecta do dispositivo atual
    fun disconnect() {
        try {
            bluetoothSocket?.close()
            bluetoothSocket = null

            // Notifica sobre desconexão
            activity.runOnUiThread {
                deviceSelectionListener?.onConnectionStateChanged(false, null)
            }

            Log.d("BluetoothManager", "Disconnected")
        } catch (e: IOException) {
            Log.e("BluetoothManager", "Error disconnecting", e)
        }
    }

    // Fecha o socket do Bluetooth
    override fun close() {
        disconnect()
    }

}
