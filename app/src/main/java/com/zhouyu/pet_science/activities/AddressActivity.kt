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
import com.zhouyu.pet_science.network.HttpUtils
import com.zhouyu.pet_science.network.ProductHttpUtils
import com.zhouyu.pet_science.utils.ConsoleUtils
import com.zhouyu.pet_science.utils.MyToast
import com.zhouyu.pet_science.utils.PhoneMessage
import com.zhouyu.pet_science.views.dialog.MyProgressDialog
import com.google.gson.Gson
import com.zhouyu.pet_science.model.UserAddress
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject

class AddressActivity : BaseActivity() {
    private lateinit var webView: WebView
    private lateinit var progressDialog: MyProgressDialog
    private var isSelectMode = false // 是否是选择地址模式
    
    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_web_page)

        // 获取是否是选择地址模式
        isSelectMode = intent.getBooleanExtra("isSelect", false)

        // 初始化加载对话框
        progressDialog = MyProgressDialog(this).apply {
            setTitleStr("加载中")
            setHintStr("正在获取地址信息...")
        }

        initViews()
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun initViews() {
        val main = findViewById<View>(R.id.main)
        
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
        webView.loadUrl("file:///android_asset/html/address/address.html")

        // 处理系统UI和软键盘
        ViewCompat.setOnApplyWindowInsetsListener(main) { v, insets ->
            val imeInsets = insets.getInsets(WindowInsetsCompat.Type.ime())
            v.updatePadding(bottom = imeInsets.bottom)
            insets
        }
    }

    // JavaScript接口类
    inner class WebAppInterface {
        @JavascriptInterface
        fun getAddressList(): String {
            // 同步调用获取地址列表
            return runBlocking {
                try {
                    val result = ProductHttpUtils.getUserAddresses()
                    if (result?.code == 200 && result.data != null) {
                        Gson().toJson(mapOf("success" to true, "data" to result.data))
                    } else {
                        Gson().toJson(mapOf("success" to false, "message" to (result?.message ?: "获取地址列表失败")))
                    }
                } catch (e: Exception) {
                    ConsoleUtils.logErr("getAddressList: ${e.message}")
                    Gson().toJson(mapOf("success" to false, "message" to "获取地址列表异常: ${e.message}"))
                }
            }
        }

        @JavascriptInterface
        fun addAddress(addressJson: String): String {
            // 同步调用添加地址
            return runBlocking {
                try {
                    val address = Gson().fromJson(addressJson, UserAddress::class.java)
                    val result = ProductHttpUtils.addAddress(address)
                    if (result?.code == 200 && result.data != null) {
                        Gson().toJson(mapOf("success" to true, "data" to result.data))
                    } else {
                        Gson().toJson(mapOf("success" to false, "message" to (result?.message ?: "添加地址失败")))
                    }
                } catch (e: Exception) {
                    ConsoleUtils.logErr("addAddress: ${e.message}")
                    Gson().toJson(mapOf("success" to false, "message" to "添加地址异常: ${e.message}"))
                }
            }
        }

        @JavascriptInterface
        fun updateAddress(addressJson: String): String {
            // 同步调用更新地址
            return runBlocking {
                try {
                    val address = Gson().fromJson(addressJson, UserAddress::class.java)
                    val result = ProductHttpUtils.updateAddress(address)
                    if (result?.code == 200) {
                        Gson().toJson(mapOf("success" to true, "message" to (result.data ?: "更新成功")))
                    } else {
                        Gson().toJson(mapOf("success" to false, "message" to (result?.message ?: "更新地址失败")))
                    }
                } catch (e: Exception) {
                    ConsoleUtils.logErr("updateAddress: ${e.message}")
                    Gson().toJson(mapOf("success" to false, "message" to "更新地址异常: ${e.message}"))
                }
            }
        }

        @JavascriptInterface
        fun deleteAddress(addressId: Int): String {
            // 同步调用删除地址
            return runBlocking {
                try {
                    val result = ProductHttpUtils.deleteAddress(addressId)
                    if (result?.code == 200) {
                        Gson().toJson(mapOf("success" to true, "message" to (result.data ?: "删除成功")))
                    } else {
                        Gson().toJson(mapOf("success" to false, "message" to (result?.message ?: "删除地址失败")))
                    }
                } catch (e: Exception) {
                    ConsoleUtils.logErr("deleteAddress: ${e.message}")
                    Gson().toJson(mapOf("success" to false, "message" to "删除地址异常: ${e.message}"))
                }
            }
        }

        @JavascriptInterface
        fun setDefaultAddress(addressId: Int): String {
            // 同步调用设置默认地址
            return runBlocking {
                try {
                    val result = ProductHttpUtils.setDefaultAddress(addressId)
                    if (result?.code == 200) {
                        Gson().toJson(mapOf("success" to true, "message" to (result.data ?: "设置默认地址成功")))
                    } else {
                        Gson().toJson(mapOf("success" to false, "message" to (result?.message ?: "设置默认地址失败")))
                    }
                } catch (e: Exception) {
                    ConsoleUtils.logErr("setDefaultAddress: ${e.message}")
                    Gson().toJson(mapOf("success" to false, "message" to "设置默认地址异常: ${e.message}"))
                }
            }
        }

        @JavascriptInterface
        fun showToast(message: String) {
            runOnUiThread {
                Toast.makeText(this@AddressActivity, message, Toast.LENGTH_SHORT).show()
            }
        }

        @JavascriptInterface
        fun goBack() {
            runOnUiThread {
                finish()
            }
        }

        @JavascriptInterface
        fun getRegionData(): String {
            // 读取省市区数据文件
            return try {
                val inputStream = assets.open("province_new.json")
                val size = inputStream.available()
                val buffer = ByteArray(size)
                inputStream.read(buffer)
                inputStream.close()
                String(buffer, Charsets.UTF_8)
            } catch (e: Exception) {
                ConsoleUtils.logErr("getRegionData: ${e.message}")
                "[]" // 出错时返回空数组
            }
        }

        @JavascriptInterface
        fun selectAddress(addressJson: String) {
            // 如果是选择地址模式，则将选中的地址保存到ProductDetailActivity中
            if (isSelectMode) {
                try {
                    val address = Gson().fromJson(addressJson, UserAddress::class.java)
                    ProductDetailActivity.selectedAddress = address
                    runOnUiThread {
                        finish() // 选择完成后关闭页面
                    }
                } catch (e: Exception) {
                    ConsoleUtils.logErr("selectAddress: ${e.message}")
                    runOnUiThread {
                        MyToast.show("选择地址失败")
                    }
                }
            }
        }

        @JavascriptInterface
        fun isSelectMode(): Boolean {
            return isSelectMode
        }
    }

    override fun onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack()
        } else {
            super.onBackPressed()
        }
    }
}