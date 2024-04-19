package com.example.florascope.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.florascope.databinding.FragmentFeedbackBinding
import com.example.florascope.rest.GoogleFormApi
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class FeedbackFragment : Fragment() {
    private lateinit var binding: FragmentFeedbackBinding

    // Create a Retrofit instance for API communication
    private val retrofit = Retrofit.Builder()
        .baseUrl("https://docs.google.com/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    // Create an API interface using Retrofit
    private val api = retrofit.create(GoogleFormApi::class.java)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentFeedbackBinding.inflate(inflater)

        binding.sendButton.setOnClickListener {
            // Get user input from EditText fields
            val email = binding.editEmail.text.toString()
            val feedback = binding.editFeedback.text.toString()

            // Create a call to send form data
            val call = api.sendFormData(email, feedback)

            // Execute the API call asynchronously
            call.enqueue(object : Callback<Void> {
                override fun onResponse(call: Call<Void>, response: Response<Void>) {
                    try {
                        if (response.isSuccessful) {
                            Toast.makeText(context, "Successful", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "Unsuccessful", Toast.LENGTH_SHORT).show()
                        }
                    } catch (e: Exception) {
                        Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<Void>, t: Throwable) {
                    try {
                        Toast.makeText(context, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                    } catch (e: Exception) {
                        Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            })

            binding.editEmail.text?.clear()
            binding.editFeedback.text?.clear()
            binding.editEmailContainer.clearFocus()
            binding.editFeedbackContainer.clearFocus()
        }

        return binding.root
    }
}