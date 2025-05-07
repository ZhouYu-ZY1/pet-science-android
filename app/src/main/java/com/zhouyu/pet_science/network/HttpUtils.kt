package com.zhouyu.pet_science.network

import android.annotation.SuppressLint
import com.zhouyu.pet_science.utils.StorageUtils
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import java.net.Proxy
import java.security.KeyStore
import java.security.SecureRandom
import java.security.cert.X509Certificate
import java.util.Random
import java.util.concurrent.TimeUnit
import javax.net.ssl.HostnameVerifier
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSession
import javax.net.ssl.SSLSocketFactory
import javax.net.ssl.TrustManager
import javax.net.ssl.TrustManagerFactory
import javax.net.ssl.X509TrustManager

object HttpUtils {
//    private const val IP = "172.18.13.253"
//    private const val IP = "192.168.83.202"
//    const val BASE_URL = "http://$IP:8888"
    const val URL = "www.u1156996.nyat.app:50327" //内网渗透地址
//    const val URL = "192.168.83.202:8888" //内网渗透地址
    const val BASE_URL = "https://$URL"

    @JvmField
    var client = OkHttpClient()
        .newBuilder()
        .sslSocketFactory(sSLSocketFactory, x509TrustManager!!)
        .addInterceptor { //添加token
            val originalRequest: Request = it.request()
            if(originalRequest.url.toString().startsWith(BASE_URL)){
                val token = StorageUtils.get<String>("token")
                val requestWithToken: Request = originalRequest.newBuilder()
                    .header("Authorization", "Bearer $token")
                    .build()
                it.proceed(requestWithToken)
            }else{
                it.proceed(originalRequest)
            }
        }
        .connectTimeout(8, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS).build()

    @JvmField
    var testClient = OkHttpClient()
        .newBuilder()
        .proxy(Proxy.NO_PROXY) //禁止使用代理
        .connectTimeout(5, TimeUnit.SECONDS)
        .readTimeout(6, TimeUnit.SECONDS).build()


    fun get(url: String): String? {
        val request = Request.Builder()
            .url(url)
            .get()
            .build()

        val response = this.client.newCall(request).execute()
        return response.body?.string()
    }

    fun post(url: String,json: String): String? {
        val requestBody = json.toRequestBody("application/json; charset=utf-8".toMediaType())
        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .build()
        val response = client.newCall(request).execute()
        return response.body?.string()
    }

    fun put(url: String,json: String): String? {
        val requestBody = json.toRequestBody("application/json; charset=utf-8".toMediaType())
        val request = Request.Builder()
            .url(url)
            .put(requestBody)
            .build()
        val response = client.newCall(request).execute()
        return response.body?.string()
    }

    fun delete(url: String): String? {
        val request = Request.Builder()
            .url(url)
            .delete()
            .build()

        val response = this.client.newCall(request).execute()
        return response.body?.string()
    }

    /**
     * 测试url是否可用
     */
    fun testUrlConnection(url: String?): Boolean {
        var isOk = false
        var response: Response? = null
        try {
            val request: Request = Request.Builder().url(url!!).build()
            response = testClient.newCall(request).execute() //发送请求
            val code = response.code
            if (200 == code) {
                // 200是请求地址顺利连通。。
                isOk = true
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            response?.close()
        }
        return isOk
    }

    private val sSLSocketFactory: SSLSocketFactory
        //    public static String serverURL = "http://103.45.177.179:8000/";
        get() = try {
            val sslContext = SSLContext.getInstance("SSL")
            sslContext.init(null, trustManager, SecureRandom())
            sslContext.socketFactory
        } catch (e: Exception) {
            throw RuntimeException(e)
        }

    @get:SuppressLint("CustomX509TrustManager")
    private val trustManager: Array<TrustManager>
        //获取TrustManager
        get() = arrayOf(
            object : X509TrustManager {
                @SuppressLint("TrustAllX509TrustManager")
                override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String) {}
                @SuppressLint("TrustAllX509TrustManager")
                override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) {
                }

                override fun getAcceptedIssuers(): Array<X509Certificate> {
                    return arrayOf()
                }
            }
        )
    private val x509TrustManager: X509TrustManager?
        get() {
            var trustManager: X509TrustManager? = null
            try {
                val trustManagerFactory =
                    TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm())
                trustManagerFactory.init(null as KeyStore?)
                val trustManagers = trustManagerFactory.trustManagers
                check(!(trustManagers.size != 1 || trustManagers[0] !is X509TrustManager)) { "Unexpected default trust managers:" + trustManagers.contentToString() }
                trustManager = trustManagers[0] as X509TrustManager
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return trustManager
        }
    private val hostnameVerifier: HostnameVerifier
        //    private static String[] allowCallDomain = new String[]{"quqi.com","music.163.com","qq.com","kuwo.cn","kugou.com"};
        get() = HostnameVerifier { s: String?, sslSession: SSLSession? -> true }

    @JvmStatic
    fun randomUA(type: String): String {
        val random = Random()
        val r1 = random.nextInt(9) + 1
        val r2 = random.nextInt(9) + 1
        val r3 = random.nextInt(9) + 1
        val r4 = random.nextInt(9) + 1
        return if (type.equals("android", ignoreCase = true)) {
            "Mozilla/5.0 (Linux; Android 6.0; Nexus 5 Build/MRA58N) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/" + r1 + "5." + r4 + ".4" + r2 + "32.2" + r3 + "2 Mobile Safari/5" + r4 + "5.06"
        } else if (type.equals("win2", ignoreCase = true)) {
            "Mozilla/5.0 (Windows; U; Windows NT 5.2;. en-US) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/" + r1 + "5." + r4 + ".2" + r2 + "32.2" + r3 + "2 Mobile Safari/5" + r4 + "3.06"
        } else {
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/" + r1 + "5." + r4 + ".2" + r2 + "32.2" + r3 + "2 Mobile Safari/5" + r4 + "3.06"
        }
    }

}
