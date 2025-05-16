package com.zhouyu.pet_science.model

import java.util.Date

data class Pet(
    var id: Long,
    var name: String,
    var type: String, // "cat", "dog", "other"
    var breed: String,
    var birthday: Date,
    var avatarUrl: String = ""
)