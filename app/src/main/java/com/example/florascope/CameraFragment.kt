package com.example.florascope

import android.Manifest
import android.content.pm.PackageManager
import android.content.res.AssetManager
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.florascope.databinding.FragmentCameraBinding
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel

class CameraFragment : Fragment() {
    private lateinit var binding: FragmentCameraBinding
    private lateinit var takePicturePreviewLauncher: ActivityResultLauncher<Void?>
    private lateinit var requestPermissionLauncher: ActivityResultLauncher<String>
    private lateinit var interpreter: Interpreter
    private var isModelLoaded = false
    private lateinit var currentModelFileName: String
    private var numOfClasses: Int = 0
    private lateinit var nameOfClasses: Array<String>

    companion object {
        private const val TAG = "CameraFragment"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize takePicturePreviewLauncher
        takePicturePreviewLauncher =
            registerForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap ->
                // This is where you receive the result (bitmap)
                if (bitmap != null) {
                    binding.clickImage.setImageBitmap(bitmap)

                    Log.d(
                        TAG,
                        "Bitmap received from camera: width=${bitmap.width}, height=${bitmap.height}"
                    )

                    if (isModelLoaded) {
                        processImageWithModel(bitmap)
                    } else {
                        Toast.makeText(requireContext(), "Model is not loaded", Toast.LENGTH_LONG)
                            .show()
                    }
                } else {
                    Log.e(TAG, "Bitmap received from camera is null")
                    Toast.makeText(
                        requireContext(),
                        "Error: Bitmap received from camera is null",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }

        // Initialize requestPermissionLauncher
        requestPermissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
                if (isGranted) {
                    // Permission is granted, you can launch the camera here
                    takePicturePreviewLauncher.launch(null)
                } else {
                    // Permission is denied. Handle the error.
                    Toast.makeText(
                        requireContext(),
                        "Camera permission is required to take pictures",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCameraBinding.inflate(inflater)

        binding.b1.setOnClickListener {
            numOfClasses = 4
            nameOfClasses = Array(4) { "" }
            nameOfClasses[0] = "Apple Scab"
            nameOfClasses[1] = "Apple Black rot"
            nameOfClasses[2] = "Apple Cedar rust"
            nameOfClasses[3] = "Apple Healthy"

            loadModel("apple_model.tflite")
            launchCamera()
        }

        binding.b2.setOnClickListener {
            numOfClasses = 7
            nameOfClasses = Array(7) { "" }
            nameOfClasses[0] = "Banana Healthy"
            nameOfClasses[1] = "Banana Panama Disease"
            nameOfClasses[2] = "Banana Black Sigatoka Disease"
            nameOfClasses[3] = "Banana Bract Mosaic Virus Disease"
            nameOfClasses[4] = "Banana Insect Pest Disease"
            nameOfClasses[5] = "Banana Moko Disease"
            nameOfClasses[6] = "Banana Yellow Sigatoka Disease "

            loadModel("banana_model.tflite")
            launchCamera()
        }

        binding.b3.setOnClickListener {
            numOfClasses = 1
            nameOfClasses = Array(2) { "" }
            nameOfClasses[0] = "Cherry Powdery Mildew"
            nameOfClasses[1] = "Cherry Healthy"

            loadModel("cherry_model.tflite")
            launchCamera()
        }

        binding.b4.setOnClickListener {
            this.numOfClasses = 4
            nameOfClasses = Array(4) { "" }
            nameOfClasses[0] = "Corn Cercospora leaf spot & Gray leaf spot"
            nameOfClasses[1] = "Corn Common Rust"
            nameOfClasses[2] = "Corn Nothern Leaf Blight"
            nameOfClasses[3] = "Corn Healthy"

            loadModel("corn_model.tflite")
            launchCamera()
        }

        binding.b5.setOnClickListener {
            this.numOfClasses = 4
            nameOfClasses = Array(4) { "" }
            nameOfClasses[0] = "Grape Black Rot"
            nameOfClasses[1] = "Grape Esca (Black Measles)"
            nameOfClasses[2] = "Grape Leaf Blight (Isariopsis Leaf Spot)"
            nameOfClasses[3] = "Grape Healthy"

            loadModel("grape_model.tflite")
            launchCamera()
        }

        binding.b6.setOnClickListener {
            this.numOfClasses = 10
            nameOfClasses = Array(10) { "" }
            nameOfClasses[0] = "Tomato Bacterial Spot"
            nameOfClasses[1] = "Tomato Early Blight"
            nameOfClasses[2] = "Tomato Late Blight"
            nameOfClasses[3] = "Tomato Leaf Mold"
            nameOfClasses[4] = "Tomato Septoria Leaf Spot"
            nameOfClasses[5] = "Tomato Spider mites || Two spotted spider mite"
            nameOfClasses[6] = "Tomato Target Spot"
            nameOfClasses[7] = "Tomato Yellow Leaf Curl Virus"
            nameOfClasses[8] = "Tomato Mosaic Virus"
            nameOfClasses[9] = "Tomato Healthy"

            loadModel("tomatoes_model.tflite")
            launchCamera()
        }

        return binding.root
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            interpreter.close()
        } catch (e: Exception) {
            Log.e(TAG, "Error closing interpreter: ${e.message}", e)
        }
    }

    private fun launchCamera() {
        when {
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED -> {
                // Permission is already granted; launch the camera
                takePicturePreviewLauncher.launch(null)
            }

            else -> {
                // Request the permission
                requestPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }
    }

    private fun getPredictedClass(output: Array<FloatArray>): Int {
        // If plant's labels are binary
        if (output[0].size == 1) {
            return if (output[0][0] > 0.5) {
                1
            } else
                0
        }

        // If plant's labels are multilabel
        var maxIdx = 0
        var maxVal = output[0][0]
        for (i in 1 until output[0].size) {
            if (output[0][i] > maxVal) {
                maxVal = output[0][i]
                maxIdx = i
            }
        }
        return maxIdx
    }

    private fun loadModel(modelFileName: String) {
        currentModelFileName = modelFileName
        // Load the model file
        val modelBuffer = loadModelFile(requireContext().assets, modelFileName)
        interpreter = Interpreter(modelBuffer)
        isModelLoaded = true
    }

    private fun loadModelFile(assetManager: AssetManager, filename: String): MappedByteBuffer {
        val fileDescriptor = assetManager.openFd(filename)
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        val startOffset = fileDescriptor.startOffset
        val declaredLength = fileDescriptor.declaredLength
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
    }

    private fun processImageWithModel(bitmap: Bitmap) {
        try {
            // Convert bitmap to a ByteBuffer with the required shape (1, 200, 200, 3)
            val resizedBitmap = Bitmap.createScaledBitmap(bitmap, 200, 200, true)
            val byteBuffer = convertBitmapToByteBuffer(resizedBitmap)
            Log.d(TAG, "Bitmap converted to ByteBuffer")

            // Run inference
            val output = Array(1) { FloatArray(this.numOfClasses) }
            interpreter.run(byteBuffer, output)
            Log.d(TAG, "Model inference completed")

            // Process the output to get the predicted class
            val predictedClass = nameOfClasses[getPredictedClass(output)]
            Log.d(TAG, "Predicted class: $predictedClass")

        } catch (e: Exception) {
            Log.e(TAG, "Error processing image with model: ${e.message}", e)
            Toast.makeText(requireContext(), "Error processing image with model", Toast.LENGTH_LONG)
                .show()
        }
    }

    private fun convertBitmapToByteBuffer(bitmap: Bitmap): ByteBuffer {
        val byteBuffer = ByteBuffer.allocateDirect(1 * 200 * 200 * 3 * 4) // 4 bytes per float
        byteBuffer.order(ByteOrder.nativeOrder())
        val intValues = IntArray(200 * 200)
        bitmap.getPixels(intValues, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)
        var pixel = 0
        for (i in 0 until 200) {
            for (j in 0 until 200) {
                val value = intValues[pixel++]
                byteBuffer.putFloat(((value shr 16) and 0xFF) / 1.0f)
                byteBuffer.putFloat(((value shr 8) and 0xFF) / 1.0f)
                byteBuffer.putFloat((value and 0xFF) / 1.0f)
            }
        }
        return byteBuffer
    }
}