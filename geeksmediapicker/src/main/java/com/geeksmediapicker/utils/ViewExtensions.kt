package com.geeksmediapicker.utils

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import android.widget.Toast
import com.geeksmediapicker.R


internal fun View.visible() {
    visibility = View.VISIBLE
}

internal fun View.gone() {
    visibility = View.GONE
}

internal fun View.invisible() {
    visibility = View.INVISIBLE
}

internal fun View.isVisible(): Boolean = visibility == View.VISIBLE

internal fun Context.showToast(message: String) {
    val myInflater = LayoutInflater.from(this)
    val view = myInflater.inflate(R.layout.my_custom_toast, null)
    val myToast = Toast(this)
    myToast.duration = Toast.LENGTH_SHORT
    myToast.view = view
    view.findViewById<TextView>(R.id.tvMsg).text = message
    myToast.show()
}



