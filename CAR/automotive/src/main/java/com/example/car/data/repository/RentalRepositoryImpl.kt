package com.example.car.data.repository


import android.util.Log
import com.example.car.domain.repository.RentalRepository
import com.example.car.data.car_api.dto.Rental
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class RentalRepositoryImpl(
    private val firebaseUrl: String = "https://carr-2336b-default-rtdb.firebaseio.com"
) : RentalRepository {

    private val database = FirebaseDatabase.getInstance(firebaseUrl)

    override suspend fun pushRental(rental: Rental) = suspendCancellableCoroutine<Unit> { cont ->
        val rentalsRef = database.getReference("rentals")
        val task = rentalsRef.push().setValue(rental)
        task.addOnSuccessListener {
            Log.d("RentalRepositoryImpl", "Rental data pushed: $rental")
            if (!cont.isCompleted) cont.resume(Unit)
        }.addOnFailureListener { e ->
            Log.e("RentalRepositoryImpl", "Failed to push rental: ${e.message}", e)
            if (!cont.isCompleted) cont.resumeWithException(e)
        }

        cont.invokeOnCancellation {
            // no direct way to cancel setValue, but listener will be GC-ed on coroutine cancellation
            Log.d("RentalRepositoryImpl", "Coroutine cancelled while pushing rental")
        }
    }
}
