package com.fp.ai_driving_asist

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.fp.ai_driving_asist.databinding.ActivityMainBinding
import com.google.mediapipe.tasks.vision.core.RunningMode
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class MainActivity : AppCompatActivity(), FaceLandmarkerHelper.LandmarkerListener {

    private lateinit var binding: ActivityMainBinding
    private lateinit var faceLandmarkerHelper: FaceLandmarkerHelper
    private lateinit var blinkDetector: BlinkDetector
    private lateinit var drowsinessDetector: DrowsinessDetector
    private lateinit var cameraExecutor: ExecutorService

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                startCamera()
            } else {
                Toast.makeText(this, "Camera permission required", Toast.LENGTH_SHORT).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        cameraExecutor = Executors.newSingleThreadExecutor()
        blinkDetector = BlinkDetector()
        drowsinessDetector = DrowsinessDetector()

        faceLandmarkerHelper =
            FaceLandmarkerHelper(context = this, faceLandmarkerHelperListener = this)

        if (
            ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) ==
            PackageManager.PERMISSION_GRANTED
        ) {
            startCamera()
        } else {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener(
            {
                val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

                val preview =
                    Preview.Builder().build().also {
                        it.setSurfaceProvider(binding.viewFinder.surfaceProvider)
                    }

                val imageAnalyzer =
                    ImageAnalysis.Builder()
                        .setBackpressureStrategy(
                            ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST
                        )
                        .setOutputImageFormat(
                            ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888
                        )
                        .build()
                        .also {
                            it.setAnalyzer(cameraExecutor) { image ->
                                faceLandmarkerHelper.detectLiveStream(
                                    imageProxy = image,
                                    isFrontCamera = true
                                )
                            }
                        }

                val cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA

                try {
                    cameraProvider.unbindAll()
                    cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageAnalyzer)
                } catch (exc: Exception) {
                    Log.e(TAG, "Use case binding failed", exc)
                }
            },
            ContextCompat.getMainExecutor(this)
        )
    }

    override fun onError(error: String, errorCode: Int) {
        runOnUiThread { Toast.makeText(this, error, Toast.LENGTH_SHORT).show() }
    }

    override fun onResults(resultBundle: FaceLandmarkerHelper.ResultBundle) {
        val result = resultBundle.result

        runOnUiThread {
            // gambar landmark
            binding.overlay.setResults(
                result,
                resultBundle.inputImageHeight,
                resultBundle.inputImageWidth,
                RunningMode.LIVE_STREAM
            )

            val faces = result.faceLandmarks()
            if (faces.isNotEmpty()) {
                val landmarks = faces[0]

                val blinkResult =
                    blinkDetector.process(
                        landmarks,
                        resultBundle.inputImageWidth,
                        resultBundle.inputImageHeight
                    )

                binding.tvBlinkCount.text = "Blinks: ${blinkResult.blinkCount}"

                val isDrowsy = drowsinessDetector.update(
                    eyesClosed = blinkResult.eyesClosed,
                    frameTimestampMs = result.timestampMs()
                )

                // Update status UI
                when {
                    isDrowsy -> {
                        binding.tvStatus.text = "Status: DROWSY"
                        binding.tvStatus.setTextColor(
                            ContextCompat.getColor(this, android.R.color.holo_red_light)
                        )
                    }
                    blinkResult.eyesClosed -> {
                        binding.tvStatus.text = "Status: Eyes closed"
                        binding.tvStatus.setTextColor(
                            ContextCompat.getColor(this, android.R.color.holo_orange_light)
                        )
                    }
                    else -> {
                        binding.tvStatus.text = "Status: SAFE"
                        binding.tvStatus.setTextColor(
                            ContextCompat.getColor(this, android.R.color.holo_green_light)
                        )
                    }
                }

                binding.overlay.setDrowsyState(isDrowsy)
            } else {
                binding.tvStatus.text = "Status: No face detected"
                binding.tvStatus.setTextColor(
                    ContextCompat.getColor(this, android.R.color.darker_gray)
                )
                binding.overlay.setDrowsyState(false)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
        faceLandmarkerHelper.clearFaceLandmarker()
        binding.overlay.clear()
    }

    companion object {
        private const val TAG = "MainActivity"
    }
}
