package com.zhouyu.pet_science.activities

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.text.Html
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.TextPaint
import android.text.style.ClickableSpan
import android.text.style.UnderlineSpan
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.drawerlayout.widget.DrawerLayout
import androidx.drawerlayout.widget.DrawerLayout.DrawerListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.ViewPager.OnPageChangeListener
import com.zhouyu.pet_science.R
import com.zhouyu.pet_science.activities.base.BaseActivity
import com.zhouyu.pet_science.fragments.shop.ShopFragment
import com.zhouyu.pet_science.manager.ActivityManager
import com.zhouyu.pet_science.tools.CleanCacheTool
import com.zhouyu.pet_science.tools.StorageTool
import com.zhouyu.pet_science.tools.utils.PhoneMessage
import com.zhouyu.pet_science.views.CustomViewPager
import com.zhouyu.pet_science.views.dialog.MyDialog
import me.ibrahimsn.lib.OnItemSelectedListener
import me.ibrahimsn.lib.SmoothBottomBar

class MainActivity : BaseActivity() {
    private var fragmentList: ArrayList<Fragment>? = null
    private var viewPager: CustomViewPager? = null
    private var smoothBottomBar: SmoothBottomBar? = null
    var drawerLayout: DrawerLayout? = null

    @SuppressLint("RtlHardcoded")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 初始化布局元素
        initViews()

        initLeftView()

//        Handler(Looper.getMainLooper()).postDelayed({
//            //提示语
//            TimeGreetings().showGreetings()
//        }, 500)

        // 是否同意使用协议
//        if(!isAgreeStatement){
//            showStatementDialog(this);
//        }
    }

    @SuppressLint("RtlHardcoded")
    private fun initViews() {
        setTopBarView(findViewById(R.id.main_view_pager), true)
        viewPager = findViewById(R.id.main_view_pager)
        fragmentList = ArrayList()
        fragmentList!!.add(ShopFragment())
        fragmentList!!.add(ShopFragment())
        fragmentList!!.add(ShopFragment())
        fragmentList!!.add(ShopFragment())

        //设置预加载页数
        viewPager!!.setOffscreenPageLimit(fragmentList!!.size)
        viewPager!!.setAdapter(object :
            FragmentPagerAdapter(supportFragmentManager, BEHAVIOR_SET_USER_VISIBLE_HINT) {
            override fun getItem(i: Int): Fragment {
                return fragmentList!![i]
            }

            override fun getCount(): Int {
                return fragmentList!!.size
            }
        })
        viewPager!!.addOnPageChangeListener(object : OnPageChangeListener {
            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {
            }

            override fun onPageSelected(position: Int) {
                if (smoothBottomBar != null) {
                    smoothBottomBar!!.itemActiveIndex = position
                }
                viewPager!!.setCurrentItem(position)
            }

            override fun onPageScrollStateChanged(state: Int) {}
        })

        smoothBottomBar = findViewById(R.id.main_bottomBar)
        smoothBottomBar!!.onItemSelectedListener = object : OnItemSelectedListener {
            override fun onItemSelect(pos: Int): Boolean {
                viewPager!!.setCurrentItem(pos)
                return false
            }
        }
        val leftView = findViewById<LinearLayout>(R.id.main_left_view)
        leftView.setPadding(0, PhoneMessage.statusBarHeight, 0, 0)
        leftView.setOnClickListener { } //防止点击到底部组件

        val leftViewLayoutParams = leftView.layoutParams
        // 设置宽度为屏幕宽度的 80%
        leftViewLayoutParams.width = (PhoneMessage.getWidthPixels() * 0.8).toInt()
        leftView.layoutParams = leftViewLayoutParams
        drawerLayout = findViewById(R.id.main_drawer_layout)
        drawerLayout!!.addDrawerListener(object : DrawerListener {
            override fun onDrawerSlide(drawerView: View, slideOffset: Float) {
                // 实现整个 Activity 左移效果
                // 当侧滑菜单滑动时，移动主内容
                val content = drawerLayout!!.getChildAt(0)
                val distance = drawerView.width * slideOffset
                content.translationX = distance
            }

            override fun onDrawerOpened(drawerView: View) {}
            override fun onDrawerClosed(drawerView: View) {}
            override fun onDrawerStateChanged(newState: Int) {}
        })
        StorageTool.put("noFirstOpenAPP", true)
    }

    @SuppressLint("RtlHardcoded", "SetTextI18n")
    private fun initLeftView() {
        cacheSize = findViewById(R.id.cache_size)

        //清除缓存
        findViewById<View>(R.id.clear_cache).setOnClickListener {
            calculateCacheSize()
            cleanCacheTool.showDialog(this, cacheSize)
        }
        calculateCacheSize()
    }

    private val cleanCacheTool = CleanCacheTool.getInstance()
    private var cacheSize: TextView? = null

    /**
     * 计算缓存大小
     */
    @SuppressLint("SetTextI18n", "DefaultLocale")
    fun calculateCacheSize() {
        cleanCacheTool.calculateCacheSize(cacheSize)
    }

    private var touchTime: Long = 0

    @SuppressLint("WrongConstant", "ShowToast", "RtlHardcoded")
    override fun finish() {
        if (drawerLayout!!.isOpen) {
            drawerLayout!!.closeDrawer(Gravity.LEFT)
            return
        }
        if (isRemove) {
            super.finish()
            return
        }
        //返回时提示再按一次退出程序
        val currentTime = System.currentTimeMillis()
        //等待的时间
        if (currentTime - touchTime >= 1000L) {
            //让Toast的显示时间和等待时间相同
            Toast.makeText(this, "再按一次退出程序", Toast.LENGTH_SHORT).show()
            touchTime = currentTime
        } else {
            moveTaskToBack(true)
        }
    }

    companion object {
        private var isAgreeStatement = false
        fun showStatementDialog(context: Context) {
            val myDialog = MyDialog(context, true)
            val appName = context.getString(R.string.app_name)
            myDialog.setTitle(appName)
            myDialog.messageGravity = Gravity.START
            myDialog.setCanceledOnTouchOutside(false)
            val spannableStringBuilder = SpannableStringBuilder()
            spannableStringBuilder.append("本应用尊重并保护所有用户的个人隐私权。根据相关政策规定，在使用“")
                .append(appName).append("”之前，您需要认真阅读")

            //隐私政策点击
            val useRules: ClickableSpan = object : ClickableSpan() {
                override fun onClick(widget: View) {
//                Intent intent = new Intent(context, WebBrowserActivity.class);
//                intent.putExtra("url","https://docs.qq.com/doc/p/991885d9fcf3006fa10dd1f640e2f9d5bd296fcf");
//                context.startActivity(intent);
                }

                override fun updateDrawState(ds: TextPaint) {}
            }
            val useRulesSpan =
                SpannableString(Html.fromHtml("<font color=\"#66C1FB\">《" + appName + "软件使用条例》</font>"))
            useRulesSpan.setSpan(
                useRules,
                0,
                useRulesSpan.length,
                Spanned.SPAN_INCLUSIVE_EXCLUSIVE
            ) //设置点击事件
            useRulesSpan.setSpan(
                UnderlineSpan(),
                0,
                useRulesSpan.length,
                Spanned.SPAN_INCLUSIVE_EXCLUSIVE
            ) //设置下划线
            spannableStringBuilder.append(useRulesSpan)
            spannableStringBuilder.append("和")
            //隐私政策点击
            val privacyClickablespan: ClickableSpan = object : ClickableSpan() {
                override fun onClick(widget: View) {
//                Intent intent = new Intent(context, WebBrowserActivity.class);
//                intent.putExtra("url","https://docs.qq.com/doc/p/9b764cc0d3017c94a154f08d2bffc32854680dd1");
//                context.startActivity(intent);
                }

                override fun updateDrawState(ds: TextPaint) {
                    // 可选：改变点击文本的外观，如颜色
//                ds.setColor(Color.BLUE);
//                ds.setUnderlineText(true); // 某些情况下需要手动添加下划线，但通常不是必须的
                }
            }
            val privacyPolicySpan =
                SpannableString(Html.fromHtml("<font color=\"#66C1FB\">《" + appName + "隐私政策》</font>"))
            privacyPolicySpan.setSpan(
                privacyClickablespan,
                0,
                privacyPolicySpan.length,
                Spanned.SPAN_INCLUSIVE_EXCLUSIVE
            ) //设置点击事件
            privacyPolicySpan.setSpan(
                UnderlineSpan(),
                0,
                privacyPolicySpan.length,
                Spanned.SPAN_INCLUSIVE_EXCLUSIVE
            ) //设置下划线
            spannableStringBuilder.append(privacyPolicySpan)
            spannableStringBuilder.append("，您同意并接受全部条款后方可开始使用“").append(appName)
                .append("”。\n\n")
            spannableStringBuilder.append("为了保证软件的正常运行，软件可能会请求使用以下权限")
            myDialog.setMessage(spannableStringBuilder)
            myDialog.setYesOnclickListener("我同意") {
                myDialog.dismiss()
                isAgreeStatement = true
                StorageTool.put("isAgreeStatement", true)
            }
            myDialog.setNoOnclickListener("不同意") {
                myDialog.dismiss()
                isAgreeStatement = false
                StorageTool.put("isAgreeStatement", false)
            }
            myDialog.setOnDismissListener {
                if (!isAgreeStatement) {
                    ActivityManager.instance.finishApplication()
                }
            }
            myDialog.show()
        }

        @JvmField
        var isRemove = false
    }
}