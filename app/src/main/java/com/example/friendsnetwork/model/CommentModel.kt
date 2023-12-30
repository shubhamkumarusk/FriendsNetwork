package com.example.friendsnetwork.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize


data class CommentModel(
    val user:UserModel,
    val comment:String
)