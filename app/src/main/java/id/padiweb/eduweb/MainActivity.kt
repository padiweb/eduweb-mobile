package id.padiweb.eduweb

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.view.KeyEvent
import android.view.View
import android.webkit.*
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : AppCompatActivity() {

    companion object {
        const val APP_URL = "https://eduweb.smkaltansch.id"
    }

    private lateinit var webView: WebView
    private lateinit var swipeRefresh: SwipeRefreshLayout
    private lateinit var offlineLayout: LinearLayout

    private var filePathCallback: ValueCallback<Array<Uri>>? = null
    private var cameraImageUri: Uri? = null
    private val handler = Handler(Looper.getMainLooper())

    // Launcher untuk pilih file/foto
    private val fileChooserLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data = result.data
            val uri = data?.dataString?.let { Uri.parse(it) } ?: cameraImageUri
            filePathCallback?.onReceiveValue(uri?.let { arrayOf(it) })
        } else {
            filePathCallback?.onReceiveValue(null)
        }
        filePathCallback = null
        cameraImageUri = null
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        webView       = findViewById(R.id.webView)
        swipeRefresh  = findViewById(R.id.swipeRefresh)
        offlineLayout = findViewById(R.id.offlineLayout)
        val retryBtn: Button = findViewById(R.id.retryButton)

        requestPermissions()
        setupWebView()

        // Warna pull-to-refresh sesuai brand EduWeb
        swipeRefresh.setColorSchemeColors(getColor(R.color.primary))
        swipeRefresh.setOnRefreshListener {
            webView.reload()
            swipeRefresh.isRefreshing = false
        }

        retryBtn.setOnClickListener {
            if (isOnline()) {
                offlineLayout.visibility = View.GONE
                swipeRefresh.visibility  = View.VISIBLE
                webView.loadUrl(APP_URL)
            }
        }

        if (isOnline()) webView.loadUrl(APP_URL)
        else showOffline()
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun setupWebView() {
        webView.settings.apply {
            javaScriptEnabled    = true
            domStorageEnabled    = true
            databaseEnabled      = true
            allowFileAccess      = true
            allowContentAccess   = true
            setSupportZoom(true)
            builtInZoomControls  = false
            displayZoomControls  = false
            loadsImagesAutomatically = true
            mixedContentMode     = WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE
            cacheMode            = WebSettings.LOAD_DEFAULT
            geolocationEnabled   = true

            // User agent - identifikasi sebagai EduWeb App
            userAgentString = "Mozilla/5.0 (Linux; Android ${Build.VERSION.RELEASE}) " +
                "AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36 " +
                "EduWebApp/1.0 Padiweb"
        }

        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                swipeRefresh.isRefreshing = false
            }

            override fun onReceivedError(view: WebView?, request: WebResourceRequest?,
                                          error: WebResourceError?) {
                if (request?.isForMainFrame == true) showOffline()
            }

            override fun onReceivedSslError(view: WebView?, handler: SslErrorHandler?,
                                             error: android.net.http.SslError?) {
                // Percayai SSL dari domain sendiri saja
                if (view?.url?.contains("smkaltansch.id") == true) handler?.proceed()
                else handler?.cancel()
            }

            override fun shouldOverrideUrlLoading(view: WebView?,
                                                   request: WebResourceRequest?): Boolean {
                val url = request?.url?.toString() ?: return false
                // Buka link eksternal di browser biasa
                if (!url.startsWith("https://eduweb.smkaltansch.id") &&
                    !url.startsWith("about:") && !url.startsWith("blob:")) {
                    startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                    return true
                }
                return false
            }
        }

        webView.webChromeClient = object : WebChromeClient() {

            // Handle upload file (foto bukti bayar, selfie absensi, dll)
            override fun onShowFileChooser(webView: WebView?, callback: ValueCallback<Array<Uri>>?,
                                            params: FileChooserParams?): Boolean {
                filePathCallback = callback
                val acceptTypes = params?.acceptTypes
                val isImage = acceptTypes?.any { it.contains("image") } == true

                if (isImage) openImageChooser()
                else fileChooserLauncher.launch(params?.createIntent())

                return true
            }

            // GPS permission dari web
            override fun onGeolocationPermissionsShowPrompt(origin: String?,
                                                             callback: GeolocationPermissions.Callback?) {
                callback?.invoke(origin, true, false)
            }

            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                if (newProgress == 100) swipeRefresh.isRefreshing = false
            }
        }

        // JavaScript bridge - web bisa deteksi ini app native
        webView.addJavascriptInterface(object : Any() {
            @JavascriptInterface fun getAppInfo(): String =
                """{"platform":"android","version":"${Build.VERSION.RELEASE}","isApp":true,"appVersion":"1.0.0"}"""
        }, "EduWebBridge")
    }

    private fun openImageChooser() {
        // Intent kamera
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        try {
            val photoFile = createImageFile()
            cameraImageUri = FileProvider.getUriForFile(
                this, "${applicationContext.packageName}.provider", photoFile
            )
            cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, cameraImageUri)
        } catch (e: IOException) {
            e.printStackTrace()
        }

        // Intent galeri
        val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)

        // Chooser: kamera atau galeri
        val chooser = Intent.createChooser(galleryIntent, "Pilih Foto")
        chooser.putExtra(Intent.EXTRA_INITIAL_INTENTS, arrayOf(cameraIntent))
        fileChooserLauncher.launch(chooser)
    }

    @Throws(IOException::class)
    private fun createImageFile(): File {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        return File.createTempFile("EDUWEB_$timestamp", ".jpg", getExternalFilesDir(null))
    }

    private fun requestPermissions() {
        val perms = mutableListOf(
            Manifest.permission.CAMERA,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            perms.add(Manifest.permission.POST_NOTIFICATIONS)
        }
        val denied = perms.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }
        if (denied.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, denied.toTypedArray(), 100)
        }
    }

    private fun isOnline(): Boolean {
        val cm = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        return cm.getNetworkCapabilities(cm.activeNetwork)
            ?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
    }

    private fun showOffline() {
        swipeRefresh.visibility  = View.GONE
        offlineLayout.visibility = View.VISIBLE
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK && webView.canGoBack()) {
            webView.goBack()
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onResume() {
        super.onResume()
        webView.onResume()
    }

    override fun onPause() {
        super.onPause()
        webView.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        webView.stopLoading()
        webView.destroy()
    }
}
