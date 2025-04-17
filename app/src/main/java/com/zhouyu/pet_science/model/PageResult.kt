data class PageResult<T>(
    val pageNum: Int,
    val pageSize: Int,
    val total: Int,
    val list: List<T>
)