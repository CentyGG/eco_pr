package com.example.eco_pr

import android.content.pm.PackageManager
import android.location.Criteria
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import com.example.eco_pr.R
import RecordController
import android.os.Bundle
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.core.view.WindowCompat
import android.Manifest
import android.view.Gravity
import android.view.View
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import android.util.Log
import android.view.animation.OvershootInterpolator
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.example.eco_pr.databinding.ActivityMainBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.security.Timestamp
import java.text.ChoiceFormat.nextDouble
import java.time.Instant
import java.time.Instant.now
import java.time.LocalDate.now
import java.time.LocalDateTime
import java.time.LocalDateTime.now
import java.time.format.DateTimeFormatter
import java.util.*


class MainActivity : AppCompatActivity() , LocationListener{
    private lateinit var bottomNav: BottomNavigationView
    private lateinit var binding: ActivityMainBinding
    private lateinit var recordController: RecordController
    private val decibelsArray: ArrayList<Double> = arrayListOf()
    private var switch:Boolean = true
    private var location: Location? = null
    private val LOCATION_PERMISSION_CODE = 2
    private var granted: Boolean = false
    private lateinit var airMapFragment: AirMapFragment
    override fun onCreate(savedInstanceState: Bundle?) {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)


        // Получение сервиса LocationManager
        val locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
        requestLocationUpdates()
        // Инициализация RecordController
        recordController = RecordController(this)

        // Запрос разрешений
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.RECORD_AUDIO),
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

        // Запрос обновлений местоположения с помощью GPS провайдера
        locationManager.requestLocationUpdates(
            LocationManager.GPS_PROVIDER,
            10000,
            20f,
            this
        )
        // Получение последнего местоположения
        val prv = locationManager.getBestProvider(Criteria(), true)
        if (prv != null) {
            location = locationManager.getLastKnownLocation(prv)
            if (location != null) {
                Log.v(
                    "MAPS_API",
                    "Последнее местоположение: ${location?.latitude} - ${location?.longitude}"
                )
                airMapFragment =
                    AirMapFragment.newInstance(location!!.latitude, location!!.longitude)
                supportFragmentManager
                    .beginTransaction()
                    .add(R.id.nav_host_fragment, airMapFragment)
                    .commit()
            }
        }

        //navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        //navController = navHostFragment.findNavController()
        //Макс лох

        binding.layerB.setOnClickListener(object : View.OnClickListener {
            override fun onClick(p0: View?) {
                binding.layerB.setOnClickListener(object : View.OnClickListener {
                    override fun onClick(p0: View?) {
                        val dialog = MapChoiceSheet.newInstance(location?.latitude ?:55.6697114 ,
                            location?.longitude ?:37.4810593
                        )
                        dialog.show(supportFragmentManager, "MapChoiceSheet")
                    }

            })
        }})
        binding.updateB.setOnClickListener {
            if (::airMapFragment.isInitialized) {
                (airMapFragment as UpdateMapListener).updateMap()
            } else {
                airMapFragment = AirMapFragment.newInstance(55.753995, 37.614069)
                supportFragmentManager
                    .beginTransaction()
                    .add(R.id.nav_host_fragment, airMapFragment)
                    .commit()
            }
        }
    }

    // Проверка и запрос разрешения на использование местоположения
    fun checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Перед запросом разрешения, рекомендуется объяснить пользователю, зачем ваше приложение нуждается в этом разрешении
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_CODE
            )
        } else {
            // Разрешение уже предоставлено, можно выполнять операции с местоположением
            // Например, запрос обновлений местоположения
        }
    }

    // Метод обработки результата запроса разрешений
    // Метод обработки результата запроса разрешений
    private fun requestLocationUpdates() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_CODE
            )
        } else {
            startLocationUpdates()
        }
    }

    private fun startLocationUpdates() {
        try {
            val locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
            locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                10000,
                20f,
                this
            )
        } catch (e: SecurityException) {
            Log.e("LocationUpdate", "Unable to request location updates: ${e.message}")
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            LOCATION_PERMISSION_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Разрешение предоставлено, можно запросить обновления местоположения
                    startLocationUpdates()
                } else {
                    // Разрешение не предоставлено, сообщите об этом пользователю
                    Toast.makeText(this, "Доступ к местоположению запрещен", Toast.LENGTH_SHORT).show()
                }
            }
        }
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
        // Начать запись шума
        val currentDateTime = LocalDateTime.now()
        val formattedDateTime = currentDateTime.format(DateTimeFormatter.ofPattern("dd.MM.yyyy.HH.mm.ss"))

        // Получить экземпляр Firebase Firestore
        val db = FirebaseFirestore.getInstance()

        try {
            val locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
            val prv = locationManager.getBestProvider(Criteria(), true)

            var latitude = 55.6697114
            var longitude = 37.4810593

            if (prv != null) {
                val location = locationManager.getLastKnownLocation(prv)
                if (location != null) {
                    latitude = location.latitude
                    longitude = location.longitude
                }
                val data = hashMapOf(
                    "id" to formattedDateTime,
                    "latitude" to latitude,
                    "longitude" to longitude,
                    "sound" to kotlin.random.Random.nextDouble(74.0, 83.0)
                )
                db.collection("sound")
                    .add(data)
                    .addOnSuccessListener { Log.i("TAG", "Success") }
                    .addOnFailureListener {  Log.i("TAG", "Error") }
            } else {
                Log.e("TAG", "Provider is null")
            }
        } catch (e: SecurityException) {
            Log.e("TAG", "Security exception occurred: ${e.message}")
        } catch (e: Exception) {
            Log.e("TAG", "Error occurred: ${e.message}")
        }
    }

    override fun onLocationChanged(location: Location) {
        Log.v("MAPS_API", "Локация изменилась ${location.latitude} - ${location.longitude}")
    }

    companion object {
        private const val MAX_RECORD_AMPLITUDE = 32768.0
        private const val VOLUME_UPDATE_DURATION = 100L
        private val interpolator = OvershootInterpolator()
    }
}
