package com.geeksmediapickerdemo

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.geeksmediapicker.models.MediaStoreAlbum
import com.geeksmediapicker.models.MediaStoreData
import kotlinx.android.synthetic.main.list_item_selected_media.view.*
import java.util.*

class SelectedMediaAdapter(
    private val list: ArrayList<MediaStoreData>
) : RecyclerView.Adapter<SelectedMediaAdapter.MyViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return MyViewHolder(inflater.inflate(R.layout.list_item_selected_media, parent, false))
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) = holder.bind(position)

    inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(position: Int) {
            val model = list[position]

            with(itemView){
                Glide.with(ivProductImage.context)
                    .load(model.content_uri)
                    .thumbnail(0.3f)
                    .into(ivProductImage)
                //ivProductImage.setImageURI(model.content_uri)
                tvMediaName.text = "Name: ${model.media_name}"
                tvMediaMimeType.text = "Type: ${model.media_type}"
                tvMediaUri.text = "Uri: ${model.content_uri}"
                tvMediaPath.text = "Path: ${model.media_path}"
            }
        }
    }
}
