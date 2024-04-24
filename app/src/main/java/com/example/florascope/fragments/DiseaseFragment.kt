package com.example.florascope.fragments

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.findNavController
import com.example.florascope.R
import com.example.florascope.databinding.FragmentDiseaseBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.util.Locale

class DiseaseFragment : Fragment() {
    private lateinit var binding: FragmentDiseaseBinding
    private lateinit var databaseReference: DatabaseReference

    private lateinit var title: String
    private lateinit var symptoms: String
    private lateinit var prevention: String

    companion object {
        private const val TAG = "DiseaseFragment"
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentDiseaseBinding.inflate(inflater)

        // Initialize the database reference
        databaseReference = FirebaseDatabase.getInstance().reference

        // Assuming you have a method to fetch the data
        fetchData()

        return binding.root
    }

    private fun fetchData() {
        // Assuming you have the reference to the "diseases" node
        val diseasesReference = databaseReference.child("diseases")
            .child(arguments?.getString("modelName").toString())
            .child(arguments?.getString("diseaseIndex").toString())

        // Add a listener for fetching data
        diseasesReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // Check if the data exists
                if (dataSnapshot.exists()) {
                    when (getPersistedLanguage()) {
                        "en" -> {
                            title = dataSnapshot.child("title/en").getValue(String::class.java).toString()
                            symptoms = dataSnapshot.child("symptoms/en").getValue(String::class.java).toString()
                            prevention = dataSnapshot.child("prevention/en").getValue(String::class.java).toString()
                        }
                        "ru" -> {
                            title = dataSnapshot.child("title/ru").getValue(String::class.java).toString()
                            symptoms = dataSnapshot.child("symptoms/ru").getValue(String::class.java).toString()
                            prevention = dataSnapshot.child("prevention/ru").getValue(String::class.java).toString()
                        }
                    }

                    binding.diseaseTitle.text = title
                    binding.symptomsText.text = symptoms
                    binding.preventionText.text = prevention

                    binding.diseaseTitle.stopLoading()
                    binding.symptomsTitle.stopLoading()
                    binding.symptomsText.stopLoading()
                    binding.preventionTitle.stopLoading()
                    binding.preventionText.stopLoading()
                } else {
                    Toast.makeText(requireContext(), "No data found", Toast.LENGTH_LONG).show()
                    view?.findNavController()?.navigate(R.id.action_diseaseFragment_to_cameraFragment)
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Toast.makeText(requireContext(), "Failed to read value", Toast.LENGTH_LONG).show()
                view?.findNavController()?.navigate(R.id.action_diseaseFragment_to_cameraFragment)
            }
        })
    }

    private fun getPersistedLanguage(): String {
        val sharedPref = activity?.getSharedPreferences("AppSettingsPrefs", Context.MODE_PRIVATE)
        return sharedPref?.getString("AppLang", Locale.getDefault().language) ?: Locale.getDefault().language
    }
}