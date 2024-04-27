package com.example.eco_pr

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.eco_pr.AirMapFragment.Companion.newInstance
import com.example.eco_pr.databinding.LayerSheetDialogBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.protobuf.DescriptorProtos.SourceCodeInfo.Location
import java.lang.reflect.Array.newInstance

class MapChoiceSheet : BottomSheetDialogFragment() {
    private lateinit var binding: LayerSheetDialogBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        var latitude = arguments?.getDouble(ARG_LATITUDE)
        var longtitude = arguments?.getDouble(ARG_LONGITUDE)
        binding = LayerSheetDialogBinding.inflate(inflater, container, false)

        binding.airB.setOnClickListener {
            val airMapFragment = AirMapFragment.newInstance(latitude!!,longtitude!!)
            fragmentManager?.beginTransaction()
                ?.replace(R.id.nav_host_fragment, airMapFragment)
                ?.commit()
            dismiss()
        }
        binding.soundB.setOnClickListener {
            val noiseMapFragment = NoiseMapFragment.newInstance(latitude!!,longtitude!!)
            fragmentManager?.beginTransaction()
                ?.replace(R.id.nav_host_fragment, noiseMapFragment)
                ?.commit()
            dismiss()
        }

        return binding.root
    }
    companion object {

        const val ARG_LATITUDE = "ARG_LATITUDE"
        const val ARG_LONGITUDE = "ARG_LONGITUDE"
        fun newInstance(latitude: Double, longtitude: Double): MapChoiceSheet {
            val args = Bundle()
            args.putDouble(ARG_LATITUDE, latitude)
            args.putDouble(ARG_LONGITUDE, longtitude)
            val fragment = MapChoiceSheet()
            fragment.arguments = args
            return fragment
        }
    }
}