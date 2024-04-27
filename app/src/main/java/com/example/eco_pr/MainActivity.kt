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
import java.util.Objects


class MainActivity : AppCompatActivity() , LocationListener{
    private lateinit var bottomNav: BottomNavigationView
    private lateinit var binding: ActivityMainBinding
    private lateinit var recordController: RecordController
    private val decibelsArray: ArrayList<Double> = arrayListOf()
    private var switch:Boolean = true

    private val LOCATION_PERMISSION_CODE = 2
    private var granted: Boolean = false
    override fun onCreate(savedInstanceState: Bundle?) {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Получение сервиса LocationManager
        val locationManager = getSystemService(LOCATION_SERVICE) as LocationManager

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
            val location =
                locationManager.getLastKnownLocation(prv) // Получение последнего местоположения
            if (location != null) {
                Log.v(
                    "MAPS_API",
                    "Последнее местоположение: ${location.latitude} - ${location.longitude}"
                )
                val airMapFragment =
                    AirMapFragment.newInstance(location.latitude, location.longitude)
                supportFragmentManager
                    .beginTransaction()
                    .add(R.id.nav_host_fragment, airMapFragment)
                    .commit()
            }
        }

        //navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        //navController = navHostFragment.findNavController()

        binding.searchB.setOnClickListener(object : View.OnClickListener {
            override fun onClick(p0: View?) {
                var dialog = BottomSheetDialog(this@MainActivity)
                dialog.setContentView(R.layout.search_sheet_dialog)
                dialog.window?.setGravity(Gravity.BOTTOM)
                dialog.show()
            }
        })
        binding.layerB.setOnClickListener(object : View.OnClickListener {
            override fun onClick(p0: View?) {
                var dialog = BottomSheetDialog(this@MainActivity)
                dialog.setContentView(R.layout.layer_sheet_dialog)
                dialog.window?.setGravity(Gravity.BOTTOM)
                dialog.show()
            }
        })
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, // эта переменная возвращает значение от LPC в зависимости от того, выдано оно или нет
        permissions: Array<out String>, //перечень разрешений
        grantResults: IntArray //переменна, в которой содержится количество разрешений
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults) //обратная функция
        if(requestCode == LOCATION_PERMISSION_CODE){ //проверка на то, является ли RC тем же разрешением
            granted = true //если это так, то присваиваем true
            if(grantResults.size>0){ //проверка на то, что разрешения все таки выданы и они не отрицательные
                for( i in  grantResults){ // проверка на те разрешения, которые нас интересуют среди всех разрешений
                    if (i != PackageManager.PERMISSION_GRANTED){ //если нет тех разрешений, которые нам нужны
                        granted=false // просто оставляем false
                        Toast.makeText(this, "Access Denied", Toast.LENGTH_SHORT ).show()//сообщение о том, что разрешение недоступно
                    }
                }
            }else{
                granted=false //разрешение пришло, но мы его отклонили
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

        // Проверка значения шума перед отправкой в Firebase
        if (decibelsArray.isNotEmpty()) {
            val averageDecibels = decibelsArray.average()
            if (averageDecibels < YOUR_THRESHOLD_VALUE || averageDecibels > ANOTHER_THRESHOLD_VALUE) {
                Log.i("TAG", "Decibels value is out of range, not uploading to Firebase.")
                return // Не выгружаем данные в Firebase, если значение шума не в допустимом диапазоне
            }
        }

        // Продолжаем с записью шума в Firebase, если прошли проверку
        val db = FirebaseFirestore.getInstance()
        val data = hashMapOf(
            "address" to "your_address_value",
            "sound" to decibelsArray.average().toString()
        )
        db.collection("sound")
            .document("27.04.2024")
            .set(data)
            .addOnSuccessListener {
                // Данные успешно отправлены
            }
            .addOnFailureListener { e ->
                // Обработка ошибки
            }
    }

    override fun onLocationChanged(location: Location) {
        Log.v("MAPS_API", "Локация изменилась ${location.latitude} - ${location.longitude}")
    }

    companion object {
        private const val YOUR_THRESHOLD_VALUE = 20.0 // Установите необходимые диапазоны значений шума
        private const val ANOTHER_THRESHOLD_VALUE = 160.0
    }
}
