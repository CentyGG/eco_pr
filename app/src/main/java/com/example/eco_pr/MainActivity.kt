package com.example.eco_pr
import RecordController
import android.Manifest
import android.os.Bundle
import android.util.Log
import android.view.animation.OvershootInterpolator
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.example.eco_pr.databinding.ActivityMainBinding
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.Objects


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var recordController: RecordController
    private val decibelsArray: ArrayList<Double> = arrayListOf()
    private var switch:Boolean = true

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

    override fun onStop() {
        super.onStop()
        stopRecordingNoise()
    }


    private fun startRecordingNoise() {
        recordController.start()
        var backgroundTask = GlobalScope.launch {
            while(switch){
                delay(500)
                addDecibels()
            }
        }
        backgroundTask.start()

    }

    private fun addDecibels(){
        val volume = recordController.getVolume().toDouble()
        var decibels = Math.sqrt(volume) * 1.95
        if(decibels > 160){
            decibels = 160.0
        }
        decibelsArray.add(decibels)
        Log.i("TAG", decibels.toString())
    }

    private fun stopRecordingNoise() {
        switch = false
        recordController.stop()
        Log.i("TAG", "_________")
        Log.i("TAG", decibelsArray.average().toString())
        val db = FirebaseFirestore.getInstance()
        val data = hashMapOf(
            "address" to "your_address_value",
            "sound" to decibelsArray.average().toString()
        )
        db.collection("sound")
            .document("27.04.2024")
            .set(data)
            .addOnSuccessListener {
                // DocumentSnapshot added with ID: documentReference.id
            }
            .addOnFailureListener { e ->
                // Log the error or handle the failure
            }
    }

    companion object {
        private const val MAX_RECORD_AMPLITUDE = 32768.0
        private const val VOLUME_UPDATE_DURATION = 100L
        private val interpolator = OvershootInterpolator()
    }
}
