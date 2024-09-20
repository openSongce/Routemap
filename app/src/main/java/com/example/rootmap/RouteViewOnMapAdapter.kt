package com.example.rootmap

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.rootmap.databinding.RouteViewOnMapLayoutBinding

class RouteViewOnMapAdapter: RecyclerView.Adapter<RouteViewOnMapAdapter.Holder>() {
    var list = mutableListOf<MyLocation>()
    lateinit var parent: ViewGroup
    //lateinit var myDb: CollectionReference
    lateinit var docId:String

    //var postView=false
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        this.parent = parent
        val binding =
            RouteViewOnMapLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return Holder(binding)
    }
    override fun onBindViewHolder(holder: Holder, position: Int) {
        var screen = list[position]
        holder.setData(screen,position)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    inner class Holder(
        val binding: RouteViewOnMapLayoutBinding
    ) :
        RecyclerView.ViewHolder(binding.root) {
        //  fun bind

        fun setData(myLocation: MyLocation, position: Int) {
            binding.locationName.text = myLocation.name
        }
    }

    interface OnItemClickListener {
        fun onClick(v: View, position: Int)
    }

    fun setItemClickListener(onItemClickListener: OnItemClickListener) {
        this.itemClickListener = onItemClickListener
    }

    private lateinit var itemClickListener: OnItemClickListener

}