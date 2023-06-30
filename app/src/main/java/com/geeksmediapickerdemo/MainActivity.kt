package com.geeksmediapickerdemo

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.geeksmediapicker.GeeksMediaPicker
import com.geeksmediapicker.GeeksMediaType
import com.geeksmediapicker.models.MediaStoreData
import com.geeksmediapickerdemo.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {


    private lateinit var binding : ActivityMainBinding
    private val mediaList = ArrayList<MediaStoreData>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        inItAdapter()
        inItListener()
    }

    private fun inItAdapter(){
        binding.rvSelectedMedia.adapter = SelectedMediaAdapter(mediaList)
    }

    private fun inItListener() {
        binding.startPickerBtn.setOnClickListener {

            val includesFilePath = binding.cbIncludesPath.isChecked
            val isCompressionEnable = binding.cbEnableImageCompression.isChecked

            val mediaType = if (binding.rbImage.isChecked) {
                GeeksMediaType.IMAGE
            } else {
                GeeksMediaType.VIDEO
            }

            if (binding.rbSingle.isChecked) {
                GeeksMediaPicker.with(this)
                    .setMediaType(mediaType)
                    .setIncludesFilePath(includesFilePath)
                    .setEnableCompression(isCompressionEnable)
                    .startSingle { data ->
                        mediaList.clear()
                        mediaList.add(data)
                        binding.rvSelectedMedia.adapter?.notifyDataSetChanged()
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
                        binding.rvSelectedMedia.adapter?.notifyDataSetChanged()
                        //Log.e("My TAG", "${dataList}")
                    }
            }
        }

        binding.startCameraBtn.setOnClickListener {
            val isCompressionEnable = binding.cbEnableImageCompression.isChecked
            if (binding.rbVideo.isChecked) {
                Toast.makeText(this, "Under Development", Toast.LENGTH_SHORT).show()
            }else {
                GeeksMediaPicker.with(this)
                    .setEnableCompression(isCompressionEnable)
                    .startCamera { mediaStoreData ->
                        mediaList.clear()
                        mediaList.add(mediaStoreData)
                        binding.rvSelectedMedia.adapter?.notifyDataSetChanged()
                    }
            }
        }
    }
}
