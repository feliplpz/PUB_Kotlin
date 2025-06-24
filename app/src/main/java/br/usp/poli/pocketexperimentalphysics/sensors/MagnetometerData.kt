package br.usp.poli.pocketexperimentalphysics.sensors

import org.json.JSONObject

/**
 * Dados do magnetômetro
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