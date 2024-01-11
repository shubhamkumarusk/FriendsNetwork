package com.example.friendsnetwork.model

import android.net.Uri
import android.os.Parcelable
import kotlinx.parcelize.Parcelize


data class FeedModel(
    val feedId: String="",
    val image:String="",
    val caption: String="",
    val liked_by: ArrayList<String> = ArrayList(),
    val comments: ArrayList<CommentModel> = ArrayList(),
    val userModel: UserModel?=null
)