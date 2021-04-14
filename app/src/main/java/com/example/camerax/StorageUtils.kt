package com.example.camerax

import androidx.appcompat.app.AppCompatActivity
import java.io.File

class StorageUtils {
    companion object {
        fun getOutputDirectory(activity: AppCompatActivity): File {
            val mediaDir = activity.externalMediaDirs.firstOrNull()?.let {
                File(it, activity.resources.getString(R.string.app_name)).apply { mkdirs() }
            }
            return if (mediaDir != null && mediaDir.exists())
                mediaDir else activity.filesDir
        }

    }
}