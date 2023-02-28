package com.example.googlelenstest

import android.annotation.SuppressLint
import android.util.Log
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.lifecycle.MutableLiveData
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.label.ImageLabeling
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions
import kotlin.math.roundToInt


class ImageLabelAnalyzer: ImageAnalysis.Analyzer {

    private val labeler = ImageLabeling.getClient(
        ImageLabelerOptions.Builder()
            .setConfidenceThreshold(0.7F)
            .build()
    )
    public val imageResult = MutableLiveData<String>()
    val imagePrediction = MutableLiveData<String>()

    @SuppressLint("UnsafeExperimentalUsageError", "UnsafeOptInUsageError")
    override fun analyze(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image
        mediaImage?.let {
            val image = InputImage.fromMediaImage(it, imageProxy.imageInfo.rotationDegrees)
            // Passing image to an ML Kit Vision API
            labeler.process(image)
                .addOnSuccessListener { labels ->
                    // SUCCESS check all the labels
                    for (label in labels) {
                        Log.d("IMAGE RESULT:", """ Format = ${label.text} Value = ${label.confidence} """.trimIndent())
                        imageResult.value=label.text
                        imagePrediction.value= "%.2f".format(label.confidence * 100)
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("Image Analysis", "Detection failed", e)
                }
                .addOnCompleteListener {
                    imageProxy.close()
                }
        } ?: imageProxy.close()
    // close if none labels found
    }
}
