package com.example.car.presentation.screen


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
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.car.data.repository.RentalRepositoryImpl
import com.example.car.data.repository.SpeedRepositoryImpl
import com.example.car.domain.usecase.ObserveSpeedLimitUseCase
import com.example.car.domain.usecase.PushRentalUseCase
import com.example.car.domain.usecase.SaveSpeedLimitUseCase
import com.example.car.presentation.viewmodel.SpeedViewModel
import com.example.car.data.car_api.dto.Rental
import com.example.car.data.car_api.remote.GetVehicleData

private const val CHANNEL_ID = "speed_warning_channel"

/**
 * Provide a default ViewModelProvider.Factory that wires real implementations.
 * This keeps things simple for copy/paste — no Hilt required.
 */
fun provideDefaultSpeedViewModelFactory(context: Context): ViewModelProvider.Factory {
    return object : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            // Create concrete repository implementations (uses AppContextProvider inside impl)
            val speedRepo = SpeedRepositoryImpl(context)
            val rentalRepo = RentalRepositoryImpl()

            val observeSpeedUC = ObserveSpeedLimitUseCase(speedRepo)
            val saveSpeedUC = SaveSpeedLimitUseCase(speedRepo)
            val pushRentalUC = PushRentalUseCase(rentalRepo)

            // Use your existing GetVehicleData helper if present (kept unchanged in your repo)
            val getVehicleData = GetVehicleData(context)

            val vm = SpeedViewModel(observeSpeedUC, saveSpeedUC, pushRentalUC, getVehicleData)
            return vm as T
        }
    }
}

@RequiresApi(Build.VERSION_CODES.P)
@Composable
fun SpeedMonitorScreen(viewModel: SpeedViewModel = viewModel(factory = provideDefaultSpeedViewModelFactory(LocalContext.current))) {
    val context = LocalContext.current
    val vehicleData by viewModel.vehicleData.collectAsState()
    val fetchedSpeedLimit by viewModel.speedLimit.collectAsState()

    var manualSpeed by remember { mutableStateOf(0) }

    // Create notification channel once and any other startup things
    LaunchedEffect(Unit) {
        createNotificationChannel(context)
    }

    Column(Modifier.padding(16.dp)) {
        OutlinedTextField(
            value = manualSpeed.toString(),
            onValueChange = { manualSpeed = it.toIntOrNull() ?: 0 },
            label = { Text("Set Manual Speed (km/h)") },
            singleLine = true
        )
        Spacer(Modifier.height(16.dp))

        if (vehicleData != null && fetchedSpeedLimit != null) {
            val carSpeed = vehicleData!!.speed.value
            val currentSpeedToUse = if (carSpeed > 0) carSpeed else manualSpeed
            Text("Ignition Status: ${vehicleData!!.ignitionStatus.value}")
            Text("Speed (CarProperty): $carSpeed km/h")
            Text("Speed (Manual Input): $manualSpeed km/h")
            Text("Using Speed: $currentSpeedToUse km/h")
            Text("Speed Limit (from Firebase): $fetchedSpeedLimit km/h")

            if (vehicleData!!.ignitionStatus.value == 4) {
                if (currentSpeedToUse > fetchedSpeedLimit!!) {
                    // Push rental and show notification
                    viewModel.pushRentalForSpeedExceeded(currentSpeedToUse, fetchedSpeedLimit!!)
                    showSpeedWarningNotification(context, currentSpeedToUse, fetchedSpeedLimit!!)
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
            CHANNEL_ID,
            "Speed Warning Channel",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Notifications for exceeding speed limits."
        }

        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
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
