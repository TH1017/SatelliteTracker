package com.example.satellitetracker

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL

data class SatelliteData(
    val name: String,
    val catalogNumber: Int,
    val epoch: String,
    val inclination: Double,
    val eccentricity: Double,
    val meanMotion: Double,
    val line1: String,
    val line2: String
)

class SatelliteRepository {

    suspend fun getIssData(): SatelliteData = withContext(Dispatchers.IO) {

        val url = URL(
            "https://celestrak.org/NORAD/elements/gp.php?CATNR=25544&FORMAT=TLE"
        )

        val connection = url.openConnection() as HttpURLConnection

        try {
            connection.requestMethod = "GET"
            connection.connectTimeout = 10000
            connection.readTimeout = 10000

            val response = connection.inputStream
                .bufferedReader()
                .use { it.readText() }

            val lines = response
                .lines()
                .map { it.trim() }
                .filter { it.isNotBlank() }

            if (lines.size < 3) {
                throw IllegalStateException(
                    "TLE data could not be retrieved."
                )
            }

            val name = lines[0]
            val line1 = lines[1]
            val line2 = lines[2]

            if (!line1.startsWith("1 ") || !line2.startsWith("2 ")) {
                throw IllegalStateException(
                    "Invalid TLE data."
                )
            }

            val catalogNumber = line2
                .substring(2, 7)
                .trim()
                .toInt()

            val epoch = line1
                .substring(18, 32)
                .trim()

            val inclination = line2
                .substring(8, 16)
                .trim()
                .toDouble()

            val eccentricity = line2
                .substring(26, 33)
                .trim()
                .toDouble() / 10000000.0

            val meanMotion = line2
                .substring(52, 63)
                .trim()
                .toDouble()

            SatelliteData(
                name = name,
                catalogNumber = catalogNumber,
                epoch = epoch,
                inclination = inclination,
                eccentricity = eccentricity,
                meanMotion = meanMotion,
                line1 = line1,
                line2 = line2
            )

        } finally {
            connection.disconnect()
        }
    }
}