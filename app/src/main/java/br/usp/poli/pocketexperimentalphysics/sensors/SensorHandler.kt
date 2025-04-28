package br.usp.poli.pocketexperimentalphysics.sensors

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import br.usp.poli.pocketexperimentalphysics.sensors.interfaces.SensorDataListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.sample
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

/**
 * Gerenciador de sensores abstraído para suportar múltiplos tipos de sensores
 */
class SensorHandler(private val activity: AppCompatActivity) {
    private val sensorManager: SensorManager = activity.getSystemService(AppCompatActivity.SENSOR_SERVICE) as SensorManager
    private val registeredSensors = mutableMapOf<Int, SensorEventListener>()
    private val coroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    // Flows para os diferentes tipos de sensores
    private val accelerometerFlow = MutableSharedFlow<AccelerometerData>()
    private val gyroscopeFlow = MutableSharedFlow<GyroscopeData>()

    // Constantes para configuração de sensores
    private val samplingPeriodUs = TimeUnit.MILLISECONDS.toMicros(50) // 50ms
    private val throttleIntervalMs = 100L // 100ms

    /**
     * Registra o sensor de acelerômetro e configura o fluxo de dados
     */
    @OptIn(FlowPreview::class)
    fun setupAccelerometer(listener: SensorDataListener<AccelerometerData>) {
        val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        if (accelerometer != null) {
            val accelerometerListener = createSensorEventListener { event ->
                val x = event.values[0].toDouble()
                val y = event.values[1].toDouble()
                val z = event.values[2].toDouble()

                coroutineScope.launch {
                    accelerometerFlow.emit(AccelerometerData(x, y, z))
                }
            }

            sensorManager.registerListener(
                accelerometerListener,
                accelerometer,
                samplingPeriodUs.toInt()
            )

            registeredSensors[Sensor.TYPE_ACCELEROMETER] = accelerometerListener

            Log.d("SensorHandler", "Accelerometer registered with ${samplingPeriodUs}μs sampling")

            // Configura o flow para emitir dados com regulagem (throttling)
            coroutineScope.launch {
                accelerometerFlow.sample(throttleIntervalMs)
                    .collect { data ->
                        listener.onDataReceived(data)
                    }
            }
        } else {
            Log.e("SensorHandler", "No accelerometer found on device")
        }
    }

    /**
     * Registra o sensor de giroscópio e configura o fluxo de dados
     */
    @OptIn(FlowPreview::class)
    fun setupGyroscope(listener: SensorDataListener<GyroscopeData>) {
        val gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)

        if (gyroscope != null) {
            val gyroscopeListener = createSensorEventListener { event ->
                val x = event.values[0].toDouble()
                val y = event.values[1].toDouble()
                val z = event.values[2].toDouble()

                coroutineScope.launch {
                    gyroscopeFlow.emit(GyroscopeData(x, y, z))
                }
            }

            sensorManager.registerListener(
                gyroscopeListener,
                gyroscope,
                samplingPeriodUs.toInt()
            )

            registeredSensors[Sensor.TYPE_GYROSCOPE] = gyroscopeListener

            Log.d("SensorHandler", "Gyroscope registered with ${samplingPeriodUs}μs sampling")

            // Configura o flow para emitir dados com regulagem (throttling)
            coroutineScope.launch {
                gyroscopeFlow.sample(throttleIntervalMs)
                    .collect { data ->
                        listener.onDataReceived(data)
                    }
            }
        } else {
            Log.e("SensorHandler", "No gyroscope found on device")
        }
    }

    /**
     * Cria um listener de eventos de sensor genérico
     */
    private fun createSensorEventListener(onSensorChanged: (SensorEvent) -> Unit): SensorEventListener {
        return object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent?) {
                event?.let { onSensorChanged(it) }
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
                Log.d("SensorHandler", "Sensor accuracy changed: ${sensor?.name}, accuracy: $accuracy")
            }
        }
    }

    /**
     * Libera todos os recursos e cancela os listeners
     */
    fun cleanup() {
        registeredSensors.values.forEach { listener ->
            sensorManager.unregisterListener(listener)
        }
        registeredSensors.clear()
        coroutineScope.cancel()
        Log.d("SensorHandler", "All sensor resources cleaned up")
    }
}