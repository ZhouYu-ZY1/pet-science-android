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
    
    companion object {
        var refreshInfo = false
        var userInfo: User? = null
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
        
        setupViews()
        setupListeners()
        loadUserData()
    }
    
    override fun onResume() {
        super.onResume()
        if (refreshInfo) {
            loadUserData()
            refreshInfo = false
        }
    }

    private fun setupViews() {
        binding.moreBtn.setOnClickListener{
            (activity as MainActivity).drawerLayout!!.openDrawer(GravityCompat.END)
        }
        setTopBarView(binding.topBar,  true)

        // 设置宠物列表
        petAdapter = PetAdapter(requireContext(), petList)
        binding.rvPets.apply {
            layoutManager = LinearLayoutManager(context, RecyclerView.HORIZONTAL, false)
            adapter = petAdapter
        }
        
        // 设置ViewPager和TabLayout
        val tabTitles = listOf("我的发布", "我的点赞")
        val fragments = listOf(
            ContentGridFragment.newInstance(ContentGridFragment.TYPE_POSTS),
            ContentGridFragment.newInstance(ContentGridFragment.TYPE_LIKES)
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
        
        // 功能菜单点击事件
//        setupFunctionMenuListeners()

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

                    // 设置更多按钮动画
                    binding.moreBtn.animate()
                        .setDuration(300)
                        .alpha(1f)
                        .withStartAction {
                            binding.moreBtn.setBackgroundResource(android.R.color.transparent)
                            binding.moreBtn.imageTintList = ColorStateList.valueOf(Color.BLACK)
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
                    binding.moreBtn.animate()
                        .setDuration(300)
                        .alpha(1f)
                        .withStartAction {
                            binding.moreBtn.setBackgroundResource(R.drawable.view_semicircle)
                            binding.moreBtn.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#73000000"))
                            binding.moreBtn.imageTintList = ColorStateList.valueOf(requireContext().getColor(R.color.viewColor))
                        }
                        .start()

                    BaseActivity.setStatusBarTextColor(false,requireActivity().window)
                    topUserInfoHide = true
                }
            }
        }
    }
    private var topUserInfoHide: Boolean = true
    
//    private fun setupFunctionMenuListeners() {
//        // 我的收藏
//        binding.layoutMyCollection.setOnClickListener {
//            Toast.makeText(requireContext(), "即将进入我的收藏页面", Toast.LENGTH_SHORT).show()
//        }
//
//        // 我的订单
//        binding.layoutMyOrders.setOnClickListener {
//            Toast.makeText(requireContext(), "即将进入我的订单页面", Toast.LENGTH_SHORT).show()
//        }
//
//        // 收货地址
//        binding.layoutAddress.setOnClickListener {
//            Toast.makeText(requireContext(), "即将进入收货地址页面", Toast.LENGTH_SHORT).show()
//        }
//
//
//        // 帮助中心
//        binding.layoutHelp.setOnClickListener {
//            Toast.makeText(requireContext(), "即将进入帮助中心页面", Toast.LENGTH_SHORT).show()
//        }
//
//        // 设置
//        binding.layoutSettings.setOnClickListener {
//            Toast.makeText(requireContext(), "即将进入设置页面", Toast.LENGTH_SHORT).show()
//        }
//    }
    
    @SuppressLint("SetTextI18n")
    private fun loadUserData() {
        executeThread {
            try {
                userInfo = UserHttpUtils.getUserInfo()
                activity?.runOnUiThread {
                    userInfo?.apply {
                        val mainActivity = activity as MainActivity

                        // 更新用户信息
                        binding.tvNickname.text = nickname
                        binding.topTitle.text = nickname
                        mainActivity.findViewById<TextView>(R.id.left_view_username).text = nickname

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
//                        binding.tvPostsCount.text = postCount.toString()


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
                        // 加载抽屉头像
                        Glide.with(this@PersonalCenterFragment)
                            .load(loadUrl)
                            .apply(RequestOptions())
                            .transform(CircleCrop())
                            .into(mainActivity.findViewById(R.id.left_view_user_avatar))

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
    
    @SuppressLint("NotifyDataSetChanged")
    private fun updatePetList(pets: List<Pet>) {
        petList.clear()
        petList.addAll(pets)
        petAdapter.notifyDataSetChanged()
        if(petList.isEmpty()){
            binding.tvEmptyPetTip.visibility = View.VISIBLE
        }else{
            binding.tvEmptyPetTip.visibility = View.GONE
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
