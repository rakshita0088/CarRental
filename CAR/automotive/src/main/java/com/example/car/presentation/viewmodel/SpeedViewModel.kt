package com.example.car.presentation.viewmodel



import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.car.domain.usecase.ObserveSpeedLimitUseCase
import com.example.car.domain.usecase.PushRentalUseCase
import com.example.car.domain.usecase.SaveSpeedLimitUseCase
import com.example.car.data.car_api.dto.Rental
import com.example.car.data.car_api.remote.GetVehicleData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * Thin ViewModel: observes speed limit via Flow and exposes StateFlow to UI.
 * Also provides functions to push rental data via PushRentalUseCase.
 *
 * This ViewModel purposely does not directly depend on Android framework classes
 * except for logging — data access lives in data layer.
 */
class SpeedViewModel(
    private val observeSpeedLimitUseCase: ObserveSpeedLimitUseCase,
    private val saveSpeedLimitUseCase: SaveSpeedLimitUseCase,
    private val pushRentalUseCase: PushRentalUseCase,
    // Optional: we use the existing GetVehicleData helper from your codebase to collect vehicle properties
    private val getVehicleData: GetVehicleData
) : ViewModel() {

    private val _speedLimit = MutableStateFlow<Int?>(null)
    val speedLimit: StateFlow<Int?> = _speedLimit

    // Expose vehicle data as MutableState (keeps same behavior as earlier)
    private val _vehicleData = MutableStateFlow<com.example.car.data.car_api.dto.VehicleData?>(null)
    val vehicleData: StateFlow<com.example.car.data.car_api.dto.VehicleData?> = _vehicleData

    private val vehicleIdForLimit = "car_test_vehicle" // keep same default as earlier

    init {
        // Observe speed limit updates
        viewModelScope.launch {
            observeSpeedLimitUseCase(vehicleIdForLimit).collect { limit ->
                _speedLimit.value = limit
                Log.d("SpeedViewModel", "Observed speed limit update: $limit")
            }
        }

        // Collect vehicle data from existing GetVehicleData (keeps same behavior)
        viewModelScope.launch {
            getVehicleData.fetchData().collect { data ->
                _vehicleData.value = data
                Log.d("SpeedViewModel", "Collected vehicle data: ${data.ignitionStatus.value} / speed=${data.speed.value}")
            }
        }
    }

    fun saveSpeedLimit(vehicleId: String, limit: Int) {
        viewModelScope.launch {
            saveSpeedLimitUseCase(vehicleId, limit)
            _speedLimit.value = limit
        }
    }

    fun pushRentalForSpeedExceeded(currentSpeed: Int, maxSpeedLimitKmph: Int) {
        val rental = Rental(
            customerId = "cust_test_driver",
            vehicleId = vehicleIdForLimit,
            maxSpeedLimitKmph = maxSpeedLimitKmph,
            notificationChannel = "firebase_fcm",
            rentalStatus = "speed_exceeded_at_${currentSpeed}kmh"
        )

        viewModelScope.launch {
            try {
                pushRentalUseCase(rental)
            } catch (e: Exception) {
                Log.e("SpeedViewModel", "Failed to push rental: ${e.message}")
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        Log.d("SpeedViewModel", "ViewModel cleared")
    }
}
