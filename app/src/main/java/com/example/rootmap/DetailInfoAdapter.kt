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

            // 코스개요에서 <br /> 태그 제거
            val cleanedOverview = item.subdetailoverview?.replace("<br />", "")
            binding.subdetailoverview.text = cleanedOverview
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
