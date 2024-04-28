import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.example.eco_pr.R
import com.example.eco_pr.databinding.BottomSheetFragmentAirBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import org.json.JSONObject

class BottomSheetFragmentAir : BottomSheetDialogFragment() {
    private var aqi: Int = 0
    private var city: String = ""
    private lateinit var binding: BottomSheetFragmentAirBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = BottomSheetFragmentAirBinding.inflate(inflater, container, false)

        // Получаем данные из аргументов
        arguments?.let {
            val cityJsonString = it.getString("city")
            val cityJson = JSONObject(cityJsonString)
            val cityName = cityJson.getString("name")
            val aqi = it.getInt("aqi")

            // Находим TextView для названия города и устанавливаем значение
            val cityLabel = binding.root.findViewById<TextView>(R.id.city_label)
            cityLabel.text = "Название города: $cityName"

            // Находим TextView для AQI и устанавливаем значение
            val aqiTv = binding.root.findViewById<TextView>(R.id.aqi_tv)
            aqiTv.text = aqi.toString()
        }
        return binding.root
    }

    companion object {
        fun newInstance(aqi: Int, cityJsonString: String): BottomSheetDialogFragment {
            val args = Bundle().apply {
                putInt("aqi", aqi)
                putString("city", cityJsonString)
            }
            val fragment = BottomSheetFragmentAir()
            fragment.arguments = args
            return fragment
        }
    }
}