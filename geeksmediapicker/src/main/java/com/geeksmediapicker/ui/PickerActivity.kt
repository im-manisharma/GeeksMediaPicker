package com.geeksmediapicker.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.net.Uri
import android.os.Bundle
import android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.geeksmediapicker.GeeksMediaPicker
import com.geeksmediapicker.GeeksMediaPicker.Companion.EXTRA_ENABLE_COMPRESSION
import com.geeksmediapicker.GeeksMediaPicker.Companion.EXTRA_INCLUDES_FILE_PATH
import com.geeksmediapicker.GeeksMediaPicker.Companion.EXTRA_MAX_COUNT
import com.geeksmediapicker.GeeksMediaPicker.Companion.EXTRA_MEDIA_TYPE
import com.geeksmediapicker.GeeksMediaPicker.Companion.EXTRA_MULTI_SELECTION
import com.geeksmediapicker.GeeksMediaPicker.Companion.EXTRA_TOOLBAR_COLOR
import com.geeksmediapicker.GeeksMediaType
import com.geeksmediapicker.R
import com.geeksmediapicker.adapters.AlbumAdapter
import com.geeksmediapicker.adapters.GridSpacingItemDecoration
import com.geeksmediapicker.adapters.MediaAdapter
import com.geeksmediapicker.databinding.ActivityPickerBinding
import com.geeksmediapicker.interfaces.ItemClickListener
import com.geeksmediapicker.interfaces.MediaPickerListener
import com.geeksmediapicker.models.MediaStoreAlbum
import com.geeksmediapicker.models.MediaStoreData
import com.geeksmediapicker.utils.*
import kotlinx.coroutines.*

class PickerActivity : AppCompatActivity(), View.OnClickListener {

    private var selectedCount: Int = 0
    private var selectedItemPos: Int = -1
    private var selectedAlbumPos: Int = -1
    private var mediaList = ArrayList<MediaStoreData>()
    private val albumList = ArrayList<MediaStoreAlbum>()
    private lateinit var viewModel: PickerActivityVM
    private lateinit var binding: ActivityPickerBinding

    private val mediaType: String
        get() = intent.getStringExtra(EXTRA_MEDIA_TYPE) ?: GeeksMediaType.IMAGE

    private val isMultiSelection: Boolean
        get() = intent.getBooleanExtra(EXTRA_MULTI_SELECTION, false)

    private val includesFilePath: Boolean
        get() = intent.getBooleanExtra(EXTRA_INCLUDES_FILE_PATH, false)

    private val isCompressionEnable: Boolean
        get() = intent.getBooleanExtra(EXTRA_ENABLE_COMPRESSION, false)

    private val toolbarColor: Int
        get() = intent.getIntExtra(EXTRA_TOOLBAR_COLOR, 0)

    private val maxCount: Int
        get() = intent.getIntExtra(EXTRA_MAX_COUNT, -1)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_picker)
        viewModel = ViewModelProvider(this).get(PickerActivityVM::class.java)

        inItUI()
        inItControl()
        inItAdapter()
        inItObservable()
        startFetchingMedia()
    }

    private fun inItUI() {
        if (toolbarColor != -1) {
            binding.toolbarLayout.setBackgroundColor(toolbarColor)
        }
    }

    override fun onBackPressed() {
        if (binding.rvMedia.isVisible()) {
            albumList[selectedAlbumPos].medias.forEach {
                it.isSelected = false
            }
            showAlbumLayout()
        } else {
            super.onBackPressed()
        }
    }

    private fun inItControl() {
        binding.backBtn.setOnClickListener(this)
        binding.okBtn.setOnClickListener(this)
    }

    private fun showAlbumLayout() {
        selectedItemPos = -1
        selectedCount = 0
        binding.apply {
            rvMedia.gone()
            okBtn.invisible()
            rvAlbums.visible()
            tvTitle.text = getString(R.string.select_album)
        }
    }

    private fun showMediaLayout() {
        with(binding) {
            okBtn.visible()
            rvAlbums.gone()
            rvMedia.visible()
            tvTitle.text = if (isMultiSelection) {
                String.format("%d %s", selectedCount, getString(R.string.selected))
            } else {
                getString(R.string.please_select_a_media)
            }
        }
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.backBtn -> onBackPressed()
            R.id.okBtn -> if (isMultiSelection && selectedCount == 0) {
                showToast("Please select an item")
            } else {
                val selectedMediaList = ArrayList(mediaList.filter { it.isSelected })
                //Log.e("check", "${selectedMediaList.size}")
                if (selectedMediaList.isNotEmpty()) {
                    val mediaPickerListener: MediaPickerListener = GeeksMediaPicker.listenerDeque.pop()
                    GeeksMediaPicker.listenerDeque.clear()
                    if (isCompressionEnable && mediaType == GeeksMediaType.IMAGE) {
                        binding.layoutProgressBar.visible()
                        lifecycleScope.launch {
                            selectedMediaList.map {
                                async {
                                    ImageCompressor.getCompressedImage(
                                        this@PickerActivity,
                                        it.content_uri
                                    ) { filePath ->
                                        //Log.e("PickerActivity", "compressed image path -->> ${filePath}")
                                        it.media_path = filePath
                                    }
                                }
                            }.awaitAll()

                            launch(Dispatchers.Main) {
                                binding.layoutProgressBar.gone()
                                finish()
                                mediaPickerListener.onMediaPicked(selectedMediaList)
                            }
                        }
                    } else {
                        if (includesFilePath) {
                            for (media in selectedMediaList) {
                                if (media.content_uri != null) {
                                    media.media_path = FileUtils.getFilePath(this, media.content_uri!!)
                                }
                            }
                        }
                        mediaPickerListener.onMediaPicked(selectedMediaList)
                        finish()
                    }
                } else {
                    showToast("Please select an item")
                }
            }
        }
    }

    private fun inItAdapter() {
        with(binding.rvAlbums) {
            adapter = AlbumAdapter(albumList, object : ItemClickListener {
                override fun onClick(position: Int, event: Any?) {
                    selectedAlbumPos = position
                    mediaList.clear()
                    mediaList.addAll(albumList[position].medias)
                    //Log.e("list_size", "${mediaList.size}")
                    binding.rvMedia.adapter?.notifyDataSetChanged()
                    showMediaLayout()
                }
            })
            addItemDecoration(GridSpacingItemDecoration(2, 8))
        }

        with(binding.rvMedia) {
            adapter = MediaAdapter(mediaList, object : ItemClickListener {
                override fun onClick(position: Int, event: Any?) {
                    if (isMultiSelection) {
                        val isSelected = !mediaList[position].isSelected
                        if (isSelected) {
                            selectedCount += 1
                        } else {
                            selectedCount -= 1
                        }
                        //Log.e("My Check", "$selectedCount")
                        if (maxCount != -1 && selectedCount >= maxCount + 1 ) {
                            selectedCount -= 1
                            showToast("Can't select more than $maxCount media items.")
                        }else {
                            binding.tvTitle.text = String.format("%d %s", selectedCount, getString(R.string.selected))
                            mediaList[position].isSelected = isSelected
                            binding.rvMedia.adapter?.notifyItemChanged(position)
                        }
                    } else {
                        if (selectedItemPos == -1) {
                            mediaList[position].isSelected = true
                            binding.rvMedia.adapter?.notifyItemChanged(position)
                        } else {
                            mediaList[selectedItemPos].isSelected = false
                            binding.rvMedia.adapter?.notifyItemChanged(selectedItemPos)
                            mediaList[position].isSelected = true
                            binding.rvMedia.adapter?.notifyItemChanged(position)
                        }
                        selectedItemPos = position
                    }
                }
            })
            addItemDecoration(GridSpacingItemDecoration(3, 8))
        }
    }


    private fun inItObservable() {
        viewModel.mediaList.observe(this, { images ->
            val mAlbumList = images.groupBy { it.bucket_id }.map {
                MediaStoreAlbum(
                    bucket_id = it.key,
                    bucket_name = it.value[0].bucket_name,
                    imageUri = it.value[0].content_uri,
                    medias = it.value
                )
            }

            albumList.clear()
            albumList.addAll(mAlbumList)
            binding.rvAlbums.adapter?.notifyDataSetChanged()
        })
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQ_EXTERNAL_STORAGE -> {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.isNotEmpty() && grantResults[0] == PERMISSION_GRANTED) {
                    loadImages()
                } else {
                    // If we weren't granted the permission, check to see if we should show
                    // rationale for the permission.
                    val showRationale =
                        ActivityCompat.shouldShowRequestPermissionRationale(
                            this,
                            Manifest.permission.READ_EXTERNAL_STORAGE
                        )
                    if (showRationale) {
                        requestPermission()
                    } else {
                        showPermissionSettingDialog()
                    }
                }
                return
            }
        }
    }

    private fun loadImages() {
        viewModel.loadImages(mediaType, this)
    }

    private fun startFetchingMedia() {
        if (haveStoragePermission()) {
            loadImages()
        } else {
            requestPermission()
        }
    }

    private fun goToSettings() {
        Intent(ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:$packageName")).apply {
            addCategory(Intent.CATEGORY_DEFAULT)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }.also { intent ->
            startActivity(intent)
        }
    }

    /**
     * Convenience method to check if [Manifest.permission.READ_EXTERNAL_STORAGE] permission
     * has been granted to the app.
     */
    private fun haveStoragePermission() =
        ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.READ_EXTERNAL_STORAGE
        ) == PERMISSION_GRANTED

    /**
     * Convenience method to request [Manifest.permission.READ_EXTERNAL_STORAGE] permission.
     */
    private fun requestPermission() {
        ActivityCompat.requestPermissions(this, arrayOf(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        ), REQ_EXTERNAL_STORAGE)
    }
}

