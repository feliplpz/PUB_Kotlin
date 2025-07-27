package br.usp.poli.pocketexperimentalphysics.sensors

import org.json.JSONObject

/**
 * Represents magnetometer sensor data.
 * @param x Magnetic field strength on X-axis (μT)
 * @param y Magnetic field strength on Y-axis (μT)
 * @param z Magnetic field strength on Z-axis (μT)
 */
data class MagnetometerData(val x: Double, val y: Double, val z: Double) : SensorData() {
    override fun toJson(): JSONObject {
        return JSONObject().apply {
            put("type", "magnetometer")
            put("x", x)
            put("y", y)
            put("z", z)
        }
    }
}