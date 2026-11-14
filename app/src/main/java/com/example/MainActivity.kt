package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.screens.MainScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.MainViewModel

class MainActivity : ComponentActivity() {

  private val requestPermissionLauncher = registerForActivityResult(
    ActivityResultContracts.RequestMultiplePermissions()
  ) { results ->
    results.forEach { (permission, isGranted) ->
      android.util.Log.d("MainActivity", "Permission $permission isGranted: $isGranted")
    }
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()

    // Startup notifications check and FCM initialization
    requestNotificationPermissionIfAvailable()
    com.example.notification.FcmNotificationManager.init(this)

    // Connectivity monitoring for sovereign P2P & SimpleX queue resiliency
    val cm = getSystemService(android.content.Context.CONNECTIVITY_SERVICE) as? android.net.ConnectivityManager
    cm?.registerDefaultNetworkCallback(object : android.net.ConnectivityManager.NetworkCallback() {
      override fun onAvailable(network: android.net.Network) {
        com.example.data.SovereignNodeManager.updateNetworkConnectivity(true)
      }
      override fun onLost(network: android.net.Network) {
        com.example.data.SovereignNodeManager.updateNetworkConnectivity(false)
      }
    })

    val viewModel: MainViewModel
    try {
      viewModel = androidx.lifecycle.ViewModelProvider(this)[MainViewModel::class.java]
    } catch (t: Throwable) {
      android.util.Log.e("MainActivity", "CRITICAL: FAILED TO CREATE VIEWMODEL", t)
      var cause = t.cause
      while (cause != null) {
        android.util.Log.e("MainActivity", "CRITICAL_CAUSED_BY: ", cause)
        cause = cause.cause
      }
      throw t
    }

    handleNotificationIntent(intent, viewModel)

    setContent {
      val skinCode by viewModel.selectedSkin.collectAsState()
      val isDarkTheme by viewModel.isDarkTheme.collectAsState()
      MyApplicationTheme(skinCode = skinCode, isDarkTheme = isDarkTheme) {
        MainScreen(viewModel = viewModel)
      }
    }
  }

  override fun onNewIntent(intent: android.content.Intent) {
    super.onNewIntent(intent)
    setIntent(intent)
    try {
      val viewModel = androidx.lifecycle.ViewModelProvider(this)[MainViewModel::class.java]
      handleNotificationIntent(intent, viewModel)
    } catch (e: Exception) {
      android.util.Log.w("MainActivity", "Error handling onNewIntent: ${e.message}")
    }
  }

  private fun handleNotificationIntent(intent: android.content.Intent?, viewModel: MainViewModel) {
    val notifType = intent?.getStringExtra("notification_type") ?: return
    when (notifType) {
      "appointment_reminder" -> viewModel.navigateTo("booking")
      "promotion" -> viewModel.navigateTo("designs")
    }
  }

  private fun requestNotificationPermissionIfAvailable() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
      if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
        requestPermissionLauncher.launch(arrayOf(Manifest.permission.POST_NOTIFICATIONS))
      }
    }
  }
}
