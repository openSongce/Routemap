package com.example.rootmap

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.rootmap.databinding.ItemTouristBinding

class TouristAdapter(private val items: List<TouristItem>) : RecyclerView.Adapter<TouristAdapter.TouristViewHolder>() {

    inner class TouristViewHolder(private val binding: ItemTouristBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: TouristItem) {
            binding.title.text = item.title
            binding.addr1.text = item.addr1
            binding.addr2.text = item.addr2 ?: ""

            Glide.with(binding.root.context)
                .load(item.firstimage)
                .into(binding.image)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TouristViewHolder {
        val binding = ItemTouristBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TouristViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TouristViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size
}
