package br.usp.poli.pocketexperimentalphysics.sensors

import org.json.JSONObject

/**
 * Dados do giroscópio
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