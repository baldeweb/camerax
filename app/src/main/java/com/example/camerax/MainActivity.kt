package com.example.camerax

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.camera.core.*
import androidx.core.app.ActivityCompat
import com.example.camerax.CameraUtil.Companion.startCamera
import com.example.camerax.CameraUtil.Companion.takePhoto
import com.example.camerax.PermissionUtils.Companion.REQUEST_CODE_PERMISSIONS
import com.example.camerax.PermissionUtils.Companion.REQUIRED_PERMISSIONS
import com.example.camerax.PermissionUtils.Companion.allPermissionsGranted
import com.example.camerax.StorageUtils.Companion.getOutputDirectory
import com.example.camerax.databinding.ActivityMainBinding
import java.io.File
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class MainActivity : AppCompatActivity() {
    private var imageCapture: ImageCapture? = null
    private lateinit var outputDirectory: File
    private lateinit var cameraExecutor: ExecutorService
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initializeData()
        setListeners()
        checkPermissions()
    }

    private fun initializeData() {
        outputDirectory = getOutputDirectory(this)
        cameraExecutor = Executors.newSingleThreadExecutor()
    }

    private fun setListeners() {
        binding.cameraCaptureButton.setOnClickListener {
            takePhoto(this, outputDirectory, imageCapture)
        }
    }

    private fun checkPermissions() {
        if (allPermissionsGranted(this)) {
            startCamera(this, cameraExecutor) { imageCapture = it }
        } else {
            ActivityCompat.requestPermissions(
                this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults:
        IntArray
    ) {
        if (requestCode == REQUEST_CODE_PERMISSIONS && allPermissionsGranted(this)) {
            startCamera(this, cameraExecutor) { imageCapture = it }
        } else {
            Toast.makeText(
                this,
                "Permissions not granted by the user.",
                Toast.LENGTH_SHORT
            ).show()
            finish()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }
}