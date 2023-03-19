package com.example.activities

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.ui.ImageLabelAnalyzer
import io.socket.client.Ack
import io.socket.client.IO
import io.socket.client.Socket
import io.socket.emitter.Emitter
import io.socket.engineio.client.transports.WebSocket
import java.io.File
import java.net.URISyntaxException
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.logging.*
import java.util.logging.Logger


class CameraActivity : AppCompatActivity() {
    private var imageCapture: ImageCapture? = null
    private lateinit var cameraExecutor: ExecutorService
    private lateinit var viewFinder: PreviewView
    private lateinit var outputDirectory: File
    private lateinit var mSocket: Socket
    private var imageLabelingStarted = false
    private lateinit var tvImageResult: TextView
    private lateinit var tvPredictionConfidence: TextView
    private lateinit var imageAnalyzer: ImageLabelAnalyzer

    //Image analyzer build
    private var imageAnalysis = ImageAnalysis.Builder()
        .setImageQueueDepth(ImageAnalysis.STRATEGY_BLOCK_PRODUCER)
        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
        .build()

    private val opts = IO.Options().apply {
        transports = listOf(WebSocket.NAME).toTypedArray()
    }

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //AndroidLoggingHandler.reset(AndroidLoggingHandler())
        Logger.getLogger("my.category").level = Level.FINEST
        setContentView(com.example.chatting.R.layout.activity_google_lens)
        tvImageResult = findViewById(com.example.chatting.R.id.tv_img_label)
        tvPredictionConfidence = findViewById(com.example.chatting.R.id.tv_prediction_accuracy)
        imageAnalyzer = ImageLabelAnalyzer()
        //Dynamically observe LiveData changes from the ImageAnalyzer
        imageAnalyzer.imageResult.observe(this) { img ->
            tvImageResult.text = img
        }
        imageAnalyzer.imagePrediction.observe(this) { prediction ->
            tvPredictionConfidence.text = "$prediction%"
        }
        viewFinder = findViewById(com.example.chatting.R.id.pvv_main_preview)
        val cameraCaptureButton =
            findViewById<Button>(com.example.chatting.R.id.btn_main_picture_taking)
        try {
            mSocket = IO.socket("http://192.168.8.162:3000", opts)
        } catch (e: URISyntaxException) {
            throw RuntimeException(e)
        }
        mSocket.on(Socket.EVENT_CONNECT, onConnect);
        mSocket.on(Socket.EVENT_DISCONNECT, onDisconnect);
        mSocket.on(Socket.EVENT_CONNECT_ERROR, onConnectError);
        mSocket.connect()

        // camera permissions
        if (allPermissionsGranted()) {
            startCamera()
        } else {
            ActivityCompat.requestPermissions(
                this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS
            )
        }

        // Set up the listener for take photo button
        cameraCaptureButton.setOnClickListener {
            if (imageLabelingStarted) {
                return@setOnClickListener
            }

            //Call Image Analysis Function
            startImageLabeling()
            imageLabelingStarted = true

            //when picture capture button is pressed, send an echo test to socket server
            println("emitting")
            mSocket.emit("echoTest", "from Android!", Ack { args ->
                Log.d(TAG, "Ack $args")
            })
        }

        //outputDirectory = getOutputDirectory()

        // cameraExecutor = Executors.newSingleThreadExecutor()
    }

    //start image labeling (running constantly)
    private fun startImageLabeling() {
        imageAnalysis.setAnalyzer(
            ContextCompat.getMainExecutor(this),
            imageAnalyzer
        )
        tvImageResult.text = imageAnalyzer.imageResult.value
        tvPredictionConfidence.text = imageAnalyzer.imagePrediction.value.toString()
    }

    private val onConnect = Emitter.Listener {
        Log.d(TAG, "connected...")
        // This doesn't run in the UI thread, so use:
        // .runOnUiThread if you want to do something in the UI
    }
    private val onDisconnect = Emitter.Listener {
        Log.d(TAG, "disconnected...")
        // This doesn't run in the UI thread, so use:
        // .runOnUiThread if you want to do something in the UI
    }
    private val onConnectError = Emitter.Listener {
        Log.d(TAG, "error!...")
        // This doesn't run in the UI thread, so use:
        // .runOnUiThread if you want to do something in the UI
    }

/*
    private fun getOutputDirectory(): File {
        val mediaDir = externalMediaDirs.firstOrNull()?.let {
            File(it, resources.getString(R.string.app_name)).apply { mkdirs() }
        }
        return if (mediaDir != null && mediaDir.exists())
            mediaDir else filesDir
    }
 */

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            // Used to bind the lifecycle of cameras to the lifecycle owner
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            // Preview
            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(viewFinder.surfaceProvider)
                }
            // Select back camera as a default
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                // Unbind use cases before rebinding
                cameraProvider.unbindAll()

                // Bind use cases to camera image analysis
                cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, imageAnalysis
                )

            } catch (exc: Exception) {
                Log.e(TAG, "Use case binding failed", exc)
            }

        }, ContextCompat.getMainExecutor(this))
    }
/*
    private fun takePhoto() {
        // Get a stable reference of the modifiable image capture use case
        val imageCapture = imageCapture ?: return

        // Create time-stamped output file to hold the image
        val photoFile = File(
            outputDirectory,
            SimpleDateFormat(
                FILENAME_FORMAT, Locale.US
            ).format(System.currentTimeMillis()) + ".jpg"
        )

        // Create output options object which contains file + metadata
        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()


        // Set up image capture listener, which is triggered after photo has
        // been taken
        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageSavedCallback {
                override fun onError(exc: ImageCaptureException) {
                    Log.e(TAG, "Photo capture failed: ${exc.message}", exc)
                }

                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    val savedUri = Uri.fromFile(photoFile)
                    val msg = "Photo capture succeeded: $savedUri"
                    Toast.makeText(baseContext, msg, Toast.LENGTH_SHORT).show()
                    Log.d(TAG, msg)
                }
            })
    } */

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(
            baseContext, it
        ) == PackageManager.PERMISSION_GRANTED
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults:
        IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                startCamera()
            } else {
                Toast.makeText(
                    this,
                    "Permissions not granted by the user.",
                    Toast.LENGTH_SHORT
                ).show()
                finish()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }

    companion object {
        private const val TAG = "CameraXBasic"
        private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
        private const val REQUEST_CODE_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
    }

}


/**
 * Make JUL work on Android.

class AndroidLoggingHandler : Handler() {
override fun close() {}
override fun flush() {}
override fun publish(record: LogRecord) {
if (!super.isLoggable(record)) return
val name = record.loggerName
val maxLength = 30
val tag = if (name.length > maxLength) name.substring(name.length - maxLength) else name
try {
val level = getAndroidLevel(record.level)
Log.println(level, tag, record.message)
if (record.thrown != null) {
Log.println(level, tag, Log.getStackTraceString(record.thrown))
}
} catch (e: RuntimeException) {
Log.e("AndroidLoggingHandler", "Error logging message.", e)
}
}

companion object {
fun reset(rootHandler: Handler?) {
val rootLogger = LogManager.getLogManager().getLogger("")
val handlers = rootLogger.handlers
for (handler in handlers) {
rootLogger.removeHandler(handler)
}
if (rootHandler != null) {
rootLogger.addHandler(rootHandler)
}
}

fun getAndroidLevel(level: Level): Int {
val value = level.intValue()
return if (value >= Level.SEVERE.intValue()) {
Log.ERROR
} else if (value >= Level.WARNING.intValue()) {
Log.WARN
} else if (value >= Level.INFO.intValue()) {
Log.INFO
} else {
Log.DEBUG
}
}
}
}
 */