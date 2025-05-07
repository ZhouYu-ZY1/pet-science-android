package com.zhouyu.pet_science.model

data class Result<T>(
    val code: Int,
    val message: String,
    val data: T?
)