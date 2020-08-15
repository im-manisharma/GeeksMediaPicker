package com.geeksmediapicker.interfaces

import com.geeksmediapicker.models.MediaStoreData

interface MediaPickerListener {
    fun onMediaPicked(selectedMediaList: ArrayList<MediaStoreData> =  ArrayList(), mediaStoreData: MediaStoreData = MediaStoreData())
}
