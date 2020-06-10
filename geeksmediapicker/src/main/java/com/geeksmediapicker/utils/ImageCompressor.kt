package com.geeksmediapicker.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.util.Log
import androidx.exifinterface.media.ExifInterface
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream

object ImageCompressor {

    private const val MAX_WIDTH = 612
    private const val MAX_HEIGHT = 816
    private const val IMAGE_QUALITY = 80


    suspend fun getCompressedImage(context: Context, uri: Uri?, success: (String) -> Unit) {
        if (uri == null) {
            throw NullPointerException("uri can't be null.")
        }

        try {
            val compressedFilePath = compressImageFromUri(context, uri)
            success(compressedFilePath)
        } catch (e: Exception) {
            Log.e("ImageCompressor", "error --> ${e.localizedMessage}")
        }
    }

    private suspend fun compressImageFromUri(context: Context, uri: Uri): String {
        var filePath = ""

        withContext(Dispatchers.IO){
            val options = BitmapFactory.Options()
            // Here true means that we don’t want to load bitmap into memory.
            // We just want to get information(width, height, etc.) about image. So we can calculate scale factor with that information.
            options.inJustDecodeBounds = true


            val fileName = FileUtils.getFileName(context.contentResolver, uri)


            context.contentResolver.openInputStream(uri)?.use {inputStream1->
                BitmapFactory.decodeStream(inputStream1, null, options)

                // Calculate and set inSampleSize
                options.inSampleSize = calculateInSampleSize(
                    options,
                    MAX_WIDTH,
                    MAX_HEIGHT
                )
                // Decode bitmap with inSampleSize set
                options.inJustDecodeBounds = false

                context.contentResolver.openInputStream(uri)?.use {inputStream2 ->
                    BitmapFactory.decodeStream(inputStream2, null, options)?.also {bitmap ->

                        //Log.e("Check", "scaled bitmap size : ${bitmap.byteCount}")

                        context.contentResolver.openInputStream(uri)?.use {
                            val scaledBitmap = rotateImageIfRequired(bitmap, it)
                            val newFile = File(context.getExternalFilesDir(null), "compressed_$fileName")
                            //Log.e("Check", "cache file path : ${newFile.absolutePath}")

                            if (newFile.exists()) {
                                newFile.delete()
                                //Log.e("Check", "File deleted")
                            }

                            val fileOutputStream = FileOutputStream(newFile)
                            scaledBitmap?.compress(Bitmap.CompressFormat.JPEG, IMAGE_QUALITY, fileOutputStream)

                            //Log.e("Check", "Is bitmap compressed: $bitmapCompressed")
                            //Log.e("ImageCompressor", "resize scaled bitmap size : ${scaledBitmap?.byteCount}")

                            fileOutputStream.flush()
                            fileOutputStream.close()

                            filePath = newFile.absolutePath
                        }
                    }
                }

            }

        }
        return filePath
    }

    private fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
        // Raw height and width of image
        val (height: Int, width: Int) = options.run { outHeight to outWidth }
        var inSampleSize = 1

        if (height > reqHeight || width > reqWidth) {

            val halfHeight: Int = height / 2
            val halfWidth: Int = width / 2

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
                inSampleSize *= 2
            }
        }

        return inSampleSize
    }

    /**
     * Rotate an image if required.
     *
     * @param bitmap            The image bitmap
     * @param inputStream       Image InputStream
     * @return The resulted Bitmap after manipulation
     */
    @Throws(IOException::class)
    private fun rotateImageIfRequired(bitmap: Bitmap, inputStream: InputStream): Bitmap? {
        return when (ExifInterface(inputStream).getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)) {
            ExifInterface.ORIENTATION_ROTATE_90 -> rotateImage(bitmap, 90)
            ExifInterface.ORIENTATION_ROTATE_180 -> rotateImage(bitmap, 180)
            ExifInterface.ORIENTATION_ROTATE_270 -> rotateImage(bitmap, 270)
            else -> bitmap
        }
    }

    private fun rotateImage(img: Bitmap, degree: Int): Bitmap? {
        val matrix = Matrix()
        matrix.postRotate(degree.toFloat())
        val rotatedImg = Bitmap.createBitmap(img, 0, 0, img.width, img.height, matrix, true)
        img.recycle()
        return rotatedImg
    }
}