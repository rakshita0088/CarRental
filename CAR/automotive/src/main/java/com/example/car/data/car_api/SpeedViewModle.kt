package com.example.car.data.car_api

//import android.os.Build
//import androidx.annotation.RequiresApi
//import androidx.compose.runtime.mutableStateOf
//import androidx.lifecycle.ViewModel
//import androidx.lifecycle.viewModelScope
//import com.example.car.ui.AppContextProvider
//import com.google.android.gms.location.*
//import kotlinx.coroutines.flow.MutableStateFlow
//import kotlinx.coroutines.flow.StateFlow
//import kotlinx.coroutines.launch
//import androidx.compose.runtime.State
//import com.example.car.data.car_api.dto.VehicleData
//import com.example.car.data.car_api.remote.GetVehicleData
//
//class SpeedViewModel : ViewModel() {
//
//    private val getVehicleData = GetVehicleData(AppContextProvider.get())
//    private val _vehicleData = mutableStateOf<VehicleData?>(null)
//    val vehicleData: State<VehicleData?> = _vehicleData
//
//    // Use AppContextProvider instead of Application parameter
//    private val fusedLocationClient =
//        LocationServices.getFusedLocationProviderClient(AppContextProvider.get())
//
//    private val _speed = MutableStateFlow(0)
//    val speed: StateFlow<Int> = _speed
//
//    private val _speedLimit = MutableStateFlow(0)
//    val speedLimit: StateFlow<Int> = _speedLimit
//
//
//    private val locationCallback = object : LocationCallback() {
//        override fun onLocationResult(result: LocationResult) {
//            result.lastLocation?.let { loc ->
//                val speedKmh = (loc.speed * 3.6).toInt()
//                _speed.value = speedKmh
//            }
//        }
//    }
//
//    init {
//
//    }
//
//    @RequiresApi(Build.VERSION_CODES.P)
//    fun collectData() {
//        viewModelScope.launch {
//            getVehicleData.fetchData().collect { vehicleData ->
//                _vehicleData.value = vehicleData
//            }
//        }
//    }
//
//
//
//    fun setSpeedLimit(limit: Int) {
//        _speedLimit.value = limit
//    }
//
//}
//package com.example.car.data.car_api

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
