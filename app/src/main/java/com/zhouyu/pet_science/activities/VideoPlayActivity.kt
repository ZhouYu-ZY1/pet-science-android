package com.zhouyu.pet_science.activities

import android.os.Bundle
import com.zhouyu.pet_science.R
import com.zhouyu.pet_science.activities.base.BaseActivity
import com.zhouyu.pet_science.fragments.VideoPlayFragment
import com.zhouyu.pet_science.pojo.Video

class VideoPlayActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        setContentView(R.layout.activity_video_play)
        super.onCreate(savedInstanceState)
        setStatusBarTextColor(false, window)
        setTopBarView(findViewById(R.id.main), true)
        val videoPlayFragment = VideoPlayFragment(this).apply {
            arguments = Bundle().apply {
                putString("listType", "user")
            }
        }

        val fragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.fragment_view, videoPlayFragment)
        fragmentTransaction.commit()
    }

    override fun onDestroy() {
        position = 0
        super.onDestroy()
    }

    companion object {
        var videoList: List<Video.Data>? = null
        var position = 0
    }
}