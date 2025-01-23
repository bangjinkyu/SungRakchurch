package app.vercel.srapp_10db2.twa


import android.Manifest.permission.POST_NOTIFICATIONS
import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.Manifest.permission.READ_MEDIA_IMAGES
import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.annotation.SuppressLint
import android.app.Activity
import android.content.ActivityNotFoundException

import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.webkit.ValueCallback
import android.webkit.WebView
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.app.ActivityCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import app.vercel.srapp_10db2.twa.model.SplashViewModel
import app.vercel.srapp_10db2.twa.ui.theme.SugnRakchurchTheme
import com.google.accompanist.web.AccompanistWebChromeClient
import com.google.accompanist.web.AccompanistWebViewClient
import com.google.accompanist.web.WebContent
import com.google.accompanist.web.WebView
import com.google.accompanist.web.WebViewState
import com.google.accompanist.web.rememberWebViewStateWithHTMLData
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

        if (Intent.ACTION_VIEW.equals(intent.getAction())) {
            val uri = intent.data
            if(uri != null) {
                viewModel.weblink = uri.getQueryParameter("url").toString()
                Log.d(TAG, "room getIntent: ${viewModel.weblink}")
            }
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
            ActivityResultContracts.RequestMultiplePermissions()
        ) { deniedList ->

            deniedList.entries.forEach {
                when {
                    it.value -> {
                        Log.d(TAG, "PERMISSION GRANTED")
                    }

                    shouldShowRequestPermissionRationale(it.key) -> {
                        Log.d(TAG, "Permission required to use app")
                    }

                    else -> {
                        Log.d(TAG, "PERMISSION denied")
                    }
                }
            }
        }

        if(isAllPermissionsGranted()) {
            Log.d(TAG, "PERMISSION GRANTED")

        } else {
            SideEffect {
                requestPermissionLauncher.launch(REQUIRED_PERMISSIONS)
            }
        }

        WebViewPage()
    }

    @SuppressLint("SetJavaScriptEnabled")
    @Composable
    fun WebViewPage() {
        Log.d("Firebase", " WebViewPage()")
        Log.d("Firebase", " viewModel ${viewModel.weblink}")


        var fileChooserIntent by remember { mutableStateOf<Intent?>(null) }

        val webViewChromeClient = remember {
            FileUploadWebChromeClient(
                onShowFilePicker = {
                    fileChooserIntent = it
                }
            )
        }

        val launcher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.StartActivityForResult(),
        ) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data = result.data?.data
                if (data != null) {
                    // 현재 한개의 데이터만 선택되게 했지만, 멀티 선택인 경우를 고려해도 좋을 것 같아요
                    webViewChromeClient.selectFiles(arrayOf(data))
                } else {
                    webViewChromeClient.cancelFileChooser()
                }
            } else {
                webViewChromeClient.cancelFileChooser()
            }
        }

        LaunchedEffect(key1 = fileChooserIntent) {
            if (fileChooserIntent != null) {
                try {
                    launcher.launch(fileChooserIntent)
                } catch (e: ActivityNotFoundException) {
                    // 기기에 알맞는 File picker가 없을 경우 취소
                    webViewChromeClient.cancelFileChooser()
                }
            }
        }

        WebView(
            state = WebViewState(
                WebContent.Url(
                    url = viewModel.weblink,
                    additionalHttpHeaders = emptyMap()
                )
            ),
            client = AccompanistWebViewClient(),
            chromeClient = webViewChromeClient,
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

    private fun isAllPermissionsGranted(): Boolean = REQUIRED_PERMISSIONS.all { permission ->
        ActivityCompat.checkSelfPermission(this, permission) ==
                PackageManager.PERMISSION_GRANTED
    }

    class FileUploadWebChromeClient(
        private val onShowFilePicker: (Intent) -> Unit
    ): AccompanistWebChromeClient() {
        private var filePathCallback: ValueCallback<Array<Uri>>? = null

        override fun onShowFileChooser(
            webView: WebView?,
            filePathCallback: ValueCallback<Array<Uri>>?,
            fileChooserParams: FileChooserParams?
        ): Boolean {
            this.filePathCallback = filePathCallback
            val filePickerIntent = fileChooserParams?.createIntent()
            if (filePickerIntent == null) {
                cancelFileChooser()
            } else {
                onShowFilePicker(filePickerIntent)
            }
            return true
        }

        fun selectFiles(uris: Array<Uri>) {
            filePathCallback?.onReceiveValue(uris)
            filePathCallback = null
        }

        fun cancelFileChooser() {
            filePathCallback?.onReceiveValue(null)
            filePathCallback = null
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

    companion object {
        private val REQUIRED_PERMISSIONS = if(Build.VERSION.SDK_INT <= Build.VERSION_CODES.P){
            arrayOf(
                WRITE_EXTERNAL_STORAGE,
                READ_EXTERNAL_STORAGE
            )
        } else if(Build.VERSION.SDK_INT <= Build.VERSION_CODES.S_V2){
            arrayOf(
                READ_EXTERNAL_STORAGE
            )
        } else {
            arrayOf(
                POST_NOTIFICATIONS,
                READ_MEDIA_IMAGES
            )
        }
    }
}