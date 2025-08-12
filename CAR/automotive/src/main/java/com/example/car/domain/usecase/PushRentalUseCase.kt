package com.example.car.domain.usecase

import com.example.car.domain.repository.RentalRepository
import com.example.car.data.car_api.dto.Rental

class PushRentalUseCase(private val repo: RentalRepository) {
    suspend operator fun invoke(rental: Rental) {
        repo.pushRental(rental)
    }
}