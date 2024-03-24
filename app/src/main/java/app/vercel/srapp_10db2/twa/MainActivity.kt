package app.vercel.srapp_10db2.twa


import android.Manifest.permission.POST_NOTIFICATIONS
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import app.vercel.srapp_10db2.twa.model.SplashViewModel
import app.vercel.srapp_10db2.twa.ui.theme.SugnRakchurchTheme
import com.google.accompanist.web.*
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.messaging.FirebaseMessaging


private const val s = "Firebase"

class MainActivity : ComponentActivity() {

    private val TAG = "MainActivity"
    private val viewModel: SplashViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        installSplashScreen().apply {
            setKeepOnScreenCondition { viewModel.isLoading.value }
        }

        //백그라운드
       val extras = intent.extras
        if (extras != null) {
            viewModel.weblink = extras.getString("link").toString()
            Log.d(TAG, "back room body: ${viewModel.weblink}")
        }

        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.d(TAG, "Fetching FCM registration token failed", task.exception)
                return@OnCompleteListener
            }

            // Get new FCM registration token
            val token = task.result

            Log.d(TAG, "room token " + token)
        })


        setContent {
            SugnRakchurchTheme(darkTheme = false) {
                MyApp()
//            SugnRakchurchTheme {
//                // A surface container using the 'background' color from the theme
//                Surface(
//                    modifier = Modifier.fillMaxSize(),
//                    color = MaterialTheme.colors.background
//                ) {
//                    Greeting("Android")
//                }
            }
        }
    }
   //프그라운드
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)

        val extras = intent.extras
        if (extras != null) {
            viewModel.weblink =  extras.getString("link").toString()
            Log.d(TAG, "intent.extras link : ${viewModel.weblink }")
        }

        setContent {
            MyApp()
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        if (hasFocus) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
           //     window.setDecorFitsSystemWindows(false)
                window.insetsController?.hide(WindowInsets.Type.navigationBars())
                window.insetsController?.systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            } else {
                window.decorView.systemUiVisibility =
                    (View.SYSTEM_UI_FLAG_IMMERSIVE or
                            View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                            View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                            View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION)
            }
        }
    }


        @Composable
    fun MyApp() {
        val context = LocalContext.current
        val requestPermissionLauncher  = rememberLauncherForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            if (isGranted) {
                Log.d("ExampleScreen", "PERMISSION GRANTED")

            } else {
                Log.d("ExampleScreen", "PERMISSION DENIED")

            }

        }

        if (context.checkSelfPermission( POST_NOTIFICATIONS) ==
            PackageManager.PERMISSION_GRANTED
        ) {
            Log.d("ExampleScreen", "Code requires permission")

        } else {
            SideEffect {
                requestPermissionLauncher.launch(POST_NOTIFICATIONS)
            }
        }



        WebViewPage()
    }

    @Composable
    fun WebViewPage() {
        Log.d("Firebase", " WebViewPage()")
        Log.d("Firebase", " viewModel ${viewModel.weblink}")
        WebView(
            state = WebViewState(
                WebContent.Url(
                    url = viewModel.weblink,
                    additionalHttpHeaders = emptyMap()
                )
            ),
            client = AccompanistWebViewClient(),
            chromeClient = AccompanistWebChromeClient(),
            navigator = viewModel.webViewNavigator,
            captureBackPresses = false,
            modifier = Modifier.fillMaxSize(),
            onCreated = { view ->
                with(view) {
                    settings.run {
                        javaScriptEnabled = true
                        domStorageEnabled = true
                        javaScriptCanOpenWindowsAutomatically = false
                        userAgentString = System.getProperty("http.agent")
                    }
                }
            }
        )


        BackHandler(enabled = true) {
            if (viewModel.webViewNavigator.canGoBack) {
                viewModel.webViewNavigator.navigateBack()
            } else {
                //webView?.goBack()
                finish()
            }
        }
    }



    @Composable
    fun Greeting(name: String) {
        Text(text = "Hello $name!")
    }

    @Preview(showBackground = true)
    @Composable
    fun DefaultPreview() {
        SugnRakchurchTheme {
            Greeting("Android")
        }
    }
}