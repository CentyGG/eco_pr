package com.example.eco_pr

import android.os.Bundle
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

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

    }
}