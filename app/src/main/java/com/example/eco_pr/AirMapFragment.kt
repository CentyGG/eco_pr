package com.example.eco_pr

import BottomSheetFragmentAir
import android.graphics.Color
import android.location.Address
import android.location.Geocoder
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.example.eco_pr.databinding.FragmentAirMapBinding
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.Polygon
import com.google.android.gms.maps.model.PolygonOptions
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException
import java.util.Locale
import kotlin.math.*

class AirMapFragment : Fragment(), OnMapReadyCallback, UpdateMapListener {

    private lateinit var binding: FragmentAirMapBinding

    private lateinit var mMap: GoogleMap
    private val apiKey = "399161e1dbb1ab97bd00240c864bf5cbac035c2d"

    private val polygonList: MutableList<Polygon> = mutableListOf()
    private val locationsAirQualityMap = HashMap<LatLng, JSONObject>()

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

        val latLng = LatLng(startLat, startLon)
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng))

        val cameraPosition = CameraPosition.Builder()
            .target(latLng)
            .zoom(15f)
            .build()

        mMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))

        mMap.setOnPolygonClickListener { polygon ->
            val center = polygon.tag as LatLng
            val data = locationsAirQualityMap[center]
            val aqi = data?.getInt("aqi") ?: 0
            val city = data?.optString("city") ?: ""

            val dialog = BottomSheetFragmentAir.newInstance(aqi, city)
            dialog.show(childFragmentManager, "BottomSheetFragment")

        }
    }

    private fun getAirQualityFromLocation(location: LatLng, hexagonCenter: LatLng, hexagonRadius: Double) {
        val client = OkHttpClient()
        val url = "https://api.waqi.info/feed/geo:${location.latitude};${location.longitude}/?token=$apiKey"

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

                        requireActivity().runOnUiThread {
                            val vertices = calculateHexagonVertices(hexagonCenter.latitude, hexagonCenter.longitude, hexagonRadius)
                            drawHexagon(hexagonCenter, vertices, data)
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

        val RADIUS_RATIO = 1 / cos(Math.toRadians(centerLat)) // Adjust for latitude distortion

        for (i in 0 until 6) {
            val angleRad = Math.PI / 3.0 * i
            val x = centerLng + radius * RADIUS_RATIO * cos(angleRad)
            val y = centerLat + radius * sin(angleRad)
            vertices[i][0] = y
            vertices[i][1] = x
        }

        return vertices
    }

    private fun drawHexagon(hexagonCenter: LatLng, vertices: Array<DoubleArray>, data: JSONObject) {
        val aqi = data.getInt("aqi")
        val color = when(aqi) {
            in 1..10 -> ContextCompat.getColor(this.requireContext(), R.color.dark_green);
            in 11..20 -> ContextCompat.getColor(this.requireContext(), R.color.light_green);
            in 21..30 -> ContextCompat.getColor(this.requireContext(), R.color.yellow);
            in 31..40 -> ContextCompat.getColor(this.requireContext(), R.color.orange);
            else -> ContextCompat.getColor(this.requireContext(), R.color.red);
        }

        val polygonOptions = PolygonOptions()
            .strokeWidth(2f)
            .fillColor(color)
            .clickable(true)

        for (i in 0 until 6) {
            polygonOptions.add(LatLng(vertices[i][0], vertices[i][1]))
        }

        val polygon = mMap.addPolygon(polygonOptions)
        polygon.tag = hexagonCenter
        locationsAirQualityMap.put(hexagonCenter, data)
        polygonList.add(polygon)
    }

    private fun clearAllPolygons() {
        for (polygon in polygonList) {
            polygon.remove() // Remove the polygon from the map
        }
        polygonList.clear() // Clear the list of Polygon objects
        locationsAirQualityMap.clear()
    }

    private fun calculateHexagonCenters(map: GoogleMap, numHexagons: Int) {
        val mapProjection = map.projection
        val centerPoint = mapProjection.visibleRegion.latLngBounds.center

        val zoomLevel = map.cameraPosition.zoom.toDouble()

        // Determine the hexagon size based on map scale and number of hexagons
        val hexagonSize = calculateHexagonSize(zoomLevel)

        val hexagonRadius = calculateHexagonRadius(hexagonSize)

        // Calculate offset between hexagons
        val hexagonHorizontalSpacing = calculateHorizontalHexagonSpacing(hexagonSize)
        val hexagonVerticalSpacing = calculateVerticalHexagonSpacing(hexagonSize)

        // Calculate hexagon positions to cover the screen
        val hexagonCenters = ArrayList<LatLng>()
//        val numHexagonsPerRow = Math.ceil(Math.sqrt(numHexagons.toDouble())).toInt()

        val hexagonsNumPerRow = 3
        val hexagonsNumPerColumn = 18

        val overallOffsetY = (hexagonsNumPerColumn / 3.5 * hexagonSize)
        val overallOffsetX = (hexagonsNumPerRow * 1.2 * hexagonSize)

        for (i in 0 until hexagonsNumPerRow) {
            for (j in 0 until hexagonsNumPerColumn) {
                var offsetY = j * hexagonVerticalSpacing;
                var offsetX = (i % 2 * hexagonHorizontalSpacing / 2 + i / 2 * hexagonHorizontalSpacing);

                if (j % 2 == 0) {
                    offsetX += hexagonSize * 1.5
                }

                val hexagonCenter = LatLng(centerPoint.latitude + offsetY - overallOffsetY, centerPoint.longitude + offsetX - overallOffsetX)
                hexagonCenters.add(hexagonCenter)
            }
        }

        for (center in hexagonCenters) {
            getAirQualityFromLocation(center, center, hexagonRadius)
        }

    }

    private fun calculateHexagonSize(zoomLevel: Double): Double {
        // Calculate hexagon size based on the map zoom level
        // Adjust this calculation based on your requirements
        return 100 / Math.pow(2.0, zoomLevel)
    }

    private fun calculateHexagonRadius(hexagonSize: Double): Double {
        // Calculate the radius based on the hexagon size for proper coverage
        // Consider the spacing and arrangement of hexagons to cover the screen
        // Adjust this calculation based on your requirements
        return hexagonSize / Math.sqrt(3.0) // Assuming regular hexagons
    }

    private fun calculateHorizontalHexagonSpacing(hexagonSize: Double): Double {
        // Set the spacing between hexagons based on the hexagon size
        // Adjust this value as needed for proper coverage
        return hexagonSize * 6
    }

    private fun calculateVerticalHexagonSpacing(hexagonSize: Double): Double {
        // Set the spacing between hexagons based on the hexagon size
        // Adjust this value as needed for proper coverage
        return hexagonSize / 2
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

    override fun updateMap() {
        clearAllPolygons()
        calculateHexagonCenters(mMap, 20)
    }
}