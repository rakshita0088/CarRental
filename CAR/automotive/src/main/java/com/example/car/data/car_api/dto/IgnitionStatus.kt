package com.example.car.data.car_api.dto

//class IgnitionStatus (
//    val propertyId: Int = 0,
//    val status: String = "Unknown",
//    val unit: String = "percent",
//    val value: Int = 0
//)
//package com.example.car.data.car_api.dto

/**
 * Represents the ignition status of the vehicle.
 */
data class IgnitionStatus(
    val propertyId: Int = 0,
    val status: String = "Unknown",
    val unit: String = "STATE",
    val value: Int = 0
)
