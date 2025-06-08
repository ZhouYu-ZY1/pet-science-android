package com.zhouyu.pet_science.activities

import android.os.Bundle
import com.zhouyu.pet_science.R
import com.zhouyu.pet_science.activities.base.BaseActivity
import com.zhouyu.pet_science.databinding.ActivityVideoPlayBinding
import com.zhouyu.pet_science.fragments.VideoPlayFragment
import com.zhouyu.pet_science.pojo.Video

class VideoPlayActivity : BaseActivity() {
    private lateinit var binding: ActivityVideoPlayBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVideoPlayBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setStatusBarTextColor(false, window)
        setTopBarView(binding.main, true)
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