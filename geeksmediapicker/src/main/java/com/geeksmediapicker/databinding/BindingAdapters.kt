package com.geeksmediapicker.databinding

import android.net.Uri
import android.widget.ImageView
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide


@BindingAdapter("bind:loadImageUri")
fun loadImageUri(imageView: ImageView, uri: Uri?) {
    if (uri == null)
        return

    Glide.with(imageView.context)
        .load(uri)
        .thumbnail(0.3f)
        .into(imageView)
}
