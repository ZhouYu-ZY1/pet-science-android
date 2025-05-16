package com.zhouyu.pet_science.activities

import Product
import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import com.bumptech.glide.Glide
import com.zhouyu.pet_science.R
import com.zhouyu.pet_science.activities.base.BaseActivity
import com.zhouyu.pet_science.databinding.ActivityOrderDetailBinding
import com.zhouyu.pet_science.model.Order
import com.zhouyu.pet_science.network.OrderHttpUtils
import com.zhouyu.pet_science.network.ProductHttpUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import android.os.CountDownTimer
import com.zhouyu.pet_science.utils.MyToast
import com.zhouyu.pet_science.views.dialog.MyDialog

class OrderDetailActivity : BaseActivity() {

    private var orderId: Int = 0
    private var order: Order? = null
    private val decimalFormat = DecimalFormat("¥#,##0.00")
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
    private var countDownTimer: CountDownTimer? = null
    
    private lateinit var binding: ActivityOrderDetailBinding

    // 订单状态描述映射
    private val statusDescMap = mapOf(
        "pending" to "请尽快完成支付，超时订单将自动取消",
        "paid" to "商家正在备货中，请耐心等待",
        "shipped" to "包裹正在配送中，请耐心等待",
        "completed" to "订单已完成，感谢您的购买",
        "cancelled" to "订单已取消，请重新下单"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOrderDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 获取订单ID
        orderId = intent.getIntExtra("orderId", 0)
        if (orderId == 0) {
            finish()
            return
        }

        setTopBarView(binding.toolbar, true)

        // 设置点击事件
        setupClickListeners()

        // 加载订单详情
        loadOrderDetail()
    }

    private fun setupClickListeners() {
        // 返回按钮
        binding.btnBack.setOnClickListener {
            finish()
        }

        // 更多按钮
        binding.btnMore.setOnClickListener {
            // TODO: 显示更多菜单
        }

        // 查看物流详情
        binding.tvViewLogistics.setOnClickListener {
            // TODO: 跳转到物流详情页面
        }

        // 联系客服
        binding.btnContactService.setOnClickListener {
            // TODO: 实现联系客服功能
        }

        // 查看物流
        binding.btnViewLogistics.setOnClickListener {
            // TODO: 跳转到物流详情页面
        }

        // 确认收货
        binding.btnConfirmReceipt.setOnClickListener {
            confirmReceipt()
        }
    }

    private fun loadOrderDetail() {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val result = withContext(Dispatchers.IO) {
                    OrderHttpUtils.getOrderDetail(orderId)
                }
                
                if (result?.code == 200 && result.data != null) {
                    order = result.data
                    updateUI(order!!)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun updateUI(order: Order) {
        // 更新订单状态
        updateOrderStatus(order)
        
        // 更新物流信息
        updateLogisticsInfo(order)
        
        // 更新收货信息
        updateShippingInfo(order)
        
        // 更新订单信息
        updateOrderInfo(order)
        
        // 更新商品信息
        updateProductInfo(order)
        
        // 更新价格信息
        updatePriceInfo(order)
        
        // 更新底部按钮
        updateBottomButtons(order)
    }

    private fun updateOrderStatus(order: Order) {
        val statusText = when (order.status) {
            "pending" -> "等待付款"
            "paid" -> "等待发货"
            "shipped" -> "商品已发货"
            "completed" -> "订单已完成"
            "cancelled" -> "订单已取消"
            else -> order.status
        }
        
        binding.tvOrderStatus.text = statusText
        binding.tvStatusDesc.text = statusDescMap[order.status] ?: ""
        
        // 根据订单状态设置图标
        val iconResId = when (order.status) {
            "pending" -> R.drawable.ic_wallet // 钱包图标
            "paid" -> R.drawable.ic_box // 包裹图标
            "shipped" -> R.drawable.ic_truck // 货车图标
            "completed" -> R.drawable.ic_check_circle // 完成图标
            "cancelled" -> R.drawable.ic_cancel // 取消图标
            else -> R.drawable.ic_box
        }
        
        binding.ivStatusIcon.setImageResource(iconResId)
        
        // 如果是待付款状态，获取并显示订单过期时间
        if (order.status == "pending") {
            binding.tvOrderExpiration.visibility = View.VISIBLE
            getOrderExpiration(order.orderId)
        } else {
            binding.tvOrderExpiration.visibility = View.GONE
            // 取消倒计时
            countDownTimer?.cancel()
            countDownTimer = null
        }
    }

    private fun updateLogisticsInfo(order: Order) {
        // 只有已发货状态才显示物流信息
        if (order.status == "shipped" && order.shipping != null && !order.shipping.trackingNumber.isNullOrEmpty()) {
            binding.cardLogistics.visibility = View.VISIBLE
            
            // 设置物流单号和物流公司信息
            binding.tvLogisticsNumber.text = "物流单号：${order.shipping.trackingNumber}"
            
            // 根据物流公司代码显示完整名称
            val companyName = when(order.shipping.shippingCompany) {
                "SF" -> "顺丰速运"
                "YTO" -> "圆通速递"
                "STO" -> "申通快递"
                "ZTO" -> "中通快递"
                "YD" -> "韵达快递"
                "JD" -> "京东物流"
                else -> order.shipping.shippingCompany
            }
            binding.tvLogisticsCompany.text = "物流公司：$companyName"
            
            // TODO: 实际中应该调用物流查询接口获取物流信息
            // 这里只是简单设置一些示例数据
            val now = System.currentTimeMillis()
            binding.tvLogisticsTime1.text = dateFormat.format(Date(now))
            binding.tvLogisticsDesc1.text = "【广州市】快件已由【广州转运中心】发出，正在运往【深圳转运中心】"
            
        } else {
            binding.cardLogistics.visibility = View.GONE
        }
    }

    private fun updateShippingInfo(order: Order) {
        val shipping = order.shipping
        if (shipping != null) {
            binding.tvReceiverName.text = shipping.receiverName
            // 隐私保护，只显示部分手机号
            val phone = shipping.receiverMobile
            if (phone.length >= 11) {
                binding.tvReceiverPhone.text = "${phone.substring(0, 3)}****${phone.substring(7)}"
            } else {
                binding.tvReceiverPhone.text = phone
            }
            binding.tvAddress.text = shipping.address
        }
    }

    private fun updateOrderInfo(order: Order) {
        binding.tvOrderNo.text = order.orderNo
        binding.tvOrderTime.text = dateFormat.format(Date(order.createdAt))
        
        // 支付方式
        val paymentMethod = order.payment?.paymentMethod ?: "未支付"
        binding.tvPaymentMethod.text = when (paymentMethod) {
            "wx" -> "微信支付"
            "zfb" -> "支付宝"
            "card" -> "银联支付"
            else -> paymentMethod
        }
    }

    @SuppressLint("SetTextI18n")
    private fun updateProductInfo(order: Order) {
        val orderItem = order.orderItem
        if (orderItem != null) {
            binding.tvProductName.text = orderItem.productName
            binding.tvProductSpec.text = "规格：标准" // 假设规格信息，实际应该从API获取
            binding.tvProductPrice.text = decimalFormat.format(orderItem.price)
            binding.tvProductQuantity.text = "x${orderItem.quantity}"
            
            // 加载商品图片
            val imageUrl = ProductHttpUtils.getFirstImage(orderItem.productImage)
            Glide.with(this)
                .load(imageUrl)
                .placeholder(R.drawable.image_placeholder)
                .error(R.drawable.image_placeholder)
                .into(binding.ivProductImage)
        }
    }

    @SuppressLint("SetTextI18n")
    private fun updatePriceInfo(order: Order) {
        val orderItem = order.orderItem
        if (orderItem != null) {
            // 商品金额 = 单价 × 数量
            val productAmount = orderItem.price * orderItem.quantity
            binding.tvProductAmount.text = decimalFormat.format(productAmount)
            
            // 运费，假设为0
            binding.tvShippingFee.text = "¥0.00"
            
            // 优惠，计算优惠金额
            val discount = productAmount - order.totalAmount
            if (discount > 0) {
                binding.tvDiscount.text = "-${decimalFormat.format(discount)}"
            } else {
                binding.tvDiscount.text = "-¥0.00"
            }
            
            // 实付款
            binding.tvTotalAmount.text = decimalFormat.format(order.totalAmount)
        }
    }

    private fun updateBottomButtons(order: Order) {
        // 根据订单状态设置底部按钮
        when (order.status) {
            "pending" -> {
                binding.btnContactService.visibility = View.VISIBLE
                binding.btnViewLogistics.visibility = View.GONE
                binding.btnConfirmReceipt.text = "去付款"
                binding.btnConfirmReceipt.visibility = View.VISIBLE
                
                // 去付款按钮点击事件
                binding.btnConfirmReceipt.setOnClickListener {
                    val intent = android.content.Intent(this, OrderPayActivity::class.java)
                    intent.putExtra("orderId", order.orderId)
                    intent.putExtra("orderNo", order.orderNo)
                    intent.putExtra("amount", order.totalAmount)
                    startActivity(intent)
                }
            }
            "paid" -> {
                binding.btnContactService.visibility = View.VISIBLE
                binding.btnViewLogistics.visibility = View.GONE
                binding.btnConfirmReceipt.text = "提醒发货"
                binding.btnConfirmReceipt.visibility = View.VISIBLE
                
                // 提醒发货按钮点击事件
                binding.btnConfirmReceipt.setOnClickListener {
                    // TODO: 实现提醒发货功能
                    MyToast.show( "已提醒商家发货",true)
                }
            }
            "shipped" -> {
                binding.btnContactService.visibility = View.VISIBLE
                binding.btnViewLogistics.visibility = View.VISIBLE
                binding.btnConfirmReceipt.text = "确认收货"
                binding.btnConfirmReceipt.visibility = View.VISIBLE
                
                // 确认收货按钮点击事件
                binding.btnConfirmReceipt.setOnClickListener {
                    confirmReceipt()
                }
            }
            "completed" -> {
                binding.btnContactService.visibility = View.VISIBLE
                binding.btnViewLogistics.visibility = View.VISIBLE
                binding.btnConfirmReceipt.text = "再次购买"
                binding.btnConfirmReceipt.visibility = View.VISIBLE
                
                // 再次购买按钮点击事
                binding.btnConfirmReceipt.setOnClickListener {
                    startActivity(Intent(this, ProductDetailActivity::class.java).apply {
                        order.orderItem?.apply {
                            ProductDetailActivity.product = Product(productId,productName,"",price,1,"","",productImage,"2.3万",1,"","")
                        }
                    })
                }
            }
            else -> {
                // 其他状态，如已取消等，只显示联系客服按钮
                binding.btnContactService.visibility = View.VISIBLE
                binding.btnViewLogistics.visibility = View.GONE
                binding.btnConfirmReceipt.visibility = View.GONE
            }
        }
    }

    /**
     * 确认收货
     */
    private fun confirmReceipt() {
        MyDialog(this).apply {
            setTitle("确认收货"); setMessage("您确认已收到商品吗？")
            setYesOnclickListener("确认") {
                dismiss()
                // 确认收货
                CoroutineScope(Dispatchers.Main).launch {
                    try {
                        val result = withContext(Dispatchers.IO) {
                            OrderHttpUtils.completeOrder(orderId)
                        }

                        if (result?.code == 200) {
                            // 重新加载订单详情
                            loadOrderDetail()
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
            setNoOnclickListener("取消") {
                dismiss()
            }; show()
        }
    }

    /**
     * 获取订单过期时间
     */
    private fun getOrderExpiration(orderId: Int) {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val result = withContext(Dispatchers.IO) {
                    OrderHttpUtils.getOrderExpiration(orderId)
                }
                
                if (result?.code == 200 && result.data != null) {
                    val expirationSeconds = result.data
                    if (expirationSeconds > 0) {
                        startCountDown(expirationSeconds * 1000) // 转换为毫秒
                    } else {
                        binding.tvOrderExpiration.text = "订单已超时"
                    }
                } else {
                    binding.tvOrderExpiration.text = "获取过期时间失败"
                }
            } catch (e: Exception) {
                e.printStackTrace()
                binding.tvOrderExpiration.text = "获取过期时间失败"
            }
        }
    }

    /**
     * 开始倒计时
     */
    private fun startCountDown(milliseconds: Long) {
        // 取消之前的倒计时
        countDownTimer?.cancel()
        countDownTimer = object : CountDownTimer(milliseconds, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val hours = millisUntilFinished / (60 * 60 * 1000)
                val minutes = (millisUntilFinished % (60 * 60 * 1000)) / (60 * 1000)
                val seconds = (millisUntilFinished % (60 * 1000)) / 1000
                
                binding.tvOrderExpiration.text = String.format("支付剩余时间: %02d:%02d:%02d", hours, minutes, seconds)
            }
            
            override fun onFinish() {
                binding.tvOrderExpiration.text = "订单已超时，请重新下单"
                // 可以在这里添加自动刷新订单状态的逻辑
                loadOrderDetail()
            }
        }.start()
    }
    /**
     * 取消倒计时
     */
    override fun onDestroy() {
        super.onDestroy()
        // 取消倒计时
        countDownTimer?.cancel()
        countDownTimer = null
    }
}