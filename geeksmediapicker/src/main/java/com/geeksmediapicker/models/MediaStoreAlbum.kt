package com.geeksmediapicker.models

import android.net.Uri

/**
 * Simple data class to hold information about an Album included in the device's MediaStore.
 */
data class MediaStoreAlbum(
    val bucket_id: Long,
    val bucket_name: String?,
    val imageUri: Uri?,
    val medias: List<MediaStoreData>
){
    fun getTotalImages(): String{
        return medias.size.toString()
    }
}