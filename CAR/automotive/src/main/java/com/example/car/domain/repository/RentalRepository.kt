package com.example.car.domain.repository


import com.example.car.data.car_api.dto.Rental

interface RentalRepository {
    /**
     * Push rental data (e.g., when speed exceeded)
     */
    suspend fun pushRental(rental: Rental)
}
