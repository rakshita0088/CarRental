package com.example.car.domain.usecase


import com.example.car.domain.repository.SpeedRepository
import kotlinx.coroutines.flow.Flow

class ObserveSpeedLimitUseCase(private val repo: SpeedRepository) {
    operator fun invoke(vehicleId: String): Flow<Int?> = repo.observeSpeedLimit(vehicleId)
}
