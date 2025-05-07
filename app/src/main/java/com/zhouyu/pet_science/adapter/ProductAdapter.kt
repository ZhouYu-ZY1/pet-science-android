package com.zhouyu.pet_science.adapter

import Product
import android.annotation.SuppressLint
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestOptions
import com.zhouyu.pet_science.activities.ProductDetailActivity
import com.zhouyu.pet_science.databinding.ItemShopProductBinding
import com.zhouyu.pet_science.utils.PhoneMessage

// 商品适配器
class ProductAdapter(var products: MutableList<Product>) :
    RecyclerView.Adapter<ProductAdapter.ProductViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val binding = ItemShopProductBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ProductViewHolder(binding)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        val item = products[position]
        val options = RequestOptions.bitmapTransform(RoundedCorners(PhoneMessage.dpToPx(10f)))
        Glide.with(holder.itemView)
            .load(item.imageUrl)
            .transition(DrawableTransitionOptions.withCrossFade())
            .apply(options)
            .into(holder.binding.productImage)
        holder.binding.productName.text = item.productName
        holder.binding.productPrice.text = "￥"+item.price
        holder.binding.productSales.text = item.sales+"人付款"

        // 添加点击事件，跳转到商品详情页
        holder.itemView.setOnClickListener {
            val context = holder.itemView.context
            val intent = Intent(context, ProductDetailActivity::class.java)
            ProductDetailActivity.product = item
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return products.size
    }

    inner class ProductViewHolder(val binding: ItemShopProductBinding) : RecyclerView.ViewHolder(binding.root)
}