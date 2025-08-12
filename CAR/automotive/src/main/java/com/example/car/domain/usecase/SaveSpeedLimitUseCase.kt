package com.example.car.domain.usecase


import com.example.car.domain.repository.SpeedRepository

class SaveSpeedLimitUseCase(private val repo: SpeedRepository) {
    suspend operator fun invoke(vehicleId: String, speedLimit: Int) {
        repo.saveSpeedLimit(vehicleId, speedLimit)
    }
}