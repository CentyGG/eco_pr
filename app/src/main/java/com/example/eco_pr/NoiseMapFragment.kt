package com.example.eco_pr

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup


class NoiseMapFragment : Fragment() {


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {


        return inflater.inflate(R.layout.fragment_noise_map, container, false)
    }
    companion object {
        const val ARG_LATITUDE = "ARG_LATITUDE"
        const val ARG_LONGITUDE = "ARG_LONGITUDE"

        fun newInstance(latitude: Double, longitude: Double): NoiseMapFragment {
            val args = Bundle()
            args.putDouble(ARG_LATITUDE, latitude)
            args.putDouble(ARG_LONGITUDE, longitude)
            val fragment = NoiseMapFragment()
            fragment.arguments = args
            return fragment
        }
    }

}