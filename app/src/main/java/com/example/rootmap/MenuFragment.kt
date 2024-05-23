package com.example.rootmap

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.rootmap.databinding.FragmentMenuBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.simplexml.SimpleXmlConverterFactory
import kotlin.random.Random

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class MenuFragment : Fragment() {
    private var param1: String? = null
    private var param2: String? = null

    private lateinit var binding: FragmentMenuBinding
    private lateinit var apiService: TouristApiService
    private var currentAreaCode = 1
    private var retryCount = 0
    private val maxRetries = 5
    private var totalPages = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }

        // Retrofit 초기화
        val retrofit = Retrofit.Builder()
            .baseUrl("https://apis.data.go.kr/B551011/KorService1/")
            .addConverterFactory(SimpleXmlConverterFactory.create())
            .build()

        apiService = retrofit.create(TouristApiService::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMenuBinding.inflate(inflater, container, false)

        // 도시 목록을 정의
        val cityList = resources.getStringArray(R.array.city_array)

        // ArrayAdapter를 생성하여 Spinner에 연결
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, cityList)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.citySpinner.adapter = adapter

        binding.citySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                currentAreaCode = when (position) {
                    0 -> 1
                    1 -> 2
                    2 -> 3
                    3 -> 4
                    4 -> 5
                    5 -> 6
                    6 -> 7
                    7 -> 8
                    8 -> 31
                    9 -> 32
                    else -> 1
                }
                retryCount = 0 // 스피너가 선택될 때마다 재시도 카운트 초기화
                fetchTouristInfo(currentAreaCode)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())

        // SwipeRefreshLayout 설정
        binding.swipeRefreshLayout.setOnRefreshListener {
            retryCount = 0 // 새로고침 시 재시도 카운트 초기화
            fetchTouristInfo(currentAreaCode, randomPage = true)
        }

        // 드롭다운 메뉴의 기본값을 서울로 설정하고 초기 데이터 로드
        binding.citySpinner.setSelection(0) // 서울이 0번째 인덱스에 있다고 가정
        fetchTouristInfo(1) // 서울의 지역 코드는 1

        return binding.root
    }

    private fun fetchTouristInfo(areaCode: Int, randomPage: Boolean = false) {
        if (retryCount >= maxRetries) {
            binding.swipeRefreshLayout.isRefreshing = false
            return
        }

        val pageNo = if (randomPage) Random.nextInt(1, totalPages + 1) else 1
        apiService.getTouristInfo(
            numOfRows = 10,
            pageNo = pageNo,
            mobileOS = "AND",
            mobileApp = "MobileApp",
            contentTypeId = 12,
            areaCode = areaCode,
            serviceKey = "iIzVkyvN4jIuoBR82vVZ0iFXlV65w0gsaiuOlUboGQ45v7PnBXkVOsDoBxoqMul10rfSMk7J+X5YKBxqu2ANRQ=="
        ).enqueue(object : Callback<TouristResponse> {
            override fun onResponse(call: Call<TouristResponse>, response: Response<TouristResponse>) {
                if (response.isSuccessful) {
                    val totalCount = response.body()?.body?.totalCount?.toIntOrNull() ?: 0
                    totalPages = (totalCount / 10) + 1

                    Log.d("totalCount", "Total count: ${totalCount}")

                    val items = response.body()?.body?.items?.item ?: emptyList()
                    Log.d("API_SUCCESS", "Fetched ${items.size} items")
                    if (items.isNotEmpty()) {
                        val adapter = TouristAdapter(items)
                        binding.recyclerView.adapter = adapter
                    } else {
                        Log.d("API_SUCCESS", "No items found")
                        retryFetchTouristInfo(areaCode) // 데이터가 없으면 재시도
                    }
                } else {
                    Log.e("API_ERROR", "Response code: ${response.code()}")
                    retryFetchTouristInfo(areaCode) // 에러 발생 시 재시도
                }
                binding.swipeRefreshLayout.isRefreshing = false
            }

            override fun onFailure(call: Call<TouristResponse>, t: Throwable) {
                // 에러 처리
                Log.e("API_FAILURE", "Failed to fetch data", t)
                retryFetchTouristInfo(areaCode) // 실패 시 재시도
                binding.swipeRefreshLayout.isRefreshing = false
            }
        })
    }

    private fun retryFetchTouristInfo(areaCode: Int) {
        retryCount++
        fetchTouristInfo(areaCode, randomPage = true)
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            MenuFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}
