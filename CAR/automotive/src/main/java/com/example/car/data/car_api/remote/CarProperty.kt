package com.example.car.data.car_api.remote

//
//import androidx.annotation.RequiresApi
//import android.car.Car
//import android.car.hardware.CarPropertyValue
//import android.car.hardware.property.CarPropertyManager
//import android.content.Context
//import android.os.Build
//import android.util.Log
//
//
//class CarProperty private constructor(
//    private val context: Context,
//    private val propertyIds: List<Int>,
//    private val callback: ((propertyId: Int?, value: Any?) -> Unit)?
//){
//
//    private var car: Car? = null
//    private var carPropertyManager: CarPropertyManager? = null
//    private val carPropertyEventCallback = object : CarPropertyManager.CarPropertyEventCallback{
//        override fun onChangeEvent(p0: CarPropertyValue<*>?) {
//            callback?.invoke(p0?.propertyId, p0?.value)
//        }
//
//        override fun onErrorEvent(p0: Int, p1: Int) {
//            Log.e("CarPropertyReader", "Error for $p0 at zone $p1")
//        }
//
//    }
//
//
//    @RequiresApi(Build.VERSION_CODES.P)
//    @Suppress("DEPRECATION") // To suppress the deprecation warning
//    fun startListening() {
//        car = Car.createCar(context)
//
//        car?.let { carNonNull ->
//            carPropertyManager = carNonNull.getCarManager(Car.PROPERTY_SERVICE) as CarPropertyManager
//            propertyIds.forEach { id ->
//                carPropertyManager?.registerCallback(
//                    carPropertyEventCallback,
//                    id,
//                    CarPropertyManager.SENSOR_RATE_ONCHANGE
//                )
//            }
//        }
//    }
//
//
//    fun stopListening(){
//        try {
//
//        }catch (e: Exception){
//            Log.e("stopListening", "unsubscribePropertyEvents creation failed: ${e.message}", e)
//        }
//    }
//
//    class Builder(private val context: Context){
//        private val props = mutableListOf<Int>()
//        private var cb: ((Int?, Any?) -> Unit)? = null
//
//        fun addProperty(propertyId: Int) = apply { props += propertyId }
//        fun setCallBack(callBack: (propertyId: Int?, value: Any?) -> Unit) = apply { cb = callBack }
//
//        fun build(): CarProperty{
//            require(cb != null){"you must call setCallBack"}
//            return CarProperty(context, props, cb)
//        }
//    }
//}
//package com.example.car.data.car_api.remote

import android.car.Car
import android.car.hardware.CarPropertyValue
import android.car.hardware.property.CarPropertyManager
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi

/**
 * Handles reading vehicle property values from the Android Automotive API.
 */
class CarProperty private constructor(
    private val context: Context,
    private val propertyIds: List<Int>,
    private val callback: ((propertyId: Int?, value: Any?) -> Unit)?
) {
    private var car: Car? = null
    private var carPropertyManager: CarPropertyManager? = null

    private val carPropertyEventCallback = object : CarPropertyManager.CarPropertyEventCallback {
        override fun onChangeEvent(p0: CarPropertyValue<*>?) {
            callback?.invoke(p0?.propertyId, p0?.value)
        }

        override fun onErrorEvent(p0: Int, p1: Int) {
            Log.e("CarProperty", "Error for $p0 at zone $p1")
        }
    }

    @RequiresApi(Build.VERSION_CODES.P)
    fun startListening() {
        car = Car.createCar(context)
        car?.let { carNonNull ->
            carPropertyManager = carNonNull.getCarManager(Car.PROPERTY_SERVICE) as CarPropertyManager
            propertyIds.forEach { id ->
                carPropertyManager?.registerCallback(
                    carPropertyEventCallback,
                    id,
                    CarPropertyManager.SENSOR_RATE_ONCHANGE
                )
            }
        }
    }

    fun stopListening() {
        try {
            carPropertyManager?.unregisterCallback(carPropertyEventCallback)
            car?.disconnect()
        } catch (e: Exception) {
            Log.e("CarProperty", "stopListening failed: ${e.message}", e)
        }
    }

    class Builder(private val context: Context) {
        private val props = mutableListOf<Int>()
        private var cb: ((Int?, Any?) -> Unit)? = null

        fun addProperty(propertyId: Int) = apply { props += propertyId }
        fun setCallBack(callBack: (propertyId: Int?, value: Any?) -> Unit) = apply { cb = callBack }

        fun build(): CarProperty {
            require(cb != null) { "Callback must be set before building CarProperty" }
            return CarProperty(context, props, cb)
        }
    }
}
