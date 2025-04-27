package com.zhouyu.pet_science.fragments.shop

import android.annotation.SuppressLint
import android.graphics.Rect
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.GridLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.scwang.smart.refresh.footer.ClassicsFooter
import com.scwang.smart.refresh.header.ClassicsHeader
import com.youth.banner.indicator.CircleIndicator
import com.zhouyu.pet_science.R
import com.zhouyu.pet_science.adapter.ProductAdapter
import com.zhouyu.pet_science.databinding.FragmentShopCategoryBinding
import com.zhouyu.pet_science.fragments.BaseFragment
import com.zhouyu.pet_science.model.Category
import com.zhouyu.pet_science.network.HttpUtils.BASE_URL
import com.zhouyu.pet_science.network.ProductHttpUtils
import com.zhouyu.pet_science.utils.MyToast
import com.zhouyu.pet_science.utils.PhoneMessage
import com.zhouyu.pet_science.adapter.BannerTextAdapter
import com.zhouyu.pet_science.model.ProductItem


class ShopCategoryFragment() : BaseFragment() {

    private var category: Category = Category(0, "", "")
    private var recycledViewPool: RecyclerView.RecycledViewPool? = null

    constructor(category: Category, recycledViewPool: RecyclerView.RecycledViewPool? = null) : this() {
        this.category = category
        this.recycledViewPool = recycledViewPool
    }

    private var _binding: FragmentShopCategoryBinding? = null
    private val binding get() = _binding!!
    private var currentPage = 1
    private val pageSize = 10
    private var isLoading = false
    private val productItems: MutableList<ProductItem> = ArrayList()

    // 懒加载相关变量
    private var isViewCreated = false  // 视图是否已创建
    private var isDataInitialized = false  // 数据是否已初始化

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentShopCategoryBinding.inflate(inflater, container, false)
        initViews()
        isViewCreated = true
        return binding.root
    }


    // 懒加载数据的核心方法
    private fun lazyLoadDataIfNeeded() {
        // 当Viewpager滑动到Fragment可见时，才加载数据
        if (isViewCreated && !isDataInitialized && isVisible) {
            // 加载Banner广告
            loadBanner()
            // 加载商品数据
            refreshData()

            isDataInitialized = true
        }
    }

    private fun initViews() {
        // 使用GridLayoutManager替换LinearLayoutManager，设置为2列
        val layoutManager = GridLayoutManager(context, 2)
        binding.productRecyclerView.layoutManager = layoutManager
        // 设置间距
        val spacing = PhoneMessage.dpToPx(5f) // 定义间距值
        binding.productRecyclerView.addItemDecoration(object : RecyclerView.ItemDecoration() {
            override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
                outRect.set(spacing, spacing, spacing, spacing) // 设置上下左右的间距
            }
        })

        // 使用共享的 RecycledViewPool
        recycledViewPool?.let {
            binding.productRecyclerView.setRecycledViewPool(it)
        }

        // 初始化适配器
        val productAdapter = ProductAdapter(productItems)
        binding.productRecyclerView.adapter = productAdapter

        // 设置下拉刷新和加载更多
        binding.refreshLayout.setReboundDuration(300) // 设置回弹动画时间

        // 设置刷新头
        val classicsHeader = ClassicsHeader(requireContext())

        // 设置加载更多
        val classicsFooter = ClassicsFooter(requireContext())

        classicsHeader.setFinishDuration(100) // 设置完成动画时间
        classicsFooter.setFinishDuration(100)
        binding.refreshLayout.setEnableLoadMoreWhenContentNotFull(false) // 不满一页禁止加载更多

        binding.refreshLayout.setRefreshHeader(classicsHeader)
        binding.refreshLayout.setRefreshFooter(classicsFooter)

        // 设置下拉刷新和加载更多事件
        binding.refreshLayout.setOnRefreshListener {
            refreshData()
        }
        binding.refreshLayout.setOnLoadMoreListener {
            loadMoreData()
        }

        binding.refreshLayout.setEnableFooterFollowWhenNoMoreData(false) // 加载完毕时底部显示无更多数据

        // 设置图标网格
        setupIconGrid()
    }

    // 设置Banner广告
    private fun loadBanner() {
        // 创建Banner数据
        val bannerItems = listOf(
            BannerTextAdapter.BannerItem(
                "$BASE_URL/images/banner/photo-1583337130417-3346a1be7dee.jpg",
                "新品上市","精选宠物折优惠"
            ),
            BannerTextAdapter.BannerItem(
                "$BASE_URL/images/banner/premium_photo-1707353401897-da9ba223f807.jpg",
                "买二赠一","宠物保健品"
            ),
            BannerTextAdapter.BannerItem(
                "$BASE_URL/images/banner/premium_photo-1708724049005-192fe5c23269.jpg",
                "限时特惠","优质宠物食品"
            ),
            BannerTextAdapter.BannerItem(
                "$BASE_URL/images/banner/photo-1599572743109-61c820b3a79d.jpg",
                "满300减50","宠物服饰专区"
            ),
            BannerTextAdapter.BannerItem(
                "$BASE_URL/images/banner/photo-1548199973-03cce0bbc87b.jpg",
                "全场低至5折","宠物玩具大促"
            )
        )

        // 设置Banner适配器
        binding.banner.setAdapter(BannerTextAdapter(bannerItems))

        // 设置指示器
        binding.banner.indicator = CircleIndicator(requireContext())

        // 设置轮播时间间隔和自动轮播
        binding.banner.setLoopTime(5000)
        binding.banner.isAutoLoop(true)

        // 设置点击事件
        binding.banner.setOnBannerListener { data, position ->
            val item = data as BannerTextAdapter.BannerItem
            MyToast.show("点击了第${position + 1}个广告: ${item.text}")
        }

        // 开始轮播
        binding.banner.start()
    }

    override fun onStart() {
        super.onStart()
        // 只有当Fragment可见时才开始轮播
        if (isVisible && isDataInitialized) {
            binding.banner.start()
        }
    }

    override fun onStop() {
        super.onStop()
        // 停止自动轮播
        binding.banner.stop()
    }

    // 添加可见性变化监听
    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (!hidden && isDataInitialized) {
            // Fragment变为可见时，开始轮播
            binding.banner.start()
        } else {
            // Fragment变为不可见时，停止轮播
            binding.banner.stop()
        }
    }

    // 使用 onResume 检测可见性
    override fun onResume() {
        super.onResume()
        // 当Fragment恢复到前台时检查是否需要加载数据
        lazyLoadDataIfNeeded()

        // 如果数据已初始化且Fragment可见，则开始轮播
        if (isDataInitialized && isVisible) {
            binding.banner.start()
        }
    }

    override fun onPause() {
        super.onPause()
        // 当Fragment暂停时停止轮播
        binding.banner.stop()
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private fun setupIconGrid() {
        // 添加图标项
        val iconTitles = arrayOf(
            "品牌专区", "疯狂折扣", "每日签到", "天天领券", "免费好礼",
            "限时秒杀", "城市漫步", "潮流好价", "全球好货", "直播秒杀"
        )
        
        // 添加对应的图标资源ID
        val iconResources = intArrayOf(
            R.drawable.ic_brand_zone, 
            R.drawable.ic_crazy_discount,
            R.drawable.ic_daily_checkin,
            R.drawable.ic_daily_coupon,
            R.drawable.ic_free_gift,
            R.drawable.ic_flash_sale,
            R.drawable.ic_city_walk, 
            R.drawable.ic_trend_price, 
            R.drawable.ic_global_goods, 
            R.drawable.ic_live_sale
        )

        // 清除现有视图
        binding.iconGridLayout.removeAllViews()

        // 设置网格布局参数
        val columnCount = 5
        binding.iconGridLayout.columnCount = columnCount
        binding.iconGridLayout.rowCount = 2

        // 设置GridLayout的布局参数
        val gridParams = binding.iconGridLayout.layoutParams
        gridParams.width = ViewGroup.LayoutParams.MATCH_PARENT
        gridParams.height = ViewGroup.LayoutParams.WRAP_CONTENT
        binding.iconGridLayout.layoutParams = gridParams
        
        for (i in iconTitles.indices) {
            val iconView =
                LayoutInflater.from(context).inflate(R.layout.item_shop_icon, binding.iconGridLayout, false)
            val iconImage = iconView.findViewById<ImageView>(R.id.icon_image)
            val iconTitle = iconView.findViewById<TextView>(R.id.icon_title)

            // 设置对应的图标和标题
            iconImage.setImageResource(iconResources[i])
            iconTitle.text = iconTitles[i]

            // 添加到网格布局
            val params = GridLayout.LayoutParams()
            params.columnSpec = GridLayout.spec(i % columnCount, 1, 1f)
            params.rowSpec = GridLayout.spec(i / columnCount, 1)
            params.setGravity(Gravity.CENTER)
            iconView.layoutParams = params
            binding.iconGridLayout.addView(iconView)
        }
    }

    private fun refreshData() {
        // 刷新，重置页码和状态
        currentPage = 1
        productItems.clear()
        loadProductsByPage(currentPage, true)
    }

    private fun loadMoreData() {
        // 加载更多，页码加1
        if (!isLoading) {
            currentPage++
            loadProductsByPage(currentPage, false)
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun loadProductsByPage(page: Int, isRefresh: Boolean) {
        // 根据category加载对应分类的商品数据
        isLoading = true

        // 在IO线程执行网络请求
        executeThread {
            val result = ProductHttpUtils.getProductList(page, pageSize, null, category.categoryCode)
            result?.let { pageResult ->
                // 获取商品列表
                val products = pageResult.list.toMutableList()
                // 随机打乱
                products.shuffle()
                // 处理商品列表
                products.forEach { product ->
                    val imageUrl = ProductHttpUtils.getFirstImage(product.mainImage)
                    // 处理每个商品
                    productItems.add(
                        ProductItem(
                            imageUrl,
                            product.productName,
                            "¥" + product.price,
                            "2.3万+人付款"
                        )
                    )
                }

                runUiThread{
                    if(isDetached || _binding == null) return@runUiThread

                    // 更新UI
                    binding.productRecyclerView.adapter?.notifyDataSetChanged()
                    isLoading = false
                    val isEmpty = products.isEmpty()
                    // 根据操作类型完成刷新或加载更多
                    if (isRefresh) {
                        binding.refreshLayout.finishRefresh(true)
                        binding.refreshLayout.resetNoMoreData()
                    } else if (page > 1) {
                        binding.refreshLayout.finishLoadMore(100, true, isEmpty)
                    }
                }
                return@executeThread
            } ?: run {
                runUiThread{
                    if(isDetached || _binding == null) return@runUiThread

                    // 处理请求失败的情况
                    isLoading = false
                    if (isRefresh) {
                        binding.refreshLayout.finishRefresh( false)
                        binding.refreshLayout.resetNoMoreData()
                    } else if (page > 1) {
                        binding.refreshLayout.finishLoadMore(100, false, true)
                    }
                }
                return@executeThread
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        isViewCreated = false
        isDataInitialized = false
        _binding = null
    }

    companion object {
        // 更新 newInstance 方法，接收 RecycledViewPool 参数
        fun newInstance(
            category: Category, 
            recycledViewPool: RecyclerView.RecycledViewPool? = null
        ): ShopCategoryFragment {
            return ShopCategoryFragment(category, recycledViewPool)
        }
    }
}