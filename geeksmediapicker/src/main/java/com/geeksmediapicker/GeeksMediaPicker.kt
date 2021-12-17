package com.geeksmediapicker

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.fragment.app.Fragment
import com.geeksmediapicker.interfaces.MediaPickerListener
import com.geeksmediapicker.models.MediaStoreData
import com.geeksmediapicker.ui.CameraActivity
import com.geeksmediapicker.ui.PickerActivity
import java.lang.ref.WeakReference
import java.util.*

class GeeksMediaPicker private constructor(activity: Activity?, fragment: Fragment?) {

    private val activityWeakRef: WeakReference<Activity?> = WeakReference(activity)
    private val fragmentWeakRef: WeakReference<Fragment?> = WeakReference(fragment)

    companion object {
        const val EXTRA_MEDIA_TYPE = "MEDIA_TYPE"
        const val EXTRA_MULTI_SELECTION = "MULTI_SELECTION"
        const val EXTRA_INCLUDES_FILE_PATH = "INCLUDES_FILE_PATH"
        const val EXTRA_ENABLE_COMPRESSION = "ENABLE_COMPRESSION"
        const val EXTRA_TOOLBAR_COLOR = "TOOLBAR_COLOR"
        const val EXTRA_MAX_COUNT = "MAX_COUNT"

        @JvmStatic
        fun with(activity: Activity) = GeeksMediaPicker(activity, null)

        @JvmStatic
        fun with(fragment: Fragment) = GeeksMediaPicker(null, fragment)

        @JvmStatic
        val listenerDeque: Deque<MediaPickerListener> = ArrayDeque()
    }

    private var maxCount: Int = -1
    private var toolBarColor: Int = -1
    private var mediaType: String = GeeksMediaType.IMAGE
    private var isMultiSelection: Boolean = false
    private var includesFilePath: Boolean = false
    private var isCompressionEnable: Boolean = false

    fun setMediaType(mediaType: String): GeeksMediaPicker {
        this.mediaType = mediaType
        return this
    }

    fun setToolbarColor(toolBarColor: Int): GeeksMediaPicker {
        this.toolBarColor = toolBarColor
        return this
    }

    fun setIncludesFilePath(includesFilePath: Boolean): GeeksMediaPicker {
        this.includesFilePath = includesFilePath
        return this
    }

    fun setEnableCompression(isCompressionEnable: Boolean): GeeksMediaPicker {
        this.isCompressionEnable = isCompressionEnable
        return this
    }

    fun startSingle(action: (MediaStoreData) -> Unit) {
        listenerDeque.clear()
        listenerDeque.push(object : MediaPickerListener {
            override fun onMediaPicked(selectedMediaList: ArrayList<MediaStoreData>, mediaStoreData: MediaStoreData) {
                action(selectedMediaList[0])
            }
        })

        isMultiSelection = false
        startPicker()
    }

    fun startMultiple(maxCount: Int = -1, action: (ArrayList<MediaStoreData>) -> Unit) {
        this.maxCount = maxCount
        listenerDeque.clear()
        listenerDeque.push(object : MediaPickerListener {
            override fun onMediaPicked(selectedMediaList: ArrayList<MediaStoreData>, mediaStoreData: MediaStoreData) {
                action(selectedMediaList)
            }
        })

        isMultiSelection = true
        startPicker()
    }

    fun startCamera(action: (MediaStoreData) -> Unit) {
        listenerDeque.clear()
        listenerDeque.push(object : MediaPickerListener {
            override fun onMediaPicked(selectedMediaList: ArrayList<MediaStoreData>, mediaStoreData: MediaStoreData) {
                action(mediaStoreData)
            }
        })

        val activity = activityWeakRef.get()
        val fragment = fragmentWeakRef.get()

        when {
            activity != null -> openCamera(activity)
            fragment != null -> openCamera(fragment.requireActivity())
            else -> {
                listenerDeque.clear()
                throw NullPointerException("activity or fragment can't be null.")
            }
        }
    }

    private fun openCamera(activity: Activity) {
        with(activity) {
            startActivity(Intent(this, CameraActivity::class.java).apply {
                putExtra(EXTRA_ENABLE_COMPRESSION, isCompressionEnable)
            })
        }
    }

    private fun startPicker() {
        val activity = activityWeakRef.get()
        val fragment = fragmentWeakRef.get()

        when {
            activity != null -> activity.startActivity(getIntentWithData(activity))
            fragment != null -> fragment.startActivity(getIntentWithData(fragment.requireContext()))
            else -> {
                listenerDeque.clear()
                throw NullPointerException("activity or fragment can't be null.")
            }
        }
    }

    private fun getIntentWithData(context: Context?): Intent {
        return Intent(context, PickerActivity::class.java).apply {
            putExtra(EXTRA_MEDIA_TYPE, mediaType)
            putExtra(EXTRA_MULTI_SELECTION, isMultiSelection)
            putExtra(EXTRA_INCLUDES_FILE_PATH, includesFilePath)
            putExtra(EXTRA_ENABLE_COMPRESSION, isCompressionEnable)
            putExtra(EXTRA_TOOLBAR_COLOR, toolBarColor)
            putExtra(EXTRA_MAX_COUNT, maxCount)
        }
    }
}