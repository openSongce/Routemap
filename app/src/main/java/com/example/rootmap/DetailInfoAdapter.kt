package com.example.rootmap

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.rootmap.databinding.ItemDetailInfoBinding

class DetailInfoAdapter(private var items: List<DetailInfoItem>) :
    RecyclerView.Adapter<DetailInfoAdapter.DetailInfoViewHolder>() {

    inner class DetailInfoViewHolder(private val binding: ItemDetailInfoBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: DetailInfoItem, isLastItem: Boolean) {
            val courseNumber = item.subnum?.plus(1) ?: 1
            binding.subnumText.text = "제 ${courseNumber} 코스"

            binding.subname.text = item.subname

            // 코스개요에서 <br /> 태그 제거
            val cleanedOverview = item.subdetailoverview?.replace("<br />", "")
            binding.subdetailoverview.text = cleanedOverview

            binding.arrowImage.visibility = if (isLastItem) View.GONE else View.VISIBLE
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DetailInfoViewHolder {
        val binding = ItemDetailInfoBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return DetailInfoViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DetailInfoViewHolder, position: Int) {
        val isLastItem = position == items.size - 1
        holder.bind(items[position], isLastItem)
    }

    override fun getItemCount(): Int = items.size

    fun updateItems(newItems: List<DetailInfoItem>) {
        items = newItems
        notifyDataSetChanged()
    }
}
