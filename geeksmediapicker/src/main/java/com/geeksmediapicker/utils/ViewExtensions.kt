package com.geeksmediapicker.utils

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import com.geeksmediapicker.R
import kotlinx.android.synthetic.main.my_custom_toast.view.*


fun View.visible() {
    if (visibility != View.VISIBLE){
        visibility = View.VISIBLE
    }
}

fun View.gone() {
    if (visibility != View.GONE){
        visibility = View.GONE
    }
}

fun View.invisible() {
    if (visibility != View.INVISIBLE) {
        visibility = View.INVISIBLE
    }
}

fun View.isVisible(): Boolean = visibility == View.VISIBLE

fun Context.showToast(message: String) {
    val myInflater = LayoutInflater.from(this)
    val view = myInflater.inflate(R.layout.my_custom_toast, null)
    val myToast = Toast(this)
    myToast.duration = Toast.LENGTH_SHORT
    myToast.view = view
    view.tvMsg.text = message
    myToast.show()
}



