package com.example.eco_pr

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.text.isDigitsOnly
import com.example.eco_pr.databinding.FragmentNoiseMapBinding
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.Polygon
import com.google.firebase.firestore.FirebaseFirestore
import kotlin.math.cos
import kotlin.math.sin


class NoiseMapFragment : Fragment(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap

    private val polygonList: MutableList<Polygon> = mutableListOf()
    private val squaresCenters = MoscowSquaresCenters().centers;
    private val locationsNoiseQualityMap = HashMap<LatLng, ArrayList<Int>>()


    private lateinit var binding: FragmentNoiseMapBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentNoiseMapBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val noiseFragment = childFragmentManager.findFragmentById(R.id.noiseMapFragment) as SupportMapFragment
        noiseFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        val startLat = arguments?.getDouble(NoiseMapFragment.ARG_LATITUDE) ?: 52.28
        val startLon = arguments?.getDouble(NoiseMapFragment.ARG_LONGITUDE) ?: 104.3

        val latLng = LatLng(startLat, startLon)
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng))

        val cameraPosition = CameraPosition.Builder()
            .target(latLng)
            .zoom(15f)
            .build()

        mMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))

        mMap.setOnPolygonClickListener { polygon ->
            val center = polygon.tag as LatLng
            val data = locationsNoiseQualityMap[center]
        }

        getNoisesFromFirebase()

//        for (center in squaresCenters) {
//            val vertices = calculateSquareVertices(center.latitude, center.longitude, 0.005)
//            drawSquare(center, vertices)
//        }
    }

    private fun getNoisesFromFirebase() {
        val db = FirebaseFirestore.getInstance()

        db
            .collection("sound")
            .get()
            .addOnSuccessListener {
                for (document in it) {
                    val data = document.data
                    val latitude = data.get("latitude").toString().toDoubleOrNull() ?: 37.352366509
                    val longitude = data.get("longitude").toString().toDoubleOrNull() ?: 55.575162222

                    Log.v("FIREBASE", "${latitude} ${longitude}")
                    val markerPosition = LatLng(latitude, longitude);
                    mMap.addMarker(MarkerOptions().position(markerPosition).title(data.get("sound").toString()))
                }
            }
    }

//    private fun calculateSquareVertices(centerLat: Double, centerLng: Double, radius: Double): Array<DoubleArray> {
//        val vertices = Array(4) { DoubleArray(2) }
//
//        val RADIUS_RATIO = 1 / cos(Math.toRadians(centerLat)) // Adjust for latitude distortion
//
//        for (i in 0 until 4) {
//            val angleRad = Math.PI / 2.0 * i
//            val x = centerLng + radius * RADIUS_RATIO * cos(angleRad)
//            val y = centerLat + radius * sin(angleRad)
//            vertices[i][0] = y
//            vertices[i][1] = x
//        }
//
//        return vertices
//    }

//    private fun drawSquare(squareCenter: LatLng, vertices: Array<DoubleArray>) {
//        val color = when(aqi) {
//            in 1..10 -> ContextCompat.getColor(this.requireContext(), R.color.dark_green);
//            in 11..20 -> ContextCompat.getColor(this.requireContext(), R.color.light_green);
//            in 21..30 -> ContextCompat.getColor(this.requireContext(), R.color.yellow);
//            in 31..40 -> ContextCompat.getColor(this.requireContext(), R.color.orange);
//            else -> ContextCompat.getColor(this.requireContext(), R.color.red);
//        }
//
//        val polygonOptions = PolygonOptions()
//            .strokeWidth(2f)
//            .fillColor(color)
//            .clickable(true)
//
//        for (i in 0 until 4) {
//            polygonOptions.add(LatLng(vertices[i][0], vertices[i][1]))
//        }
//
//        val polygon = mMap.addPolygon(polygonOptions)
//        polygon.tag = squareCenter
//        locationsNoiseQualityMap.put(squareCenter, arrayListOf())
//        polygonList.add(polygon)
//    }


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