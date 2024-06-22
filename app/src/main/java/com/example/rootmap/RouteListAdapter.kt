package com.example.rootmap

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.rootmap.databinding.RoutelistLayoutBinding
import kotlinx.coroutines.tasks.await

class RouteListAdapter() : RecyclerView.Adapter<RouteListAdapter.Holder>()  {
    var list = mutableListOf<SearchLocation>()
    var postList= mutableListOf<RoutePost>()
    var postMode=false

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val binding =
            RoutelistLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return Holder(binding)
    }

    override fun getItemCount(): Int {
        if(postMode) return postList.size
        return list.size
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        if(postMode){
            var screen=postList.get(position)
            holder.postSetData(screen)
        }else{
            var screen =list.get(position)
            holder.setData(screen)
        }
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

        fun postSetData(routePost:RoutePost){
            binding.run {
                locationName.text=routePost.routeName
                locationAdress.text=routePost.ownerName
                addLcationBt.text="↓"
                heartClickButton.visibility=View.VISIBLE
                commentButton.visibility=View.VISIBLE
                root.setOnClickListener {
                    itemClickListener.onClick(it, position)
                }
                addLcationBt.setOnClickListener {
                    itemClickListener.onButtonClick(it, position)
                }
                heartClickButton.setOnClickListener {
                    heartClickButton.setImageResource(R.drawable.heart_filled)
                    itemClickListener.heartClick(it, position)
                }
                commentButton.setOnClickListener {
                    itemClickListener.commentClick(it, position)
                }
            }
        }
        }
    interface OnItemClickListener {
        fun onClick(v: View, position: Int)
        fun onButtonClick(v: View, position: Int)

        fun heartClick(v: View, position: Int)
        fun commentClick(v: View, position: Int)
    }

    fun setItemClickListener(onItemClickListener: OnItemClickListener) {
        this.itemClickListener = onItemClickListener
    }

    private lateinit var itemClickListener : OnItemClickListener
    }



