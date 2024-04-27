package com.example.eco_pr

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.eco_pr.databinding.FragmentHomeBinding
import com.google.firebase.firestore.FirebaseFirestore
import com.uber.h3core.H3Core
import java.text.SimpleDateFormat
import java.util.*

private lateinit var binding: FragmentHomeBinding
class HomeFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentHomeBinding.inflate(inflater,container,false)
        val view = binding.root
        val currentDate = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(Date())
        val db = FirebaseFirestore.getInstance()
        val data = hashMapOf(
            "address" to "Da",
            "sound" to "Ga"
        )


        db.collection("sound")
            .document(currentDate)
            .set(data)
            .addOnSuccessListener {
                // DocumentSnapshot added with ID: documentReference.id
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Failed to write document", e)
            }

        return view
    }


}