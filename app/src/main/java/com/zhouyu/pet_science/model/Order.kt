package com.zhouyu.pet_science.model

// 请求体模型
data class CreateOrderRequest(
    val orderItem: OrderItemRequest? = null, // 单商品订单（保持向后兼容）
    val orderItems: List<OrderItemRequest>? = null, // 多商品订单
    val shipping: ShippingRequest,
    val remark: String?,
)

data class OrderItemRequest(
    val productId: Int,
    val quantity: Int,
    val price: Double // 传递单价，总价由后端计算
)

data class ShippingRequest(
    val address: String,
    val receiverMobile: String,
    val receiverName: String
)

// 订单模型
data class Order(
    val orderId: Int,
    val userId: Int,
    val orderNo: String,
    val totalAmount: Double,
    val status: String,
    val remark: String?,
    val createdAt: Long,
    val updatedAt: Long,
    val orderItem: OrderItem?, // 单商品订单（保持向后兼容）
    val orderItems: List<OrderItem>?, // 多商品订单
    val payment: Payment?,
    val shipping: Shipping?
) {
    // 获取所有订单项（兼容单商品和多商品）
    fun getAllOrderItems(): List<OrderItem> {
        return when {
            orderItems != null && orderItems.isNotEmpty() -> orderItems
            orderItem != null -> listOf(orderItem)
            else -> emptyList()
        }
    }

    // 判断是否为多商品订单
    fun isMultiProduct(): Boolean {
        return orderItems != null && orderItems.size > 1
    }

    // 获取商品总数量
    fun getTotalQuantity(): Int {
        return getAllOrderItems().sumOf { it.quantity }
    }

    // 获取商品种类数量
    fun getProductCount(): Int {
        return getAllOrderItems().size
    }
}

// 订单项模型
data class OrderItem(
    val orderItemId: Int,
    val orderId: Int,
    val productId: Int,
    val productName: String,
    val productImage: String,
    val quantity: Int,
    val price: Double,
    val subtotal: Double,
    val createdAt: Long,
    val updatedAt: Long
)

// 支付信息模型
data class Payment(
    val paymentId: Int?,
    val orderId: Int?,
    val paymentAmount: Double?,
    val paymentMethod: String?,
    val paymentStatus: String?,
    val paymentTime: Long?,
    val transactionId: String?,
    val createdAt: Long?,
    val updatedAt: Long?
)

// 配送信息模型
data class Shipping(
    val shippingId: Int,
    val orderId: Int,
    val address: String,
    val receiverName: String,
    val receiverMobile: String,
    val shippingStatus: String,
    val trackingNumber: String?,
    val shippingTime: Long?,
    val completionTime: Long?,
    val shippingCompany: String?,
    val createdAt: Long,
    val updatedAt: Long
)

// 响应体模型
data class OrderResponse(
    val orderId: Int,
    val orderNo: String,
    val totalAmount: Double,
    val status: String
)