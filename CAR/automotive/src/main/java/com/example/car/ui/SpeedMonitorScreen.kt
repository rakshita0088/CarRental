package com.example.car.ui

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
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

    // Fixed speed limit
    val speedLimit = 95
    var currentSpeed by remember { mutableStateOf(0) }

    // Create notification channel when screen loads
    LaunchedEffect(Unit) {
        createNotificationChannel(context)
        viewModel.collectData()
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

        if (vehicleData != null) {
            Text("Ignition Status: ${vehicleData.ignitionStatus.value}")
            Text("Manual Speed: $currentSpeed km/h")
            Text("Speed Limit: $speedLimit km/h")

            if (vehicleData.ignitionStatus.value == 2) { // Ignition ON
                if (currentSpeed > speedLimit) {
                    // Push data to Firebase
                    pushMockRentalData(currentSpeed)

                    // Show speed warning notification
                    showSpeedWarningNotification(context, currentSpeed, speedLimit)
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
            "channel_id",
            "Channel Name",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "This is my channel description"
        }

        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }
}

fun pushMockRentalData(currentSpeed: Int) {
    val database = FirebaseDatabase.getInstance("https://carr-2336b-default-rtdb.firebaseio.com")
    val rentalsRef = database.getReference("rentals")

    val rental = Rental(
        customerId = "cust_test_driver",
        vehicleId = "car_test_vehicle",
        maxSpeedLimitKmph = 95,
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

