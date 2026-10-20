package com.softnet.cardforge

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.webkit.CookieManager
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast

class MainActivity : Activity() {
    private lateinit var webView: WebView
    private lateinit var progress: ProgressBar
    private lateinit var status: TextView
    private lateinit var serverButton: Button
    private val prefs by lazy { getSharedPreferences("softnet_android", MODE_PRIVATE) }
    private val serverUrl: String
        get() = prefs.getString("server_url", "") ?: ""

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = Color.rgb(16, 24, 40)
        window.navigationBarColor = Color.BLACK
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)

        val root = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setBackgroundColor(Color.rgb(245, 247, 250)) }
        val header = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL; setPadding(dp(14), dp(8), dp(10), dp(8)); setBackgroundColor(Color.rgb(16, 24, 40)) }
        val title = TextView(this).apply { text = "SOFTNET  •  CARDFORGE"; setTextColor(Color.WHITE); textSize = 16f; typeface = android.graphics.Typeface.DEFAULT_BOLD }
        header.addView(title, LinearLayout.LayoutParams(0, dp(44), 1f))
        serverButton = Button(this).apply { text = "الاتصال"; setTextColor(Color.WHITE); setBackgroundColor(Color.rgb(38, 87, 166)); setOnClickListener { showServerDialog() } }
        header.addView(serverButton, LinearLayout.LayoutParams(dp(100), dp(44)))
        root.addView(header)

        progress = ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal).apply { max = 100; visibility = View.GONE }
        root.addView(progress, LinearLayout.LayoutParams(-1, dp(3)))
        status = TextView(this).apply { textSize = 12f; setTextColor(Color.DKGRAY); setPadding(dp(12), dp(6), dp(12), dp(6)); visibility = View.GONE }
        root.addView(status)

        webView = WebView(this).apply {
            setBackgroundColor(Color.WHITE)
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            settings.databaseEnabled = true
            settings.loadsImagesAutomatically = true
            settings.javaScriptCanOpenWindowsAutomatically = true
            settings.allowFileAccess = false
            settings.allowContentAccess = false
            settings.setSupportZoom(true)
            settings.builtInZoomControls = true
            settings.displayZoomControls = false
        }
        CookieManager.getInstance().setAcceptCookie(true)
        CookieManager.getInstance().setAcceptThirdPartyCookies(webView, true)
        webView.webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                progress.progress = newProgress
                progress.visibility = if (newProgress in 1..99) View.VISIBLE else View.GONE
            }
        }
        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                val uri = request?.url ?: return false
                val scheme = uri.scheme?.lowercase() ?: return false
                if (scheme == "http" || scheme == "https") {
                    val host = uri.host ?: return false
                    val appHost = Uri.parse(serverUrl).host
                    if (host == appHost) return false
                    openExternal(uri); return true
                }
                if (scheme == "tel" || scheme == "mailto" || scheme == "intent" || scheme == "whatsapp") { openExternal(uri); return true }
                return true
            }
            override fun onPageFinished(view: WebView?, url: String?) { status.visibility = View.GONE; serverButton.text = "إعدادات" }
            override fun onReceivedError(view: WebView?, request: WebResourceRequest?, error: android.webkit.WebResourceError?) {
                if (request?.isForMainFrame == true) {
                    status.text = "تعذر الاتصال. تأكد أن V58 يعمل وأن الهاتف والكمبيوتر على نفس شبكة Wi-Fi."
                    status.visibility = View.VISIBLE
                    serverButton.text = "الاتصال"
                }
            }
        }
        root.addView(webView, LinearLayout.LayoutParams(-1, 0, 1f))

        val footer = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; setPadding(dp(8), dp(4), dp(8), dp(4)); setBackgroundColor(Color.WHITE) }
        footer.addView(Button(this).apply { text = "رجوع"; setOnClickListener { if (webView.canGoBack()) webView.goBack() } }, LinearLayout.LayoutParams(0, dp(44), 1f))
        footer.addView(Button(this).apply { text = "تحديث"; setOnClickListener { webView.reload() } }, LinearLayout.LayoutParams(0, dp(44), 1f))
        footer.addView(Button(this).apply { text = "الرئيسية"; setOnClickListener { if (serverUrl.isNotBlank()) webView.loadUrl(serverUrl) else showServerDialog() } }, LinearLayout.LayoutParams(0, dp(44), 1f))
        root.addView(footer)
        setContentView(root)

        if (serverUrl.isBlank()) showServerDialog(firstRun = true) else loadHome()
    }

    private fun showServerDialog(firstRun: Boolean = false) {
        val box = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(dp(20), dp(8), dp(20), 0) }
        box.addView(TextView(this).apply {
            text = "أدخل عنوان الكمبيوتر الذي يعمل عليه SoftNet CardForge V58. مثال: http://192.168.88.10:8080"
            textSize = 14f; setTextColor(Color.DKGRAY)
        })
        val input = EditText(this).apply {
            setSingleLine(true); hint = "http://192.168.88.10:8080"; setText(serverUrl)
            inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_URI
        }
        box.addView(input)
        AlertDialog.Builder(this).setTitle("إعداد اتصال الخادم").setView(box).setCancelable(!firstRun)
            .setPositiveButton("حفظ واتصال") { _, _ ->
                val normalized = normalizeUrl(input.text.toString())
                val uri = Uri.parse(normalized)
                if (uri.host.isNullOrBlank() || (uri.scheme != "http" && uri.scheme != "https")) {
                    Toast.makeText(this, "العنوان غير صحيح. استخدم http://IP:8080", Toast.LENGTH_LONG).show()
                    showServerDialog(firstRun); return@setPositiveButton
                }
                prefs.edit().putString("server_url", normalized).apply()
                loadHome()
            }.setNegativeButton(if (firstRun) "إغلاق" else "إلغاء") { dialog, _ ->
                dialog.dismiss()
                if (firstRun) { status.text = "اضغط الاتصال لإدخال عنوان الخادم."; status.visibility = View.VISIBLE }
            }.show()
    }

    private fun normalizeUrl(value: String): String {
        var s = value.trim().trimEnd('/')
        if (!s.startsWith("http://") && !s.startsWith("https://")) s = "http://$s"
        return s
    }
    private fun loadHome() {
        if (serverUrl.isBlank()) { showServerDialog(); return }
        status.text = "الاتصال بـ $serverUrl"; status.visibility = View.VISIBLE; webView.loadUrl(serverUrl)
    }
    private fun openExternal(uri: Uri) {
        try { startActivity(Intent(Intent.ACTION_VIEW, uri)) }
        catch (_: Exception) { Toast.makeText(this, "لا يوجد تطبيق مناسب لفتح الرابط", Toast.LENGTH_LONG).show() }
    }
    private fun dp(v: Int) = (v * resources.displayMetrics.density).toInt()
    @Deprecated("Deprecated in Android API")
    override fun onBackPressed() {
        if (::webView.isInitialized && webView.canGoBack()) webView.goBack() else super.onBackPressed()
    }
    override fun onDestroy() { if (::webView.isInitialized) { webView.stopLoading(); webView.destroy() }; super.onDestroy() }
}
