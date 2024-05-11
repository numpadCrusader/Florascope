package com.example.florascope.fragments

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.ColorSpace.Model
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
import androidx.navigation.findNavController
import com.example.florascope.R
import com.example.florascope.backend.AnalysisResponse
import com.example.florascope.backend.AnalyzeRequest
import com.example.florascope.backend.ModelApi
import com.example.florascope.databinding.FragmentCameraBinding
import com.example.florascope.rest.GoogleFormApi
import com.google.firebase.ktx.Firebase
import com.google.firebase.ml.modeldownloader.CustomModel
import com.google.firebase.ml.modeldownloader.CustomModelDownloadConditions
import com.google.firebase.ml.modeldownloader.FirebaseModelDownloader
import com.google.firebase.ml.modeldownloader.DownloadType
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import okhttp3.OkHttpClient
import org.json.JSONObject
import org.tensorflow.lite.Interpreter
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.concurrent.TimeUnit

class CameraFragment : Fragment() {
    private lateinit var binding: FragmentCameraBinding
    private lateinit var takePicturePreviewLauncher: ActivityResultLauncher<Void?>
    private lateinit var requestPermissionLauncher: ActivityResultLauncher<String>

    private lateinit var modelName: String
    private var numOfClasses: Int = 0
    private lateinit var nameOfClasses: Array<String>

    // Firebase
    private lateinit var storage: FirebaseStorage
    private lateinit var storageRef: StorageReference

    companion object {
        private const val TAG = "CameraFragment"
    }

    object RetrofitClient {
        private const val BASE_URL = "https://florascope1.el.r.appspot.com/"

        val instance: ModelApi by lazy {
            val okHttpClient = OkHttpClient.Builder()
                .connectTimeout(9000, TimeUnit.SECONDS) // Increase connect timeout
                .readTimeout(9000, TimeUnit.SECONDS)    // Increase read timeout
                .writeTimeout(9000, TimeUnit.SECONDS)   // Increase write timeout
                .build()

            val retrofit = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build()

            retrofit.create(ModelApi::class.java)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize takePicturePreviewLauncher
        takePicturePreviewLauncher =
            registerForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap ->
                // This is where you receive the result (bitmap)
                if (bitmap != null) {
                    Log.d(TAG, "Bitmap received from camera: width=${bitmap.width}, height=${bitmap.height}")

                    // Upload the bitmap to Firebase Storage
                    uploadImageToFirebase(bitmap)

                } else {
                    Log.e(TAG, "Bitmap received from camera is null")
                    Toast.makeText(requireContext(), "Error: Bitmap received from camera is null", Toast.LENGTH_LONG).show()
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
                    Toast.makeText(requireContext(), "Camera permission is required to take pictures", Toast.LENGTH_LONG).show()
                }
            }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCameraBinding.inflate(inflater)

        // Initialize Firebase
        storage = Firebase.storage
        storageRef = storage.reference

        binding.b1.setOnClickListener {
            numOfClasses = 4
            nameOfClasses = Array(4) { "" }
            nameOfClasses[0] = "Apple Healthy"
            nameOfClasses[1] = "Apple Black Rot"
            nameOfClasses[2] = "Apple Rust"
            nameOfClasses[3] = "Apple Scab"

            modelName = "apple"
            launchCamera()
        }

        binding.b2.setOnClickListener {
            numOfClasses = 4
            nameOfClasses = Array(4) { "" }
            nameOfClasses[0] = "Black Rot"
            nameOfClasses[1] = "Esca (Black Measles)"
            nameOfClasses[2] = "Leaf blight (Isariopsis Leaf Spot)"
            nameOfClasses[3] = "Healthy"

            modelName = "grape"
            launchCamera()
        }

        return binding.root
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

    private fun uploadImageToFirebase(originalBitmap: Bitmap) {
        val resizedBitmap = Bitmap.createScaledBitmap(originalBitmap, 600, 600, true)

        val baos = ByteArrayOutputStream()
        resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val data = baos.toByteArray()

        val imageRef = storageRef.child("images/image.jpg")

        val uploadTask = imageRef.putBytes(data)
        uploadTask.addOnSuccessListener {
            Toast.makeText(requireContext(), "Image uploaded successfully", Toast.LENGTH_SHORT).show()
            sendApiRequest()
        }.addOnFailureListener {
            Toast.makeText(requireContext(), "Failed to upload image", Toast.LENGTH_SHORT).show()
        }
    }

    private fun sendApiRequest() {
        val request = AnalyzeRequest("image.jpg", modelName + "_model")
        RetrofitClient.instance.analyzeImage(request)
            .enqueue(object : retrofit2.Callback<AnalysisResponse> {
                override fun onResponse(
                    call: retrofit2.Call<AnalysisResponse>,
                    response: retrofit2.Response<AnalysisResponse>
                ) {
                    if (response.isSuccessful) {
                        Log.d("APIResponse", "Success: ${response.body()}")

                        val diseaseName = nameOfClasses[findIndexOfLargest(response.body()!!.result)]

                        if(diseaseName.contains("Healthy")){
                            Toast.makeText(requireContext(), "Leaves seem to be healthy", Toast.LENGTH_SHORT).show()
                        }
                        else{
                            val bundle = Bundle().apply {
                                putString("modelName", modelName)
                                putString("diseaseName", diseaseName)
                            }
                            view?.findNavController()?.navigate(R.id.action_cameraFragment_to_diseaseFragment, bundle)
                        }
                    } else {
                        Toast.makeText(requireContext(), "Error processing image on server, try again", Toast.LENGTH_SHORT).show()
                        Log.d("APIResponse", "Error: HTTP ${response.code()} ${response.message()}")
                        Log.d("APIResponse", "Headers: ${response.headers()}")
                        Log.d("APIResponse", "Error Body: ${response.errorBody()?.string()}")
                    }
                }

                override fun onFailure(call: retrofit2.Call<AnalysisResponse>, t: Throwable) {
                    Toast.makeText(requireContext(), "Failure processing image, try again", Toast.LENGTH_SHORT).show()
                    Log.d("APIResponse", "Failure: ${t.message}")
                    Log.d("APIResponse", "Exception Type: ${t.javaClass.simpleName}")
                    Log.e("APIResponse", "Stack Trace: ", t)
                }
            })
    }

    fun findIndexOfLargest(resultString: String): Int {
        try {
            // Remove the double brackets and split by spaces
            val numbers = resultString
                .replace("[[", "")
                .replace("]]", "")
                .trim()
                .split("\\s+".toRegex())  // Using regex to handle variable whitespace
                .map { it.toDouble() }

            // Find the largest number and its index
            val maxNumber = numbers.maxOrNull() ?: throw IllegalArgumentException("List is empty")
            return numbers.indexOf(maxNumber)
        } catch (e: Exception) {
            println("Error parsing response: ${e.message}")
            throw e  // Re-throw the exception after logging
        }
    }
}
