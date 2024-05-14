package com.example.rootmap

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.Friend
import com.example.rootmap.databinding.FriendLayoutBinding

class FriendAdapter: RecyclerView.Adapter<FriendHolder>(){
    var list= mutableListOf<Friend>()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FriendHolder {
        val binding=FriendLayoutBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return FriendHolder(binding)
    }
    override fun onBindViewHolder(holder: FriendHolder, position: Int) {
        val screen=list.get(position)
        holder.setFriend(screen)
    }

    override fun getItemCount(): Int {
        return list.size
    }


}

class FriendHolder(val binding: FriendLayoutBinding): RecyclerView.ViewHolder(binding.root){
    init {
        /*클릭 시 발생할 이벤트 코드*/

    }
    fun setFriend(friend: Friend){
        //해당 binding화면에 친구 정보 제공
        binding.friendName.text=friend.name
        binding.friendId.text=friend.id
        //친구에게 프로필 사진 정보가 있으면
        //binding.picture.setImageResource(R.drawable.ic_launcher_foreground)

    }

}