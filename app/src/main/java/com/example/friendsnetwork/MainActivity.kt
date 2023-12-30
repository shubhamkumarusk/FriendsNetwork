package com.example.friendsnetwork

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.window.OnBackInvokedDispatcher
import androidx.navigation.NavController
import androidx.navigation.NavHost
import com.example.friendsnetwork.fragements.HomePage

class MainActivity : AppCompatActivity() {
    private lateinit var navController: NavController
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val navHost = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHost
        navController = navHost.navController

    }




}