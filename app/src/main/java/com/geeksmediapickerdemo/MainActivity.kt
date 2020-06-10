package com.geeksmediapickerdemo

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.geeksmediapicker.GeeksMediaPicker
import com.geeksmediapicker.models.MediaStoreData
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {


    private val mediaList = ArrayList<MediaStoreData>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        inItAdapter()

        startPickerBtn.setOnClickListener {

            val mediaType = if (rbImage.isChecked) {
                GeeksMediaPicker.TYPE_IMAGE
            } else {
                GeeksMediaPicker.TYPE_VIDEO
            }

            val includesFilePath = cbIncludesPath.isChecked
            val isCompressionEnable = cbEnableImageCompression.isChecked


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
    }

    private fun inItAdapter(){
        rvSelectedMedia.adapter = SelectedMediaAdapter(mediaList)
    }
}
