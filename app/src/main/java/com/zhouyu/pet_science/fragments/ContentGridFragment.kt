package com.zhouyu.pet_science.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.zhouyu.pet_science.R
import com.zhouyu.pet_science.adapter.ContentAdapter
import com.zhouyu.pet_science.model.Content
import com.zhouyu.pet_science.network.ContentHttpUtils

class ContentGridFragment : Fragment() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var contentAdapter: ContentAdapter
    private val contentList = mutableListOf<Content>()
    private var contentType: Int = TYPE_POSTS

    companion object {
        const val TYPE_POSTS = 0
        const val TYPE_LIKES = 1
        private const val ARG_TYPE = "arg_type"

        fun newInstance(type: Int): ContentGridFragment {
            val fragment = ContentGridFragment()
            val args = Bundle()
            args.putInt(ARG_TYPE, type)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            contentType = it.getInt(ARG_TYPE, TYPE_POSTS)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_content_grid, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        recyclerView = view.findViewById(R.id.recyclerView)
        setupRecyclerView()
        loadContent()
    }

    private fun setupRecyclerView() {
        contentAdapter = ContentAdapter(requireContext(), contentList)
        recyclerView.apply {
            layoutManager = GridLayoutManager(context, 3)
            adapter = contentAdapter
        }
    }

    private fun loadContent() {
        // 在实际应用中，这里应该从网络加载数据
        Thread {
            try {
                val contents = if (contentType == TYPE_POSTS) {
                    ContentHttpUtils.getUserPosts()
                } else {
                    ContentHttpUtils.getUserLikes()
                }
                
                activity?.runOnUiThread {
                    if (contents != null) {
                        contentList.clear()
                        contentList.addAll(contents)
                        contentAdapter.notifyDataSetChanged()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                activity?.runOnUiThread {
                    Toast.makeText(
                        requireContext(),
                        if (contentType == TYPE_POSTS) "加载发布内容失败" else "加载点赞内容失败",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }.start()
    }
}