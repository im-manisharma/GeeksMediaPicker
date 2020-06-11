package com.geeksmediapicker.ui

import android.app.Application
import android.content.ContentProvider
import android.content.ContentResolver
import android.content.ContentUris
import android.database.ContentObserver
import android.database.Cursor
import android.provider.MediaStore
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.geeksmediapicker.GeeksMediaPicker
import com.geeksmediapicker.GeeksMediaType
import com.geeksmediapicker.models.MediaStoreData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*
import java.util.concurrent.TimeUnit

class PickerActivityVM(application: Application) : AndroidViewModel(application) {

    private val TAG = "PickerActivityVM"

    private val _mediaList = MutableLiveData<List<MediaStoreData>>()
    val mediaList: LiveData<List<MediaStoreData>> get() = _mediaList

    private var contentObserver: ContentObserver? = null


    /**
     * Performs a one shot load of images from [MediaStore.Images.Media.EXTERNAL_CONTENT_URI] into
     * the [_mediaList] [LiveData] above.
     */
    fun loadImages(mediaType: String) {
        viewModelScope.launch {
            val mediaList = if (mediaType == GeeksMediaType.IMAGE) queryImages() else queryVideos()
            _mediaList.postValue(mediaList)
        }
    }

    private suspend fun queryImages(): List<MediaStoreData> {
        val images = mutableListOf<MediaStoreData>()

        /**
         * Working with [ContentResolver]s can be slow, so we'll do this off the main
         * thread inside a coroutine.
         */
        withContext(Dispatchers.IO) {

            /**
             * A key concept when working with Android [ContentProvider]s is something called
             * "projections". A projection is the list of columns to request from the provider,
             * and can be thought of (quite accurately) as the "SELECT ..." clause of a SQL
             * statement.
             *
             * It's not _required_ to provide a projection. In this case, one could pass `null`
             * in place of `projection` in the call to [ContentResolver.query], but requesting
             * more data than is required has a performance impact.
             *
             * For this sample, we only use a few columns of data, and so we'll request just a
             * subset of columns.
             */

            val mediaUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            val columnMediaId = MediaStore.Images.Media._ID
            val columnMediaName = MediaStore.Images.Media.DISPLAY_NAME
            val columnBucketName = MediaStore.Images.Media.BUCKET_DISPLAY_NAME
            val columnBucketId = MediaStore.Images.Media.BUCKET_ID
            val columnMediaDateAdded = MediaStore.Images.Media.DATE_ADDED


            val projection = arrayOf(
                columnMediaId,
                columnMediaName,
                columnBucketId,
                columnBucketName,
                columnMediaDateAdded
            )

            /**
             * The `selection` is the "WHERE ..." clause of a SQL statement. It's also possible
             * to omit this by passing `null` in its place, and then all rows will be returned.
             * In this case we're using a selection based on the date the image was taken.
             *
             * Note that we've included a `?` in our selection. This stands in for a variable
             * which will be provided by the next variable.
             */
            val selection = MediaStore.Images.Media.SIZE + " > 0"

            /**
             * The `selectionArgs` is a list of values that will be filled in for each `?`
             * in the `selection`.
             */
            /*val selectionArgs = arrayOf(
                // Release day of the G1. :)
                dateToTimestamp(day = 22, month = 10, year = 2008).toString()
            )*/

            /**
             * Sort order to use. This can also be null, which will use the default sort
             * order. For [MediaStore.Images], the default sort order is ascending by date taken.
             */
            val sortOrder = "$columnMediaDateAdded DESC"

            getApplication<Application>().contentResolver.query(
                mediaUri,
                projection,
                selection,
                null,
                sortOrder
            )?.use { cursor ->

                /**
                 * In order to retrieve the data from the [Cursor] that's returned, we need to
                 * find which index matches each column that we're interested in.
                 *
                 * There are two ways to do this. The first is to use the method
                 * [Cursor.getColumnIndex] which returns -1 if the column ID isn't found. This
                 * is useful if the code is programmatically choosing which columns to request,
                 * but would like to use a single method to parse them into objects.
                 *
                 * In our case, since we know exactly which columns we'd like, and we know
                 * that they must be included (since they're all supported from API 1), we'll
                 * use [Cursor.getColumnIndexOrThrow]. This method will throw an
                 * [IllegalArgumentException] if the column named isn't found.
                 *
                 * In either case, while this method isn't slow, we'll want to cache the results
                 * to avoid having to look them up for each row.
                 */

                val indexColMediaId = cursor.getColumnIndexOrThrow(columnMediaId)
                val indexColMediaDateAdded = cursor.getColumnIndexOrThrow(columnMediaDateAdded)
                val indexColMediaName = cursor.getColumnIndexOrThrow(columnMediaName)
                val indexColBucketName = cursor.getColumnIndexOrThrow(columnBucketName)
                val indexColBucketId = cursor.getColumnIndexOrThrow(columnBucketId)


                while (cursor.moveToNext()) {

                    try{
                        // Here we'll use the column index that we found above.
                        val mediaId = cursor.getLong(indexColMediaId)
                        val dateModified = Date(TimeUnit.SECONDS.toMillis(cursor.getLong(indexColMediaDateAdded)))
                        val mediaName = cursor.getString(indexColMediaName)
                        val bucketName = cursor.getString(indexColBucketName)
                        val bucketId = cursor.getLong(indexColBucketId)


                        //Log.e(TAG, "Album name --> $bucketName  Album Id --> $bucketId")


                        /**
                         * This is one of the trickiest parts:
                         *
                         * Since we're accessing images (using
                         * [MediaStore.Images.Media.EXTERNAL_CONTENT_URI], we'll use that
                         * as the base URI and append the ID of the image to it.
                         *
                         * This is the exact same way to do it when working with [MediaStore.Video] and
                         * [MediaStore.Audio] as well. Whatever `Media.EXTERNAL_CONTENT_URI` you
                         * query to get the items is the base, and the ID is the document to
                         * request there.
                         */
                        val contentUri = ContentUris.withAppendedId(mediaUri, mediaId)
                        val image = MediaStoreData(
                            media_id = mediaId,
                            bucket_id = bucketId,
                            media_name = mediaName,
                            media_type = GeeksMediaType.IMAGE,
                            bucket_name = bucketName,
                            date_added = dateModified,
                            content_uri = contentUri
                        )
                        images.add(image)

                        // For debugging, we'll output the image objects we create to logcat.
                        //Log.v(TAG, "Added image: $image")
                    }catch (e: Exception){
                        e.printStackTrace()
                        Log.e("Exception", "Message -> ${e.localizedMessage}")
                    }

                }
            }
        }

        //Log.v(TAG, "Found ${images.size} images")
        return images
    }

    private suspend fun queryVideos(): List<MediaStoreData> {
        val videos = mutableListOf<MediaStoreData>()

        /**
         * Working with [ContentResolver]s can be slow, so we'll do this off the main
         * thread inside a coroutine.
         */
        withContext(Dispatchers.IO) {

            /**
             * A key concept when working with Android [ContentProvider]s is something called
             * "projections". A projection is the list of columns to request from the provider,
             * and can be thought of (quite accurately) as the "SELECT ..." clause of a SQL
             * statement.
             *
             * It's not _required_ to provide a projection. In this case, one could pass `null`
             * in place of `projection` in the call to [ContentResolver.query], but requesting
             * more data than is required has a performance impact.
             *
             * For this sample, we only use a few columns of data, and so we'll request just a
             * subset of columns.
             */

            val mediaUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
            val columnMediaId = MediaStore.Video.Media._ID
            val columnMediaName = MediaStore.Video.Media.DISPLAY_NAME
            val columnBucketName = MediaStore.Video.Media.BUCKET_DISPLAY_NAME
            val columnBucketId = MediaStore.Video.Media.BUCKET_ID
            val columnMediaDateAdded = MediaStore.Video.Media.DATE_ADDED
            val columnMediaDuration = MediaStore.Video.Media.DURATION


            val projection = arrayOf(
                columnMediaId,
                columnMediaName,
                columnBucketId,
                columnBucketName,
                columnMediaDateAdded,
                columnMediaDuration
            )

            /**
             * The `selection` is the "WHERE ..." clause of a SQL statement. It's also possible
             * to omit this by passing `null` in its place, and then all rows will be returned.
             * In this case we're using a selection based on the date the image was taken.
             *
             * Note that we've included a `?` in our selection. This stands in for a variable
             * which will be provided by the next variable.
             */
            val selection = MediaStore.Images.Media.SIZE + " > 0"

            /**
             * The `selectionArgs` is a list of values that will be filled in for each `?`
             * in the `selection`.
             */
            /*val selectionArgs = arrayOf(
                // Release day of the G1. :)
                dateToTimestamp(day = 22, month = 10, year = 2008).toString()
            )*/

            /**
             * Sort order to use. This can also be null, which will use the default sort
             * order. For [MediaStore.Images], the default sort order is ascending by date taken.
             */
            val sortOrder = "$columnMediaDateAdded DESC"

            getApplication<Application>().contentResolver.query(
                mediaUri,
                projection,
                selection,
                null,
                sortOrder
            )?.use { cursor ->

                /**
                 * In order to retrieve the data from the [Cursor] that's returned, we need to
                 * find which index matches each column that we're interested in.
                 *
                 * There are two ways to do this. The first is to use the method
                 * [Cursor.getColumnIndex] which returns -1 if the column ID isn't found. This
                 * is useful if the code is programmatically choosing which columns to request,
                 * but would like to use a single method to parse them into objects.
                 *
                 * In our case, since we know exactly which columns we'd like, and we know
                 * that they must be included (since they're all supported from API 1), we'll
                 * use [Cursor.getColumnIndexOrThrow]. This method will throw an
                 * [IllegalArgumentException] if the column named isn't found.
                 *
                 * In either case, while this method isn't slow, we'll want to cache the results
                 * to avoid having to look them up for each row.
                 */

                val indexColMediaId = cursor.getColumnIndexOrThrow(columnMediaId)
                val indexColMediaDateAdded = cursor.getColumnIndexOrThrow(columnMediaDateAdded)
                val indexColMediaName = cursor.getColumnIndexOrThrow(columnMediaName)
                val indexColBucketName = cursor.getColumnIndexOrThrow(columnBucketName)
                val indexColBucketId = cursor.getColumnIndexOrThrow(columnBucketId)
                val indexColDuration = cursor.getColumnIndexOrThrow(columnMediaDuration)


                while (cursor.moveToNext()) {

                    try{
                        // Here we'll use the column index that we found above.
                        val mediaId = cursor.getLong(indexColMediaId)
                        val dateModified = Date(TimeUnit.SECONDS.toMillis(cursor.getLong(indexColMediaDateAdded)))
                        val mediaName = cursor.getString(indexColMediaName)
                        val bucketName = cursor.getString(indexColBucketName)
                        val bucketId = cursor.getLong(indexColBucketId)
                        val mediaDuration = cursor.getLong(indexColDuration)


                        //Log.e(TAG, "Album name --> $bucketName  Album Id --> $bucketId")


                        /**
                         * This is one of the trickiest parts:
                         *
                         * Since we're accessing images (using
                         * [MediaStore.Images.Media.EXTERNAL_CONTENT_URI], we'll use that
                         * as the base URI and append the ID of the image to it.
                         *
                         * This is the exact same way to do it when working with [MediaStore.Video] and
                         * [MediaStore.Audio] as well. Whatever `Media.EXTERNAL_CONTENT_URI` you
                         * query to get the items is the base, and the ID is the document to
                         * request there.
                         */
                        val contentUri = ContentUris.withAppendedId(mediaUri, mediaId)
                        val video = MediaStoreData(
                            media_id = mediaId,
                            bucket_id = bucketId,
                            media_name = mediaName,
                            media_type = GeeksMediaType.VIDEO,
                            bucket_name = bucketName,
                            date_added = dateModified,
                            content_uri = contentUri,
                            media_duration = mediaDuration
                        )
                        videos.add(video)

                        // For debugging, we'll output the image objects we create to logcat.
                        //Log.v(TAG, "Added image: $image")
                    }catch (e: Exception){
                        e.printStackTrace()
                        Log.e("Exception", "Message -> ${e.localizedMessage}")
                    }
                }
            }
        }

        //Log.v(TAG, "Found ${videos.size} Videos")
        return videos
    }
}