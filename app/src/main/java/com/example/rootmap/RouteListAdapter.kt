package com.example.rootmap

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.rootmap.databinding.RoutelistLayoutBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class RouteListAdapter : RecyclerView.Adapter<RouteListAdapter.Holder>() {
    var list = mutableListOf<SearchLocation>()
    var postList = mutableListOf<RoutePost>()
    var postMode = false

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val binding =
            RoutelistLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return Holder(binding)
    }

    override fun getItemCount(): Int {
        return if (postMode) postList.size else list.size
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        if (postMode) {
            val screen = postList[position]
            holder.postSetData(screen)
        } else {
            val screen = list[position]
            holder.setData(screen)
        }
    }

    inner class Holder(
        val binding: RoutelistLayoutBinding,
    ) : RecyclerView.ViewHolder(binding.root) {

        fun setData(searchLocation: SearchLocation) {
            binding.locationName.text = searchLocation.name
            binding.locationAdress.text = searchLocation.adress
            binding.root.setOnClickListener {
                itemClickListener.onClick(it, adapterPosition)
            }
            binding.addLcationBt.setOnClickListener {
                itemClickListener.onButtonClick(it, adapterPosition)
            }
        }

        fun postSetData(routePost: RoutePost) {
            binding.run {
                locationName.text = routePost.routeName
                locationAdress.text = routePost.ownerName
                heartCount.text = routePost.like.toString()
                addLcationBt.visibility = View.GONE
                heartClickButton.visibility = View.VISIBLE
                updateHeartButton(routePost.isLiked)

                // Format the timestamp and set it to the TextView
                val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                val date = Date(routePost.timestamp)
                timestamp.text = dateFormat.format(date)

                root.setOnClickListener {
                    itemClickListener.onClick(it, adapterPosition)
                }
                heartClickButton.setOnClickListener {
                    itemClickListener.heartClick(it, adapterPosition)
                }
            }
        }

        private fun updateHeartButton(isLiked: Boolean) {
            if (isLiked) {
                binding.heartClickButton.setImageResource(R.drawable.heart_filled)
            } else {
                binding.heartClickButton.setImageResource(R.drawable.heart_empty)
            }
        }
    }

    interface OnItemClickListener {
        fun onClick(v: View, position: Int)
        fun onButtonClick(v: View, position: Int)
        fun heartClick(v: View, position: Int)
    }

    fun setItemClickListener(onItemClickListener: OnItemClickListener) {
        this.itemClickListener = onItemClickListener
    }

    private lateinit var itemClickListener: OnItemClickListener
}
