package br.usp.poli.pocketexperimentalphysics.sensors

import org.json.JSONObject

/**
 * Represents accelerometer sensor data.
 * @param x Acceleration on X-axis (m/s²)
 * @param y Acceleration on Y-axis (m/s²)
 * @param z Acceleration on Z-axis (m/s²)
 */
data class AccelerometerData(val x: Double, val y: Double, val z: Double) : SensorData() {
    override fun toJson(): JSONObject {
        return JSONObject().apply {
            put("type", "accelerometer")
            put("x", x)
            put("y", y)
            put("z", z)
        }
    }
}