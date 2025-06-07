package com.zhouyu.pet_science.activities

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
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
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.drawerlayout.widget.DrawerLayout.DrawerListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.ViewPager.OnPageChangeListener
import com.zhouyu.pet_science.R
import com.zhouyu.pet_science.activities.base.BaseActivity
import com.zhouyu.pet_science.application.WebSocketManager
import com.zhouyu.pet_science.databinding.ActivityMainBinding
import com.zhouyu.pet_science.fragments.MessageFragment
import com.zhouyu.pet_science.fragments.PersonalCenterFragment
import com.zhouyu.pet_science.fragments.VideoPlayFragment
import com.zhouyu.pet_science.fragments.shop.ShopFragment
import com.zhouyu.pet_science.manager.ActivityManager
import com.zhouyu.pet_science.utils.CleanCacheUtils
import com.zhouyu.pet_science.utils.ConsoleUtils
import com.zhouyu.pet_science.utils.NotificationHelper
import com.zhouyu.pet_science.utils.PhoneMessage
import com.zhouyu.pet_science.utils.StorageUtils
import com.zhouyu.pet_science.views.CustomViewPager
import com.zhouyu.pet_science.views.dialog.MyDialog
import me.ibrahimsn.lib.OnItemSelectedListener

class MainActivity : BaseActivity() {
    private var fragmentList: ArrayList<Fragment>? = null
    private var viewPager: CustomViewPager? = null
    var drawerLayout: DrawerLayout? = null
    private lateinit var binding: ActivityMainBinding

    @SuppressLint("RtlHardcoded")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

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

        // 连接WebSocket
        WebSocketManager.connectWebSocket(this)

        // 获取通知权限
        NotificationHelper.getNotification(this)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        ConsoleUtils.logErr("onNewIntent")
        if (intent.getBooleanExtra(OPEN_CHAT_ACTIVITY, false)) {
            val startIntent = Intent(this, ChatActivity::class.java)
            val extras = intent.extras
            if (extras != null) {
                startIntent.putExtras(extras)
            }
            startActivity(startIntent)
        }
    }

    @SuppressLint("RtlHardcoded")
    private fun initViews() {
//        setTopBarView(binding.mainViewPager, true)
        viewPager = binding.mainViewPager
        fragmentList = ArrayList()
        fragmentList!!.let {
            it.add(ShopFragment())
            it.add(VideoPlayFragment())
            it.add(MessageFragment())
            it.add(PersonalCenterFragment())
        }

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
        val cutOffLineView: View = findViewById(R.id.cutOffLineView)
        if(fragmentList!![0] is VideoPlayFragment){
            binding.mainBottomBar.barBackgroundColor = getColor(R.color.black)
            cutOffLineView.setBackgroundColor(getColor(R.color.black))
        }
        viewPager!!.addOnPageChangeListener(object : OnPageChangeListener {
            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {
            }

            override fun onPageSelected(position: Int) {
                binding.mainBottomBar.itemActiveIndex = position

                // 设置状态栏文字颜色
                val fragment = fragmentList!![position]
                when (fragment) {
                    is ShopFragment -> {
                        binding.mainBottomBar.barBackgroundColor = getColor(R.color.white)
                        cutOffLineView.setBackgroundColor(getColor(R.color.cutOffLine))
                        setStatusBarTextColor(true, window)
                    }
                    is MessageFragment -> {
                        binding.mainBottomBar.barBackgroundColor = getColor(R.color.white)
                        cutOffLineView.setBackgroundColor(getColor(R.color.cutOffLine))
                        setStatusBarTextColor(true, window)
                    }
                    is PersonalCenterFragment -> {
                        binding.mainBottomBar.barBackgroundColor = getColor(R.color.white)
                        cutOffLineView.setBackgroundColor(getColor(R.color.cutOffLine))
                        setStatusBarTextColor(false, window)
                    }
                    is VideoPlayFragment -> {
                        binding.mainBottomBar.barBackgroundColor = getColor(R.color.black)
                        cutOffLineView.setBackgroundColor(getColor(R.color.black))
                        setStatusBarTextColor(false, window)
                    }
                }
            }

            override fun onPageScrollStateChanged(state: Int) {}
        })
        
        binding.mainBottomBar.onItemSelectedListener = object : OnItemSelectedListener {
            override fun onItemSelect(pos: Int): Boolean {
                viewPager!!.setCurrentItem(pos)
                return false
            }
        }
        val leftView = binding.mainLeftView
        leftView.setPadding(0, PhoneMessage.statusBarHeight, 0, 0)
        leftView.setOnClickListener { } //防止点击到底部组件

        val leftViewLayoutParams = leftView.layoutParams
        // 设置宽度为屏幕宽度的 80%
        leftViewLayoutParams.width = (PhoneMessage.widthPixels * 0.8).toInt()
        leftView.layoutParams = leftViewLayoutParams
        drawerLayout = binding.mainDrawerLayout
        drawerLayout!!.addDrawerListener(object : DrawerListener {
            override fun onDrawerSlide(drawerView: View, slideOffset: Float) {
                // 实现整个 Activity 左移效果
                // 当侧滑菜单滑动时，移动主内容
                val content = drawerLayout!!.getChildAt(0)
                val distance = drawerView.width * slideOffset
                content.translationX = -distance
            }

            private var isChange = false
            override fun onDrawerOpened(drawerView: View) {
                isChange = false
                if(!isStatusBarDark){
                    setStatusBarTextColor(true, window)
                    isChange = true
                }
            }
            override fun onDrawerClosed(drawerView: View) {
                if(isChange){
                    setStatusBarTextColor(false, window)
                }
            }
            override fun onDrawerStateChanged(newState: Int) {}
        })
        StorageUtils.put("noFirstOpenAPP", true)
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

        //退出登录
        findViewById<View>(R.id.quit_login).setOnClickListener{
            StorageUtils.delete("token")
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        //我的订单
        findViewById<View>(R.id.layoutMyOrders).setOnClickListener{
            startActivity(Intent(this, MyOrdersActivity::class.java))
        }

        //地址管理
        findViewById<View>(R.id.layoutAddress).setOnClickListener{
            startActivity(Intent(this, AddressActivity::class.java))
        }
    }

    private val cleanCacheTool = CleanCacheUtils.instance
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
        if (drawerLayout!!.isDrawerVisible(GravityCompat.END)) {
            drawerLayout!!.closeDrawer(GravityCompat.END)
            return
        }
        if (isRemove) {
            WebSocketManager.closeWebSocket()
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
        const val OPEN_CHAT_ACTIVITY = "OPEN_CHAT_ACTIVITY"
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
                StorageUtils.put("isAgreeStatement", true)
            }
            myDialog.setNoOnclickListener("不同意") {
                myDialog.dismiss()
                isAgreeStatement = false
                StorageUtils.put("isAgreeStatement", false)
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