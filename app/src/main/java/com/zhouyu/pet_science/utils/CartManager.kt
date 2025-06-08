package com.zhouyu.pet_science.utils

import Product
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.zhouyu.pet_science.model.CartItem

/**
 * 购物车管理工具类
 */
object CartManager {
    
    private const val CART_KEY = "pet_cart"
    private val gson = Gson()
    
    /**
     * 获取购物车商品列表
     */
    fun getCartItems(): MutableList<CartItem> {
        val cartJson = StorageUtils.get<String>(CART_KEY)
        return if (cartJson.isNullOrEmpty()) {
            mutableListOf()
        } else {
            try {
                val type = object : TypeToken<MutableList<CartItem>>() {}.type
                gson.fromJson(cartJson, type) ?: mutableListOf()
            } catch (e: Exception) {
                e.printStackTrace()
                mutableListOf()
            }
        }
    }
    
    /**
     * 保存购物车商品列表
     */
    private fun saveCartItems(cartItems: List<CartItem>) {
        val cartJson = gson.toJson(cartItems)
        StorageUtils.put(CART_KEY, cartJson)
    }
    
    /**
     * 添加商品到购物车
     */
    fun addToCart(product: Product, quantity: Int = 1): Boolean {
        val cartItems = getCartItems()
        val existingItem = cartItems.find { it.productId == product.productId }
        
        if (existingItem != null) {
            // 商品已存在，增加数量
            val newQuantity = existingItem.quantity + quantity
            if (newQuantity <= 99 && newQuantity <= product.stock) {
                existingItem.quantity = newQuantity
                saveCartItems(cartItems)
                return true
            }
            return false
        } else {
            // 新商品，添加到购物车
            if (quantity <= product.stock) {
                val cartItem = CartItem(
                    id = System.currentTimeMillis().toInt(),
                    productId = product.productId,
                    name = product.productName,
                    price = product.price,
                    image = product.imageUrl,
                    quantity = quantity,
                    selected = true,
                    stock = product.stock,
                    description = product.description
                )
                cartItems.add(cartItem)
                saveCartItems(cartItems)
                return true
            }
            return false
        }
    }
    
    /**
     * 从购物车移除商品
     */
    fun removeFromCart(cartItemId: Int) {
        val cartItems = getCartItems()
        cartItems.removeAll { it.id == cartItemId }
        saveCartItems(cartItems)
    }
    
    /**
     * 更新商品数量
     */
    fun updateQuantity(cartItemId: Int, quantity: Int): Boolean {
        if (quantity < 1 || quantity > 99) return false
        
        val cartItems = getCartItems()
        val item = cartItems.find { it.id == cartItemId }
        return if (item != null && quantity <= item.stock) {
            item.quantity = quantity
            saveCartItems(cartItems)
            true
        } else {
            false
        }
    }
    
    /**
     * 更新商品选中状态
     */
    fun updateSelection(cartItemId: Int, selected: Boolean) {
        val cartItems = getCartItems()
        val item = cartItems.find { it.id == cartItemId }
        if (item != null) {
            item.selected = selected
            saveCartItems(cartItems)
        }
    }
    
    /**
     * 全选/取消全选
     */
    fun selectAll(selected: Boolean) {
        val cartItems = getCartItems()
        cartItems.forEach { it.selected = selected }
        saveCartItems(cartItems)
    }
    
    /**
     * 删除选中的商品
     */
    fun removeSelectedItems() {
        val cartItems = getCartItems()
        cartItems.removeAll { it.selected }
        saveCartItems(cartItems)
    }
    
    /**
     * 获取购物车商品总数量
     */
    fun getCartCount(): Int {
        return getCartItems().sumOf { it.quantity }
    }
    
    /**
     * 获取选中商品的总数量
     */
    fun getSelectedCount(): Int {
        return getCartItems().filter { it.selected }.sumOf { it.quantity }
    }
    
    /**
     * 获取选中商品的总价格
     */
    fun getSelectedTotalPrice(): Double {
        return getCartItems().filter { it.selected }.sumOf { it.getSubtotal() }
    }
    
    /**
     * 获取选中的商品列表
     */
    fun getSelectedItems(): List<CartItem> {
        return getCartItems().filter { it.selected }
    }
    
    /**
     * 清空购物车
     */
    fun clearCart() {
        StorageUtils.delete(CART_KEY)
    }
    
    /**
     * 检查是否全选
     */
    fun isAllSelected(): Boolean {
        val cartItems = getCartItems()
        return cartItems.isNotEmpty() && cartItems.all { it.selected }
    }

    /**
     * 添加测试商品到购物车（用于测试）
     */
    fun addTestProducts() {
        val testProducts = listOf(
            CartItem(
                id = 1,
                productId = 1,
                name = "高品质宠物狗粮 营养均衡 适合各种犬类",
                price = 128.00,
                image = "https://example.com/dog_food.jpg",
                quantity = 2,
                selected = true,
                stock = 50,
                description = "营养均衡的高品质狗粮"
            ),
            CartItem(
                id = 2,
                productId = 2,
                name = "猫咪玩具球 互动娱乐",
                price = 25.50,
                image = "https://example.com/cat_toy.jpg",
                quantity = 1,
                selected = true,
                stock = 100,
                description = "有趣的猫咪玩具"
            ),
            CartItem(
                id = 3,
                productId = 3,
                name = "宠物洗护用品套装",
                price = 89.90,
                image = "https://example.com/pet_shampoo.jpg",
                quantity = 1,
                selected = false,
                stock = 30,
                description = "温和的宠物洗护套装"
            )
        )

        val cartItems = getCartItems()
        cartItems.addAll(testProducts)
        saveCartItems(cartItems)
    }
}
