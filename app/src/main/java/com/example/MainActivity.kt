package com.example

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.Network
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.auth.authStateFlow
import com.example.data.SovereignNodeManager
import com.example.notification.FcmNotificationManager
import com.example.security.configureAppCheck
import com.example.ui.screens.AuthScreen
import com.example.ui.screens.MainScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.MainViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import kotlinx.coroutines.flow.MutableStateFlow

class MainActivity : ComponentActivity() {

  private val latestIntentFlow = MutableStateFlow<Intent?>(null)
  private var connectivityManager: ConnectivityManager? = null
  private val networkCallback = object : ConnectivityManager.NetworkCallback() {
    override fun onAvailable(network: Network) {
      SovereignNodeManager.updateNetworkConnectivity(true)
    }

    override fun onLost(network: Network) {
      SovereignNodeManager.updateNetworkConnectivity(false)
    }
  }

  private val requestPermissionLauncher = registerForActivityResult(
    ActivityResultContracts.RequestMultiplePermissions()
  ) { results ->
    results.forEach { (permission, isGranted) ->
      android.util.Log.d("MainActivity", "Permission $permission isGranted: $isGranted")
    }
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    // Must configure FirebaseApp & App Check before any Firebase SDK or ViewModel calls
    configureAppCheck(this, intent)
    enableEdgeToEdge()

    latestIntentFlow.value = intent
    requestNotificationPermissionIfAvailable()
    FcmNotificationManager.init(this)

    connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
    runCatching {
      connectivityManager?.registerDefaultNetworkCallback(networkCallback)
    }

    setContent {
      val authFlow = remember { Firebase.auth.authStateFlow() }
      val currentUser by authFlow.collectAsStateWithLifecycle(initialValue = Firebase.auth.currentUser)
      val activeIntent by latestIntentFlow.collectAsStateWithLifecycle()

      val uid = currentUser?.uid
      if (uid.isNullOrBlank()) {
        MyApplicationTheme(skinCode = "gaudi", isDarkTheme = true) {
          AuthScreen(onAuthSuccess = {
            // Firebase.auth.authStateFlow emits the signed-in FirebaseUser automatically.
          })
        }
      } else {
        key(uid) {
          val viewModel: MainViewModel = viewModel(
            key = "nailix_vm_$uid",
            factory = MainViewModel.provideFactory(application, uid)
          )
          LaunchedEffect(activeIntent, uid) {
            handleNotificationIntent(activeIntent, viewModel)
          }
          val skinCode by viewModel.selectedSkin.collectAsStateWithLifecycle()
          val isDarkTheme by viewModel.isDarkTheme.collectAsStateWithLifecycle()
          MyApplicationTheme(skinCode = skinCode, isDarkTheme = isDarkTheme) {
            MainScreen(viewModel = viewModel)
          }
        }
      }
    }
  }

  override fun onNewIntent(intent: Intent) {
    super.onNewIntent(intent)
    setIntent(intent)
    latestIntentFlow.value = intent
  }

  override fun onDestroy() {
    runCatching {
      connectivityManager?.unregisterNetworkCallback(networkCallback)
    }
    super.onDestroy()
  }

  private fun handleNotificationIntent(intent: Intent?, viewModel: MainViewModel) {
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
