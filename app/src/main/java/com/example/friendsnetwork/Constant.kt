package com.example.friendsnetwork

import android.app.ProgressDialog
import android.content.Context

const val USER_ID_FIRESTOREPATH = "User"

fun buildDialog(context: Context, message: String): ProgressDialog {
    val dialog = ProgressDialog(context)
    dialog.setMessage(message)
    dialog.setCancelable(false)
    return dialog
}






