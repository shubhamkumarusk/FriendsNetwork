package com.example.friendsnetwork.model

import android.net.Uri
import android.os.Parcelable
import kotlinx.parcelize.Parcelize


data class FeedModel(
    val userId: String,
    val image: Uri,
    val caption: String,
    val liked_by: List<UserModel> = ArrayList(),
    val comments: List<CommentModel> = ArrayList(),
    val userModel: UserModel?=null
)