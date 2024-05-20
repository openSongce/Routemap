package com.example.rootmap

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.Friend
import com.example.rootmap.databinding.FriendLayoutBinding
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import android.app.AlertDialog
import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.core.view.isInvisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import com.example.rootmap.databinding.DialogLayooutBinding


class FriendAdapter() : RecyclerView.Adapter<FriendHolder>() {
    var list = mutableListOf<Friend>()
    var myid: String = "id"
    var mode: String = "De"

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FriendHolder {
        val binding =
            FriendLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return FriendHolder(binding, mode, parent)
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

class FriendHolder(
    val binding: FriendLayoutBinding,
    mode: String,
    parent: ViewGroup,
) :
    RecyclerView.ViewHolder(binding.root) {
    var parent = parent

    init {
        when (mode) { //버튼 텍스트와 기능 설정
            "List" -> {//일단 완
                binding.friendButton2.setOnClickListener {// 버튼 기능
                    FriendList.getInstance()?.showAction(binding.friendId.text.toString())
                }
            }
            "Request" -> {
                binding.friendButton.text = "거절"
                binding.friendButton2.text = "수락"
                binding.friendButton.visibility = View.VISIBLE
                binding.friendButton.setOnClickListener {
                    FriendRequest.getInstance()?.showCancle(binding.friendId.text.toString())
                }
                binding.friendButton2.setOnClickListener {//수락 버튼 기능
                    FriendRequest.getInstance()?.showAccept(binding.friendId.text.toString())
                }
            }

            "Add" -> { //완
                binding.friendButton2.text = "취소"
                binding.friendButton2.setOnClickListener {//취소 버튼 기능
                    FriendAdd.getInstance()?.showCancle(binding.friendId.text.toString())
                }
            }
        }
    }

    fun setFriend(friend: Friend) {
        //해당 binding화면에 친구 정보 제공
        binding.friendName.text = friend.nickname
        binding.friendId.text = friend.id
        //친구에게 프로필 사진 정보가 있으면
        //binding.picture.setImageResource(R.drawable.ic_launcher_foreground)
    }
}