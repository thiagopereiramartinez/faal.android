package com.projetointegrador.faal.ui

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.ImageFormat
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.LifecycleOwner
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.projetointegrador.faal.R
import com.projetointegrador.faal.databinding.ActivityScannerTextoBinding
import com.projetointegrador.faal.utils.YuvToRgbConverter
import java.io.ByteArrayOutputStream
import java.util.*

class ScannerTextoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityScannerTextoBinding
    private lateinit var cameraProvider: ProcessCameraProvider
    private lateinit var imageCapture: ImageCapture
    private lateinit var camera: Camera

    private val storage = Firebase.storage

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_scanner_texto)

        checkPermissions()
    }

    private fun checkPermissions() {
        when {
            permissions.all {
                ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
            } -> startCameraPreview()
            else -> requestPermissions()
        }
    }

    private fun requestPermissions() {
        ActivityCompat.requestPermissions(this, permissions, 200)
    }

    @SuppressLint("RestrictedApi", "UnsafeOptInUsageError")
    private fun startCameraPreview() {
        cameraProvider = ProcessCameraProvider.getInstance(this).get()

        val preview = Preview.Builder().build()
        val cameraSelector = CameraSelector.Builder()
            .requireLensFacing(CameraSelector.LENS_FACING_BACK)
            .build()

        imageCapture = ImageCapture.Builder()
            .setBufferFormat(ImageFormat.YUV_420_888)
            .build()

        preview.setSurfaceProvider(binding.cameraPreview.surfaceProvider)
        camera = cameraProvider.bindToLifecycle(this as LifecycleOwner, cameraSelector, preview, imageCapture)

        binding.btnTirarFoto.setOnClickListener {
            binding.progress.isVisible = true

            imageCapture.takePicture(ContextCompat.getMainExecutor(this), object : ImageCapture.OnImageCapturedCallback() {

                override fun onCaptureSuccess(image: ImageProxy) {
                    image.use {
                        uploadImage(it)
                    }
                }

            })
        }
    }

    @SuppressLint("UnsafeOptInUsageError")
    private fun uploadImage(imageProxy: ImageProxy) {
        val storageRef = storage.reference
        val imageRef = storageRef.child("users/${FirebaseAuth.getInstance().currentUser!!.uid}/images/${Calendar.getInstance().timeInMillis}.jpg")

        val bitmap = Bitmap.createBitmap(imageProxy.width, imageProxy.height, Bitmap.Config.ARGB_8888)
        YuvToRgbConverter(this).yuvToRgb(imageProxy.image!!, bitmap)

        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)

        imageRef.putBytes(baos.toByteArray()).addOnSuccessListener {
            binding.progress.isVisible = false
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        checkPermissions()
    }

    companion object {
        private val permissions = arrayOf(
            Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
        )
    }

}