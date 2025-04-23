package com.zhouyu.pet_science.model

import java.util.Date

data class User(
    val userId: Int,
    val username: String,
    val password: String,
    val email: String,
    val mobile: String,
    val avatarUrl: String,
    val nickname: String, // 昵称
    val gender: Int, // 性别（0：男，1：女，2：保密）
    val birthday: Date, // 生日
    val location: String, // 位置
    val bio: String, // 个人简介
    val createdAt: Date, // 账号创建时间
    val updatedAt: Date, // 信息更新时间
    val status: Int, // 账号状态（0：正常，1：封禁）
    var isFollowed : Boolean = false, // 是否已关注
    val followTime: Long, // 关注时间
    var followCount: Int, // 关注数
    val fansCount: Int // 粉丝数
)
