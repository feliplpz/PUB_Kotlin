package com.example.projetoickotlinempty

import android.annotation.SuppressLint
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.app.ActivityCompat
import com.example.projetoickotlinempty.ui.theme.ProjetoICKotlinEmptyTheme
import kotlin.concurrent.thread

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity(), SensorEventListener {

    val stuff: BluetoothStuff = BluetoothStuff()

    lateinit var sensorManager: SensorManager

    private val handler = Handler(Looper.getMainLooper()) // Handler na UI thread
    private val sendInterval: Long = 500 // Intervalo de 500 ms (0.5 segundos)

    data class Message(
        val x: Double,
        val y: Double,
        val z: Double
    )

    fun onConnect() {
        // código que será executado quando/se a conexão bluetooth for estabelecida
        stuff.sendMessage(Message(1.0, 2.0, 3.0))
    }

    override fun onDestroy() {
        super.onDestroy()
        this.stuff.close()
        sensorManager.unregisterListener(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        sensorManager.registerListener(
            this,
            sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
            SensorManager.SENSOR_DELAY_NORMAL
        )

        val helper = BluetoothBoilerplate(this)

        helper.callback = { adapter ->
            stuff.connectBluetooth(adapter) {
                onConnect()
            }
        }

        helper.setup()

    }

    override fun onSensorChanged(event: SensorEvent?) {
        event?.let{
            val x = it.values[0].toDouble()
            val y = it.values[1].toDouble()
            val z = it.values[2].toDouble()

            stuff.sendMessage(Message(x,y,z))
            Thread.sleep(100)
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
    }
}