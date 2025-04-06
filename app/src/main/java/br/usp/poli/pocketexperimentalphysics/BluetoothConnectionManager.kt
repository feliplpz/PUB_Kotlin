package br.usp.poli.pocketexperimentalphysics

import android.app.Activity.RESULT_OK
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import br.usp.poli.pocketexperimentalphysics.MainActivity.Message
import org.json.JSONObject
import java.io.DataOutputStream
import java.io.IOException
import java.io.OutputStream
import kotlin.concurrent.thread


class BluetoothConnectionManager(private val activity: AppCompatActivity) : AutoCloseable {

    var bluetoothSocket: BluetoothSocket? = null
    var callback: ((BluetoothAdapter) -> Unit)? = null

    fun setup() {
        val bluetoothManager =
            activity.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothManager.adapter

        if (hasBluetoothPermission()) {
            Log.d("IC Log Tests", "Has permission")
            callback?.invoke(getAdapter())
        } else {
            Log.d("IC Log Tests", "Has no permission")
            requestBluetooth()
        }
    }

    private fun getAdapter(): BluetoothAdapter {
        val bluetoothManager: BluetoothManager? =
            activity.getSystemService(BluetoothManager::class.java)
        val bluetoothAdapter: BluetoothAdapter? = bluetoothManager?.adapter

        if (bluetoothAdapter == null) {
            Log.e("IC Log Tests", "No adapter found")
            // TODO: tratar erro
            throw UnsupportedOperationException("No BluetoothManager Available")
        }
        Log.d("IC Log Tests", "Adapter found")
        return bluetoothAdapter
    }

    private val requestMultiplePermissions =
        activity.registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            if (permissions.entries.all { it.value }) {
                Log.d("IC Log Tests", "Multiple Permissions Success")

                callback?.invoke(getAdapter())
            } else {
                Log.d("IC Log Tests", "Multiple Permissions Failure")
            }
        }

    private val requestEnableBluetooth =
        activity.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                Log.d("IC Log Tests", "Enable Bluetooth OK")

                callback?.invoke(getAdapter())
            } else {
                Log.e("IC Log Tests", "Enable Bluetooth Failure")
            }
        }

    private fun requestBluetooth() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            requestMultiplePermissions.launch(
                arrayOf(
                    android.Manifest.permission.BLUETOOTH_SCAN,
                    android.Manifest.permission.BLUETOOTH_CONNECT
                )
            )
        } else {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            requestEnableBluetooth.launch(enableBtIntent)
        }
    }

    private fun hasBluetoothPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            // Verifique permissões específicas para Bluetooth em Android 12 e superior
            ActivityCompat.checkSelfPermission(
                activity, android.Manifest.permission.BLUETOOTH_SCAN
            ) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                activity, android.Manifest.permission.BLUETOOTH_CONNECT
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            // Para versões anteriores, a permissão já é concedida pelo manifesto
            true
        }
    }

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
        } else {
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
        val device = pairedDevices.find { it.address == "8C:17:59:26:93:E5" }
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
