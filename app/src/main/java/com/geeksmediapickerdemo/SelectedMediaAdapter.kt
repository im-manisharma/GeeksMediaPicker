package com.geeksmediapickerdemo

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.geeksmediapicker.models.MediaStoreData
import com.geeksmediapickerdemo.databinding.ListItemSelectedMediaBinding
import java.util.*

class SelectedMediaAdapter(
    private val list: ArrayList<MediaStoreData>
) : RecyclerView.Adapter<SelectedMediaAdapter.MyViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return MyViewHolder(ListItemSelectedMediaBinding.inflate(inflater))
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) = holder.bind(position)

    inner class MyViewHolder(val binding: ListItemSelectedMediaBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(position: Int) {
            val model = list[position]

            with(itemView){
                Glide.with(binding.ivProductImage.context)
                    .load(model.content_uri)
                    .thumbnail(0.3f)
                    .into(binding.ivProductImage)
                //ivProductImage.setImageURI(model.content_uri)
                binding.tvMediaName.text = "Name: ${model.media_name}"
                binding.tvMediaMimeType.text = "Type: ${model.media_type}"
                binding.tvMediaUri.text = "Uri: ${model.content_uri}"
                binding.tvMediaPath.text = "Path: ${model.media_path}"
            }
        }
    }
}
