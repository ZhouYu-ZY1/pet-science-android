package com.zhouyu.pet_science.activities

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.WindowManager
import com.bumptech.glide.Glide
import com.zhouyu.pet_science.R
import com.zhouyu.pet_science.activities.base.BaseActivity
import com.zhouyu.pet_science.fragments.MessageFragment
import com.zhouyu.pet_science.pojo.MessageListItem
import com.zhouyu.pet_science.tools.MessageArrayList
import com.zhouyu.pet_science.tools.StorageTool
import com.zhouyu.pet_science.tools.Tool
import com.zhouyu.pet_science.tools.utils.ConsoleUtils

class StartActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_start)

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
            .into(findViewById(R.id.back_image))

        executeThread{
            // 加载数据
            val messageList = MessageArrayList.loadList(this)
            if(!messageList.isNullOrEmpty()){
                // 遍历列表
                for (item in messageList) {
                    ConsoleUtils.logErr(item.lastMessage)
                }
                MessageFragment.setMessageList(messageList as ArrayList<MessageListItem>)
            }

            val token = StorageTool.get<String>("token")
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
}