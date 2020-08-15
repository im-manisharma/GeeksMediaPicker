package com.geeksmediapickerdemo

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.geeksmediapicker.GeeksMediaPicker
import com.geeksmediapicker.GeeksMediaType
import com.geeksmediapicker.models.MediaStoreData
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {


    private val mediaList = ArrayList<MediaStoreData>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        inItAdapter()

        val includesFilePath = cbIncludesPath.isChecked
        val isCompressionEnable = cbEnableImageCompression.isChecked

        startPickerBtn.setOnClickListener {

            val mediaType = if (rbImage.isChecked) {
                GeeksMediaType.IMAGE
            } else {
                GeeksMediaType.VIDEO
            }




            if (rbSingle.isChecked) {
                GeeksMediaPicker.with(this)
                    .setMediaType(mediaType)
                    .setIncludesFilePath(includesFilePath)
                    .setEnableCompression(isCompressionEnable)
                    .startSingle { data ->
                        mediaList.clear()
                        mediaList.add(data)
                        rvSelectedMedia.adapter?.notifyDataSetChanged()
                        //Log.e("My TAG", "${data}")
                    }
            } else {
                GeeksMediaPicker.with(this)
                    .setMediaType(mediaType)
                    .setIncludesFilePath(includesFilePath)
                    .setEnableCompression(isCompressionEnable)
                    .startMultiple { dataList ->
                        mediaList.clear()
                        mediaList.addAll(dataList)
                        rvSelectedMedia.adapter?.notifyDataSetChanged()
                        //Log.e("My TAG", "${dataList}")
                    }
            }
        }

        startCameraBtn.setOnClickListener {
            if (rbVideo.isChecked) {
                Toast.makeText(this, "Under Development", Toast.LENGTH_SHORT).show()
            }else {
                GeeksMediaPicker.with(this)
                    .setEnableCompression(isCompressionEnable)
                    .startCamera { mediaStoreData ->
                        Log.e("file path", mediaStoreData.media_path)
                        mediaList.clear()
                        mediaList.add(mediaStoreData)
                        rvSelectedMedia.adapter?.notifyDataSetChanged()
                    }
            }
        }
    }

    private fun inItAdapter(){
        rvSelectedMedia.adapter = SelectedMediaAdapter(mediaList)
    }
}
