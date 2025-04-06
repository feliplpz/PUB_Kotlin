package br.usp.poli.pocketexperimentalphysics

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity(), SensorEventListener {

    val bluetoothConnectionManager: BluetoothConnectionManager = BluetoothConnectionManager(this)

    lateinit var sensorManager: SensorManager

    private val handler = Handler(Looper.getMainLooper()) // Handler na UI thread
    private val sendInterval: Long = 500 // Intervalo de 500 ms (0.5 segundos)

    data class Message(
        val x: Double, val y: Double, val z: Double
    )

    fun onConnect() {
        // código que será executado quando/se a conexão bluetooth for estabelecida
        bluetoothConnectionManager.sendMessage(Message(1.0, 2.0, 3.0))
    }

    override fun onDestroy() {
        super.onDestroy()
        this.bluetoothConnectionManager.close()
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

        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        sensorManager.registerListener(
            this,
            sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
            SensorManager.SENSOR_DELAY_NORMAL
        )

        bluetoothConnectionManager.callback = { adapter ->
            bluetoothConnectionManager.connectBluetooth(adapter) {
                onConnect()
            }
        }

        bluetoothConnectionManager.setup()

    }

    override fun onSensorChanged(event: SensorEvent?) {
        event?.let {
            val x = it.values[0].toDouble()
            val y = it.values[1].toDouble()
            val z = it.values[2].toDouble()

            bluetoothConnectionManager.sendMessage(Message(x, y, z))
            Thread.sleep(100)
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
    }
}