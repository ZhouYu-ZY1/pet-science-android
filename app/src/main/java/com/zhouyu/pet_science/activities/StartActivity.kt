package com.zhouyu.pet_science.activities

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import com.zhouyu.pet_science.R
import com.zhouyu.pet_science.activities.base.BaseActivity

class StartActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_start)
        Handler().postDelayed({
            startActivity(
                Intent(
                    this@StartActivity,
                    MainActivity::class.java
                )
            )
        }, 1000)
    }
}