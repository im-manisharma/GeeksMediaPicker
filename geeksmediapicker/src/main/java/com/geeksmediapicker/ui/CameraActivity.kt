package com.geeksmediapicker.ui

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import com.geeksmediapicker.GeeksMediaPicker
import com.geeksmediapicker.GeeksMediaType
import com.geeksmediapicker.R
import com.geeksmediapicker.interfaces.MediaPickerListener
import com.geeksmediapicker.models.MediaStoreData
import com.geeksmediapicker.utils.ImageCompressor
import com.geeksmediapicker.utils.REQ_CODE_CAPTURE_IMAGE
import com.geeksmediapicker.utils.showPermissionSettingDialog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.util.*

class CameraActivity : AppCompatActivity() {
    private var mImageName = ""
    private var mCapturedImagePath = ""
    private val isCompressionEnable: Boolean
        get() = intent.getBooleanExtra(GeeksMediaPicker.EXTRA_ENABLE_COMPRESSION, false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera)
        checkPermission()
    }

    private fun checkPermission() {
        if (isPermissionGranted(this)) {
            openCamera()
        } else {
            requestCameraPermission()
        }
    }

    private fun requestCameraPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.CAMERA),
            REQ_CODE_CAPTURE_IMAGE
        )
    }

    private fun isPermissionGranted(context: Context) =
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED

    private fun openCamera() {
        mImageName = ""
        mCapturedImagePath = ""

        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        val directory = File(getExternalFilesDir(null), "Camera")
        if (!directory.exists()) {
            directory.mkdir()
        }
        val file = File.createTempFile("Img_", ".jpg", directory)
        mImageName = file.name
        mCapturedImagePath = file.absolutePath

        val fileUri = FileProvider.getUriForFile(this, "${packageName}.provider", file)

        val resolvedIntentActivities = packageManager.queryIntentActivities(
            cameraIntent, PackageManager.MATCH_DEFAULT_ONLY
        )
        for (resolvedIntentInfo in resolvedIntentActivities) {
            val packageName = resolvedIntentInfo.activityInfo.packageName
            grantUriPermission(
                packageName,
                fileUri,
                Intent.FLAG_GRANT_WRITE_URI_PERMISSION or Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
        }
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri)
        startActivityForResult(cameraIntent, REQ_CODE_CAPTURE_IMAGE)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQ_CODE_CAPTURE_IMAGE && grantResults.isNotEmpty()) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCamera()
            } else if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
                val shouldShowRequestPermissionRationale =
                    ActivityCompat.shouldShowRequestPermissionRationale(
                        this,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    )
                if (shouldShowRequestPermissionRationale) {
                    requestCameraPermission()
                } else {
                    showPermissionSettingDialog()
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQ_CODE_CAPTURE_IMAGE && resultCode == Activity.RESULT_OK) {
            val mediaPickerListener: MediaPickerListener = GeeksMediaPicker.listenerDeque.pop()
            GeeksMediaPicker.listenerDeque.clear()

            val mediaUri = Uri.fromFile(File(mCapturedImagePath))

            if (isCompressionEnable) {
                lifecycleScope.launch {
                    ImageCompressor.getCompressedImage(this@CameraActivity, mediaUri) { filePath ->
                        launch(Dispatchers.Main) {
                            val mediaStoreData = MediaStoreData(
                                media_type = GeeksMediaType.IMAGE,
                                media_path = filePath,
                                media_name = File(filePath).name,
                                content_uri = Uri.fromFile(File(filePath))
                            )
                            mediaPickerListener.onMediaPicked(mediaStoreData = mediaStoreData)
                            finish()
                        }
                    }
                }
            } else {
                val mediaStoreData = MediaStoreData(
                    media_type = GeeksMediaType.IMAGE,
                    media_path = mCapturedImagePath,
                    media_name = mImageName,
                    content_uri = mediaUri
                )
                mediaPickerListener.onMediaPicked(mediaStoreData = mediaStoreData)
                finish()
            }
        } else {
            finish()
        }
    }
}