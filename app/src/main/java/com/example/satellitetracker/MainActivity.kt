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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.graphics.Color
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
                    containerColor = Color.Black
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
        mapView.onStop()
        super.onStop()
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
            modifier = Modifier.height(24.dp)
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
            modifier = Modifier.height(24.dp)
        )

        Text(
            text = satelliteData?.name ?: "ISS",
            fontSize = 22.sp,
            color = Color.White
        )

        Spacer(
            modifier = Modifier.height(8.dp)
        )

        satelliteData?.let {
            Text(
                text = "NORAD: ${it.catalogNumber}",
                fontSize = 16.sp,
                color = Color.LightGray
            )

            Text(
                text = "傾斜角: ${it.inclination}°",
                fontSize = 16.sp,
                color = Color.LightGray
            )

            Text(
                text = "離心率: ${it.eccentricity}",
                fontSize = 16.sp,
                color = Color.LightGray
            )

            Text(
                text = "平均運動: ${it.meanMotion}",
                fontSize = 16.sp,
                color = Color.LightGray
            )
        }

        satellitePosition?.let {
            Spacer(
                modifier = Modifier.height(16.dp)
            )

            Text(
                text = "Latitude: ${"%.4f".format(it.latitude)}°",
                fontSize = 16.sp,
                color = Color.White
            )

            Text(
                text = "Longitude: ${"%.4f".format(it.longitude)}°",
                fontSize = 16.sp,
                color = Color.White
            )

            Text(
                text = "Altitude: ${"%.1f".format(it.altitude)} km",
                fontSize = 16.sp,
                color = Color.White
            )
        }

        errorMessage?.let {
            Spacer(
                modifier = Modifier.height(8.dp)
            )

            Text(
                text = it,
                fontSize = 14.sp,
                color = Color.Red
            )
        }
    }
}