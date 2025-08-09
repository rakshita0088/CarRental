package com.example.car


//
//// Standard Android framework imports
//import android.Manifest
//import android.content.pm.PackageManager
//import android.os.Build
//import android.os.Bundle
//import android.util.Log // For logging Firebase operations and permissions
//
//// Jetpack Compose activity and UI imports
//import androidx.activity.ComponentActivity
//import androidx.activity.compose.setContent
//import androidx.annotation.RequiresApi // For specific API level requirements
//import androidx.compose.foundation.layout.Arrangement // For arranging UI elements
//import androidx.compose.foundation.layout.Box
//import androidx.compose.foundation.layout.Column // For vertical stacking of UI elements
//import androidx.compose.foundation.layout.Spacer // For adding space between UI elements
//import androidx.compose.foundation.layout.fillMaxSize // To make UI fill the screen
//import androidx.compose.foundation.layout.height // To control height of spacers
//import androidx.compose.foundation.layout.padding
//import androidx.compose.material3.Button // Compose Material Design Button
//import androidx.compose.material3.MaterialTheme // For app theming
//import androidx.compose.material3.Surface // A background surface
//import androidx.compose.material3.Text // Compose Text composable
//import androidx.compose.runtime.Composable
//import androidx.compose.ui.Alignment // For aligning UI elements
//import androidx.compose.ui.Modifier // Modifiers to control composable appearance and behavior
//import androidx.compose.ui.unit.dp // For density-independent pixels
//import androidx.core.app.ActivityCompat // For requesting permissions
//import androidx.core.content.ContextCompat // For checking permissions
//
//// Firebase Realtime Database specific imports
//
//// Your application's custom data and UI components
//import com.example.car.data.car_api.dto.Rental // Assuming this is your correct Rental data class
//import com.example.car.ui.AppContextProvider // Your custom context provider
//import com.example.car.ui.SpeedMonitorScreen // Your Speed Monitor Compose screen
//import com.example.car.ui.theme.Theme // Your app's Compose theme
//import com.google.firebase.database.DatabaseReference
//import com.google.firebase.database.FirebaseDatabase
//
///**
// * The main activity for your Carr app, now handling both location monitoring
// * and Firebase Realtime Database interactions.
// */
//class MainActivity : ComponentActivity() {
//
//    // Unique request code for location permissions
//    private val REQUEST_LOCATION = 1001
//
//    // Firebase Realtime Database instances
//    private lateinit var database: FirebaseDatabase
//    private lateinit var rentalsRef: DatabaseReference
//
//    /**
//     * Called when the activity is first created.
//     * This is where you initialize your app's core components and set up your UI.
//     */
//    @RequiresApi(Build.VERSION_CODES.P) // Keep this annotation as your SpeedMonitorScreen might need it
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//
//        AppContextProvider.init(this)
//
//        // 4. Set up your Jetpack Compose UI
//        setContent {
//            Theme { // Apply your app's custom theme
//                Surface(color = MaterialTheme.colorScheme.background) {
//                    Column(
//                        modifier = Modifier.fillMaxSize(), // Make the column fill the entire screen
//                        horizontalAlignment = Alignment.CenterHorizontally, // Center content horizontally
//                        verticalArrangement = Arrangement.Center // Center content vertically
//                    ) {
//                        // Your Speed Monitor UI (from the second MainActivity file)
//                        SpeedMonitorScreen()
//
//                    }
//                }
//            }
//        }
//    }
//
//
//
//    /**
//     * Pushes mock `Rental` data objects to the Firebase Realtime Database.
//     * This demonstrates both setting data with a predefined key and letting Firebase generate a key.
//     */
//
//
//    /**
//     * Requests the ACCESS_FINE_LOCATION permission from the user if it hasn't been granted yet.
//     */
//    private fun requestLocationPermission() {
//        // Check if the permission is already granted
//        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
//            != PackageManager.PERMISSION_GRANTED) {
//            // If not granted, request it from the user
//            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), REQUEST_LOCATION)
//        }
//    }
//
//    /**
//     * Callback for the result of requesting permissions.
//     * This is where you handle what happens after the user grants or denies a permission.
//     */
//    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults) // Always call super
//
//        when (requestCode) {
//            REQUEST_LOCATION -> {
//                // Check if the location permission was granted
//                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
//                    Log.d("Permissions", "Location permission granted! 🎉")
//                    // You might want to re-initialize location-dependent services here
//                } else {
//                    Log.d("Permissions", "Location permission denied. 😔")
//                    // Inform the user that the functionality might be limited without the permission
//                    // Or disable features that require location
//                }
//                return
//            }
//            // Handle other permission request codes if you have more
//        }
//    }
//}
//package com.example.car

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import com.example.car.ui.AppContextProvider
import com.example.car.ui.SpeedMonitorScreen
import com.example.car.ui.theme.Theme

class MainActivity : ComponentActivity() {

    @RequiresApi(Build.VERSION_CODES.P)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppContextProvider.init(this)

        setContent {
            Theme {
                Surface(color = MaterialTheme.colorScheme.background) {
                    SpeedMonitorScreen()
                }
            }
        }
    }
}
