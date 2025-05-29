package com.zhouyu.pet_science.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.zhouyu.pet_science.R
import com.zhouyu.pet_science.activities.base.BaseActivity
import com.zhouyu.pet_science.fragments.PersonalCenterFragment

/**
 * 用户个人中心Activity
 * 用于显示个人中心页面，可以查看自己或他人的个人资料
 */
class UserProfileActivity : BaseActivity() {
    
    private var userId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_profile)
        
        // 获取传入的用户ID，-1表示查看自己的个人中心
        userId = intent.getIntExtra("userId", -1)
        
        // 加载PersonalCenterFragment
        loadFragment()
    }
    
    private fun loadFragment() {
        val fragment = PersonalCenterFragment.newInstance(userId)
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }
    
    companion object {
        /**
         * 创建查看用户个人中心的Intent
         * @param userId 要查看的用户ID，传-1表示查看自己的个人中心
         */
        fun createIntent(context: Context, userId: Int = -1): Intent {
            return Intent(context, UserProfileActivity::class.java).apply {
                putExtra("userId", userId)
            }
        }
    }
} 