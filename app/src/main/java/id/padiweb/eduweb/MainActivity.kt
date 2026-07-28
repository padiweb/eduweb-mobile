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
import android.provider.MediaStore
import android.view.KeyEvent
import android.view.View
import android.webkit.*
import android.widget.Button
import android.widget.LinearLayout
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.view.WindowCompat
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : AppCompatActivity() {

    companion object {
        const val APP_URL = "https://eduweb.smkaltan.sch.id"
    }

    private lateinit var webView: WebView
    private lateinit var swipeRefresh: SwipeRefreshLayout
    private lateinit var offlineLayout: LinearLayout

    private var filePathCallback: ValueCallback<Array<Uri>>? = null
    private var cameraImageUri: Uri? = null

    private val fileChooserLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val uri = result.data?.data ?: cameraImageUri
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

        // Biarkan sistem handle insets - pakai fitsSystemWindows di XML
        WindowCompat.setDecorFitsSystemWindows(window, true)

        webView       = findViewById(R.id.webView)
        swipeRefresh  = findViewById(R.id.swipeRefresh)
        offlineLayout = findViewById(R.id.offlineLayout)
        val retryBtn: Button = findViewById(R.id.retryButton)

        requestPermissions()
        setupWebView()

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
            javaScriptEnabled        = true
            domStorageEnabled        = true
            databaseEnabled          = true
            allowFileAccess          = true
            allowContentAccess       = true
            setSupportZoom(false)
            builtInZoomControls      = false
            displayZoomControls      = false
            loadsImagesAutomatically = true
            mixedContentMode         = WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE
            cacheMode                = WebSettings.LOAD_DEFAULT
            mediaPlaybackRequiresUserGesture = false
            userAgentString          = userAgentString +
                " AltanEduWebApp/1.0 Padiweb"
        }

        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                swipeRefresh.isRefreshing = false
            }

            override fun onReceivedError(
                view: WebView?, request: WebResourceRequest?, error: WebResourceError?
            ) {
                if (request?.isForMainFrame == true) showOffline()
            }

            override fun onReceivedSslError(
                view: WebView?, handler: SslErrorHandler?, error: android.net.http.SslError?
            ) {
                if (view?.url?.contains("smkaltan.sch.id") == true) handler?.proceed()
                else handler?.cancel()
            }

            override fun shouldOverrideUrlLoading(
                view: WebView?, request: WebResourceRequest?
            ): Boolean {
                val url = request?.url?.toString() ?: return false
                return if (!url.startsWith("https://eduweb.smkaltan.sch.id") &&
                           !url.startsWith("about:") && !url.startsWith("blob:")) {
                    startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                    true
                } else false
            }
        }

        webView.webChromeClient = object : WebChromeClient() {
            override fun onShowFileChooser(
                webView: WebView?,
                callback: ValueCallback<Array<Uri>>?,
                params: FileChooserParams?
            ): Boolean {
                filePathCallback = callback
                val isImage = params?.acceptTypes?.any { it.contains("image") } == true
                if (isImage) openImageChooser()
                else fileChooserLauncher.launch(params?.createIntent() ?: return true)
                return true
            }

            override fun onGeolocationPermissionsShowPrompt(
                origin: String?, callback: GeolocationPermissions.Callback?
            ) {
                callback?.invoke(origin, true, false)
            }

            // Grant kamera/mic untuk getUserMedia (prakerin selfie)
            override fun onPermissionRequest(request: android.webkit.PermissionRequest?) {
                runOnUiThread { request?.grant(request.resources) }
            }

            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                if (newProgress == 100) swipeRefresh.isRefreshing = false
            }
        }

        // Bridge JS agar web tahu ini native app
        webView.addJavascriptInterface(object : Any() {
            @JavascriptInterface
            fun getAppInfo(): String =
                """{"platform":"android","isApp":true,"appVersion":"1.0.0","appName":"AltanEduWeb"}"""
        }, "EduWebBridge")
    }

    private fun openImageChooser() {
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

        val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
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

    override fun onResume()  { super.onResume();  webView.onResume() }
    override fun onPause()   { super.onPause();   webView.onPause() }
    override fun onDestroy() { super.onDestroy(); webView.stopLoading(); webView.destroy() }
}
