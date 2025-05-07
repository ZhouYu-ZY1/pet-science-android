package com.zhouyu.pet_science.model

data class Pet(
    val id: Long,
    val name: String,
    val type: String, // "cat", "dog", "other"
    val breed: String,
    val ageYear: Int,
    val ageMonth: Int,
    val avatarUrl: String = ""
)