package com.example.eco_pr

import com.google.android.gms.maps.model.LatLng

class MoscowSquaresCenters {
    val centers = arrayListOf<LatLng>()

    init {
        var i = 37.352366509
        var j = 55.575162222

        while (i < 37.850871148) {
            while (j < 55.903656382) {
                val center = LatLng(i, j)
                centers.add(center)
                j += 0.01
            }
            i += 0.01
        }
    }
}