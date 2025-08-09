package com.example.car.data.car_api.dto

//data class VehicleData(
//    var ignitionStatus: IgnitionStatus = IgnitionStatus(),
//    var speed: Speed = Speed(),
//    var rental: Rental = Rental(),
//)
//package com.example.car.data.car_api.dto

/**
 * Combines all vehicle-related data into one model.
 */
data class VehicleData(
    var ignitionStatus: IgnitionStatus = IgnitionStatus(),
    var speed: Speed = Speed(),
    var rental: Rental = Rental()
)
