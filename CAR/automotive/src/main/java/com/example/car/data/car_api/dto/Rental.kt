package com.example.car.data.car_api.dto


/**
 * Represents rental information for a vehicle.
 */
data class Rental(
    val customerId: String? = null,
    val vehicleId: String? = null,
    val maxSpeedLimitKmph: Int? = null,
    val notificationChannel: String? = null,
    val rentalStatus: String? = null
)
