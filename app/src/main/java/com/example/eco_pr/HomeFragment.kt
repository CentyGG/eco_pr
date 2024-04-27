package com.example.eco_pr

import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.OvershootInterpolator
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.example.eco_pr.databinding.FragmentHomeBinding
import kotlin.math.min

class HomeFragment : Fragment() {

    private lateinit var binding: FragmentHomeBinding
    private lateinit var audioButton: View
    private lateinit var recordController: RecordController // Перемещаем инициализацию в onCreateView
    private var countDownTimer: CountDownTimer? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        val view = binding.root
        audioButton = binding.startBtn
        audioButton.setOnClickListener { onButtonClicked() }
        recordController = RecordController(requireActivity()) // Инициализируем recordController


        ActivityCompat.requestPermissions(
            requireActivity(), // Используйте requireActivity() для доступа к Activity из фрагмента
            arrayOf(android.Manifest.permission.RECORD_AUDIO),
            777
        )

        return view
    }

    private fun onButtonClicked() {
        if (recordController.isAudioRecording()) {
            recordController.stop()
            countDownTimer?.cancel()
            countDownTimer = null
        } else {
            recordController.start()
            countDownTimer = object : CountDownTimer(60_000, VOLUME_UPDATE_DURATION) {
                override fun onTick(p0: Long) {
                    val volume = recordController.getVolume()
                    handleVolume(volume)
                }

                override fun onFinish() {
                }
            }.apply {
                start()
            }
        }
    }

    private fun handleVolume(volume: Int) {
        val scale = min(8.0, volume / MAX_RECORD_AMPLITUDE + 1.0).toFloat()

        audioButton.animate()
            .scaleX(scale)
            .scaleY(scale)
            .setInterpolator(interpolator)
            .duration = VOLUME_UPDATE_DURATION
    }

    companion object {
        private const val MAX_RECORD_AMPLITUDE = 32768.0
        private const val VOLUME_UPDATE_DURATION = 100L
        private val interpolator = OvershootInterpolator()
    }
}
