package com.zhouyu.pet_science.fragments

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.annotation.SuppressLint
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.tabs.TabLayoutMediator
import com.zhouyu.pet_science.R
import com.zhouyu.pet_science.activities.AddressActivity
import com.zhouyu.pet_science.activities.MainActivity
import com.zhouyu.pet_science.activities.MyOrdersActivity
import com.zhouyu.pet_science.activities.UserInfoEditActivity
import com.zhouyu.pet_science.activities.UserProfileActivity
import com.zhouyu.pet_science.activities.base.BaseActivity
import com.zhouyu.pet_science.adapter.PetAdapter
import com.zhouyu.pet_science.databinding.FragmentPersonalCenterBinding
import com.zhouyu.pet_science.model.Pet
import com.zhouyu.pet_science.model.User
import com.zhouyu.pet_science.network.HttpUtils.BASE_URL
import com.zhouyu.pet_science.network.UserHttpUtils
import com.zhouyu.pet_science.utils.ConsoleUtils
import com.zhouyu.pet_science.utils.TimeUtils
import com.zhouyu.pet_science.utils.PhoneMessage

class PersonalCenterFragment : BaseFragment() {
    private var _binding: FragmentPersonalCenterBinding? = null
    private val binding get() = _binding!!

    private lateinit var petAdapter: PetAdapter
    private val petList = mutableListOf<Pet>()
    
    // 是否查看自己的个人中心
    private var isSelfProfile = true
    // 要查看的用户ID
    private var userId: Int = -1
    
    companion object {
        var refreshInfo = false
        var userInfo: User? = null
        var otherUserInfo: User? = null
        
        fun newInstance(userId: Int = -1): PersonalCenterFragment {
            val fragment = PersonalCenterFragment()
            val bundle = Bundle()
            bundle.putInt("userId", userId)
            fragment.arguments = bundle
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPersonalCenterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // 获取传入的userId参数
        userId = arguments?.getInt("userId", -1) ?: -1
        isSelfProfile = userId == -1
        
        setupViews()
        setupListeners()
        loadUserData()
    }
    
    override fun onResume() {
        super.onResume()
        if (refreshInfo && isSelfProfile) {
            loadUserData()
            refreshInfo = false
        }
    }

    private fun setupViews() {
        setTopBarView(binding.topBar,true)
        
        // 如果不是查看自己的个人中心，添加返回按钮
        if (!isSelfProfile) {
            binding.moreBtn.visibility = View.GONE
            binding.backBtn.visibility = View.VISIBLE
            // 返回按钮逻辑，可以根据实际情况调整
            binding.backBtn.setOnClickListener {
                requireActivity().finish()
            }
            // 显示关注按钮
            binding.btnFollow.visibility = View.VISIBLE

            // 隐藏查看别人主页不需要的视图
            binding.functionsContainer.visibility = View.GONE
            binding.btnEditProfile.visibility = View.GONE
            binding.tvAddPet.visibility = View.GONE
            binding.mutual.visibility = View.GONE
        }
        binding.tvPetTitle.text = if (isSelfProfile) "我的宠物" else "TA的宠物"

        // 设置宠物列表
        petAdapter = PetAdapter(requireContext(), petList)
        binding.rvPets.apply {
            layoutManager = LinearLayoutManager(context, RecyclerView.HORIZONTAL, false)
            adapter = petAdapter
        }
        
        // 设置ViewPager和TabLayout
        val tabTitles = listOf("发布", "点赞")
        val fragments = listOf(
            ContentGridFragment.newInstance(ContentGridFragment.TYPE_POSTS, userId),
            ContentGridFragment.newInstance(ContentGridFragment.TYPE_LIKES, userId)
        )
        
        binding.viewPager.adapter = object : FragmentStateAdapter(this) {
            override fun getItemCount(): Int = fragments.size
            override fun createFragment(position: Int): Fragment = fragments[position]
        }
        
        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = tabTitles[position]
        }.attach()
    }
    
    @SuppressLint("ResourceType")
    private fun setupListeners() {
        if(isSelfProfile){  // 查看自己的个人中心
            binding.moreBtn.setOnClickListener{
                (activity as MainActivity).drawerLayout!!.openDrawer(GravityCompat.END)
            }

            // 编辑资料按钮
            binding.btnEditProfile.setOnClickListener {
                val intent = Intent(requireContext(), UserInfoEditActivity::class.java)
                intent.putExtra("type", "editUserInfo")
                UserInfoEditActivity.putUserInfo = userInfo
                startActivity(intent)
            }

            // 添加宠物按钮
            binding.tvAddPet.setOnClickListener {
                val intent = Intent(requireContext(), UserInfoEditActivity::class.java)
                intent.putExtra("type", "editPetInfo")
                intent.putExtra("petId", -1) // -1表示添加新宠物
                startActivity(intent)
            }

            //我的订单
            binding.layoutMyOrders.setOnClickListener{
                startActivity(Intent(requireActivity(), MyOrdersActivity::class.java))
            }

            //地址管理
            binding.layoutAddress.setOnClickListener{
                startActivity(Intent(requireActivity(), AddressActivity::class.java))
            }
        }else{
            // 关注按钮点击事件
            binding.btnFollow.setOnClickListener {
                if (isSelfProfile) return@setOnClickListener

                toggleFollowUser()
            }

        }


        binding.nestedScrollView.setOnScrollChangeListener { _, _, scrollY, _, _ ->
            val threshold = binding.ivCover.height - binding.topBar.height
            // 计算透明度百分比，范围从0到1
            val alphaPercent = ((scrollY.toFloat() - PhoneMessage.dpToPx(80f))
                    / PhoneMessage.dpToPx(80f)) //计算范围大小
                .coerceIn(0f, 1f)
            // 设置背景颜色的透明度
            val viewColor = requireContext().getColor(R.color.viewColor)
            val topBarAlpha = (255 * alphaPercent).toInt()
            val color = Color.argb(topBarAlpha, Color.red(viewColor), Color.green(viewColor), Color.blue(viewColor))
            binding.topBar.setBackgroundColor(color)

            val topAnimView = if (isSelfProfile) binding.moreBtn else binding.backBtn
            // 处理顶部用户信息的显示和隐藏
            if (scrollY > threshold) {
                if(topUserInfoHide){

                    // 设置顶部用户信息动画
                    binding.topUserInfo.apply {
                        animate().cancel()
                        visibility = View.VISIBLE; alpha = 0f; translationY = -20f
                        animate()
                            .alpha(1f)
                            .translationY(0f)
                            .setDuration(300)
                            .setListener(object : AnimatorListenerAdapter() {
                                override fun onAnimationEnd(animation: Animator) {}
                            }) //动画持续时间 毫秒单位
                    }

                    topAnimView.animate()
                        .setDuration(300)
                        .alpha(1f)
                        .withStartAction {
                            topAnimView.setBackgroundResource(android.R.color.transparent)
                            topAnimView.imageTintList = ColorStateList.valueOf(Color.BLACK)
                        }
                        .start()


                    BaseActivity.setStatusBarTextColor(true,requireActivity().window)
                    topUserInfoHide = false
                }
            } else {
                if(!topUserInfoHide){
                    binding.topUserInfo.apply {
                        animate().cancel();visibility = View.VISIBLE;alpha = 1f;translationY = 0f
                        animate().alpha(0f).translationY(-20f).setDuration(200) //动画持续时间 毫秒单位
                            .setListener(object : AnimatorListenerAdapter() {
                                override fun onAnimationEnd(animation: Animator) {
                                    visibility = View.INVISIBLE
                                }
                            })
                    }

                    topAnimView.animate()
                        .setDuration(300)
                        .alpha(1f)
                        .withStartAction {
                            topAnimView.setBackgroundResource(R.drawable.view_semicircle)
                            topAnimView.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#73000000"))
                            topAnimView.imageTintList = ColorStateList.valueOf(requireContext().getColor(R.color.viewColor))
                        }
                        .start()

                    BaseActivity.setStatusBarTextColor(false,requireActivity().window)
                    topUserInfoHide = true
                }
            }
        }
    }
    
    private var topUserInfoHide: Boolean = true
    
    private fun toggleFollowUser() {
        executeThread {
            try {
                val isFollowed = otherUserInfo?.isFollowed ?: false
                if (isFollowed) {
                    // 取消关注
                    UserHttpUtils.unfollowUser(userId)
                } else {
                    // 关注用户
                    UserHttpUtils.followUser(userId)
                }
                
                // 重新加载用户信息以更新关注状态
                loadUserData()
            } catch (e: Exception) {
                e.printStackTrace()
                activity?.runOnUiThread {
                    Toast.makeText(requireContext(), "操作失败，请重试", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    
    @SuppressLint("SetTextI18n")
    private fun loadUserData() {
        executeThread {
            try {
                val user = if (isSelfProfile) {
                    userInfo = UserHttpUtils.getUserInfo()
                    userInfo
                } else {
                    otherUserInfo = UserHttpUtils.getUserInfoById(userId)
                    otherUserInfo
                }
                
                activity?.runOnUiThread {
                    user?.apply {
                        // 更新用户信息
                        binding.tvNickname.text = nickname
                        binding.topTitle.text = nickname
                        
                        // 只在查看自己的个人中心时更新抽屉菜单的信息
                        if (isSelfProfile && activity is MainActivity) {
                            val mainActivity = activity as MainActivity
                            mainActivity.findViewById<TextView>(R.id.left_view_username).text = nickname
                        }

                        binding.tvBio.text = bio.ifEmpty { "这个人很懒，什么都没留下" }

                        binding.genderView.visibility = View.VISIBLE
                        binding.tvGender.text = when(gender){
                            0 -> "男"
                            1 -> "女"
                            else -> {
                                // 保密
                                binding.genderView.visibility = View.GONE
                                ""
                            }
                        }

                        binding.ageView.visibility = View.VISIBLE
                        if(birthday.time == 0L){
                            binding.tvAge.text = ""
                            binding.ageView.visibility = View.GONE
                        }else{
                            val age = TimeUtils.calculateAge(birthday.time)
                            binding.tvAge.text = "$age 岁"
                        }

                        binding.locationView.visibility = View.VISIBLE
                        binding.tvLocation.text = when(location){
                            "null", "" -> {
                                binding.locationView.visibility = View.GONE
                                ""
                            }
                            else -> location
                        }

                        //萌宠号
                        binding.tvPetId.text = "萌宠号：${username}"

                        // 更新统计数据
                        binding.mutualCount.text = mutualCount.toString()
                        binding.tvFollowingCount.text = followCount.toString()
                        binding.tvFollowersCount.text = fansCount.toString()

                        // 更新关注按钮状态（仅在查看他人个人中心时）
                        if (!isSelfProfile) {
                            updateFollowButton(isFollowed)
                        }

                        val loadUrl = BASE_URL + avatarUrl
                        // 加载头像
                        Glide.with(this@PersonalCenterFragment)
                            .load(loadUrl)
                            .apply(RequestOptions())
                            .transform(CircleCrop())
                            .into(binding.ivAvatar)
                        Glide.with(this@PersonalCenterFragment)
                            .load(loadUrl)
                            .apply(RequestOptions())
                            .transform(CircleCrop())
                            .into(binding.topAvatar)
                            
                        // 只在查看自己的个人中心时更新抽屉菜单的头像
                        if (isSelfProfile && activity is MainActivity) {
                            val mainActivity = activity as MainActivity
                            Glide.with(this@PersonalCenterFragment)
                                .load(loadUrl)
                                .apply(RequestOptions())
                                .transform(CircleCrop())
                                .into(mainActivity.findViewById(R.id.left_view_user_avatar))
                        }

                        // 更新宠物列表
                        updatePetList(pets)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                activity?.runOnUiThread {
                    Toast.makeText(requireContext(), "加载用户数据失败", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    
    private fun updateFollowButton(isFollowed: Boolean) {
        binding.btnFollow.apply {
            if (isFollowed) {
                text = "取消关注"
                backgroundTintList = ColorStateList.valueOf(Color.LTGRAY)
            } else {
                text = "关注"
                backgroundTintList = ColorStateList.valueOf(requireContext().getColor(R.color.Theme))
            }
        }
    }
    
    @SuppressLint("NotifyDataSetChanged")
    private fun updatePetList(pets: List<Pet>) {
        petList.clear()
        petList.addAll(pets)
        petAdapter.notifyDataSetChanged()
        if(petList.isEmpty()){
            binding.tvEmptyPetTip.visibility = View.VISIBLE
            // 如果是自己的个人中心，显示添加宠物的提示，否则显示用户没有宠物的提示
            binding.tvEmptyPetTip.text = if (isSelfProfile) "还没有您宠物信息，快去添加吧～" else "该用户还没有添加宠物～"
        }else{
            binding.tvEmptyPetTip.visibility = View.GONE
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
