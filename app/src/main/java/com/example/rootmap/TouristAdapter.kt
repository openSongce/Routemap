package com.example.rootmap

import android.graphics.drawable.Drawable
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import com.example.rootmap.databinding.ItemTouristBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference

class TouristAdapter(
    private val items: List<TouristItem>,
    private val database: DatabaseReference,
    private val auth: FirebaseAuth,
    private val onItemClick: (TouristItem) -> Unit
) : RecyclerView.Adapter<TouristAdapter.TouristViewHolder>() {

    inner class TouristViewHolder(private val binding: ItemTouristBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: TouristItem) {
            binding.title.text = item.title ?: "No title"
            binding.addr1.text = item.addr1 ?: "No address"
            binding.addr2.text = item.addr2 ?: ""
            binding.likeCount.text = item.likeCount.toString()
            binding.likeButton.setImageResource(
                if (item.isLiked) R.drawable.heart_filled else R.drawable.heart_empty
            )

            val imageUrl = item.firstimage?.replace("http://", "https://")

            if (!imageUrl.isNullOrEmpty()) {
                val requestOptions = RequestOptions()
                    .placeholder(R.drawable.map)  // 로딩 중일 때 보여줄 이미지
                    .error(R.drawable.map)        // 로딩 실패 시 보여줄 이미지
                    .fallback(R.drawable.map)     // 이미지 URL이 null인 경우 보여줄 이미지

                Log.d("TouristAdapter", "Loading image URL: $imageUrl")
                Glide.with(binding.root.context)
                    .setDefaultRequestOptions(requestOptions)
                    .load(imageUrl)
                    .listener(object : RequestListener<Drawable> {
                        override fun onLoadFailed(
                            e: GlideException?,
                            model: Any?,
                            target: Target<Drawable>?,
                            isFirstResource: Boolean
                        ): Boolean {
                            Log.e("TouristAdapter", "Image load failed: $imageUrl", e)
                            return false
                        }

                        override fun onResourceReady(
                            resource: Drawable?,
                            model: Any?,
                            target: Target<Drawable>?,
                            dataSource: com.bumptech.glide.load.DataSource?,
                            isFirstResource: Boolean
                        ): Boolean {
                            Log.d("TouristAdapter", "Image loaded successfully: $imageUrl")
                            return false
                        }
                    })
                    .into(binding.image)
            } else {
                // 이미지가 없는 경우 기본 이미지를 설정
                binding.image.setImageResource(R.drawable.map)
                Log.d("TouristAdapter", "Image URL is null or empty")
            }

            binding.likeButton.setOnClickListener {
                val userId = auth.currentUser?.uid ?: return@setOnClickListener

                item.isLiked = !item.isLiked
                if (item.isLiked) {
                    item.likeCount++
                    binding.likeButton.setImageResource(R.drawable.heart_filled)
                } else {
                    item.likeCount--
                    binding.likeButton.setImageResource(R.drawable.heart_empty)
                }
                binding.likeCount.text = item.likeCount.toString()

                // Firebase에 추천 수 저장
                item.contentid?.let { contentId ->
                    database.child("likes").child(contentId).setValue(item.likeCount)
                    database.child("userLikes").child(userId).child(contentId).setValue(item.isLiked)
                }
            }

            binding.root.setOnClickListener {
                onItemClick(item)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TouristViewHolder {
        val binding = ItemTouristBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TouristViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TouristViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size
}
