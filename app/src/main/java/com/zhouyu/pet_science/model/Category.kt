package com.zhouyu.pet_science.model

import java.io.Serializable

data class Category (
    val categoryId: Int,
    val categoryCode: String,
    val categoryName: String
): Serializable