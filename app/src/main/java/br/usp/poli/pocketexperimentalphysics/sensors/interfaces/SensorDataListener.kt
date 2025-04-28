package br.usp.poli.pocketexperimentalphysics.sensors.interfaces

/**
 * Interface para listener de dados de sensor
 * Permite que classes implementem tratamentos específicos para diferentes tipos de sensores
 */
interface SensorDataListener<T> {
    fun onDataReceived(data: T)
}