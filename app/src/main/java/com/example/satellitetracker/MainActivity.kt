package com.example.satellitetracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.satellitetracker.ui.theme.SatelliteTrackerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            SatelliteTrackerTheme {
                Scaffold(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black),
                    containerColor = Color.Black
                ) { innerPadding ->
                    SatelliteTrackerScreen(
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun SatelliteTrackerScreen(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Text(
            text = "Satellite Tracker",
            fontSize = 28.sp,
            color = Color.White
        )

        Spacer(
            modifier = Modifier.height(32.dp)
        )

        Text(
            text = "🛰️",
            fontSize = 80.sp
        )

        Spacer(
            modifier = Modifier.height(24.dp)
        )

        Text(
            text = "人工衛星を追跡します",
            fontSize = 20.sp,
            color = Color.White
        )

        Spacer(
            modifier = Modifier.height(16.dp)
        )

        Text(
            text = "衛星データを取得しています...",
            fontSize = 16.sp,
            color = Color.LightGray
        )
    }
}