package com.zhouyu.pet_science.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.zhouyu.pet_science.R
import com.zhouyu.pet_science.activities.base.BaseActivity
import com.zhouyu.pet_science.databinding.ActivityCartBinding
import com.zhouyu.pet_science.adapter.CartAdapter
import com.zhouyu.pet_science.fragments.shop.ShopFragment
import com.zhouyu.pet_science.model.CartItem
import com.zhouyu.pet_science.model.CreateOrderRequest
import com.zhouyu.pet_science.model.OrderItemRequest
import com.zhouyu.pet_science.model.ShippingRequest
import com.zhouyu.pet_science.model.UserAddress
import com.zhouyu.pet_science.network.OrderHttpUtils
import com.zhouyu.pet_science.network.ProductHttpUtils
import com.zhouyu.pet_science.utils.CartManager
import com.zhouyu.pet_science.utils.ConsoleUtils
import com.zhouyu.pet_science.utils.MyToast
import com.zhouyu.pet_science.views.dialog.MyDialog
import com.zhouyu.pet_science.views.dialog.MyProgressDialog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * 购物车页面
 */
class CartActivity : BaseActivity(), CartAdapter.OnCartItemListener {

    private lateinit var binding: ActivityCartBinding
    private lateinit var cartAdapter: CartAdapter
    private var cartItems = mutableListOf<CartItem>()
    private var isEditMode = false
    private var selectedAddress: UserAddress? = null
    private var isCreatingOrder = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCartBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initViews()
        setupToolbar()
        setupRecyclerView()
        setupClickListeners()
        loadCartData()
    }



    private fun initViews() {
        // ViewBinding 已经自动初始化了所有视图，无需手动 findViewById
    }

    private fun setupToolbar() {
        setTopBarView(binding.toolbar, true)

        // 设置返回按钮点击事件
        binding.btnBack.setOnClickListener {
            finish()
        }
    }

    private fun setupRecyclerView() {
        cartAdapter = CartAdapter(cartItems.toMutableList(), isEditMode)
        cartAdapter.setOnCartItemListener(this)
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = cartAdapter
    }

    private fun setupClickListeners() {
        // 全选/取消全选
        binding.cbSelectAll.setOnCheckedChangeListener { _, isChecked ->
            CartManager.selectAll(isChecked)
            loadCartData()
        }

        // 编辑模式切换
        binding.tvEditMode.setOnClickListener {
            toggleEditMode()
        }

        // 结算按钮
        binding.btnCheckout.setOnClickListener {
            checkout()
        }

        // 去购物按钮
        binding.btnGoShopping.setOnClickListener {
            // 跳转到商城页面
            finish()
        }
    }

    private fun loadCartData() {
        cartItems.clear()
        cartItems.addAll(CartManager.getCartItems())
        
        if (cartItems.isEmpty()) {
            showEmptyCart()
        } else {
            showCartContent()
            cartAdapter.updateData(cartItems)
            updateSummary()
            updateSelectAllState()
        }
    }

    private fun showEmptyCart() {
        binding.emptyCartLayout.visibility = View.VISIBLE
        binding.cartContentLayout.visibility = View.GONE
        binding.bottomLayout.visibility = View.GONE
    }

    private fun showCartContent() {
        binding.emptyCartLayout.visibility = View.GONE
        binding.cartContentLayout.visibility = View.VISIBLE
        binding.bottomLayout.visibility = View.VISIBLE
    }

    private fun toggleEditMode() {
        isEditMode = !isEditMode
        binding.tvEditMode.text = if (isEditMode) "完成" else "编辑"
        cartAdapter.setEditMode(isEditMode)

        // 编辑模式下隐藏结算按钮，显示删除按钮
        if (isEditMode) {
            binding.btnCheckout.text = "删除"
            binding.btnCheckout.setOnClickListener { deleteSelectedItems() }
        } else {
            binding.btnCheckout.text = "结算"
            binding.btnCheckout.setOnClickListener { checkout() }
        }
    }

    private fun updateSummary() {
        val selectedCount = CartManager.getSelectedCount()
        val totalPrice = CartManager.getSelectedTotalPrice()

        binding.tvSelectedCount.text = "已选择 $selectedCount 件商品"
        binding.tvTotalPrice.text = String.format("%.2f", totalPrice)

        binding.btnCheckout.isEnabled = selectedCount > 0
    }

    private fun updateSelectAllState() {
        binding.cbSelectAll.setOnCheckedChangeListener(null)
        binding.cbSelectAll.isChecked = CartManager.isAllSelected()
        binding.cbSelectAll.setOnCheckedChangeListener { _, isChecked ->
            CartManager.selectAll(isChecked)
            loadCartData()
        }
    }

    private fun checkout() {
        val selectedItems = CartManager.getSelectedItems()
        if (selectedItems.isEmpty()) {
            MyToast.show("请选择要结算的商品")
            return
        }

        // 检查是否选择了地址
        if (selectedAddress == null) {
            MyDialog(this).apply {
                setTitle("选择收货地址")
                setMessage("请先选择收货地址")
                setYesOnclickListener("去选择") {
                    val intent = Intent(this@CartActivity, AddressActivity::class.java)
                    intent.putExtra("isSelect", true)
                    startActivity(intent)
                    dismiss()
                }
                setNoOnclickListener("取消") {
                    dismiss()
                }
                show()
            }
            return
        }

        val totalPrice = CartManager.getSelectedTotalPrice()
        MyDialog(this).apply {
            setTitle("确认结算")
            setMessage("确认结算 ${selectedItems.size} 件商品，总计 ¥${String.format("%.2f", totalPrice)} 吗？")
            setYesOnclickListener("确认") {
                createOrder(selectedItems)
                dismiss()
            }
            setNoOnclickListener("取消") {
                dismiss()
            }
            show()
        }
    }

    private fun deleteSelectedItems() {
        val selectedItems = CartManager.getSelectedItems()
        if (selectedItems.isEmpty()) {
            MyToast.show("请选择要删除的商品")
            return
        }

        MyDialog(this).apply {
            setTitle("确认删除")
            setThemeColor(getColor(R.color.Theme2))
            setMessage("确定要删除选中的 ${selectedItems.size} 件商品吗？")
            setYesOnclickListener("删除") {
                CartManager.removeSelectedItems()
                loadCartData()
                MyToast.show("删除成功")
                dismiss()
            }
            setNoOnclickListener("取消") {
                dismiss()
            }
            show()
        }
    }

    // 创建订单
    private fun createOrder(selectedItems: List<CartItem>) {
        if (isCreatingOrder) {
            MyToast.show("正在创建订单，请稍等...")
            return
        }
        isCreatingOrder = true
        val myProgressDialog = MyProgressDialog(this).apply {
            setTitleStr("创建订单")
            setHintStr("正在创建订单中，请稍等...")
            show()
        }

        // 使用协程在后台创建订单
        CoroutineScope(Dispatchers.Main).launch {
            // 从选择的地址中获取收货信息
            val address = selectedAddress?.let {
                "${it.province}${it.city}${it.district} ${it.detailAddress}"
            } ?: return@launch

            val receiverMobile = selectedAddress?.recipientPhone ?: return@launch
            val receiverName = selectedAddress?.recipientName ?: return@launch
            val remark = "购物车订单"

            // 创建订单项列表
            val orderItems = selectedItems.map { cartItem ->
                OrderItemRequest(
                    productId = cartItem.productId,
                    quantity = cartItem.quantity,
                    price = cartItem.price
                )
            }

            val shipping = ShippingRequest(
                address = address,
                receiverMobile = receiverMobile,
                receiverName = receiverName
            )

            val createOrderRequest = CreateOrderRequest(
                orderItems = orderItems, // 使用orderItems而不是orderItem
                shipping = shipping,
                remark = remark
            )

            // 发送创建订单请求
            val result = OrderHttpUtils.createOrder(createOrderRequest)

            if (result?.code == 200 && result.data != null) {
                // 订单创建成功
                MyToast.show("订单创建成功", true)
                ConsoleUtils.logErr("订单创建成功: ${result.data}")

                // 从购物车中移除已结算的商品
                selectedItems.forEach { cartItem ->
                    CartManager.removeFromCart(cartItem.id)
                }

                // 跳转到支付页面
                val intent = Intent(this@CartActivity, OrderPayActivity::class.java).apply {
                    putExtra("orderId", result.data.orderId)
                }
                startActivity(intent)

                // 刷新购物车数据
                loadCartData()
            } else {
                MyToast.show("订单创建失败: ${result?.message ?: "未知错误"}", false)
            }

            myProgressDialog.dismiss()
            isCreatingOrder = false
        }
    }

    // CartAdapter.OnCartItemListener 实现
    override fun onItemSelected(position: Int, selected: Boolean) {
        if (position < cartItems.size) {
            val item = cartItems[position]
            CartManager.updateSelection(item.id, selected)
            updateSummary()
            updateSelectAllState()
        }
    }

    override fun onQuantityChanged(position: Int, quantity: Int) {
        if (position < cartItems.size) {
            val item = cartItems[position]
            if (CartManager.updateQuantity(item.id, quantity)) {
                item.quantity = quantity
                cartAdapter.notifyItemChanged(position)
                updateSummary()
            } else {
                MyToast.show("库存不足")
            }
        }
    }

    override fun onItemDelete(position: Int) {
        if (position < cartItems.size) {
            val item = cartItems[position]
            MyDialog(this).apply {
                setTitle("确认删除")
                setThemeColor(getColor(R.color.Theme2))
                setMessage("确定要删除这件商品吗？")
                setYesOnclickListener("删除") {
                    CartManager.removeFromCart(item.id)
                    cartAdapter.removeItem(position)
                    cartItems.removeAt(position)
                    updateSummary()
                    updateSelectAllState()

                    if (cartItems.isEmpty()) {
                        showEmptyCart()
                    }

                    MyToast.show("删除成功")
                    dismiss()
                }
                setNoOnclickListener("取消") {
                    dismiss()
                }
                show()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // 刷新购物车数据
        loadCartData()
        // 从地址选择页面返回时，获取选择的地址
        selectedAddress = ProductDetailActivity.selectedAddress
        // 如果选择了地址，可以在这里更新UI显示地址信息
        selectedAddress?.let {
            MyToast.show("已选择地址: ${it.recipientName}")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // 清理地址选择状态
        ProductDetailActivity.selectedAddress = null
    }
}
