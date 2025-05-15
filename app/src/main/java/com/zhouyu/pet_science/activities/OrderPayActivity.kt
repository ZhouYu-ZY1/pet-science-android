package com.zhouyu.pet_science.activities

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import com.zhouyu.pet_science.R
import com.zhouyu.pet_science.activities.base.BaseActivity
import com.zhouyu.pet_science.model.Order
import com.zhouyu.pet_science.network.OrderHttpUtils
import com.zhouyu.pet_science.network.ProductHttpUtils
import com.zhouyu.pet_science.utils.ConsoleUtils
import com.zhouyu.pet_science.utils.MyToast
import com.zhouyu.pet_science.utils.PhoneMessage
import com.zhouyu.pet_science.views.dialog.MyProgressDialog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject

class OrderPayActivity : BaseActivity() {
    private lateinit var webView: WebView
    private var order: Order? = null
    private var orderId: Int = -1
    
    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_web_page)

        // 从Intent中获取订单ID
        orderId = intent.getIntExtra("orderId",-1)
        if (0 > orderId) {
            finish()
            return
        }

        // 显示加载对话框
        val progressDialog = MyProgressDialog(this).apply {
            setTitleStr("加载中")
            setHintStr("正在获取订单信息...")
            show()
        }

        // 使用协程获取订单详情
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val result = OrderHttpUtils.getOrderDetail(orderId)
                if (result?.code == 200 && result.data != null) {
                    // 获取订单成功
                    order = result.data
                    // 初始化视图
                    initViews()
                } else {
                    // 获取订单失败
                    MyToast.show("获取订单信息失败: ${result?.message ?: "未知错误"}", false)
                    finish()
                }
            } catch (e: Exception) {
                MyToast.show("获取订单信息异常: ${e.message}", false)
                finish()
            } finally {
                progressDialog.dismiss()
            }
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun initViews() {
        val main = findViewById<View>(R.id.main)
//        setTopBarView(main, true)

        payProgressDialog = MyProgressDialog(this).apply {
            setTitleStr("支付中")
            setHintStr("正在支付订单...")
        }

        webView = findViewById(R.id.webView)
        webView.visibility = View.GONE

        // 配置WebView
        webView.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            allowFileAccess = true
            allowContentAccess = true
        }

        // 设置WebView客户端
        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                // 执行js代码处理顶部状态栏
                webView.loadUrl("javascript:setStatusBarHeight(${PhoneMessage.statusBarHeight + 25})")
                // 页面加载完成后的操作
                webView.visibility = View.VISIBLE
            }
        }

        webView.webChromeClient = WebChromeClient()

        // 添加JavaScript接口
        webView.addJavascriptInterface(WebAppInterface(), "Android")

        // 加载HTML文件
        webView.loadUrl("file:///android_asset/html/order-pay/order-pay.html")

        // 处理系统UI和软键盘
        ViewCompat.setOnApplyWindowInsetsListener(main) { v, insets ->
            val imeInsets = insets.getInsets(WindowInsetsCompat.Type.ime())
            v.updatePadding(bottom = imeInsets.bottom)
            insets
        }
    }
    lateinit var payProgressDialog:MyProgressDialog

    // JavaScript接口类
    inner class WebAppInterface {
        @JavascriptInterface
        fun getProductInfo(): String {
            // 返回订单信息的JSON字符串
            return """
                {
                    "productName": "${order?.orderItem?.productName ?: ""}",
                    "productPrice": "${order?.orderItem?.price ?: 0.0}",
                    "productSpec": "${order?.remark ?: ""}",
                    "productImage": "${order?.orderItem?.let { ProductHttpUtils.getFirstImage(it.productImage) } ?: ""}",
                    "quantity": ${order?.orderItem?.quantity ?: 1},
                    "orderId": $orderId
                }
            """.trimIndent()
        }

        @JavascriptInterface
        fun getOrderExpiration(): String {
            var expirationSeconds = 0L
            try {
                // 同步调用获取订单过期时间
                val result = OrderHttpUtils.getOrderExpiration(orderId)
                if (result?.code == 200 && result.data != null) {
                    expirationSeconds = result.data
                }
            } catch (e: Exception) {
                ConsoleUtils.logErr("获取订单过期时间异常: ${e.message}")
            }
            
            return """
                {
                    "expirationSeconds": $expirationSeconds
                }
            """.trimIndent()
        }

        @JavascriptInterface
        fun payOrder(paymentMethod: String): String {
            runOnUiThread {
                payProgressDialog.show()
            }
            try {
                // 调用支付接口
                val result = OrderHttpUtils.payOrder(orderId, paymentMethod)
                Thread.sleep(1000)
                return if (result?.code == 200) {
                    """
                        {
                            "success": true,
                            "message": "支付成功"
                        }
                    """.trimIndent()
                } else {
                    """
                        {
                            "success": false,
                            "message": "${result?.message ?: "支付失败"}"
                        }
                    """.trimIndent()
                }
            } catch (e: Exception) {
                return """
                    {
                        "success": false,
                        "message": "支付异常: ${e.message}"
                    }
                """.trimIndent()
            }
        }

        @JavascriptInterface
        fun showToast(message: String) {
            runOnUiThread {
                Toast.makeText(this@OrderPayActivity, message, Toast.LENGTH_SHORT).show()
            }
        }


        @JavascriptInterface
        fun payError(message: String) {
            runOnUiThread {
                MyToast.show(message)
                finish()
            }
        }
        @JavascriptInterface
        fun goBack() {
            runOnUiThread {
                finish()
            }
        }

        @JavascriptInterface
        fun paymentSuccess() {
            runOnUiThread {
                // 支付成功后的处理
                if(payProgressDialog.isShowing){
                    payProgressDialog.dismiss()
                }
//                MyToast.show("支付成功！", true)
//                finish()
            }
        }
    }

    override fun onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack()
        } else {
            super.onBackPressed()
        }
    }

    override fun finish() {
        if(payProgressDialog.isShowing){
            payProgressDialog.dismiss()
        }

        super.finish()
    }
}