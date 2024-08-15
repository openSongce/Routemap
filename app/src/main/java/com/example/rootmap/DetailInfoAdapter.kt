package com.example.rootmap

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.rootmap.databinding.ItemDetailInfoBinding

class DetailInfoAdapter(private var items: List<DetailInfoItem>) :
    RecyclerView.Adapter<DetailInfoAdapter.DetailInfoViewHolder>() {

    inner class DetailInfoViewHolder(private val binding: ItemDetailInfoBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: DetailInfoItem) {
            binding.subname.text = item.subname
            binding.subdetailoverview.text = item.subdetailoverview
            binding.subdetailalt.text = item.subdetailalt

            val imageUrl = item.subdetailimg?.replace("http://", "https://")
            if (!imageUrl.isNullOrEmpty()) {
                Glide.with(binding.root.context)
                    .load(imageUrl)
                    .placeholder(R.drawable.map)
                    .error(R.drawable.map)
                    .into(binding.subdetailimg)
            } else {
                binding.subdetailimg.setImageResource(R.drawable.map)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DetailInfoViewHolder {
        val binding = ItemDetailInfoBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return DetailInfoViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DetailInfoViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    fun updateItems(newItems: List<DetailInfoItem>) {
        items = newItems
        notifyDataSetChanged()
    }
}
