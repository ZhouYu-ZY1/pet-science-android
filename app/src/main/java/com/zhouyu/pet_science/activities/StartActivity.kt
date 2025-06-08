package com.zhouyu.pet_science.activities

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.WindowManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.scwang.smart.refresh.footer.ClassicsFooter
import com.scwang.smart.refresh.header.ClassicsHeader
import com.zhouyu.pet_science.R
import com.zhouyu.pet_science.activities.base.BaseActivity
import com.zhouyu.pet_science.databinding.ActivityStartBinding
import com.zhouyu.pet_science.fragments.MessageFragment
import com.zhouyu.pet_science.pojo.MessageListItem
import com.zhouyu.pet_science.utils.MessageArrayList
import com.zhouyu.pet_science.utils.StorageUtils
import com.zhouyu.pet_science.utils.Tool
import com.zhouyu.pet_science.utils.ConsoleUtils


class StartActivity : BaseActivity() {
    private lateinit var binding: ActivityStartBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStartBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //进入全屏
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            //适配刘海屏水滴屏，扩展到状态栏
            Tool.fullScreen(this)
        } else {
            window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
        }

        // 设置启动页的背景图片
        Glide.with(this)
            .load(R.mipmap.start_background)
            .centerCrop()
            .skipMemoryCache(true) // 禁用内存缓存
            .diskCacheStrategy(DiskCacheStrategy.NONE) // 禁用磁盘缓存
            .into(binding.backImage)

        executeThread{
            // 初始化刷新组件
            initSmartRefreshLayout()

            // 加载数据
            val messageList = MessageArrayList.loadList(this)
            if(!messageList.isNullOrEmpty()){
                // 遍历列表
                for (item in messageList) {
                    ConsoleUtils.logErr(item.lastMessage)
                }
                MessageFragment.setMessageList(messageList as ArrayList<MessageListItem>)
            }

            val token = StorageUtils.get<String>("token")
            Handler(Looper.getMainLooper()).postDelayed({
                val intent = if(token != null && token.isNotEmpty()){
                    Intent(this, MainActivity::class.java)
                }else{
                    Intent(this, LoginActivity::class.java)
                }
                startActivity(intent)
                finish()  // 添加这行，关闭启动页
            }, 1000)
        }
    }

    private fun initSmartRefreshLayout() {
        ClassicsHeader.REFRESH_HEADER_PULLING = getString(R.string.header_pulling);//"下拉可以刷新";
        ClassicsHeader.REFRESH_HEADER_REFRESHING = getString(R.string.header_refreshing);//"正在刷新...";
        ClassicsHeader.REFRESH_HEADER_LOADING = getString(R.string.header_loading);//"正在加载...";
        ClassicsHeader.REFRESH_HEADER_RELEASE = getString(R.string.header_release);//"释放立即刷新";
        ClassicsHeader.REFRESH_HEADER_FINISH = getString(R.string.header_finish);//"刷新完成";
        ClassicsHeader.REFRESH_HEADER_FAILED = getString(R.string.header_failed);//"刷新失败";
        ClassicsHeader.REFRESH_HEADER_UPDATE = getString(R.string.header_update);//"上次更新 M-d HH:mm";
        ClassicsHeader.REFRESH_HEADER_UPDATE = getString(R.string.header_update);//"'Last update' M-d HH:mm";
        ClassicsHeader.REFRESH_HEADER_SECONDARY = getString(R.string.header_secondary);//"释放进入二楼"

        ClassicsFooter.REFRESH_FOOTER_PULLING = getString(R.string.footer_pulling);//"上拉加载更多";
        ClassicsFooter.REFRESH_FOOTER_RELEASE = getString(R.string.footer_release);//"释放立即加载";
        ClassicsFooter.REFRESH_FOOTER_LOADING = getString(R.string.footer_loading);//"正在刷新...";
        ClassicsFooter.REFRESH_FOOTER_REFRESHING = getString(R.string.footer_refreshing);//"正在加载...";
        ClassicsFooter.REFRESH_FOOTER_FINISH = getString(R.string.footer_finish);//"加载完成";
        ClassicsFooter.REFRESH_FOOTER_FAILED = getString(R.string.footer_failed);//"加载失败";
        ClassicsFooter.REFRESH_FOOTER_NOTHING = getString(R.string.footer_nothing);//"全部加载完成";

//        //启用矢量图兼容
//        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
//        //设置全局默认配置（优先级最低，会被其他设置覆盖）
//        SmartRefreshLayout.setDefaultRefreshInitializer { _, layout -> //全局设置（优先级最低）
//            layout.setEnableAutoLoadMore(true)
//            layout.setEnableOverScrollDrag(false)
//            layout.setEnableOverScrollBounce(true)
//            layout.setEnableLoadMoreWhenContentNotFull(true)
//            layout.setEnableScrollContentWhenRefreshed(true)
//            layout.setPrimaryColorsId(R.color.colorPrimary, android.R.color.white)
//            layout.setFooterMaxDragRate(4.0f)
//            layout.setFooterHeight(45f)
//        }
    }
}