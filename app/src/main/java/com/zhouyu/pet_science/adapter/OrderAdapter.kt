package com.zhouyu.pet_science.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.zhouyu.pet_science.R
import com.zhouyu.pet_science.model.Order
import com.zhouyu.pet_science.network.ProductHttpUtils
import java.text.DecimalFormat

class OrderAdapter(private val listener: OrderActionListener) : RecyclerView.Adapter<OrderAdapter.OrderViewHolder>() {

    private val orders = ArrayList<Order>()
    private val decimalFormat = DecimalFormat("¥#,##0.00")

    // 订单状态文本映射
    private val statusTextMap = mapOf(
        "pending" to "待付款",
        "paid" to "待发货",
        "shipped" to "待收货",
        "completed" to "已完成",
        "cancelled" to "已取消",
//        "REFUNDING" to "退款中",
//        "REFUNDED" to "已退款"
    )

    // 主按钮文本映射
    private val primaryButtonTextMap = mapOf(
        "pending" to "付款",
        "paid" to "提醒发货",
        "shipped" to "确认收货",
        "completed" to "评价"
    )

    // 次要按钮文本映射
    private val secondaryButtonTextMap = mapOf(
        "pending" to "取消订单",
        "paid" to "查看详情",
        "shipped" to "查看物流",
        "completed" to "再次购买"
    )

    fun addOrders(newOrders: List<Order>) {
        val startPosition = orders.size
        orders.addAll(newOrders)
        notifyItemRangeInserted(startPosition, newOrders.size)
    }

    fun clearOrders() {
        orders.clear()
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_order, parent, false)
        return OrderViewHolder(view)
    }

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        val order = orders[position]
        holder.bind(order)
    }

    override fun getItemCount(): Int = orders.size

    inner class OrderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvOrderStatus: TextView = itemView.findViewById(R.id.tvOrderStatus)
        private val singleProductLayout: LinearLayout = itemView.findViewById(R.id.singleProductLayout)
        private val multiProductRecyclerView: RecyclerView = itemView.findViewById(R.id.multiProductRecyclerView)
        private val ivProductImage: ImageView = itemView.findViewById(R.id.ivProductImage)
        private val tvProductName: TextView = itemView.findViewById(R.id.tvProductName)
        private val tvProductSpec: TextView = itemView.findViewById(R.id.tvProductSpec)
        private val tvProductPrice: TextView = itemView.findViewById(R.id.tvProductPrice)
        private val tvProductQuantity: TextView = itemView.findViewById(R.id.tvProductQuantity)
        private val tvOrderTotal: TextView = itemView.findViewById(R.id.tvOrderTotal)
        private val btnSecondary: Button = itemView.findViewById(R.id.btnSecondary)
        private val btnPrimary: Button = itemView.findViewById(R.id.btnPrimary)

        private val orderProductAdapter = OrderProductAdapter()

        @SuppressLint("SetTextI18n")
        fun bind(order: Order) {
            // 设置订单状态
            tvOrderStatus.text = statusTextMap[order.status] ?: order.status

            // 设置按钮文本和可见性
            setupButtons(order)

            // 设置整个订单项的点击事件
            itemView.setOnClickListener {
                listener.onOrderClick(order)
            }

            // 设置商品信息
            val allOrderItems = order.getAllOrderItems()
            if (allOrderItems.isNotEmpty()) {
                if (order.isMultiProduct()) {
                    // 多商品订单 - 直接显示所有商品
                    singleProductLayout.visibility = View.GONE
                    multiProductRecyclerView.visibility = View.VISIBLE

                    // 设置多商品列表
                    if (multiProductRecyclerView.adapter == null) {
                        multiProductRecyclerView.layoutManager = LinearLayoutManager(itemView.context)
                        multiProductRecyclerView.adapter = orderProductAdapter
                    }
                    orderProductAdapter.setOrderItems(allOrderItems)

                    // 设置订单总金额
                    tvOrderTotal.text = "共${order.getProductCount()}种商品${order.getTotalQuantity()}件 合计：${decimalFormat.format(order.totalAmount)}（含运费¥0.00）"
                } else {
                    // 单商品订单显示
                    singleProductLayout.visibility = View.VISIBLE
                    multiProductRecyclerView.visibility = View.GONE

                    val orderItem = allOrderItems.first()
                    tvProductName.text = orderItem.productName
                    tvProductSpec.text = order.remark

                    // 使用Glide加载商品图片
                    val imageUrl = ProductHttpUtils.getFirstImage(orderItem.productImage)
                    Glide.with(itemView.context)
                        .load(imageUrl)
                        .placeholder(R.drawable.image_placeholder)
                        .error(R.drawable.image_placeholder)
                        .into(ivProductImage)

                    // 设置价格和数量
                    tvProductPrice.text = decimalFormat.format(orderItem.price)
                    tvProductQuantity.text = "x${orderItem.quantity}"

                    // 设置订单总金额
                    tvOrderTotal.text = "共${orderItem.quantity}件商品 合计：${decimalFormat.format(order.totalAmount)}（含运费¥0.00）"
                }
            }
        }

        private fun setupButtons(order: Order) {
            // 根据订单状态设置按钮
            val primaryText = primaryButtonTextMap[order.status]
            val secondaryText = secondaryButtonTextMap[order.status]

            if (primaryText != null) {
                btnPrimary.text = primaryText
                btnPrimary.visibility = View.VISIBLE
                btnPrimary.setOnClickListener {
                    listener.onPrimaryButtonClick(order, btnPrimary)
                }
            } else {
                btnPrimary.visibility = View.GONE
            }

            if (secondaryText != null) {
                btnSecondary.text = secondaryText
                btnSecondary.visibility = View.VISIBLE
                btnSecondary.setOnClickListener {
                    listener.onSecondaryButtonClick(order, btnSecondary)
                }
            } else {
                btnSecondary.visibility = View.GONE
            }
        }
    }

    // 订单操作监听接口
    interface OrderActionListener {
        fun onOrderClick(order: Order)
        fun onPrimaryButtonClick(order: Order, btn: Button)
        fun onSecondaryButtonClick(order: Order, btn: Button)
    }
} 