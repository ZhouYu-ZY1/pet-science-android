package com.zhouyu.pet_science.model

import com.google.gson.annotations.SerializedName

/**
 * 购物车商品项数据模型
 */
data class CartItem(
    @SerializedName("id")
    val id: Int,
    
    @SerializedName("productId")
    val productId: Int,
    
    @SerializedName("name")
    val name: String,
    
    @SerializedName("price")
    val price: Double,
    
    @SerializedName("image")
    val image: String,
    
    @SerializedName("quantity")
    var quantity: Int,
    
    @SerializedName("selected")
    var selected: Boolean = true,
    
    @SerializedName("stock")
    val stock: Int = 99,
    
    @SerializedName("description")
    val description: String? = null
) {
    /**
     * 获取商品小计
     */
    fun getSubtotal(): Double {
        return price * quantity
    }
    
    /**
     * 是否可以增加数量
     */
    fun canIncrease(): Boolean {
        return quantity < stock && quantity < 99
    }
    
    /**
     * 是否可以减少数量
     */
    fun canDecrease(): Boolean {
        return quantity > 1
    }
}
