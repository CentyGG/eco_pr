package com.example.eco_pr
import RecordController
import android.Manifest
import android.os.Bundle
import android.util.Log
import android.view.animation.OvershootInterpolator
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.example.eco_pr.databinding.ActivityMainBinding
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var recordController: RecordController
    private lateinit var decibelsArray:ArrayList<Double>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Инициализация RecordController
        recordController = RecordController(this)

        // Запрос разрешений
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.RECORD_AUDIO),
            777
        )

        startRecordingNoise()
        // Начать запись шума
    }

    override fun onDestroy() {
        super.onDestroy()
        // Остановить запись шума при закрытии активности
        stopRecordingNoise()
    }

    private fun startRecordingNoise() {
        recordController.start()
        var backgroundTask = GlobalScope.launch {
            while(true){
                delay(100)
                addDecibels()
            }
        }
        backgroundTask.start()

    }

    private fun addDecibels(){
        val decibelsArray: ArrayList<Double> = arrayListOf()
        val volume = recordController.getVolume().toDouble()
        val decibels = Math.sqrt(volume) * 1.95
        decibelsArray.add(decibels)
        Log.i("TAG", decibels.toString())
    }

    private fun stopRecordingNoise() {
        recordController.stop()
        Log.i("TAG", decibelsArray.average().toString())
    }

    companion object {
        private const val MAX_RECORD_AMPLITUDE = 32768.0
        private const val VOLUME_UPDATE_DURATION = 100L
        private val interpolator = OvershootInterpolator()
    }
}
