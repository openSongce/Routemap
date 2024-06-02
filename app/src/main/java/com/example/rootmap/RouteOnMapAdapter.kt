package com.example.rootmap

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.rootmap.databinding.RouteOnMapLayoutBinding

class RouteOnMapAdapter: RecyclerView.Adapter<RouteOnMapAdapter.Holder>() {
    var list = mutableListOf<MyLocation>()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RouteOnMapAdapter.Holder {
        val binding = RouteOnMapLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return Holder(binding)
    }

    override fun onBindViewHolder(holder: RouteOnMapAdapter.Holder, position: Int) {
        var screen = list.get(position)
        holder.setData(screen, position)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    inner class Holder(val binding: RouteOnMapLayoutBinding): RecyclerView.ViewHolder(binding.root){
        fun setData(myLocation: MyLocation, position: Int) {
            binding.locationName.text=myLocation.name
            binding.number.text=position.toString() + ". "
            binding.root.setOnClickListener {
                itemClickListener.onClick(it, position)
            }
            binding.viewInfo.setOnClickListener {
                itemClickListener.onButtonClick(it, position)
            }
        }
    }
    interface OnItemClickListener {
        fun onClick(v: View, position: Int)
        fun onButtonClick(v: View, position: Int)
    }
    fun setItemClickListener(onItemClickListener: RouteListAdapter.OnItemClickListener) {
        this.itemClickListener = onItemClickListener
    }

    private lateinit var itemClickListener : RouteListAdapter.OnItemClickListener
}