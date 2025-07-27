package br.usp.poli.pocketexperimentalphysics.sensors.interfaces

/**
 * Interface for sensor data listeners.
 * Allows classes to implement specific treatments for different sensor types.
 */
interface SensorDataListener<T> {
    /**
     * Called when sensor data is received.
     * @param data The sensor data received
     */
    fun onDataReceived(data: T)
}