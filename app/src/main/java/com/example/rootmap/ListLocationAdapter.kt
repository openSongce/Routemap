package com.example.rootmap

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.ItemTouchHelper.DOWN
import androidx.recyclerview.widget.ItemTouchHelper.UP
import androidx.recyclerview.widget.RecyclerView
import com.example.rootmap.databinding.FriendLayoutBinding
import com.example.rootmap.databinding.LocationListLayoutBinding
import com.example.rootmap.databinding.RouteaddLayoutBinding
import java.util.Collections

class ListLocationAdapter : RecyclerView.Adapter<ListLocationAdapter.Holder>()  {
    var list = mutableListOf<MyLocation>()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val binding =
            LocationListLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return Holder(binding)
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        var screen = list.get(position)

        holder.setData(screen,position)
    }
    override fun getItemCount(): Int {
        return list.size
    }
    inner class Holder(
        val binding: LocationListLayoutBinding
    ) :
        RecyclerView.ViewHolder(binding.root) {
        fun setData(myLocation: MyLocation,position: Int) {
            binding.triplocationName.text=myLocation.name
        }
    }
    fun removeData(position: Int) {
        list.removeAt(position)
        notifyItemRemoved(position)
    }
    // 현재 선택된 데이터와 드래그한 위치에 있는 데이터를 교환
    fun swapData(fromPos: Int, toPos: Int) {
        Collections.swap(list, fromPos, toPos)
        notifyItemMoved(fromPos, toPos)
    }
}
class DragManageAdapter(private var recyclerViewAdapter : ListLocationAdapter) : ItemTouchHelper.Callback() {
    override fun getMovementFlags(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder
    ): Int {
        return makeMovementFlags(UP or DOWN,0)
    }

    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean {
        val fromPos: Int = viewHolder.adapterPosition
        val toPos: Int = target.adapterPosition
        recyclerViewAdapter.swapData(fromPos, toPos)
        return true
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        TODO("Not yet implemented")
    }
}
