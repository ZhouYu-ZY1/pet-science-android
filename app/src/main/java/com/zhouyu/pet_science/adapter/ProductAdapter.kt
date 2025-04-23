package com.zhouyu.pet_science.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestOptions
import com.zhouyu.pet_science.R
import com.zhouyu.pet_science.databinding.ItemShopProductBinding
import com.zhouyu.pet_science.model.ProductItem
import com.zhouyu.pet_science.tools.utils.PhoneMessage

// 商品适配器
class ProductAdapter(private val productItems: MutableList<ProductItem>) :
    RecyclerView.Adapter<ProductAdapter.ProductViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val binding = ItemShopProductBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ProductViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        val item = productItems[position]
        val options = RequestOptions.bitmapTransform(RoundedCorners(PhoneMessage.dpToPx(10f)))
        Glide.with(holder.itemView)
            .load(item.imageUrl)
            .transition(DrawableTransitionOptions.withCrossFade())
            .apply(options)
            .into(holder.binding.productImage)
        holder.binding.productName.text = item.name
        holder.binding.productPrice.text = item.price
        holder.binding.productSales.text = item.sales
    }

    override fun getItemCount(): Int {
        return productItems.size
    }

    inner class ProductViewHolder(val binding: ItemShopProductBinding) : RecyclerView.ViewHolder(binding.root)
}