package com.zhouyu.pet_science.activities

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import android.view.View
import android.webkit.JavascriptInterface
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import com.zhouyu.pet_science.R
import com.zhouyu.pet_science.activities.base.BaseActivity
import com.zhouyu.pet_science.databinding.ActivityWebBrowserBinding
import com.zhouyu.pet_science.utils.MyToast
import com.zhouyu.pet_science.network.HttpUtils
import com.zhouyu.pet_science.utils.ConsoleUtils
import com.zhouyu.pet_science.views.dialog.MyDialog

class WebBrowserActivity : BaseActivity() {
    private lateinit var binding: ActivityWebBrowserBinding
    private var webView: WebView? = null
    private var uploadMessageAboveL: ValueCallback<Array<Uri>>? = null
    private var notSkipApp = false
    private var ua: String? = null
    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWebBrowserBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val intent = intent
        originalUrl = intent.getStringExtra("url")
        singleUrl = intent.getStringExtra("singleUrl")
        ua = intent.getStringExtra("ua")
        notSkipApp = intent.getBooleanExtra("notSkipApp", false)

        if (!TextUtils.isEmpty(singleUrl)) {
            originalUrl = singleUrl
        }

        currUrl = originalUrl
        binding.finishBtn.setOnClickListener {
            isFinish = true
            finish()
        }
        var intentUrl: String? = null
        if (Intent.ACTION_VIEW == intent.action && intent.data != null) {
            intentUrl = intent.data.toString()
        }
        if (TextUtils.isEmpty(originalUrl) && TextUtils.isEmpty(intentUrl)) {
            MyToast.show("出错了", false)
            finish()
            return
        }
        webView = binding.webView
        initWebSetting()
        if (intentUrl != null) {
            webView!!.loadUrl(intentUrl)
        } else {
            webView!!.loadUrl(originalUrl!!)
        }
        webView!!.webViewClient = object : WebViewClient() {
            @SuppressLint("SetTextI18n")
            override fun shouldInterceptRequest(
                view: WebView,
                request: WebResourceRequest
            ): WebResourceResponse? {
                return super.shouldInterceptRequest(view, request)
            }

            override fun shouldOverrideUrlLoading(
                view: WebView,
                request: WebResourceRequest
            ): Boolean {
                val url = request.url.toString()
                return loading(view, url)
            }

            @Deprecated("Deprecated in Java")
            override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                return loading(view, url)
            }

            private fun loading(view: WebView, url: String): Boolean {
                if (TextUtils.isEmpty(url)) return false
                if (!TextUtils.isEmpty(singleUrl) && !url.contains("singleUrl")) {
                    return true // 阻止加载
                }
                return if (url.startsWith("http:") || url.startsWith("https:")) {
                    view.loadUrl(url)
                    false
                } else {
                    if (!notSkipApp) {
                        val myDialog = MyDialog(this@WebBrowserActivity)
                        myDialog.setTitle("打开其他APP")
                        myDialog.setMessage("网页请求打开其他APP")
                        myDialog.setYesOnclickListener("打开") {
                            myDialog.dismiss()
                            try {
                                val openIntent = Intent(Intent.ACTION_VIEW)
                                openIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                                openIntent.setData(Uri.parse(url))
                                startActivityForResult(openIntent, APP_SKIP_RESULT_CODE)
                            } catch (e: ActivityNotFoundException) {
                                MyToast.show("开启失败，找不到指定目标", false)
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                        myDialog.setNoOnclickListener("取消") { myDialog.dismiss() }
                        myDialog.show()
                        true
                    } else {
                        false
                    }
                }
            }

            override fun onPageStarted(view: WebView, url: String, favicon: Bitmap) {
                super.onPageStarted(view, url, favicon)
                binding.webProgressBar.visibility = View.VISIBLE
            }

            override fun onPageFinished(view: WebView, url: String) {
                super.onPageFinished(view, url)
                binding.webProgressBar.visibility = View.GONE
                webView!!.loadUrl("javascript:window.java_fun.loadFinishHtml(document.body.innerHTML);")
            }
        }
        webView!!.webChromeClient = object : WebChromeClient() {
            override fun onShowFileChooser(
                webView: WebView,
                filePathCallback: ValueCallback<Array<Uri>>,
                fileChooserParams: FileChooserParams
            ): Boolean {
                uploadMessageAboveL = filePathCallback
                openImageChooserActivity()
                return true
            }

            override fun onProgressChanged(view: WebView, newProgress: Int) {
                binding.webProgressBar.progress = newProgress
                super.onProgressChanged(view, newProgress)
            }
        }
        webView!!.addJavascriptInterface(MyJavaScriptInterface(), "java_fun")
        binding.refreshBtn.setOnClickListener { webView!!.reload() }
        binding.openWebBrowser.setOnClickListener {
            try {
                val uri = Uri.parse(webView!!.url)
                val webIntent = Intent(Intent.ACTION_VIEW, uri)
                startActivity(webIntent)
            } catch (e: Exception) {
                MyToast.show("出错了", Toast.LENGTH_LONG, false)
            }
        }
        runTime()
    }

    private inner class MyJavaScriptInterface {
        @JavascriptInterface
        fun loadFinishHtml(html: String?) {
            ConsoleUtils.logErr("loadFinishHtml:$html")
        }
    }

    @SuppressLint("SetJavaScriptEnabled", "ObsoleteSdkInt")
    private fun initWebSetting() {
        val settings = webView!!.settings
        settings.javaScriptEnabled = true //允许使用js
        settings.domStorageEnabled = true
        settings.blockNetworkImage = false //解决图片不显示
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            settings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
        }
        if (!TextUtils.isEmpty(ua)) {
            if (ua == "windows") {
                //设置浏览器标识
                settings.userAgentString =
                    "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/43.0.2357.134 Safari/537.36"
                //自适应屏幕
                settings.useWideViewPort = true
                settings.loadWithOverviewMode = true
                //自动缩放
                settings.builtInZoomControls = true
                settings.setSupportZoom(true)
            } else if (ua.equals("android", ignoreCase = true)) {
                settings.userAgentString = HttpUtils.randomUA("android")
                settings.useWideViewPort = false
                settings.loadWithOverviewMode = false
                settings.builtInZoomControls = false
                settings.setSupportZoom(false)
            }
        }
    }

    private fun openImageChooserActivity() {
        val i = Intent(Intent.ACTION_GET_CONTENT)
        i.addCategory(Intent.CATEGORY_OPENABLE)
        i.setType("image/*")
        startActivityForResult(Intent.createChooser(i, "Image Chooser"), FILE_CHOOSER_RESULT_CODE)
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == FILE_CHOOSER_RESULT_CODE) {
            if (uploadMessageAboveL == null) return
            onActivityResultAboveL(requestCode, resultCode, data)
        }
    }

    private fun onActivityResultAboveL(requestCode: Int, resultCode: Int, intent: Intent?) {
        if (requestCode != FILE_CHOOSER_RESULT_CODE || uploadMessageAboveL == null) return

        var results: Array<Uri>? = null
        if (resultCode == RESULT_OK && intent != null) {
            val dataString = intent.dataString
            val clipData = intent.clipData

            if (clipData != null) {
                // 将ClipData转换为非空的Uri数组
                results = Array(clipData.itemCount) { index -> clipData.getItemAt(index).uri }
            } else if (dataString != null) {
                // 单个Uri的情况
                results = arrayOf(Uri.parse(dataString))
            }
        }

        // 确保results不为null，并过滤掉可能的空值
        uploadMessageAboveL?.onReceiveValue(results?.toList()?.toTypedArray())
        uploadMessageAboveL = null
    }

    private val handler: Handler = Handler(Looper.getMainLooper())
    private var currUrl: String? = null
    private var originalUrl: String? = null
    private var singleUrl: String? = null
    private var currTitle: String? = ""
    private fun runTime() {
        handler.post(object : Runnable {
            @SuppressLint("UseCompatLoadingForColorStateLists")
            override fun run() {
                if (currUrl != null) {
                    val url = webView!!.url
                    if (currUrl != url) {
                        currUrl = url
                    }
                    val title = webView!!.title
                    if (currTitle != title && !TextUtils.isEmpty(title)) {
                        binding.topTitle.text = title
                        currTitle = title
                    }
                }
                handler.postDelayed(this, 100)
            }
        })
    }

    override fun onResume() {
        super.onResume()
        if (webView != null) {
            webView!!.onResume()
        }
    }

    override fun onPause() {
        super.onPause()
        if (webView != null) {
            webView!!.onPause()
        }
    }

    override fun onDestroy() {
        handler.removeCallbacksAndMessages(null)
        if (webView != null) {
            webView!!.removeAllViews()
            webView!!.destroy()
            webView = null
        }
        super.onDestroy()
    }

    private var isFinish = false
    override fun finish() {
        if (TextUtils.isEmpty(currUrl)) {
            super.finish()
            return
        }
        if (webView != null && webView!!.canGoBack() && !isFinish && !isDestroyed) {
            webView!!.goBack()
        } else {
            super.finish()
        }
    }

    companion object{
        private const val APP_SKIP_RESULT_CODE = 107
        private const val FILE_CHOOSER_RESULT_CODE = 106

    }
}