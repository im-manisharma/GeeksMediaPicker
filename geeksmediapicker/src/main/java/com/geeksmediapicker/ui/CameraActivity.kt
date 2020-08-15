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
import com.geeksmediapicker.GeeksMediaPicker
import com.geeksmediapicker.GeeksMediaType
import com.geeksmediapicker.R
import com.geeksmediapicker.interfaces.MediaPickerListener
import com.geeksmediapicker.models.MediaStoreData
import com.geeksmediapicker.utils.ImageCompressor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class CameraActivity : AppCompatActivity() {

    private var capturedImagePath = ""
    private val REQ_CODE_CAPTURE_IMAGE: Int = 1110
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
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.CAMERA),
                REQ_CODE_CAPTURE_IMAGE
            )
        }
    }

    private fun isPermissionGranted(context: Context) =
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED


    private fun openCamera() {
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        val timeStamp = SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault()).format(Date())
        val fileName = "Image_$timeStamp"
        val directory = File(getExternalFilesDir(null), "Camera")
        if (!directory.exists()) {
            directory.mkdir()
        }

        capturedImagePath = ""
        val file = File.createTempFile(fileName, ".jpg", directory)
        capturedImagePath = file.absolutePath

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
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQ_CODE_CAPTURE_IMAGE && resultCode == Activity.RESULT_OK) {
            val mediaPickerListener: MediaPickerListener = GeeksMediaPicker.listenerDeque.pop()
            GeeksMediaPicker.listenerDeque.clear()

            val mediaUri = Uri.fromFile(File(capturedImagePath))
            if (isCompressionEnable) {
                GlobalScope.launch {
                    ImageCompressor.getCompressedImage(this@CameraActivity, mediaUri) { filePath ->

                        launch(Dispatchers.Main) {
                            val mediaStoreData = MediaStoreData(
                                media_type = GeeksMediaType.IMAGE,
                                media_path = filePath,
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
                    media_path = capturedImagePath,
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