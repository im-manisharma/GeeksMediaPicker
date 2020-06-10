package com.geeksmediapicker.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.geeksmediapicker.databinding.ListItemAlbumBinding
import com.geeksmediapicker.interfaces.ItemClickListener
import com.geeksmediapicker.models.MediaStoreAlbum
import java.util.*

class AlbumAdapter(
    private val list: ArrayList<MediaStoreAlbum>,
    private val itemClickListener: ItemClickListener
) : RecyclerView.Adapter<AlbumAdapter.MyViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return MyViewHolder(ListItemAlbumBinding.inflate(inflater))
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) = holder.bind(position)

    inner class MyViewHolder(private val binding: ListItemAlbumBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(position: Int) {
            val model = list[position]
            binding.albumData = model
            binding.itemPosition = position
            binding.clickListener = itemClickListener
            binding.executePendingBindings()
        }
    }
}
