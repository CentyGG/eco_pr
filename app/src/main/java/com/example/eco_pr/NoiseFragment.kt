package com.example.eco_pr

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.eco_pr.databinding.FragmentNoiseBinding

private lateinit var binding: FragmentNoiseBinding
class NoiseFragment : Fragment() {


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentNoiseBinding.inflate(inflater,container,false)
        val view = binding.root





        return view
    }

}