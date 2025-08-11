package com.example.car.data.car_api

import android.content.Context
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.car.data.car_api.dto.VehicleData
import com.example.car.data.car_api.remote.GetVehicleData
import com.example.car.ui.AppContextProvider // Ensure AppContextProvider is available and provides Context
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
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

    // MutableStateFlow for speedLimit. Initialize as null to signify "loading" or "not yet loaded"
    // Use Int? to allow for nullability.
    private val _speedLimit = MutableStateFlow<Int?>(null)
    val speedLimit: StateFlow<Int?> = _speedLimit // Now exposes Int?

    // --- SharedPreferences Constants ---
    private val PREFS_FILE_NAME = "app_prefs"
    private val KEY_SPEED_LIMIT = "cached_speed_limit"
    private val DEFAULT_FALLBACK_SPEED_LIMIT = 95 // Hardcoded default if no Firebase and no cache

    // For demonstration, using a fixed vehicleId. In a real app, this should be dynamic.
    private val vehicleId = "car_test_vehicle"

    // Get the Firebase Database instance for your project
    private val database = FirebaseDatabase.getInstance("https://carr-2336b-default-rtdb.firebaseio.com")

    // Reference to the specific location where this vehicle's speed limit is stored
    private val speedLimitRef = database.getReference("vehicles").child(vehicleId).child("speedLimit")

    // Define the ValueEventListener to listen for speed limit changes
    private val speedLimitValueEventListener = object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            val fetchedLimit = snapshot.getValue(Int::class.java)
            if (fetchedLimit != null) {
                // 1. Update the ViewModel's StateFlow with the new value from Firebase
                _speedLimit.value = fetchedLimit
                Log.d("SpeedViewModel", "Firebase: Successfully fetched speed limit: $fetchedLimit for $vehicleId")

                // 2. Save the newly fetched data to SharedPreferences for persistence
                val appContext = AppContextProvider.get()
                val sharedPrefs = appContext.getSharedPreferences(PREFS_FILE_NAME, Context.MODE_PRIVATE)
                with(sharedPrefs.edit()) {
                    putInt(KEY_SPEED_LIMIT, fetchedLimit)
                    apply() // `apply()` commits asynchronously
                }
                Log.d("SpeedViewModel", "SharedPreferences: Saved speed limit: $fetchedLimit")

            } else {
                // Firebase path exists but value is null, or path doesn't exist.
                // In this case, try to load from SharedPreferences as a fallback.
                Log.w("SpeedViewModel", "Firebase: Speed limit not found for $vehicleId. Checking cache...")
                loadSpeedLimitFromCacheOrFallback()
            }
        }

        override fun onCancelled(error: DatabaseError) {
            // This is crucial: If Firebase read fails (e.g., offline, security rules),
            // then we explicitly try to load from SharedPreferences.
            Log.e("SpeedViewModel", "Firebase: Failed to fetch speed limit. Error: ${error.message}. Attempting to load from cache...")
            loadSpeedLimitFromCacheOrFallback()
        }
    }

    // Helper function to load from cache or use default
    private fun loadSpeedLimitFromCacheOrFallback() {
        val appContext = AppContextProvider.get()
        val sharedPrefs = appContext.getSharedPreferences(PREFS_FILE_NAME, Context.MODE_PRIVATE)
        // If not found in cache, use the hardcoded default
        val cachedLimit = sharedPrefs.getInt(KEY_SPEED_LIMIT, DEFAULT_FALLBACK_SPEED_LIMIT)
        _speedLimit.value = cachedLimit
        Log.d("SpeedViewModel", "Fallback: Loaded speed limit from cache ($cachedLimit) or used default.")
    }

    init {
        // Initially, the _speedLimit is null (indicating loading).
        // Attach the Firebase listener. It will either succeed (updating _speedLimit and cache)
        // or fail (triggering load from cache/default).
        speedLimitRef.addValueEventListener(speedLimitValueEventListener)
        Log.d("SpeedViewModel", "Attached Firebase speed limit listener for $vehicleId. Initial speedLimit is null (loading).")
    }

    @RequiresApi(Build.VERSION_CODES.P)
    fun collectData() {
        viewModelScope.launch {
            getVehicleData.fetchData().collect { data ->
                _vehicleData.value = data
                Log.d("SpeedViewModel", "Collected vehicle data: ${data.ignitionStatus.value}")
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        speedLimitRef.removeEventListener(speedLimitValueEventListener)
        Log.d("SpeedViewModel", "Removed Firebase speed limit listener for $vehicleId")
    }
}
