package com.example.rootmap

import android.content.Intent
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
    val items: List<TouristItem>,
    private val database: DatabaseReference,
    private val auth: FirebaseAuth,
    private val menuFragment: MenuFragment? = null,
    private val onItemClick: (TouristItem) -> Unit
) : RecyclerView.Adapter<TouristAdapter.TouristViewHolder>() {

    inner class TouristViewHolder(private val binding: ItemTouristBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: TouristItem) {
            binding.title.text = item.title ?: ""
            binding.addr1.text = item.addr1 ?: ""
            binding.addr2.text = item.addr2 ?: ""
            binding.likeCount.text = item.likeCount.toString()
            binding.likeButton.setImageResource(
                if (item.isLiked) R.drawable.heart_filled else R.drawable.heart_empty
            )

            val imageUrl = item.firstimage?.replace("http://", "https://")

            if (!imageUrl.isNullOrEmpty()) {
                val requestOptions = RequestOptions()
                    .placeholder(R.drawable.map)
                    .error(R.drawable.map)
                    .fallback(R.drawable.map)

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
                            return false
                        }

                        override fun onResourceReady(
                            resource: Drawable?,
                            model: Any?,
                            target: Target<Drawable>?,
                            dataSource: com.bumptech.glide.load.DataSource?,
                            isFirstResource: Boolean
                        ): Boolean {
                            return false
                        }
                    })
                    .into(binding.image)
            } else {
                binding.image.setImageResource(R.drawable.map)
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

                item.title?.let { title ->
                    val sanitizedTitle = title.replace("[.\\#\\$\\[\\]]".toRegex(), "")
                    database.child("likes").child(sanitizedTitle).setValue(item.likeCount)
                    database.child("userLikes").child(userId).child(sanitizedTitle).setValue(item.isLiked)
                }
            }

            binding.addButton.setOnClickListener {
                val context = binding.root.context as MainActivity
                context.navigateToMenuFragment3WithAddress(item.addr1 ?: "")
            }

            binding.root.setOnClickListener {
                if (item.contentTypeId in listOf(12, 14, 15, 25, 28, 32, 38, 39)) {
                    item.contentid?.let { menuFragment?.fetchTouristDetailIntro(it, item.contentTypeId) }
                } else {
                    onItemClick(item)
                }
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
