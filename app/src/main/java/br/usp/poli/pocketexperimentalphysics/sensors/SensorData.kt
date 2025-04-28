package br.usp.poli.pocketexperimentalphysics.sensors

import org.json.JSONObject

/**
 * Classe abstrata para representar dados de sensores
 * Serve como base para as classes específicas de diferentes tipos de sensores
 */
abstract class SensorData {
    abstract fun toJson(): JSONObject
}