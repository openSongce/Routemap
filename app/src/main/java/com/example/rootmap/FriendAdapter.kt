package com.example.rootmap

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.Friend
import com.example.rootmap.databinding.FriendLayoutBinding
import com.google.android.material.navigation.NavigationView
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.tasks.await
import android.app.Activity
import android.util.Log
import android.widget.Toast
import com.example.rootmap.databinding.FragmentFriendAddBinding
class FriendAdapter constructor(id: String) : RecyclerView.Adapter<FriendHolder>() {
    var list = mutableListOf<Friend>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FriendHolder {
        val binding =
            FriendLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return FriendHolder(binding)
    }
    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: FriendHolder, position: Int) {
        Log.d("add_screen_check", "onBind")
        var screen = list.get(position)
        holder.setFriend(screen)
    }
}


class FriendHolder(val binding: FriendLayoutBinding) : RecyclerView.ViewHolder(binding.root) {
    init {
        /*클릭 시 발생할 이벤트 코드*/

    }

    fun setFriend(friend: Friend) {
        //해당 binding화면에 친구 정보 제공
        binding.friendName.text = friend.name
        binding.friendId.text = friend.id
        //Log.d("request_","name: ${friend.name}, id:${friend.id}")
        //친구에게 프로필 사진 정보가 있으면
        //binding.picture.setImageResource(R.drawable.ic_launcher_foreground)

    }

}
