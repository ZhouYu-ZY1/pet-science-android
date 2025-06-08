package com.zhouyu.pet_science.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.zhouyu.pet_science.R
import com.zhouyu.pet_science.model.OrderItem
import com.zhouyu.pet_science.network.ProductHttpUtils
import java.text.DecimalFormat

/**
 * 订单商品列表适配器
 */
class OrderProductAdapter : RecyclerView.Adapter<OrderProductAdapter.ProductViewHolder>() {

    private val orderItems = ArrayList<OrderItem>()
    private val decimalFormat = DecimalFormat("¥#,##0.00")

    fun setOrderItems(items: List<OrderItem>) {
        orderItems.clear()
        orderItems.addAll(items)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_order_product, parent, false)
        return ProductViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        val orderItem = orderItems[position]
        holder.bind(orderItem)
    }

    override fun getItemCount(): Int = orderItems.size

    inner class ProductViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val ivProductImage: ImageView = itemView.findViewById(R.id.ivProductImage)
        private val tvProductName: TextView = itemView.findViewById(R.id.tvProductName)
        private val tvProductPrice: TextView = itemView.findViewById(R.id.tvProductPrice)
        private val tvProductQuantity: TextView = itemView.findViewById(R.id.tvProductQuantity)
        private val tvProductSubtotal: TextView = itemView.findViewById(R.id.tvProductSubtotal)

        @SuppressLint("SetTextI18n")
        fun bind(orderItem: OrderItem) {
            // 设置商品名称
            tvProductName.text = orderItem.productName

            // 设置价格
            tvProductPrice.text = decimalFormat.format(orderItem.price)

            // 设置数量
            tvProductQuantity.text = "x${orderItem.quantity}"

            // 设置小计
            tvProductSubtotal.text = decimalFormat.format(orderItem.subtotal)

            // 加载商品图片
            val imageUrl = ProductHttpUtils.getFirstImage(orderItem.productImage)
            Glide.with(itemView.context)
                .load(imageUrl)
                .placeholder(R.drawable.image_placeholder)
                .error(R.drawable.image_placeholder)
                .into(ivProductImage)
        }
    }
}
