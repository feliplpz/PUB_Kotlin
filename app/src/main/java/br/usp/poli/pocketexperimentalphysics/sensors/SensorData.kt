package br.usp.poli.pocketexperimentalphysics.sensors

import org.json.JSONObject

/**
 * Abstract class for representing sensor data.
 * Serves as base for specific classes of different sensor types.
 */
abstract class SensorData {
    /**
     * Converts sensor data to JSON format.
     * @return JSONObject containing sensor type and measurement values
     */
    abstract fun toJson(): JSONObject
}