package com.example.car.data.repository



import android.content.Context
import android.util.Log
import com.example.car.domain.repository.SpeedRepository
import com.example.car.presentation.AppContextProvider
import com.google.firebase.database.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

/**
 * Implementation that listens to Firebase Realtime Database and uses SharedPreferences as cache/fallback.
 *
 * Preserves the same constants and logic as your old code:
 * - PREFS_FILE_NAME
 * - KEY_SPEED_LIMIT
 * - DEFAULT_FALLBACK_SPEED_LIMIT
 *
 * observeSpeedLimit emits the live Firebase value when available, otherwise emits cached/default.
 */
class SpeedRepositoryImpl(
    private val context: Context = AppContextProvider.get(),
    private val firebaseUrl: String = "https://carr-2336b-default-rtdb.firebaseio.com"
) : SpeedRepository {

    companion object {
        private const val PREFS_FILE_NAME = "app_prefs"
        private const val KEY_SPEED_LIMIT = "cached_speed_limit"
        private const val DEFAULT_FALLBACK_SPEED_LIMIT = 95
    }

    private val database: FirebaseDatabase = FirebaseDatabase.getInstance(firebaseUrl)

    override fun observeSpeedLimit(vehicleId: String): Flow<Int?> = callbackFlow {
        val speedLimitRef = database.getReference("vehicles").child(vehicleId).child("speedLimit")

        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val fetchedLimit = snapshot.getValue(Int::class.java)
                if (fetchedLimit != null) {
                    // update cache
                    try {
                        val sharedPrefs = context.getSharedPreferences(PREFS_FILE_NAME, Context.MODE_PRIVATE)
                        with(sharedPrefs.edit()) {
                            putInt(KEY_SPEED_LIMIT, fetchedLimit)
                            apply()
                        }
                    } catch (e: Exception) {
                        Log.w("SpeedRepositoryImpl", "Failed to write cache: ${e.message}")
                    }

                    trySend(fetchedLimit).isSuccess
                    Log.d("SpeedRepositoryImpl", "Firebase fetched speed limit: $fetchedLimit for $vehicleId")
                } else {
                    // Emit cached or default when Firebase path exists but value is null
                    val cached = getCachedOrDefault()
                    trySend(cached).isSuccess
                    Log.w("SpeedRepositoryImpl", "Firebase value null. Emitting cached/default: $cached")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                val cached = getCachedOrDefault()
                trySend(cached).isSuccess
                Log.e("SpeedRepositoryImpl", "Firebase read cancelled: ${error.message}. Emitting cached/default: $cached")
            }
        }

        // attach
        speedLimitRef.addValueEventListener(listener)

        // remove listener on cancel/close
        awaitClose {
            speedLimitRef.removeEventListener(listener)
            Log.d("SpeedRepositoryImpl", "Listener removed for $vehicleId")
        }
    }

    override suspend fun saveSpeedLimit(vehicleId: String, speedLimit: Int) {
        // Keep behavior consistent with earlier code: save to SharedPreferences
        try {
            val sharedPrefs = context.getSharedPreferences(PREFS_FILE_NAME, Context.MODE_PRIVATE)
            with(sharedPrefs.edit()) {
                putInt(KEY_SPEED_LIMIT, speedLimit)
                apply()
            }
            Log.d("SpeedRepositoryImpl", "Saved speed limit to prefs: $speedLimit for $vehicleId")
        } catch (e: Exception) {
            Log.e("SpeedRepositoryImpl", "Failed to save speed limit to prefs: ${e.message}")
        }

        // Optionally, if you want to push to Firebase too, you could uncomment:
        // database.getReference("vehicles").child(vehicleId).child("speedLimit").setValue(speedLimit)
    }

    private fun getCachedOrDefault(): Int {
        return try {
            val sharedPrefs = context.getSharedPreferences(PREFS_FILE_NAME, Context.MODE_PRIVATE)
            sharedPrefs.getInt(KEY_SPEED_LIMIT, DEFAULT_FALLBACK_SPEED_LIMIT)
        } catch (e: Exception) {
            Log.w("SpeedRepositoryImpl", "Failed to read cache: ${e.message}")
            DEFAULT_FALLBACK_SPEED_LIMIT
        }
    }
}
