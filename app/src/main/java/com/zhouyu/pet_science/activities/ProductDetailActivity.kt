package com.zhouyu.pet_science.activities

import Product
import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.RadioButton
import android.widget.Toast
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.youth.banner.adapter.BannerImageAdapter
import com.youth.banner.holder.BannerImageHolder
import com.youth.banner.indicator.CircleIndicator
import com.zhouyu.pet_science.R
import com.zhouyu.pet_science.activities.base.BaseActivity
import com.zhouyu.pet_science.databinding.ActivityProductDetailBinding
import com.zhouyu.pet_science.databinding.DialogProductSpecBinding
import com.zhouyu.pet_science.model.CreateOrderRequest
import com.zhouyu.pet_science.model.OrderItemRequest
import com.zhouyu.pet_science.model.ShippingRequest
import com.zhouyu.pet_science.model.UserAddress
import com.zhouyu.pet_science.network.OrderHttpUtils
import com.zhouyu.pet_science.network.ProductHttpUtils
import com.zhouyu.pet_science.utils.ConsoleUtils
import com.zhouyu.pet_science.utils.MyToast
import com.zhouyu.pet_science.views.dialog.MyProgressDialog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.atomic.AtomicBoolean

class ProductDetailActivity : BaseActivity() {

    companion object{
        var product: Product? = null
        // 用于存储选择的地址
        var selectedAddress: UserAddress? = null
    }
    private lateinit var binding: ActivityProductDetailBinding
    private var quantity = 1
    private var selectedCapacity = "2.5L"
    private var selectedColor = "白色"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProductDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setStatusBarTextColor(false,window)
        setTopBarView(binding.topBar,false)

        product?.apply {
            // 设置商品信息
            setupProductInfo(this)

            // 从网络获取最新的产品详情
            loadProductDetails(this.productId)
        }
        
        // 设置点击事件
        setupClickListeners()
    }

    @SuppressLint("SetTextI18n")
    private fun loadProductDetails(productId: Int) {
        // 使用协程在后台线程获取产品详情
        CoroutineScope(Dispatchers.IO).launch {
            val result = ProductHttpUtils.getProductDetail(productId)
            
            if (result != null) {
                // 在主线程中更新UI
                withContext(Dispatchers.Main) {
                    // 更新全局产品对象
                    product = result
                    // 更新UI
                    setupProductInfo(result)
                }
            } else {
                // 获取失败，可以在这里添加错误处理逻辑
                withContext(Dispatchers.Main) {
                    MyToast.show("获取产品详情失败")
                }
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun setupProductInfo(product: Product) {
        binding.productTitle.text = product.productName
        binding.currentPrice.text = "￥"+product.price
        binding.sales.text = "已售 ${product.sales}"

        binding.productDescription.text = product.description
        loadBanner(product.images)
    }

    // 设置Banner广告
    private fun loadBanner(images: String?) {
        if(images == null){
            return
        }
        val imageUrlList = ProductHttpUtils.getImageList(images)

        // 设置Banner适配器
        binding.banner.setAdapter(object :BannerImageAdapter<String>(imageUrlList){
            override fun onBindView(holder: BannerImageHolder, data: String, position: Int, size: Int) {
                Glide.with(this@ProductDetailActivity)
                .load(data)
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(holder.imageView)
            }
        })

        // 设置指示器
        binding.banner.indicator = CircleIndicator(this)

        // 关闭自动轮播
        binding.banner.isAutoLoop(false)

        // 开始轮播
        binding.banner.start()
    }

    private fun setupClickListeners() {
        // 返回按钮
        binding.backBtn.setOnClickListener {
            finish()
        }

        // 选择规格按钮
        binding.selectSpec.setOnClickListener {
            showSpecDialog()
        }

        // 加入购物车按钮
        binding.addCartBtn.setOnClickListener {
            showSpecDialog(isAddToCart = true)
        }

        // 立即购买按钮
        binding.buyNowBtn.setOnClickListener {
            showSpecDialog(isBuyNow = true)
        }
    }

//    @SuppressLint("SetTextI18n")
//    private fun loadProductDetails(productId: Int) {
//        // 这里应该是从网络或本地数据库加载商品详情
//        // 示例中使用模拟数据
//
//    }

    private lateinit var dialogBinding: DialogProductSpecBinding
    @SuppressLint("SetTextI18n")
    private fun showSpecDialog(isAddToCart: Boolean = false, isBuyNow: Boolean = false) {
        // 创建底部对话框，应用自定义主题
        val dialog = BottomSheetDialog(this, R.style.BottomSheetDialogTheme)
        dialogBinding = DialogProductSpecBinding.inflate(LayoutInflater.from(this))
        dialog.setContentView(dialogBinding.root)

        // 设置圆角背景
        dialog.window?.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)?.apply {
            setBackgroundResource(R.drawable.view_radius_top)
        }

        // 设置商品信息
        product?.let { product ->
            dialogBinding.productPrice.text = "￥%.2f".format(product.price)
            dialogBinding.selectedSpecText.text = "已选：$selectedCapacity $selectedColor"

            // 加载商品图片
            val imageUrlList = ProductHttpUtils.getImageList(product.images)
            if (imageUrlList.isNotEmpty()) {
                Glide.with(this)
                    .load(imageUrlList[0])
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .into(dialogBinding.productImage)
            }
        }

        // 如果没有选择地址，则获取默认地址
        if (selectedAddress == null) {
            // 在协程中获取默认地址
            CoroutineScope(Dispatchers.IO).launch {
                val defaultAddressResult = ProductHttpUtils.getDefaultAddress()
                if (defaultAddressResult?.code == 200 && defaultAddressResult.data != null) {
                    // 在主线程中更新UI
                    runOnUiThread {
                        selectedAddress = defaultAddressResult.data
                        updateAddressUI(dialogBinding)
                    }
                }
            }
        }

        // 设置地址选择点击事件
        dialogBinding.addressContainer.setOnClickListener {
            // 跳转到地址选择页面
            val intent = Intent(this, AddressActivity::class.java)
            intent.putExtra("isSelect", true)
            startActivity(intent)
        }

        // 更新地址显示
        updateAddressUI(dialogBinding)

        // 设置确认按钮文本
        if (isAddToCart) {
            dialogBinding.confirmBtn.text = "加入购物车"
        } else if (isBuyNow) {
            dialogBinding.confirmBtn.text = "立即支付"
        }

        // 设置容量选择
        dialogBinding.capacityGroup.setOnCheckedChangeListener { group, checkedId ->
            val radioButton = group.findViewById<RadioButton>(checkedId)
            selectedCapacity = radioButton.text.toString()
            updateSelectedSpec(dialogBinding)
        }

        // 设置颜色选择
        dialogBinding.colorGroup.setOnCheckedChangeListener { group, checkedId ->
            val radioButton = group.findViewById<RadioButton>(checkedId)
            selectedColor = radioButton.text.toString()
            updateSelectedSpec(dialogBinding)
        }

        // 预设选中状态
        when (selectedCapacity) {
            "2.5L" -> dialogBinding.capacity25l.isChecked = true
            "4L" -> dialogBinding.capacity4l.isChecked = true
        }

        when (selectedColor) {
            "白色" -> dialogBinding.colorWhite.isChecked = true
            "粉色" -> dialogBinding.colorPink.isChecked = true
            "蓝色" -> dialogBinding.colorBlue.isChecked = true
        }

        // 设置数量选择
        dialogBinding.quantityText.text = quantity.toString()

        dialogBinding.decreaseBtn.setOnClickListener {
            if (quantity > 1) {
                quantity--
                dialogBinding.quantityText.text = quantity.toString()
                dialogBinding.productPrice.text = "￥%.2f".format(product?.price!! * quantity)
            }
        }

        dialogBinding.increaseBtn.setOnClickListener {
            quantity++
            dialogBinding.quantityText.text = quantity.toString()
            dialogBinding.productPrice.text = "￥%.2f".format(product?.price!! * quantity)
        }

        // 设置确认按钮点击事件
        dialogBinding.confirmBtn.setOnClickListener {
            // 如果是立即购买，检查是否选择了地址
            if (isBuyNow && selectedAddress == null) {
                MyToast.show("请选择收货地址")
                return@setOnClickListener
            }
            
            dialog.dismiss() // 先关闭对话框

            when {
                isAddToCart -> {
                    val message = "已加入购物车：$selectedCapacity $selectedColor，数量：$quantity"
                    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
                    // 这里添加实际的加入购物车逻辑
                }
                isBuyNow -> {
                    product?.let { currentProduct ->
                        createOrder(currentProduct)
                    }
                }
                else -> {
                    // 只是选择规格，更新UI或变量
                    val message = "已选择：$selectedCapacity $selectedColor，数量：$quantity"
                    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
                }
            }
        }

        dialog.show()
    }

    // 更新地址UI显示
    private fun updateAddressUI(dialogBinding: DialogProductSpecBinding) {
        selectedAddress?.let { address ->
            // 显示已选择的地址
            dialogBinding.addressInfo.text = "${address.recipientName} ${address.recipientPhone}"
            dialogBinding.addressDetail.text = "${address.province}${address.city}${address.district} ${address.detailAddress}"
            dialogBinding.addressDetail.visibility = View.VISIBLE
        } ?: run {
            // 未选择地址
            dialogBinding.addressInfo.text = "请选择收货地址"
            dialogBinding.addressDetail.visibility = View.GONE
        }
    }

    private var isCreatingOrder = false

    // 创建订单
    private fun createOrder(currentProduct: Product) {
        if(isCreatingOrder){
            MyToast.show( "正在创建订单，请稍等...")
            return
        }
        isCreatingOrder = true
        val myProgressDialog = MyProgressDialog(this).apply {
            setTitleStr("创建订单");setHintStr("正在创建订单中，请稍等...")
            show()
        }

        // 使用协程在后台创建订单
        CoroutineScope(Dispatchers.Main).launch {
            // 从选择的地址中获取收货信息
            val address = selectedAddress?.let {
                "${it.province}${it.city}${it.district} ${it.detailAddress}"
            } ?: "四川省成都市金堂县" // 默认地址，实际应用中应该提示用户选择地址
            
            val receiverMobile = selectedAddress?.recipientPhone ?: "18890908888"
            val receiverName = selectedAddress?.recipientName ?: "测试用户"
            val remark = "备注信息"

            val orderItem = OrderItemRequest( // 订单项信息
                productId = currentProduct.productId, //产品ID
                quantity = quantity, // 购买数量
                price = currentProduct.price // 传递单价
            )
            val shipping = ShippingRequest(  // 收货信息
                address = address,
                receiverMobile = receiverMobile,
                receiverName = receiverName
            )
            val createOrderRequest = CreateOrderRequest(
                orderItem = orderItem,
                shipping = shipping,
                remark = "$selectedCapacity $selectedColor user:$remark"
            )

            // --- 发送创建订单请求 ---
            val result = OrderHttpUtils.createOrder(createOrderRequest)

            if (result?.code == 200 && result.data != null) {
                // 订单创建成功
                MyToast.show("订单创建成功",true)
                ConsoleUtils.logErr("订单创建成功: ${result.data}")
                val intent = Intent(this@ProductDetailActivity, OrderPayActivity::class.java).apply {
                    putExtra("orderId", result.data.orderId)
                }
                startActivity(intent)
            } else {
                MyToast.show( "订单创建失败: ${result?.message ?: "未知错误"}",false)
            }

            myProgressDialog.dismiss()
            isCreatingOrder = false
        }
    }

    @SuppressLint("SetTextI18n")
    private fun updateSelectedSpec(dialogBinding: DialogProductSpecBinding) {
        dialogBinding.selectedSpecText.text = "已选：$selectedCapacity $selectedColor"
    }

    override fun onResume() {
        super.onResume()
        // 如果从地址选择页面返回，可能需要刷新UI
        if (selectedAddress != null) {
            updateAddressUI(dialogBinding)
        }
    }

    override fun finish() {
        product = null
        selectedAddress = null
        super.finish()
    }
}