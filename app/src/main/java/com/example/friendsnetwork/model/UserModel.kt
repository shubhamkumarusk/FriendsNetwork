package com.example.friendsnetwork.model

import android.net.Uri
import android.os.Parcelable
import kotlinx.parcelize.Parcelize


data class UserModel(
    val id:String="",
    val name:String="",
    val userImage:String="",
    val caption:String=""
)

