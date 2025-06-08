package com.zhouyu.pet_science.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.zhouyu.pet_science.R
import com.zhouyu.pet_science.model.CartItem
import com.zhouyu.pet_science.utils.ConsoleUtils

/**
 * 购物车适配器
 */
class CartAdapter(
    private var cartItems: MutableList<CartItem>,
    private var isEditMode: Boolean = false
) : RecyclerView.Adapter<CartAdapter.CartViewHolder>() {

    // 接口定义
    interface OnCartItemListener {
        fun onItemSelected(position: Int, selected: Boolean)
        fun onQuantityChanged(position: Int, quantity: Int)
        fun onItemDelete(position: Int)
    }

    private var listener: OnCartItemListener? = null

    fun setOnCartItemListener(listener: OnCartItemListener) {
        this.listener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_cart, parent, false)
        return CartViewHolder(view)
    }

    override fun onBindViewHolder(holder: CartViewHolder, position: Int) {
        val item = cartItems[position]
        holder.bind(item, position)
    }

    override fun getItemCount(): Int {
        return cartItems.size
    }

    /**
     * 更新数据
     */
    fun updateData(newItems: List<CartItem>) {
        ConsoleUtils.logErr("CartAdapter", "更新数据，新商品数量: ${newItems.size}")
        cartItems.clear()
        cartItems.addAll(newItems)
        ConsoleUtils.logErr("CartAdapter", "适配器中商品数量: ${cartItems.size}")
        notifyDataSetChanged()
    }

    /**
     * 设置编辑模式
     */
    fun setEditMode(editMode: Boolean) {
        isEditMode = editMode
        notifyDataSetChanged()
    }

    /**
     * 移除指定位置的商品
     */
    fun removeItem(position: Int) {
        if (position >= 0 && position < cartItems.size) {
            cartItems.removeAt(position)
            notifyItemRemoved(position)
            notifyItemRangeChanged(position, cartItems.size)
        }
    }

    inner class CartViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val cbSelect: CheckBox = itemView.findViewById(R.id.cbSelect)
        private val ivProductImage: ImageView = itemView.findViewById(R.id.ivProductImage)
        private val tvProductName: TextView = itemView.findViewById(R.id.tvProductName)
        private val tvProductPrice: TextView = itemView.findViewById(R.id.tvProductPrice)
        private val btnDecrease: ImageView = itemView.findViewById(R.id.btnDecrease)
        private val tvQuantity: TextView = itemView.findViewById(R.id.tvQuantity)
        private val btnIncrease: ImageView = itemView.findViewById(R.id.btnIncrease)
        private val btnDelete: ImageView = itemView.findViewById(R.id.btnDelete)
        private val quantityLayout: View = itemView.findViewById(R.id.quantityLayout)

        fun bind(item: CartItem, position: Int) {
            ConsoleUtils.logErr("CartAdapter", "绑定商品 $position: ${item.name}")
            // 设置商品信息
            tvProductName.text = item.name
            tvProductPrice.text = "¥${String.format("%.2f", item.price)}"
            tvQuantity.text = item.quantity.toString()
            cbSelect.isChecked = item.selected

            // 加载商品图片
            Glide.with(itemView.context)
                .load(item.image)
                .placeholder(R.mipmap.default_avatar_image)
                .error(R.mipmap.default_avatar_image)
                .into(ivProductImage)

            // 设置编辑模式显示
            if (isEditMode) {
                quantityLayout.visibility = View.GONE
                btnDelete.visibility = View.VISIBLE
            } else {
                quantityLayout.visibility = View.VISIBLE
                btnDelete.visibility = View.GONE
            }

            // 设置按钮状态
            btnDecrease.alpha = if (item.canDecrease()) 1.0f else 0.5f
            btnIncrease.alpha = if (item.canIncrease()) 1.0f else 0.5f

            // 设置点击事件
            cbSelect.setOnCheckedChangeListener { _, isChecked ->
                listener?.onItemSelected(position, isChecked)
            }

            btnDecrease.setOnClickListener {
                if (item.canDecrease()) {
                    val newQuantity = item.quantity - 1
                    listener?.onQuantityChanged(position, newQuantity)
                }
            }

            btnIncrease.setOnClickListener {
                if (item.canIncrease()) {
                    val newQuantity = item.quantity + 1
                    listener?.onQuantityChanged(position, newQuantity)
                }
            }

            btnDelete.setOnClickListener {
                listener?.onItemDelete(position)
            }
        }
    }
}
