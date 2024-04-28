import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.example.eco_pr.R
import com.example.eco_pr.databinding.BottomSheetFragmentAirBinding
import com.example.eco_pr.databinding.LayerSheetDialogBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import org.json.JSONObject

class BottomSheetFragmentAir : BottomSheetDialogFragment() {
    private var aqi: Int = 0
    private var city: String = ""
    private var address: String = ""
    private lateinit var binding: BottomSheetFragmentAirBinding
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = BottomSheetFragmentAirBinding.inflate(inflater, container, false)

        // Получаем данные из аргументов
        arguments?.let {
            val city = it.getString("city")
            val aqi = it.getInt("aqi")

            // Находим TextView для названия города и устанавливаем значение
            val cityLabel = binding.root.findViewById<TextView>(R.id.city_label)
            cityLabel.text = "Название города: $city"

            // Находим TextView для AQI и устанавливаем значение
            val aqiTv = binding.root.findViewById<TextView>(R.id.aqi_tv)
            aqiTv.text = aqi.toString()
        }
        return binding.root
    }
    companion object {
        const val JSON_ = "JSON"

        fun newInstance(aqi: Int, city: String): BottomSheetDialogFragment {
            val args = Bundle().apply {
                putInt("aqi", aqi)
                putString("city", city)
            }
            val fragment = BottomSheetFragmentAir()
            fragment.arguments = args
            return fragment
        }
    }
}