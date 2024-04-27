package com.example.eco_pr

import android.content.pm.PackageManager
import android.location.Criteria
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import com.example.eco_pr.R
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.core.view.WindowCompat
import android.Manifest
import android.view.Gravity
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import com.example.eco_pr.databinding.ActivityMainBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.bottomsheet.BottomSheetDialog

class MainActivity : AppCompatActivity() {
    private lateinit var bottomNav: BottomNavigationView
    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    private lateinit var navHostFragment: NavHostFragment

    private val LOCATION_PERMISSION_CODE = 2
    private var granted: Boolean = false
    override fun onCreate(savedInstanceState: Bundle?) {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

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

        // Получение сервиса LocationManager
        val locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
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
            val location = locationManager.getLastKnownLocation(prv) // Получение последнего местоположения
            if (location != null) {
                Log.v("MAPS_API", "Последнее местоположение: ${location.latitude} - ${location.longitude}")
                val airMapFragment = AirMapFragment.newInstance(location.latitude, location.longitude)
                supportFragmentManager
                    .beginTransaction()
                    .add(R.id.nav_host_fragment, airMapFragment)
                    .commit()
            }
        }
    }
        navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.findNavController()

        binding.searchB.setOnClickListener(object : View.OnClickListener{
            override fun onClick(p0: View?) {
                var dialog = BottomSheetDialog(this@MainActivity)
                dialog.setContentView(R.layout.search_sheet_dialog)
                dialog.window?.setGravity(Gravity.BOTTOM)
                dialog.show()
            }
        })
        binding.layerB.setOnClickListener(object : View.OnClickListener{
            override fun onClick(p0: View?) {
                var dialog = BottomSheetDialog(this@MainActivity)
                dialog.setContentView(R.layout.layer_sheet_dialog)
                dialog.window?.setGravity(Gravity.BOTTOM)
                dialog.show()
            }
        })

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

    override fun onLocationChanged(location: Location) {
        Log.v("MAPS_API", "Локация изменилась ${location.latitude} - ${location.longitude}")
    }
}
