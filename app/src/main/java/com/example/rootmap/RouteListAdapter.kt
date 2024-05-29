package com.example.rootmap

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.rootmap.databinding.FragmentMenu3Binding
import com.example.rootmap.databinding.RoutelistLayoutBinding
import com.google.firebase.FirebaseException
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.tasks.await

class RouteListAdapter() : RecyclerView.Adapter<RouteListAdapter.Holder>()  {
    var list = mutableListOf<SearchLocation>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val binding =
            RoutelistLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return Holder(binding)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        var screen = list.get(position)
        holder.setData(screen)
    }
    inner class Holder(
        val binding: RoutelistLayoutBinding,
    ) :
        RecyclerView.ViewHolder(binding.root) {
        fun setData(searchLocation: SearchLocation) {
            binding.locationName.text=searchLocation.name
            binding.locationAdress.text=searchLocation.adress
            binding.root.setOnClickListener {
                itemClickListener.onClick(it, position)
            }
            binding.addLcationBt.setOnClickListener {
                itemClickListener.onButtonClick(it, position)
            }
        }
        }
    interface OnItemClickListener {
        fun onClick(v: View, position: Int)
        fun onButtonClick(v: View, position: Int)
    }

    fun setItemClickListener(onItemClickListener: OnItemClickListener) {
        this.itemClickListener = onItemClickListener
    }

    private lateinit var itemClickListener : OnItemClickListener
    }



