package com.example.rootmap

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.rootmap.databinding.FriendLayoutBinding
import com.example.rootmap.databinding.LocationListLayoutBinding
import com.example.rootmap.databinding.RouteaddLayoutBinding

class ListLocationAdapter : RecyclerView.Adapter<ListLocationAdapter.Holder>()  {
    var list = mutableListOf<MyLocation>()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val binding =
            LocationListLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return Holder(binding)
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        var screen = list.get(position)
        holder.setData(screen)
    }
    override fun getItemCount(): Int {
        return list.size
    }
    inner class Holder(
        val binding: LocationListLayoutBinding
    ) :
        RecyclerView.ViewHolder(binding.root) {
        fun setData(myLocation: MyLocation) {
            binding.textView3.text=myLocation.name
           // binding.imageButton2.setOnClickListener {
            //    itemClickListener.onClick(it, position)
           // }
        }
    }
    interface OnItemClickListener {
        fun onClick(v: View, position: Int)
    }
    fun setItemClickListener(onItemClickListener: OnItemClickListener) {
        this.itemClickListener = onItemClickListener
    }

    private lateinit var itemClickListener : OnItemClickListener
}
