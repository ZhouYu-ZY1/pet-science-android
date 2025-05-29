package com.zhouyu.pet_science.model

data class Content(
    val id: Long,
    val title: String,
    val coverUrl: String,
    val likeCount: Int,
    val contentType: String,
    val commentCount: Int,
    val isLiked: Boolean = false
)