package com.example.eco_pr

import android.graphics.Color
import android.location.Address
import android.location.Geocoder
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.eco_pr.databinding.FragmentAirMapBinding
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolygonOptions
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException
import java.util.Locale

class AirMapFragment : Fragment(), OnMapReadyCallback {

    private lateinit var binding: FragmentAirMapBinding

    private lateinit var mMap: GoogleMap
    private val apiKey = "399161e1dbb1ab97bd00240c864bf5cbac035c2d"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAirMapBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val mapFragment = childFragmentManager.findFragmentById(R.id.airMapFragment) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        val startLat = arguments?.getDouble(ARG_LATITUDE) ?: 52.28
        val startLon = arguments?.getDouble(ARG_LONGITUDE) ?: 104.3

        Log.v("MAPS_API", "$startLat - $startLon")

        val latLng = LatLng(startLat, startLon)
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng))

        val cameraPosition = CameraPosition.Builder()
            .target(latLng)
            .zoom(15f)
            .tilt(23f)
            .bearing(10f)
            .build()

        mMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))

        mMap.setOnMapClickListener {
            val geocoder = Geocoder(requireContext(), Locale.getDefault())
            var address: String = ""
            val addresses: List<Address>? = geocoder.getFromLocation(it.latitude, it.longitude, 1)

            if (addresses != null) {
                for (adr in addresses) {
                    address += adr.getAddressLine(addresses.indexOf(adr))
                }
            }

            addAirQualityMarker(it, address)
        }
    }

    private fun addAirQualityMarker(location: LatLng, address: String) {
        val client = OkHttpClient()
        val url = "https://api.waqi.info/feed/geo:${location.latitude};${location.longitude}/?token=$apiKey"

        Log.v("API_TEST", "${location.latitude} : ${location.longitude}")

        val vertices = calculateHexagonVertices(location.latitude, location.longitude, 0.05)
        drawHexagon(vertices, Color.argb(100, 255, 0, 0))

        val request = Request.Builder()
            .url(url)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string()
                responseBody?.let {
                    val json = JSONObject(it)
                    val status = json.getString("status")
                    if (status == "ok") {
                        val data = json.getJSONObject("data")
                        val aqi = data.getInt("aqi")

                        requireActivity().runOnUiThread {
                            Log.v("API_TEST", aqi.toString())
                            mMap.addMarker(
                                MarkerOptions()
                                    .position(location)
                                    .title("Air Quality in ${address}: $aqi")
                            )
                        }
                    } else {
                        requireActivity().runOnUiThread {
                            Log.v("API_TEST", "error")
                        }
                    }
                }
            }

            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
                requireActivity().runOnUiThread {
                    Log.v("API_TEST", "failure")
                }
            }
        })
    }

    private fun calculateHexagonVertices(centerLat: Double, centerLng: Double, radius: Double): Array<DoubleArray> {
        val vertices = Array(6) { DoubleArray(2) }
        var angleDeg: Double
        var x: Double
        var y: Double

        for (i in 0 until 6) {
            angleDeg = 60.0 * i
            val angleRad = Math.toRadians(angleDeg)
            x = centerLng + radius * Math.cos(angleRad)
            y = centerLat + radius * Math.sin(angleRad)
            vertices[i][0] = y
            vertices[i][1] = x
        }

        return vertices
    }

    private fun drawHexagon(vertices: Array<DoubleArray>, color: Int) {
        val polygonOptions = PolygonOptions()
            .strokeColor(Color.RED)
            .strokeWidth(2f)
            .fillColor(color)

        for (i in 0 until 6) {
            polygonOptions.add(LatLng(vertices[i][0], vertices[i][1]))
        }

        mMap.addPolygon(polygonOptions)
    }

    companion object {

        const val ARG_LATITUDE = "ARG_LATITUDE"
        const val ARG_LONGITUDE = "ARG_LONGITUDE"
        fun newInstance(latitude: Double, longtitude: Double): AirMapFragment {
            val args = Bundle()
            args.putDouble(ARG_LATITUDE, latitude)
            args.putDouble(ARG_LONGITUDE, longtitude)
            val fragment = AirMapFragment()
            fragment.arguments = args
            return fragment
        }
    }
}