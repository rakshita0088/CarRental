package com.example.car



import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import com.example.car.presentation.AppContextProvider
import com.example.car.presentation.screen.SpeedMonitorScreen
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
