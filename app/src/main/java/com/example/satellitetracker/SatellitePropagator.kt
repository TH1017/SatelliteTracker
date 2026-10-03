package com.example.satellitetracker

import org.orekit.frames.FramesFactory
import org.orekit.propagation.analytical.tle.TLE
import org.orekit.propagation.analytical.tle.TLEPropagator
import org.orekit.time.AbsoluteDate
import org.orekit.time.TimeScalesFactory
import org.orekit.utils.Constants
import java.util.Date
import kotlin.math.atan2
import kotlin.math.sqrt

data class SatellitePosition(
    val latitude: Double,
    val longitude: Double,
    val altitude: Double
)

class SatellitePropagator {

    fun getCurrentPosition(
        line1: String,
        line2: String
    ): SatellitePosition {

        val tle = TLE(
            line1,
            line2
        )

        val propagator = TLEPropagator.selectExtrapolator(tle)

        val utc = TimeScalesFactory.getUTC()

        val date = AbsoluteDate(
            Date(),
            utc
        )

        return calculatePosition(
            propagator = propagator,
            date = date
        )
    }

    fun getOrbitPositions(
        line1: String,
        line2: String,
        durationMinutes: Int = 90,
        stepSeconds: Int = 60
    ): List<SatellitePosition> {

        val tle = TLE(
            line1,
            line2
        )

        val propagator = TLEPropagator.selectExtrapolator(tle)

        val utc = TimeScalesFactory.getUTC()

        val startDate = AbsoluteDate(
            Date(),
            utc
        )

        val positions = mutableListOf<SatellitePosition>()

        val totalSeconds = durationMinutes * 60

        var elapsedSeconds = 0

        while (elapsedSeconds <= totalSeconds) {

            val date = startDate.shiftedBy(
                elapsedSeconds.toDouble()
            )

            val position = calculatePosition(
                propagator = propagator,
                date = date
            )

            positions.add(position)

            elapsedSeconds += stepSeconds
        }

        return positions
    }

    private fun calculatePosition(
        propagator: TLEPropagator,
        date: AbsoluteDate
    ): SatellitePosition {

        val teme = FramesFactory.getTEME()

        val pv = propagator.getPVCoordinates(
            date,
            teme
        )

        val position = pv.position

        val x = position.x
        val y = position.y
        val z = position.z

        val longitude = Math.toDegrees(
            atan2(y, x)
        )

        val latitude = Math.toDegrees(
            atan2(
                z,
                sqrt(x * x + y * y)
            )
        )

        val radius = sqrt(
            x * x + y * y + z * z
        )

        val altitude =
            radius - Constants.WGS84_EARTH_EQUATORIAL_RADIUS

        return SatellitePosition(
            latitude = latitude,
            longitude = longitude,
            altitude = altitude / 1000.0
        )
    }
}