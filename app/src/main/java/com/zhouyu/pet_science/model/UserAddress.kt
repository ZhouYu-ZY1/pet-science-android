package com.zhouyu.pet_science.model

import com.google.gson.annotations.SerializedName
import java.util.Date

/**
 * 用户收货地址模型类
 */
data class UserAddress(
    @SerializedName("id")
    val id: Int? = null, // 地址ID，新增时可能为空

    @SerializedName("userId")
    val userId: Int?, // 用户ID

    @SerializedName("recipientName")
    val recipientName: String?, // 收货人姓名

    @SerializedName("recipientPhone")
    val recipientPhone: String?, // 收货人手机号

    @SerializedName("province")
    val province: String?, // 省份

    @SerializedName("city")
    val city: String?, // 城市

    @SerializedName("district")
    val district: String?, // 区县

    @SerializedName("detailAddress")
    val detailAddress: String?, // 详细地址

    @SerializedName("addressTag")
    val addressTag: String?, // 地址标签

    @SerializedName("isDefault")
    val isDefault: Int?, // 是否默认地址：0-否，1-是 (通常后端用Integer，前端也可以用Boolean并做转换，这里保持Integer)

    @SerializedName("createdAt")
    val createdAt: String?, // 创建时间 (通常为 "yyyy-MM-dd HH:mm:ss" 格式的字符串)

    @SerializedName("updatedAt")
    val updatedAt: String? // 更新时间 (通常为 "yyyy-MM-dd HH:mm:ss" 格式的字符串)
)