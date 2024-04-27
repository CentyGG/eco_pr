import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.eco_pr.databinding.ActivityMainBinding
import android.os.CountDownTimer
import android.util.Log
import android.view.animation.OvershootInterpolator
import androidx.core.app.ActivityCompat

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var recordController: RecordController
    private var countDownTimer: CountDownTimer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Инициализация RecordController
        recordController = RecordController(this)

        // Запрос разрешений
        ActivityCompat.requestPermissions(
            this,
            arrayOf(android.Manifest.permission.RECORD_AUDIO),
            777
        )

        // Начать запись шума
        startRecordingNoise()
    }

    override fun onDestroy() {
        super.onDestroy()
        // Остановить запись шума при закрытии активности
        stopRecordingNoise()
    }

    private fun startRecordingNoise() {
        recordController.start()
        val decibelsArray: ArrayList<Double> = arrayListOf()
        countDownTimer = object : CountDownTimer(10_000, VOLUME_UPDATE_DURATION) {
            override fun onTick(p0: Long) {
                val volume = recordController.getVolume()
                val decibels = 20 + volume / 235
                decibelsArray.add(decibels.toDouble())
            }

            override fun onFinish() {
                Log.i("TAG", decibelsArray.average().toString())
            }
        }.apply {
            start()
        }
    }

    private fun stopRecordingNoise() {
        recordController.stop()
        countDownTimer?.cancel()
        countDownTimer = null
    }

    companion object {
        private const val MAX_RECORD_AMPLITUDE = 32768.0
        private const val VOLUME_UPDATE_DURATION = 100L
        private val interpolator = OvershootInterpolator()
    }
}
