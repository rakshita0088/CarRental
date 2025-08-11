package com.example.car.ui

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.car.data.car_api.SpeedViewModel
import com.example.car.data.car_api.dto.Rental
import com.google.firebase.database.FirebaseDatabase


private const val CHANNEL_ID = "speed_warning_channel"

@RequiresApi(Build.VERSION_CODES.P)
@Composable
fun SpeedMonitorScreen(viewModel: SpeedViewModel = viewModel()) {
    val context = LocalContext.current
    val vehicleData = viewModel.vehicleData.value

//    // Observe the dynamic speed limit from the ViewModel!
//    val fetchedSpeedLimit = viewModel.speedLimit.value ?: 95 // Use 95 as a fallback if not loaded yet
    // Observe the dynamic speed limit. It will be null initially, then Int.
    val fetchedSpeedLimit: Int? by viewModel.speedLimit.collectAsState() // Use collectAsState() for StateFlow

    var currentSpeed by remember { mutableStateOf(0) }

    // Create notification channel when screen loads
    LaunchedEffect(Unit) {
        createNotificationChannel(context)
        viewModel.collectData() // Ensure this is called to initialize vehicleData (and trigger speed limit fetch)
    }

    Column(Modifier.padding(16.dp)) {
        // Manually set current speed for testing
        OutlinedTextField(
            value = currentSpeed.toString(),
            onValueChange = { currentSpeed = it.toIntOrNull() ?: 0 },
            label = { Text("Set Current Speed (km/h)") },
            singleLine = true
        )
        Spacer(Modifier.height(16.dp))

        if (vehicleData != null && fetchedSpeedLimit != null) {
            Text("Ignition Status: ${vehicleData.ignitionStatus.value}")
            Text("Manual Speed: $currentSpeed km/h")
            // Display the dynamically fetched speed limit
            Text("Speed Limit (from Firebase): $fetchedSpeedLimit km/h")

            if (vehicleData.ignitionStatus.value == 4) { // Ignition ON
                if (currentSpeed > fetchedSpeedLimit!!) { // Use the fetched speed limit here
                    // Push data to Firebase, passing the fetched speed limit
                    pushMockRentalData(currentSpeed, fetchedSpeedLimit!!)

                    // Show speed warning notification, passing the fetched speed limit
                    showSpeedWarningNotification(context, currentSpeed, fetchedSpeedLimit!!)
                }
            } else {
                Text("Ignition is OFF - No speed check, no data push")
            }
        } else {
            Text("Fetching vehicle data...")
        }
    }
}

private fun createNotificationChannel(context: Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val channel = NotificationChannel(
            CHANNEL_ID, // Use the constant CHANNEL_ID
            "Speed Warning Channel", // A more descriptive name for the user
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Notifications for exceeding speed limits."
        }

        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }
}

// Modify this function to accept the actual speed limit
fun pushMockRentalData(currentSpeed: Int, maxSpeedLimitKmph: Int) {
    val database = FirebaseDatabase.getInstance("https://carr-2336b-default-rtdb.firebaseio.com")
    val rentalsRef = database.getReference("rentals")

    val rental = Rental(
        customerId = "cust_test_driver",
        vehicleId = "car_test_vehicle",
        maxSpeedLimitKmph = maxSpeedLimitKmph, // Use the dynamically fetched speed limit
        notificationChannel = "firebase_fcm",
        rentalStatus = "speed_exceeded_at_${currentSpeed}kmh"
    )

    rentalsRef.push().setValue(rental)
        .addOnSuccessListener {
            Log.d("RTDB_PUSH", "Rental data pushed: $rental")
        }
        .addOnFailureListener { e ->
            Log.e("RTDB_PUSH", "Failed to push data", e)
        }
}

// Modify this function to accept the actual speed limit
private fun showSpeedWarningNotification(context: Context, currentSpeed: Int, speedLimit: Int) {
    val builder = NotificationCompat.Builder(context, CHANNEL_ID)
        .setSmallIcon(android.R.drawable.ic_dialog_alert)
        .setContentTitle("🚨 Slow Down!")
        .setContentText("Speed Limit Exceeded: $currentSpeed km/h (Limit: $speedLimit)")
        .setPriority(NotificationCompat.PRIORITY_HIGH)
        .setAutoCancel(true)

    with(NotificationManagerCompat.from(context)) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
        ) {
            notify(1001, builder.build())
        } else {
            Log.w("Notification", "POST_NOTIFICATIONS permission not granted")
        }
    }
}




