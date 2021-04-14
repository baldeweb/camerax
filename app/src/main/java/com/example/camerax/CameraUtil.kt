package com.example.camerax

import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ExecutorService

class CameraUtil {
    companion object {
        const val TAG = "CameraXBasic"
        const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"

        fun startCamera(
            activity: MainActivity,
            cameraExecutor: ExecutorService?,
            imageCaptureParam: (ImageCapture?) -> Unit
        ) {
            /*
            This is used to bind the lifecycle of cameras to the lifecycle owner.
            This eliminates the task of opening and closing the camera since CameraX is lifecycle-aware.
            */
            val cameraProviderFuture = ProcessCameraProvider.getInstance(activity)

            cameraProviderFuture.addListener({
                // Used to bind the lifecycle of cameras to the lifecycle owner
                val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

                // Preview
                val preview = Preview.Builder()
                    .build()
                    .also {
                        it.setSurfaceProvider(activity.viewFinder.surfaceProvider)
                    }

                val imageCapture = ImageCapture.Builder().build()
                imageCaptureParam.invoke(imageCapture)

                val imageAnalyzer = ImageAnalysis.Builder()
                    .build()
                    .also {
                        cameraExecutor?.let { executor ->
                            it.setAnalyzer(executor, LuminosityAnalyzer { luma ->
                            })
                        }
                    }

                // Select back camera as a default
                val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                try {
                    // Unbind use cases before rebinding
                    cameraProvider.unbindAll()

                    // Bind use cases to camera
                    cameraProvider.bindToLifecycle(
                        activity, cameraSelector, preview, imageCapture, imageAnalyzer
                    )

                } catch (exc: Exception) {
                }

            }, ContextCompat.getMainExecutor(activity))
        }

        fun takePhoto(
            activity: AppCompatActivity,
            outputDirectory: File,
            imageCapture: ImageCapture?
        ) {
            // Create time-stamped output file to hold the image
            val photoFile = File(
                outputDirectory, SimpleDateFormat(FILENAME_FORMAT, Locale.US)
                    .format(System.currentTimeMillis()) + ".jpg"
            )

            // Create output options object which contains file + metadata
            val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

            // Set up image capture listener, which is triggered after photo has
            // been taken

            (imageCapture ?: return).takePicture(
                outputOptions,
                ContextCompat.getMainExecutor(activity),
                object : ImageCapture.OnImageSavedCallback {
                    override fun onError(exc: ImageCaptureException) {
                        Log.d(TAG, "Photo capture failed: ${exc.message}", exc)
                    }

                    override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                        val savedUri = Uri.fromFile(photoFile)
                        val msg = "Photo capture succeeded: $savedUri"
                        Toast.makeText(activity, msg, Toast.LENGTH_SHORT).show()
                        Log.d(TAG, msg)
                    }
                })
        }
    }
}