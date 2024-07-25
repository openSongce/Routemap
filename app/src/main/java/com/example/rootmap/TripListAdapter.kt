package com.example.rootmap

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.rootmap.databinding.RoutelistLayoutBinding

class TripListAdapter : RecyclerView.Adapter<TripListAdapter.TripViewHolder>() {

    var list = mutableListOf<MyRouteDocument>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TripViewHolder {
        val binding = RoutelistLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TripViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: TripViewHolder, position: Int) {
        holder.bind(list[position])
    }

    inner class TripViewHolder(private val binding: RoutelistLayoutBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(trip: MyRouteDocument) {
            binding.locationName.text = trip.docName
            binding.locationAddress.visibility = View.GONE
            binding.addLocationBt.visibility = View.GONE
        }
    }
}
