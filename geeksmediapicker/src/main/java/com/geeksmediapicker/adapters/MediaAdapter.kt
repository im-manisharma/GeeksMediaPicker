package com.geeksmediapicker.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.geeksmediapicker.databinding.ListItemMediaBinding
import com.geeksmediapicker.interfaces.ItemClickListener
import com.geeksmediapicker.models.MediaStoreData
import java.util.*
import java.util.concurrent.TimeUnit

class MediaAdapter(
    private val list: ArrayList<MediaStoreData>,
    private val itemClickListener: ItemClickListener
) : RecyclerView.Adapter<MediaAdapter.MyViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return MyViewHolder(ListItemMediaBinding.inflate(inflater))
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) = holder.bind(position)

    inner class MyViewHolder(private val binding: ListItemMediaBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(position: Int) {
            val model = list[position]
            binding.mediaData = model
            binding.itemPosition = position
            binding.clickListener = itemClickListener

            val minutes = TimeUnit.MILLISECONDS.toMinutes(model.media_duration)
            val seconds = TimeUnit.MILLISECONDS.toSeconds(model.media_duration)
            binding.tvMediaDuration.text = String.format("%d:%d s", minutes, seconds)
            binding.executePendingBindings()
        }
    }
}
