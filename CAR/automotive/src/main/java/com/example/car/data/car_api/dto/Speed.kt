package com.example.car.data.car_api.dto


/**
 * Represents the speed of the vehicle.
 */
data class Speed(
    val propertyId: Int = 0,
    val status: String = "Unknown",
    val unit: String = "km/h",
    val value: Int = 0
)