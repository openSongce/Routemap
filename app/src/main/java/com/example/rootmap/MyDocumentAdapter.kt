package com.example.rootmap

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.rootmap.databinding.FriendLayoutBinding
import com.example.rootmap.databinding.RoutelistLayoutBinding

class MyDocumentAdapter() : RecyclerView.Adapter<MyDocumentAdapter.Holder>()  {
    var list = mutableListOf<MyRouteDocument>()
    lateinit var mode:String
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val binding =
            FriendLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return Holder(binding,mode)
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        var screen = list.get(position)
        holder.setData(screen)
    }
    override fun getItemCount(): Int {
        return list.size
    }
    inner class Holder(
        val binding: FriendLayoutBinding,
        val mode:String
    ) :
        RecyclerView.ViewHolder(binding.root) {
        fun setData(myRouteDocument: MyRouteDocument) {
           // binding.friendName.text=myRouteDocument.docName
            binding.apply {
                friendName.text=myRouteDocument.docName
                picture.visibility=View.GONE
                friendId.visibility=View.GONE
            }
            if(mode=="View"){
                binding.friendButton2.text="보기"
                binding.friendButton2.setOnClickListener {
                    itemClickListener.onListClick(it, position)
                }
            }else{
                binding.friendButton2.text="추가"
                binding.friendButton2.setOnClickListener {
                    itemClickListener.onClick(it, position)
                }
            }

        }
    }
    interface OnItemClickListener {
        fun onClick(v: View, position: Int)
        fun onListClick(v: View, position: Int)
    }
    fun setItemClickListener(onItemClickListener: OnItemClickListener) {
        this.itemClickListener = onItemClickListener
    }

    private lateinit var itemClickListener : OnItemClickListener
}

