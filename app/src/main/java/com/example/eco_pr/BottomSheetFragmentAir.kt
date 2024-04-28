import android.os.Bundle
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import org.json.JSONObject

class BottomSheetFragmentAir : BottomSheetDialogFragment() {
    private var aqi: Int = 0
    private var idx: Int = 0
    private var city: String = ""
    private var dominentpol: String = ""
    private var iaqi: Iaqi = Iaqi()
    private var time: Time = Time()
    private var forecast: Forecast = Forecast()

    companion object {
        const val JSON_ = "JSON"
        fun newInstance(jsonObject: JSONObject?): BottomSheetDialogFragment {
            val args = Bundle()
            val bundle = Bundle()
            jsonObject?.let {
                var aqi = it.getInt("aqi")
                var idx = it.getInt("idx")
                var city = it.getJSONObject("city").getString("name")
                var dominentpol = it.getString("dominentpol")
                var iaqi = Iaqi(it.getJSONObject("iaqi"))
                var time = Time(it.getJSONObject("time"))
                var forecast = Forecast(it.getJSONObject("forecast"))
            }
            args.putAll(bundle)
            val fragment = BottomSheetDialogFragment()
            fragment.arguments = args
            return fragment
        }
    }

    data class Iaqi(val co: Co, val dew: Dew, val h: H, val no2: No2, val o3: O3, val p: P, val pm10: Pm10, val pm25: Pm25, val so2: So2, val t: T, val w: W, val wg: Wg)

    data class Co(val v: Double)
    data class Dew(val v: Double)
    data class H(val v: Int)
    data class No2(val v: Double)
    data class O3(val v: Double)
    data class P(val v: Int)
    data class Pm10(val v: Int)
    data class Pm25(val v: Int)
    data class So2(val v: Double)
    data class T(val v: Int)
    data class W(val v: Double)
    data class Wg(val v: Double)

    data class Time(val s: String, val tz: String, val v: Long, val iso: String)

    data class Forecast(val daily: Daily)

    data class Daily(val o3: List<O3>, val pm10: List<Pm10>, val pm25: List<Pm25>)

}