package com.example.car.data.car_api.remote

import android.car.VehiclePropertyIds
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import com.example.car.data.car_api.dto.IgnitionStatus
import com.example.car.data.car_api.dto.Speed
import com.example.car.data.car_api.dto.VehicleData
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

/**
 * Fetches vehicle data (ignition state, speed) using CarProperty API.
 */
class GetVehicleData(private val context: Context) {

    @RequiresApi(Build.VERSION_CODES.P)
    fun fetchData(): Flow<VehicleData> = callbackFlow {
        val currentStatus = VehicleData()

        val carProperty = CarProperty.Builder(context)
            .addProperty(VehiclePropertyIds.IGNITION_STATE)
            .addProperty(VehiclePropertyIds.PERF_VEHICLE_SPEED)
            .setCallBack { propertyId, value ->
                when (propertyId) {
                    VehiclePropertyIds.IGNITION_STATE -> {
                        currentStatus.ignitionStatus = IgnitionStatus(
                            propertyId = propertyId,
                            status = "OK",
                            value = value as? Int ?: 0
                        )
                    }
                    VehiclePropertyIds.PERF_VEHICLE_SPEED -> {
                        currentStatus.speed = Speed(
                            propertyId = propertyId,
                            status = "OK",
                            value = (value as? Float)?.toInt() ?: 0
                        )
                    }
                }
                trySend(currentStatus.copy())
            }
            .build()

        carProperty.startListening()
        awaitClose { carProperty.stopListening() }
    }
}