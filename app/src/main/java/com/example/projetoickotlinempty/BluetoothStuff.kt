package com.example.projetoickotlinempty

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.app.ActivityCompat
import com.example.projetoickotlinempty.MainActivity.Message
import org.json.JSONObject
import java.io.DataOutputStream
import java.io.IOException
import java.io.OutputStream
import kotlin.concurrent.thread


private val PERMISSIONS_STORAGE = arrayOf<String>(
    android.Manifest.permission.READ_EXTERNAL_STORAGE,
    android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
    android.Manifest.permission.ACCESS_FINE_LOCATION,
    android.Manifest.permission.ACCESS_COARSE_LOCATION,
    android.Manifest.permission.ACCESS_LOCATION_EXTRA_COMMANDS,
    android.Manifest.permission.BLUETOOTH_SCAN,
    android.Manifest.permission.BLUETOOTH_CONNECT,
    android.Manifest.permission.BLUETOOTH_PRIVILEGED
)
private val PERMISSIONS_LOCATION = arrayOf<String>(
    android.Manifest.permission.ACCESS_FINE_LOCATION,
    android.Manifest.permission.ACCESS_COARSE_LOCATION,
    android.Manifest.permission.ACCESS_LOCATION_EXTRA_COMMANDS,
    android.Manifest.permission.BLUETOOTH_SCAN,
    android.Manifest.permission.BLUETOOTH_CONNECT,
    android.Manifest.permission.BLUETOOTH_PRIVILEGED
)

class BluetoothStuff : AutoCloseable {

    private val PERMISSIONS_STORAGE = arrayOf<String>(
        android.Manifest.permission.READ_EXTERNAL_STORAGE,
        android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
        android.Manifest.permission.ACCESS_FINE_LOCATION,
        android.Manifest.permission.ACCESS_COARSE_LOCATION,
        android.Manifest.permission.ACCESS_LOCATION_EXTRA_COMMANDS,
        android.Manifest.permission.BLUETOOTH_SCAN,
        android.Manifest.permission.BLUETOOTH_CONNECT,
        android.Manifest.permission.BLUETOOTH_PRIVILEGED
    )
    private val PERMISSIONS_LOCATION = arrayOf<String>(
        android.Manifest.permission.ACCESS_FINE_LOCATION,
        android.Manifest.permission.ACCESS_COARSE_LOCATION,
        android.Manifest.permission.ACCESS_LOCATION_EXTRA_COMMANDS,
        android.Manifest.permission.BLUETOOTH_SCAN,
        android.Manifest.permission.BLUETOOTH_CONNECT,
        android.Manifest.permission.BLUETOOTH_PRIVILEGED
    )

    var bluetoothSocket: BluetoothSocket? = null

    fun serializeMessage(message: Message, stream: OutputStream): Boolean {
        val result = JSONObject()
        result.put("x", message.x)
        result.put("y", message.y)
        result.put("z", message.z)

        val bytes = result.toString().toByteArray(Charsets.UTF_8)
        val newStream = DataOutputStream(stream)

        if (bytes.size >= 4) {
            Log.d("IC Log Tests", "quantidade de bytes: ${bytes.size}")
            return try {
                newStream.writeInt(bytes.size)
                newStream.write(bytes)
                newStream.flush()
                true
            } catch (e: IOException) {
                e.printStackTrace()
                Log.e("Bluetooth", "Erro ao enviar mensagem")
                Thread.sleep(100)  // Espera por 100ms antes de tentar novamente
                false
            }
        }
        else {
            Log.e("Bluetooth", "Mensagem muito curta para ser válida: ${bytes.size} bytes")
            return false  // Mensagem inválida
        }
    }


    fun sendMessage(message: Message) {
        val socket = bluetoothSocket

        if (socket != null) {
            try {
                serializeMessage(message, socket.outputStream)
                Log.d("IC Log Tests", "Serialize message!")
            } catch (e: IOException) {
                Log.e("IC Log Tests", "Erro ao enviar mensagem", e)
                // Tratar erro, talvez tentar reconectar ou notificar o usuário
            }
        } else {
            Log.e("IC Log Tests", "Erro: BluetoothSocket is not connected")
            // tratar erro
        }
    }


    @SuppressLint("MissingPermission")
    fun connectBluetooth(adapter: BluetoothAdapter, onConnect: (BluetoothSocket) -> Unit) {

        // Verifique se o Bluetooth está habilitado
        if (!adapter.isEnabled) {
            Log.e("IC Log Tests", "Bluetooth not enabled")
            return
        }

        val pairedDevices: Set<BluetoothDevice>? = adapter.bondedDevices

        if (pairedDevices == null) {
            Log.d("IC Log Tests", "Couldn't find devices")
            return
        }

        Log.d("IC Log Tests", "Found ${pairedDevices.size} devices")

        // mostrar em interface gráfica lista de dispositivos para usuário selecionar e parear
        val device = pairedDevices.find { it.address == "E0:06:E6:CF:59:D2" }
        // Endereço do meu computador 00:1A:7D:DA:71:12
        // Endereço do notebook do meu pai 9C:58:1F:61:FC:01
        // Endereço do meu notebook E0:06:E6:CF:59:D2

        if (device == null) {
            Log.d("IC Log Tests", "Didn't find computer")
            return
        }

        thread {
            adapter.cancelDiscovery()

            // que horror
            val socket = device.javaClass.getMethod("createRfcommSocket", *arrayOf(Int::class.java))
                .invoke(device, 1) as BluetoothSocket

            Log.d("IC Log Tests", "Socket achieved!")

            socket.connect()

            Log.d("IC Log Tests", "Connected to socket!")

            bluetoothSocket = socket

            onConnect(socket)
        }
    }

    override fun close() {
        try {
            bluetoothSocket?.close()
        } catch (e: IOException) {
            Log.e("Bluetooth", "Erro ao fechar conexões", e)
        }
    }
}