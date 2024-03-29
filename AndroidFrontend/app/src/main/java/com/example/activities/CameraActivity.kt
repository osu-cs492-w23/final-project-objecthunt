package com.example.activities

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Base64
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
import androidx.lifecycle.Observer
import com.example.SocketHandler
import com.example.data.ItemToFind
import com.example.ui.ImageLabelAnalyzer
import io.socket.client.Ack
import io.socket.client.IO
import io.socket.client.Socket
import io.socket.emitter.Emitter
import io.socket.engineio.client.transports.WebSocket
import java.io.ByteArrayOutputStream
import java.io.File
import java.net.URISyntaxException
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
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
    private lateinit var tvObjectStatus: TextView
    private var currentItemName: String? = null

    //receive the item from the server
    //Image analyzer build
    private var imageAnalysis = ImageAnalysis.Builder()
        .setImageQueueDepth(ImageAnalysis.STRATEGY_BLOCK_PRODUCER)
        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
        .build()

    private val opts = IO.Options().apply {
        transports = listOf(WebSocket.NAME).toTypedArray()
    }


    // Initialize targetItem as null
    private var item: String? = null
    private lateinit var imageResultObserver: Observer<String?>

    private val onItemReceived = Emitter.Listener { args ->
        item = args[0] as String
        Log.d(TAG, "Item received: $item")

        // Start image labeling when the item is received
        runOnUiThread {
            // Update the UI or perform an action based on the received item
            // e.g., update a TextView or start a new process

            // Start image labeling when the item is received
            startImageLabeling()
        }
    }

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //AndroidLoggingHandler.reset(AndroidLoggingHandler())
        cameraExecutor = Executors.newSingleThreadExecutor()
        Logger.getLogger("my.category").level = Level.FINEST
        setContentView(com.example.chatting.R.layout.activity_google_lens)
        tvImageResult = findViewById(com.example.chatting.R.id.tv_img_label)
        tvPredictionConfidence = findViewById(com.example.chatting.R.id.tv_prediction_accuracy)
        imageAnalyzer = ImageLabelAnalyzer()
        tvObjectStatus = findViewById(com.example.chatting.R.id.tv_object_status)

        Log.d(TAG, "HELLO IAM HERE")
        val currentItem = intent.getSerializableExtra("currentItem") as ItemToFind
        currentItemName = currentItem?.name
        Log.d("Current Item recieved from the Game:", "$currentItemName")

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
            mSocket = SocketHandler.getSocket()
        } catch (e: URISyntaxException) {
            throw RuntimeException(e)
        }
        mSocket.on(Socket.EVENT_CONNECT, onConnect);
        mSocket.on(Socket.EVENT_DISCONNECT, onDisconnect);
        mSocket.on(Socket.EVENT_CONNECT_ERROR, onConnectError);
        mSocket.connect()


        mSocket.on("itemGenerated", onItemReceived)
        // Add the testing listener
        mSocket.on("*", Emitter.Listener { args ->
            Log.d(TAG, "Incoming event: ${args[0]}, data: ${args[1]}")
        })

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
            //when picture capture button is pressed, send an echo test to socket server
            Log.d("imageLabelingStarted", "$imageLabelingStarted")



            // Take a photo and send it to the server
            Log.d("SHOULD SEND TO SERVER?!!??!", "")
            takePhotoAndSendToServer()

            // Call Image Analysis Function
            // Do not add the "item" event listener here, it's already added in onCreate
            //imageLabelingStarted = true
        }

        outputDirectory = getOutputDirectory()

        // cameraExecutor = Executors.newSingleThreadExecutor()
        startImageLabeling()
    }

    //update status
    private fun updateObjectStatus(targetItem: String?, imageResult: String?) {
        Log.d("passed item", "$targetItem")
        if (targetItem == imageResult) {
            tvObjectStatus.text = "Object Found!"
        } else {
            tvObjectStatus.text = "Object Not Found!"
        }
    }

    //start image labeling (running constantly)
    private fun startImageLabeling() {
        if (imageLabelingStarted) {
            return
        }

        imageAnalysis.setAnalyzer(
            ContextCompat.getMainExecutor(this),
            imageAnalyzer
        )

        imageAnalyzer.imageResult.observe(this) { img ->
            val lowercaseImageResult = img?.toLowerCase(Locale.getDefault())
            tvImageResult.text = lowercaseImageResult
            tvPredictionConfidence.text = imageAnalyzer.imagePrediction.value.toString()
            if (currentItemName == lowercaseImageResult) {
                imageAnalysis.clearAnalyzer()
                imageLabelingStarted = false
                updateObjectStatus(currentItemName, lowercaseImageResult)
            }
        }

        // Update the imageLabelingStarted flag
        imageLabelingStarted = true
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


    private fun getOutputDirectory(): File {
        val mediaDir = externalMediaDirs.firstOrNull()?.let {

            File(it, getString(com.example.chatting.R.string.app_name)).apply { mkdirs() }
        }
        return if (mediaDir != null && mediaDir.exists())
            mediaDir else filesDir
    }


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

            // Initialize the imageCapture variable
            imageCapture = ImageCapture.Builder()
                .build()

            try {
                // Unbind use cases before rebinding
                cameraProvider.unbindAll()

                // Bind use cases to camera image analysis
                cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, imageAnalysis, imageCapture
                )

            } catch (exc: Exception) {
                Log.e(TAG, "Use case binding failed", exc)
            }

        }, ContextCompat.getMainExecutor(this))
    }

    private fun takePhotoAndSendToServer() {
        // Get a stable reference to the modifiable image capture use case
        Log.d("OKAY IM INSIDE!??", "")
        val imageCapture = imageCapture ?: return

        // Create a time-stamped output file to hold the image
        val photoFile = File(
            outputDirectory,
            SimpleDateFormat(FILENAME_FORMAT, Locale.US)
                .format(System.currentTimeMillis()) + ".jpg"
        )

        // Create output options object which contains file + metadata
        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        // Set up image capture listener, which is triggered after the photo has been taken
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

                    // Send the photo back to the server
                    val bitmap = BitmapFactory.decodeFile(photoFile.absolutePath)
                    val sizeBeforeCompress = bitmap.allocationByteCount / 1024
                    Log.d("BEFORE COMPRESS SIZE!!!:KB", "$sizeBeforeCompress")

                    val byteArrayOutputStream = ByteArrayOutputStream()
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 50, byteArrayOutputStream)

                    val sizeAfterCompress = byteArrayOutputStream.size() / 1024
                    Log.d("AFTER COMPRESS SIZE!!!:KB", "$sizeAfterCompress")

                    val byteArray = byteArrayOutputStream.toByteArray()
                    val encodedImage = Base64.encodeToString(byteArray, Base64.NO_WRAP)

                    Log.d("The actual image base64:", encodedImage.toString())

                    println("emitting")

                    println("Preparing to emit submitAnswer event")
                    mSocket.emit("submitAnswer", encodedImage, "111", Ack { args ->
                        Log.d(TAG, "Ack $args")
                    })
                    println("submitAnswer event emitted")
                }
            }
        )
    }

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