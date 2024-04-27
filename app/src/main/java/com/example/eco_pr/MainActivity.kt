import RecordController
import android.Manifest
import android.content.Context.LOCATION_SERVICE
import android.content.pm.PackageManager
import android.location.Criteria
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import android.view.animation.OvershootInterpolator
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.eco_pr.R
import com.example.eco_pr.databinding.ActivityMainBinding
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var recordController: RecordController
    private val decibelsArray: ArrayList<Double> = arrayListOf()
    private var switch: Boolean = true
    private lateinit var locationManager: LocationManager
    private val LOCATION_PERMISSION_CODE = 2
    private var granted: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        locationManager = getSystemService(LOCATION_SERVICE) as LocationManager

        // Инициализация RecordController
        recordController = RecordController(this)

        // Запрос разрешений
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.RECORD_AUDIO, Manifest.permission.ACCESS_FINE_LOCATION),
            777
        )

        startRecordingNoise()
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // Запрос разрешения, если оно не было предоставлено ранее
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_CODE
            )
        } else {
            granted = true // Устанавливаем флаг в true, если разрешение уже было предоставлено
        }
    }

    override fun onStop() {
        super.onStop()
        stopRecordingNoise()
    }

    override fun onResume() {
        super.onResume()
        switch = true
        startRecordingNoise()
    }

    private fun startRecordingNoise() {
        recordController.start()
        val backgroundTask = GlobalScope.launch {
            while (switch) {
                delay(500)
                addDecibels()
            }
        }
        backgroundTask.start()
    }



    private fun addDecibels() {
        val volume = recordController.getVolume().toDouble()
        var decibels = Math.sqrt(volume) * 1.95
        if (decibels > 160) {
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
}