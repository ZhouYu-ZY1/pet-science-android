data class Product(
    val productId: Int,
    val productName: String,
    val category: String,
    val price: Double,
    val stock: Int,
    val description: String,
    val images: String,
    var imageUrl: String,
    var sales: String,
    val status: Int,
    val createdAt: String,
    val updatedAt: String
)