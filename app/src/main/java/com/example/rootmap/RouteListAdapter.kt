package com.example.rootmap

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.rootmap.databinding.FragmentMenu3Binding
import com.example.rootmap.databinding.RoutelistLayoutBinding
import com.google.firebase.FirebaseException
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.tasks.await

class RouteListAdapter() : RecyclerView.Adapter<Holder>()  {
    var list = mutableListOf<Route>()
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

}
class Holder(
    val binding: RoutelistLayoutBinding,
) :
    RecyclerView.ViewHolder(binding.root) {
    fun setData(route: Route) {
        binding.locationName.text=route.name
        binding.locationName.text=route.adress
    }


}