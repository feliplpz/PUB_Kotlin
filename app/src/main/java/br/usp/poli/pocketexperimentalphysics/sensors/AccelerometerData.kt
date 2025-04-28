package br.usp.poli.pocketexperimentalphysics.sensors

import org.json.JSONObject


/**
 * Dados do acelerômetro
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