package com.example.satellitetracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import org.maplibre.android.MapLibre
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.MapView
import org.maplibre.android.style.layers.CircleLayer
import org.maplibre.android.style.layers.LineLayer
import org.maplibre.android.style.layers.PropertyFactory.circleColor
import org.maplibre.android.style.layers.PropertyFactory.circleRadius
import org.maplibre.android.style.layers.PropertyFactory.circleStrokeColor
import org.maplibre.android.style.layers.PropertyFactory.circleStrokeWidth
import org.maplibre.android.style.layers.PropertyFactory.lineColor
import org.maplibre.android.style.layers.PropertyFactory.lineWidth
import org.maplibre.android.style.sources.GeoJsonSource
import org.maplibre.geojson.Feature
import org.maplibre.geojson.LineString
import org.maplibre.geojson.Point
import com.example.satellitetracker.ui.theme.SatelliteTrackerTheme

class MainActivity : ComponentActivity() {

    private lateinit var mapView: MapView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        OrekitInitializer.initialize(this)

        MapLibre.getInstance(this)

        mapView = MapView(this)
        mapView.onCreate(savedInstanceState)

        mapView.getMapAsync { map ->
            map.setStyle(
                "https://tiles.openfreemap.org/styles/liberty"
            ) {
                map.cameraPosition = CameraPosition.Builder()
                    .target(LatLng(20.0, 0.0))
                    .zoom(1.5)
                    .build()
            }
        }

        enableEdgeToEdge()

        setContent {
            SatelliteTrackerTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    containerColor = Color.Transparent
                ) { innerPadding ->
                    SatelliteTrackerScreen(
                        mapView = mapView,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        mapView.onStart()
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        mapView.onPause()
        super.onPause()
    }

    override fun onStop() {
        super.onStop()
        mapView.onStop()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }

    override fun onDestroy() {
        mapView.onDestroy()
        super.onDestroy()
    }
}

private fun updateIssMarker(
    map: MapLibreMap,
    position: SatellitePosition
) {
    map.getStyle { style ->

        val issPoint = Point.fromLngLat(
            position.longitude,
            position.latitude
        )

        val feature = Feature.fromGeometry(
            issPoint
        )

        val source =
            style.getSourceAs<GeoJsonSource>("iss-source")

        if (source != null) {
            source.setGeoJson(feature)
        } else {

            val newSource = GeoJsonSource(
                "iss-source",
                feature
            )

            style.addSource(newSource)

            val layer = CircleLayer(
                "iss-layer",
                "iss-source"
            )

            layer.setProperties(
                circleColor(Color.Red.hashCode()),
                circleRadius(8f),
                circleStrokeColor(Color.White.hashCode()),
                circleStrokeWidth(2f)
            )

            style.addLayer(layer)
        }
    }
}

private fun updateIssOrbit(
    map: MapLibreMap,
    positions: List<SatellitePosition>
) {
    map.getStyle { style ->

        val points = positions.map {
            Point.fromLngLat(
                it.longitude,
                it.latitude
            )
        }

        val lineString = LineString.fromLngLats(
            points
        )

        val feature = Feature.fromGeometry(
            lineString
        )

        val source =
            style.getSourceAs<GeoJsonSource>("orbit-source")

        if (source != null) {
            source.setGeoJson(feature)
        } else {

            val newSource = GeoJsonSource(
                "orbit-source",
                feature
            )

            style.addSource(newSource)

            val layer = LineLayer(
                "orbit-layer",
                "orbit-source"
            )

            layer.setProperties(
                lineColor(Color.Cyan.hashCode()),
                lineWidth(3f)
            )

            style.addLayer(layer)
        }
    }
}

@Composable
private fun SpaceBackground(
    modifier: Modifier = Modifier
) {
    Canvas(
        modifier = modifier
            .fillMaxSize()
    ) {
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color(0xFF020617),
                    Color(0xFF050B1A),
                    Color(0xFF02040A)
                )
            )
        )

        val stars = listOf(
            0.08f to 0.12f,
            0.18f to 0.24f,
            0.32f to 0.09f,
            0.46f to 0.18f,
            0.58f to 0.07f,
            0.72f to 0.21f,
            0.84f to 0.11f,
            0.93f to 0.29f,
            0.12f to 0.43f,
            0.27f to 0.51f,
            0.41f to 0.39f,
            0.67f to 0.47f,
            0.79f to 0.55f,
            0.91f to 0.42f,
            0.06f to 0.67f,
            0.21f to 0.74f,
            0.38f to 0.64f,
            0.56f to 0.78f,
            0.75f to 0.69f,
            0.88f to 0.82f,
            0.14f to 0.91f,
            0.34f to 0.87f,
            0.62f to 0.93f,
            0.81f to 0.89f
        )

        stars.forEach { (x, y) ->
            drawCircle(
                color = Color.White.copy(alpha = 0.45f),
                radius = 1.2f,
                center = androidx.compose.ui.geometry.Offset(
                    size.width * x,
                    size.height * y
                )
            )
        }

        drawCircle(
            color = Color(0xFF163A5F).copy(alpha = 0.12f),
            radius = size.width * 0.75f,
            center = androidx.compose.ui.geometry.Offset(
                size.width * 0.15f,
                size.height * 0.15f
            ),
            style = Stroke(width = 80f)
        )
    }
}

@Composable
fun SatelliteTrackerScreen(
    mapView: MapView,
    modifier: Modifier = Modifier
) {
    var satelliteData by remember {
        mutableStateOf<SatelliteData?>(null)
    }

    var satellitePosition by remember {
        mutableStateOf<SatellitePosition?>(null)
    }

    var errorMessage by remember {
        mutableStateOf<String?>(null)
    }

    LaunchedEffect(Unit) {

        try {
            val data = withContext(Dispatchers.IO) {
                SatelliteRepository().getIssData()
            }

            satelliteData = data

            val propagator = SatellitePropagator()

            val orbitPositions = withContext(
                Dispatchers.Default
            ) {
                propagator.getOrbitPositions(
                    line1 = data.line1,
                    line2 = data.line2,
                    durationMinutes = 90,
                    stepSeconds = 60
                )
            }

            mapView.getMapAsync { map ->
                updateIssOrbit(
                    map = map,
                    positions = orbitPositions
                )
            }

            while (true) {

                val position = withContext(Dispatchers.Default) {
                    propagator.getCurrentPosition(
                        line1 = data.line1,
                        line2 = data.line2
                    )
                }

                satellitePosition = position

                mapView.getMapAsync { map ->
                    updateIssMarker(
                        map = map,
                        position = position
                    )
                }

                delay(5000)
            }

        } catch (e: Exception) {
            errorMessage = e.message
        }
    }

    Box(
        modifier = modifier.fillMaxSize()
    ) {

        SpaceBackground()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    horizontal = 20.dp,
                    vertical = 16.dp
                ),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Text(
                text = "SATELLITE TRACKER",
                fontSize = 25.sp,
                color = Color.White
            )

            Spacer(
                modifier = Modifier.height(4.dp)
            )

            Text(
                text = "ISS • REAL-TIME ORBIT",
                fontSize = 12.sp,
                color = Color(0xFF7DD3FC)
            )

            Spacer(
                modifier = Modifier.height(16.dp)
            )

            AndroidView(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(450.dp),
                factory = {
                    mapView
                }
            )

            Spacer(
                modifier = Modifier.height(14.dp)
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF0B1220).copy(alpha = 0.92f)
                )
            ) {

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = satelliteData?.name ?: "ISS",
                                fontSize = 20.sp,
                                color = Color.White
                            )

                            Text(
                                text = "NORAD ${satelliteData?.catalogNumber ?: "25544"}",
                                fontSize = 13.sp,
                                color = Color.LightGray
                            )
                        }

                        Text(
                            text = "● LIVE",
                            fontSize = 12.sp,
                            color = Color(0xFF4ADE80)
                        )
                    }

                    Spacer(
                        modifier = Modifier.height(12.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {

                        satellitePosition?.let {

                            SatelliteValue(
                                label = "LAT",
                                value = "${"%.2f".format(it.latitude)}°"
                            )

                            SatelliteValue(
                                label = "LON",
                                value = "${"%.2f".format(it.longitude)}°"
                            )

                            SatelliteValue(
                                label = "ALT",
                                value = "${"%.1f".format(it.altitude)} km"
                            )
                        }
                    }
                }
            }

            Spacer(
                modifier = Modifier.height(10.dp)
            )

            satelliteData?.let {

                Text(
                    text = "INC ${"%.2f".format(it.inclination)}°   •   ECC ${it.eccentricity}",
                    fontSize = 11.sp,
                    color = Color.Gray
                )
            }

            errorMessage?.let {

                Spacer(
                    modifier = Modifier.height(8.dp)
                )

                Text(
                    text = it,
                    fontSize = 13.sp,
                    color = Color.Red
                )
            }
        }
    }
}

@Composable
private fun SatelliteValue(
    label: String,
    value: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Text(
            text = label,
            fontSize = 10.sp,
            color = Color.Gray
        )

        Spacer(
            modifier = Modifier.height(2.dp)
        )

        Text(
            text = value,
            fontSize = 15.sp,
            color = Color.White
        )
    }
}