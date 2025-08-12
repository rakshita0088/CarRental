package com.example.car.domain.repository


import kotlinx.coroutines.flow.Flow

interface SpeedRepository {
    /**
     * Observes the speed limit for a vehicle. Emits updates whenever Firebase value changes.
     * Emits cached/default if Firebase read fails or value is null.
     */
    fun observeSpeedLimit(vehicleId: String): Flow<Int?>

    /**
     * Save speed limit to local cache (SharedPreferences). May also update remote if implemented.
     */
    suspend fun saveSpeedLimit(vehicleId: String, speedLimit: Int)
}
