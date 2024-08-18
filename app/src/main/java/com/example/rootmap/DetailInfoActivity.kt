package com.example.rootmap

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.rootmap.databinding.ActivityDetailInfoBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class DetailInfoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetailInfoBinding
    private lateinit var apiService: TouristApiService
    private lateinit var detailInfoAdapter: DetailInfoAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailInfoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val contentId = intent.getStringExtra("contentId")
        val contentTypeId = intent.getIntExtra("contentTypeId", 25)

        apiService = RetrofitClientInstance.getRetrofitInstance().create(TouristApiService::class.java)

        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        detailInfoAdapter = DetailInfoAdapter(emptyList())
        binding.recyclerView.adapter = detailInfoAdapter

        binding.progressBar.visibility = View.VISIBLE

        fetchDetailInfo(contentId, contentTypeId)
    }

    private fun fetchDetailInfo(contentId: String?, contentTypeId: Int) {
        if (contentId == null) {
            binding.progressBar.visibility = View.GONE
            return
        }

        apiService.getTouristDetailInfo(
            contentId = contentId.toInt(),
            contentTypeId = contentTypeId,
            mobileOS = "AND",
            mobileApp = "MobileApp",
            serviceKey = "iIzVkyvN4jIuoBR82vVZ0iFXlV65w0gsaiuOlUboGQ45v7PnBXkVOsDoBxoqMul10rfSMk7J+X5YKBxqu2ANRQ=="
        ).enqueue(object : Callback<DetailInfoResponse> {
            override fun onResponse(call: Call<DetailInfoResponse>, response: Response<DetailInfoResponse>) {
                binding.progressBar.visibility = View.GONE

                if (response.isSuccessful) {
                    response.body()?.body?.items?.item?.let {
                        detailInfoAdapter.updateItems(it)
                    }
                }
            }

            override fun onFailure(call: Call<DetailInfoResponse>, t: Throwable) {
                binding.progressBar.visibility = View.GONE
            }
        })
    }
}
