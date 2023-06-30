package com.geeksmediapicker.models

import android.net.Uri
import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.util.*

/**
 * Simple data class to hold information about an image or a video included in the device's MediaStore.
 */

@Parcelize
data class MediaStoreData(
    var media_id: Long = 0,
    var bucket_id: Long = 0,
    var media_name: String = "",
    var media_type: String = "",
    var bucket_name: String? = "",
    var date_added: Date? = null,
    var content_uri: Uri? = null,
    var media_path: String = "",
    var media_duration: Long = 0,
    var isSelected: Boolean = false
): Parcelable