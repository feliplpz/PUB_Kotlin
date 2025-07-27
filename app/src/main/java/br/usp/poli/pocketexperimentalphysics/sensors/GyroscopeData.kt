package br.usp.poli.pocketexperimentalphysics.sensors

import org.json.JSONObject

/**
 * Represents gyroscope sensor data.
 * @param x Angular velocity around X-axis (rad/s)
 * @param y Angular velocity around Y-axis (rad/s)
 * @param z Angular velocity around Z-axis (rad/s)
 */
data class GyroscopeData(val x: Double, val y: Double, val z: Double) : SensorData() {
    override fun toJson(): JSONObject {
        return JSONObject().apply {
            put("type", "gyroscope")
            put("x", x)
            put("y", y)
            put("z", z)
        }
    }
}