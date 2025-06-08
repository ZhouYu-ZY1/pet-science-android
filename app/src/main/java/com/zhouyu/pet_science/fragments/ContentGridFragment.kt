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
import com.zhouyu.pet_science.databinding.FragmentContentGridBinding
import com.zhouyu.pet_science.model.Content
import com.zhouyu.pet_science.network.ContentHttpUtils

class ContentGridFragment : Fragment() {
    private var _binding: FragmentContentGridBinding? = null
    private val binding get() = _binding!!
    private lateinit var contentAdapter: ContentAdapter
    private val contentList = mutableListOf<Content>()
    private var contentType: Int = TYPE_POSTS
    private var userId: Int = -1  // -1表示查看自己的内容

    companion object {
        const val TYPE_POSTS = 0
        const val TYPE_LIKES = 1
        private const val ARG_TYPE = "arg_type"
        private const val ARG_USER_ID = "arg_user_id"

        fun newInstance(type: Int, userId: Int = -1): ContentGridFragment {
            val fragment = ContentGridFragment()
            val args = Bundle()
            args.putInt(ARG_TYPE, type)
            args.putInt(ARG_USER_ID, userId)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            contentType = it.getInt(ARG_TYPE, TYPE_POSTS)
            userId = it.getInt(ARG_USER_ID, -1)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentContentGridBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        loadContent()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setupRecyclerView() {
        contentAdapter = ContentAdapter(requireContext(), contentList)
        binding.recyclerView.apply {
            layoutManager = GridLayoutManager(context, 3)
            adapter = contentAdapter
        }
    }

    private fun loadContent() {
        // 在实际应用中，这里应该从网络加载数据
        Thread {
            try {
                val contents = if (userId == -1) {
                    // 查看自己的内容
                    if (contentType == TYPE_POSTS) {
                        ContentHttpUtils.getUserPosts()
                    } else {
                        ContentHttpUtils.getUserLikes()
                    }
                } else {
                    // 查看其他用户的内容
                    if (contentType == TYPE_POSTS) {
                        ContentHttpUtils.getUserPostsById(userId)
                    } else {
                        ContentHttpUtils.getUserLikesById(userId)
                    }
                }
                
                activity?.runOnUiThread {
                    if (contents != null) {
                        contentList.clear()
                        contentList.addAll(contents)
                        contentAdapter.notifyDataSetChanged()
                        
                        // 显示空内容提示
                        if (contentList.isEmpty()) {
                            binding.tvEmpty.visibility = View.VISIBLE
                        } else {
                            binding.tvEmpty.visibility = View.GONE
                        }
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