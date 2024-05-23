package com.example.rootmap

import android.graphics.drawable.Drawable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target

class TouristAdapter(private val items: List<TouristItem>) : RecyclerView.Adapter<TouristAdapter.TouristViewHolder>() {

    inner class TouristViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val title: TextView = view.findViewById(R.id.title)
        val addr1: TextView = view.findViewById(R.id.addr1)
        val addr2: TextView = view.findViewById(R.id.addr2)
        val image: ImageView = view.findViewById(R.id.image)

        fun bind(item: TouristItem) {
            title.text = item.title
            addr1.text = item.addr1
            addr2.text = item.addr2 ?: ""

            val imageUrl = item.firstimage?.replace("http://", "https://")

            if (!imageUrl.isNullOrEmpty()) {
                val requestOptions = RequestOptions()
                    .placeholder(R.drawable.map)  // 로딩 중일 때 보여줄 이미지
                    .error(R.drawable.map)        // 로딩 실패 시 보여줄 이미지
                    .fallback(R.drawable.map)     // 이미지 URL이 null인 경우 보여줄 이미지

                Log.d("TouristAdapter", "Loading image URL: $imageUrl")
                Glide.with(itemView.context)
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
                    .into(image)
            } else {
                // 이미지가 없는 경우 기본 이미지를 설정
                image.setImageResource(R.drawable.map)
                Log.d("TouristAdapter", "Image URL is null or empty")
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TouristViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_tourist, parent, false)
        return TouristViewHolder(view)
    }

    override fun onBindViewHolder(holder: TouristViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size
}
