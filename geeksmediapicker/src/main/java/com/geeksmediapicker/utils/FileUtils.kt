package com.geeksmediapicker.utils

import android.content.ContentResolver
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build
import android.provider.OpenableColumns
import android.util.Log
import android.util.Size
import java.io.File
import java.io.FileOutputStream

object FileUtils {

    private const val TAG = "FileUtil"

    fun getFilePath(context: Context, uri: Uri): String {
        var filePath = ""
        val contentResolver = context.contentResolver
        val fileName = getFileName(contentResolver, uri)
        //Log.e(TAG, "file name: $fileName")
        if (fileName.isNotEmpty()) {
            contentResolver.openInputStream(uri)?.use { inputStream ->
                val file = File(context.getExternalFilesDir(null), fileName)
                Log.e(TAG, "file path: ${file.absolutePath}")
                val outputStream = FileOutputStream(file)
                inputStream.copyTo(outputStream)
                filePath = file.absolutePath
            }
        }
        return filePath
    }

    fun getFileName(contentResolver: ContentResolver, uri: Uri): String {
        var name = ""
        contentResolver.query(uri, null, null, null, null)?.use {
            val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (it.moveToFirst()) {
                name = it.getString(nameIndex)
            }
        }
        return name
    }

    fun getVideoThumb(context: Context, videoUri: Uri): String {
        val bitmapList = getVideoThumbBitmaps(context, videoUri)
        if (bitmapList.isNotEmpty()){
            val filePath = File(context.getExternalFilesDir(null), "Thumbnails")

            //Log.e("Check", "cache file path : ${filePath.absolutePath}")

            if (!filePath.exists()) {
                filePath.mkdirs()
                //Log.e("Check", "File path created")
            }

            val outFile = File(filePath, "thumb_${System.currentTimeMillis()}.jpg")
            val fileOutputStream = FileOutputStream(outFile)
            bitmapList[0].compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream)

            fileOutputStream.flush()
            fileOutputStream.close()

            return outFile.absolutePath
        }

        return ""
    }

    private fun getVideoThumbBitmaps(context: Context, videoUri: Uri): ArrayList<Bitmap> {

        val thumbnailList = ArrayList<Bitmap>()

        try {
            val mdr = MediaMetadataRetriever()
            mdr.setDataSource(context, videoUri)

            // Retrieve media data
            val videoLengthInMs = mdr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION).toLong() * 1000

            // Set thumbnail properties (Thumbs are squares)
            val thumbWidth = 512
            val thumbHeight = 512

            val numThumbs = 10

            val interval = videoLengthInMs / numThumbs

            for (i in 0 until numThumbs) {
                try {
                    var bitmap = mdr.getFrameAtTime(i * interval, MediaMetadataRetriever.OPTION_CLOSEST_SYNC)
                    bitmap = Bitmap.createScaledBitmap(bitmap, thumbWidth, thumbHeight, false)
                    thumbnailList.add(bitmap)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            mdr.release()
        }catch (e: Exception) {
            e.printStackTrace()
        }

        return thumbnailList
    }




}