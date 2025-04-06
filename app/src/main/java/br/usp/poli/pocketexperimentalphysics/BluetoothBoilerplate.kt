package br.usp.poli.pocketexperimentalphysics

import android.app.Activity.RESULT_OK
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import java.lang.UnsupportedOperationException
import androidx.core.content.ContextCompat.getSystemService
import android.Manifest
import android.content.Context
import androidx.appcompat.app.AppCompatActivity.RESULT_OK

class BluetoothBoilerplate(val activity: AppCompatActivity) {

    var callback: ((BluetoothAdapter) -> Unit)? = null

    fun setup() {
        val bluetoothManager = activity.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        val adapter = bluetoothManager.adapter

        if(hasBluetoothPermission()) {
            Log.d("IC Log Tests", "Has permission")
            callback?.invoke(getAdapter())
        }
        else {
            Log.d("IC Log Tests", "Has no permission")
            requestBluetooth()
        }
    }

    private fun getAdapter(): BluetoothAdapter {
        val bluetoothManager: BluetoothManager? = activity.getSystemService(BluetoothManager::class.java)
        val bluetoothAdapter: BluetoothAdapter? = bluetoothManager?.adapter

        if (bluetoothAdapter ==  null) {
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
                activity,
                android.Manifest.permission.BLUETOOTH_SCAN
            ) == PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(
                        activity,
                        android.Manifest.permission.BLUETOOTH_CONNECT
                    ) == PackageManager.PERMISSION_GRANTED
        } else {
            // Para versões anteriores, a permissão já é concedida pelo manifesto
            true
        }
    }

}