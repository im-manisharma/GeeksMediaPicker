package com.geeksmediapicker

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.fragment.app.Fragment
import com.geeksmediapicker.interfaces.MediaPickerListener
import com.geeksmediapicker.models.MediaStoreData
import com.geeksmediapicker.ui.PickerActivity
import java.lang.ref.WeakReference
import java.util.*
import kotlin.collections.ArrayList

class GeeksMediaPicker private constructor(activity: Activity?, fragment: Fragment?) {

    private val activityWeakRef: WeakReference<Activity?> = WeakReference(activity)
    private val fragmentWeakRef: WeakReference<Fragment?> = WeakReference(fragment)

    companion object {
        const val EXTRA_MEDIA_TYPE = "MEDIA_TYPE"
        const val EXTRA_MULTI_SELECTION = "MULTI_SELECTION"
        const val EXTRA_INCLUDES_FILE_PATH = "INCLUDES_FILE_PATH"
        const val EXTRA_ENABLE_COMPRESSION = "ENABLE_COMPRESSION"
        const val EXTRA_TOOLBAR_COLOR = "TOOLBAR_COLOR"

        @JvmStatic
        fun with(activity: Activity) = GeeksMediaPicker(activity, null)

        @JvmStatic
        fun with(fragment: Fragment) = GeeksMediaPicker(null, fragment)

        @JvmStatic
        val listenerDeque: Deque<MediaPickerListener> = ArrayDeque()
    }


    private var maxCount: Int = 0
    private var minCount: Int = 0
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

    private fun setMaxCount(maxCount: Int): GeeksMediaPicker {
        this.maxCount = maxCount
        return this
    }

    private fun setMinCount(minCount: Int): GeeksMediaPicker {
        this.minCount = minCount
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
            override fun onMediaPicked(selectedMediaList: ArrayList<MediaStoreData>) {
                action(selectedMediaList[0])
            }
        })

        isMultiSelection = false
        startPicker()
    }

    fun startMultiple(action: (ArrayList<MediaStoreData>) -> Unit) {
        listenerDeque.clear()
        listenerDeque.push(object : MediaPickerListener {
            override fun onMediaPicked(selectedMediaList: ArrayList<MediaStoreData>) {
                action(selectedMediaList)
            }
        })

        isMultiSelection = true
        startPicker()
    }

    private fun startPicker(/*reqCode: Int*/) {
        val activity = activityWeakRef.get()
        val fragment = fragmentWeakRef.get()

        when {
            activity != null -> activity.startActivity(getIntentWithData(activity))
            fragment != null -> fragment.startActivity(getIntentWithData(fragment.context))
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
        }
    }
}