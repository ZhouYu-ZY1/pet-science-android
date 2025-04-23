package com.zhouyu.pet_science.activities.base

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.ViewGroup.MarginLayoutParams
import android.view.Window
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.zhouyu.pet_science.R
import com.zhouyu.pet_science.manager.ActivityManager.Companion.instance
import com.zhouyu.pet_science.tools.utils.PhoneMessage

open class BaseActivity : AppCompatActivity() {
    var isDarkBack = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val actionBar = supportActionBar
        actionBar?.hide()

        //状态栏透明
        setStatusBarFullTransparent(window)
        if (!isDarkBack) {
            setStatusBarTextColor(true)
        }

        //添加Activity到管理器
        instance.addActivity(this)
    }

    fun setTopBarView(view: View) {
        setTopBarView(view, false)
    }

    fun setTopBarView(view: View, isPadding: Boolean) {
        if (isPadding) {
            view.setPadding(
                view.paddingLeft,
                PhoneMessage.statusBarHeight,
                view.paddingRight,
                view.paddingBottom
            )
        } else {
            val layoutParams = view.layoutParams
            if (layoutParams is MarginLayoutParams) {
                layoutParams.topMargin = PhoneMessage.statusBarHeight
                view.layoutParams = layoutParams
            }
        }
    }

    private var isLoadTopBar = false
    override fun onStart() {
        super.onStart()
        if (!isLoadTopBar) {
            topBarAdaptiveNotificationBar()
            isLoadTopBar = true
        }
    }

    private fun topBarAdaptiveNotificationBar() {
        val topBar = findViewById<RelativeLayout>(R.id.top_bar) ?: return
        val topBarHeight = PhoneMessage.statusBarHeight + PhoneMessage.dpToPx(50f)
        val topBarLayoutparams = topBar.layoutParams
        topBarLayoutparams.height = topBarHeight
        topBar.layoutParams = topBarLayoutparams
        val topBarMessage = findViewById<LinearLayout>(R.id.top_bar_message)
        val topBarMessageLayoutparams =
            topBarMessage.layoutParams as RelativeLayout.LayoutParams
        topBarMessageLayoutparams.topMargin = PhoneMessage.statusBarHeight
        topBarMessage.layoutParams = topBarMessageLayoutparams
    }

    fun executeThread(runnable: Runnable): Runnable {
        try {
            Thread(runnable).start()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return runnable
    }

    override fun finish() {
        instance.removeActivity(this)
        super.finish()
    }

    @JvmOverloads
    fun showToast(text: String?, duration: Int = Toast.LENGTH_SHORT) {
        Toast.makeText(this, text, duration).show()
    }

    fun setStatusBarTextColor(dark: Boolean) {
        val decor = window.decorView
        if (dark) {
            decor.systemUiVisibility =
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        } else {
            decor.systemUiVisibility =
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        }
    }

    companion object {
        /**
         * 状态栏透明
         */
        @JvmStatic
        fun setStatusBarFullTransparent(window: Window) {
            //21表示5.0
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_LAYOUT_STABLE)
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.statusBarColor = Color.TRANSPARENT
        }
    }
}
