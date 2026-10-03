package com.example.satellitetracker

import android.content.Context
import java.io.File
import java.io.FileOutputStream
import org.orekit.data.DataContext
import org.orekit.data.DirectoryCrawler

object OrekitInitializer {

    private var initialized = false

    fun initialize(context: Context) {
        if (initialized) {
            return
        }

        val dataDirectory = File(
            context.filesDir,
            "orekit-data"
        )

        if (!dataDirectory.exists()) {
            copyAssets(
                context,
                "orekit-data",
                dataDirectory
            )
        }

        val manager =
            DataContext.getDefault().dataProvidersManager

        manager.addProvider(
            DirectoryCrawler(dataDirectory)
        )

        initialized = true
    }

    private fun copyAssets(
        context: Context,
        assetPath: String,
        destination: File
    ) {
        destination.mkdirs()

        val assetManager = context.assets

        val files = assetManager.list(assetPath)
            ?: return

        for (fileName in files) {
            val sourcePath = "$assetPath/$fileName"
            val destinationFile = File(
                destination,
                fileName
            )

            val children = assetManager.list(sourcePath)

            if (children != null && children.isNotEmpty()) {
                copyAssets(
                    context,
                    sourcePath,
                    destinationFile
                )
            } else {
                assetManager.open(sourcePath).use { input ->
                    FileOutputStream(destinationFile).use { output ->
                        input.copyTo(output)
                    }
                }
            }
        }
    }
}