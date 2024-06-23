package com.example.rootmap

import android.annotation.SuppressLint
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.myapplication.Friend
import com.example.rootmap.databinding.CommentItemLayoutBinding
import com.example.rootmap.databinding.FriendLayoutBinding
import com.google.firebase.FirebaseException
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.tasks.await

class CommentListAdapter: RecyclerView.Adapter<CommentListAdapter.CommentHolder>() {
    var list = mutableListOf<String>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentHolder {
        val binding =
            CommentItemLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CommentHolder(binding)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: CommentHolder, position: Int) {
        Log.d("add_screen_check", "onBind")
        var screen = list.get(position)
        holder.setData(screen)
    }

    inner class CommentHolder(
        val binding: CommentItemLayoutBinding,
    ) :
        RecyclerView.ViewHolder(binding.root) {
        fun setData(commentData: String ) {
            var comment=commentData.split("@comment@:","@date@:")
            binding.nicknameText.text = comment[0]
            binding.commentText.text = comment[1]
            binding.dateText.text=comment[2]
        }

    }
}