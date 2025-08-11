package com.example.car.data.car_api


import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.car.data.car_api.dto.VehicleData
import com.example.car.data.car_api.remote.GetVehicleData
import com.example.car.ui.AppContextProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel that manages vehicle data and speed limit.
 */
class SpeedViewModel : ViewModel() {

    private val getVehicleData = GetVehicleData(AppContextProvider.get())

    private val _vehicleData = mutableStateOf<VehicleData?>(null)
    val vehicleData = _vehicleData

    private val _speedLimit = MutableStateFlow(0)
    val speedLimit: StateFlow<Int> = _speedLimit

    @RequiresApi(Build.VERSION_CODES.P)
    fun collectData() {
        viewModelScope.launch {
            getVehicleData.fetchData().collect { data ->
                _vehicleData.value = data
            }
        }
    }

    fun setSpeedLimit(limit: Int) {
        _speedLimit.value = limit
    }
}
