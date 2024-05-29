package com.example.rootmap

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplication.Friend
import com.example.rootmap.databinding.DialogLayoutBinding
import com.example.rootmap.databinding.FragmentMenu3Binding
import com.example.rootmap.databinding.RecyclerviewDialogBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest.PRIORITY_HIGH_ACCURACY
import com.google.android.gms.location.LocationServices
import com.google.firebase.Firebase
import com.google.firebase.FirebaseException
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.firestore
import com.kakao.vectormap.KakaoMap
import com.kakao.vectormap.KakaoMapReadyCallback
import com.kakao.vectormap.LatLng
import com.kakao.vectormap.MapLifeCycleCallback
import com.kakao.vectormap.camera.CameraAnimation
import com.kakao.vectormap.camera.CameraPosition
import com.kakao.vectormap.camera.CameraUpdateFactory
import com.kakao.vectormap.label.Label
import com.kakao.vectormap.label.LabelLayer
import com.kakao.vectormap.label.LabelOptions
import com.kakao.vectormap.label.LabelStyle
import com.kakao.vectormap.label.LabelStyles
import kotlinx.coroutines.async
import kotlinx.coroutines.tasks.await
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [MenuFragment3.newInstance] factory method to
 * create an instance of this fragment.
 */
// Logcat Debug 코드 "Map3"
class MenuFragment3 : Fragment() {
    private var zoomlevel = 17
    lateinit var locationPermission: ActivityResultLauncher<Array<String>>
    var startpositon:LatLng?=null
    var curPosition: LatLng?=null
    lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    var kakaomap: KakaoMap?=null
    lateinit var startCamera: CameraPosition
    val locationData: MutableList<SearchLocation> = mutableListOf()
    //프래그먼트의 binding
    val binding by lazy { FragmentMenu3Binding.inflate(layoutInflater) }
    lateinit var listAdapter: RouteListAdapter
    lateinit var routelistAdapter: MyDocumentAdapter
    val db = Firebase.firestore
    lateinit var myDb: CollectionReference
    private var currentId: String? = null
    lateinit var layers: LabelLayer
    var clickMarker: Label?=null
    var currendtMarker:Label?=null
    var searchMarker:Label?=null
    lateinit var searchText:String
    lateinit var userX:String
    lateinit var userY:String
    var layer: LabelLayer?=null
    lateinit var dialog:AlertDialog
    private val readyCallback = object: KakaoMapReadyCallback(){
        override fun onMapReady(kakaoMap: KakaoMap) {
            //현재 위치에 라벨
            kakaomap=kakaoMap
            layer=kakaoMap.labelManager?.layer
            val style = kakaoMap.getLabelManager()?.addLabelStyles(LabelStyles.from(LabelStyle.from(
                R.drawable.mylocation
            )))
            if(startpositon!=null){
                val options = LabelOptions.from(startpositon).setStyles(style)
                currendtMarker=layer?.addLabel(options)
                //val trackingManager = kakaoMap.trackingManager
                //trackingManager!!.startTracking(currendtMarker)
            }
            startCamera= kakaoMap.getCameraPosition()!!
            //지도 클릭 시 이벤트 구현
            kakaoMap.setOnMapClickListener { kakaoMap, latLng, pointF, poi ->
                //일단은 마크가 하나만 찍히도록 구현
                val styles = kakaoMap.getLabelManager()?.addLabelStyles(LabelStyles.from(LabelStyle.from(
                    R.drawable.clicklocation
                )))
                val options1 = LabelOptions.from(latLng).setStyles(styles)
                if(clickMarker==null){ //이미 찍힌 마크가 없을 때
                    clickMarker=layer?.addLabel(options1)
                    Log.d("Map3test","${latLng}")
                }else{
                    layer?.remove(clickMarker)
                    clickMarker=layer?.addLabel(options1) //마크 삭제 후 새로 추가
                }
            }
        }
        override fun getPosition(): LatLng {
            return startpositon!!
        }
        override fun getZoomLevel(): Int {
            return zoomlevel
        }
    }

    private val lifecycleCallback = object: MapLifeCycleCallback(){
        override fun onMapDestroy() {

        }
        override fun onMapResumed() {
            super.onMapResumed()
        }

        override fun onMapPaused() {
            super.onMapPaused()
        }

        override fun onMapError(p0: Exception?) {
            Toast.makeText(context,"map error!", Toast.LENGTH_SHORT).show()
        }
    }
    init{
        instance = this
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            currentId = it.getString("id")
        }
        fusedLocationProviderClient =
            LocationServices.getFusedLocationProviderClient(this.context)
        requireActivity().window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)
    }
    @SuppressLint("MissingPermission")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        //여기부터 코드 작성
        locationPermission= registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {result->
            if (result.any { permission -> !permission.value }) {
                Toast.makeText(this.context, "위치 권한을 승인하여야 사용할 수 있습니다.", Toast.LENGTH_SHORT).show()
            }else{
                getLocation()
            }
        }
        //DB
        myDb = db.collection("user").document(currentId.toString()).collection("route")
        viewLifecycleOwner.lifecycleScope.async{
            locationPermission.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION))
        }

        binding.addButton.setOnClickListener {
            Toast.makeText(this.context, "클릭", Toast.LENGTH_SHORT).show()
        }
        binding.locationButton.setOnClickListener {
            if(kakaomap!=null){
                fusedLocationProviderClient.getCurrentLocation(PRIORITY_HIGH_ACCURACY, null)
                    .addOnSuccessListener { success: Location? ->
                        success?.let { location ->
                            userX = location.longitude.toString()
                            userY = location.latitude.toString()
                            curPosition = LatLng.from(location.latitude, location.longitude)
                            var cameraUpdate= CameraUpdateFactory.newCenterPosition(curPosition, zoomlevel)
                            Log.d("Map3curLatLng", curPosition.toString())
                            kakaomap!!.moveCamera(cameraUpdate, CameraAnimation.from(500, true, true)) //왜 현재위치로 갔다가 다시돌아오지?
                            Log.d("Map3cameraUpdate", cameraUpdate.position.toString())
                        }
                    }
                    .addOnFailureListener{
                        Toast.makeText(context,"알 수 없는 오류가 발생했습니다. 재시도 해주세요.",Toast.LENGTH_SHORT).show()
                    }
            }
      }
        binding.searchText.setOnEditorActionListener{ v, actionId, event //키보드 엔터 사용시
            ->
            context?.hideKeyboard(binding.root)
            searchText=binding.searchText.text.toString()
            if(searchText==""){
                Toast.makeText(context, "빈칸입니다.", Toast.LENGTH_SHORT).show()
            }else{
                binding.recyclerView2.visibility=View.VISIBLE
                binding.disButton.visibility=View.VISIBLE
                binding.disButton.setOnClickListener {
                    binding.recyclerView2.visibility=View.GONE
                    binding.disButton.visibility=View.GONE
                }
                searchKeyword(searchText)
            }
            true
        }
        listAdapter= RouteListAdapter()
        routelistAdapter= MyDocumentAdapter()
       //검색 리스트의 클릭 이벤트 구현
        listAdapter.setItemClickListener(object: RouteListAdapter.OnItemClickListener {
            //검색 리스트 클릭 시
            override fun onClick(v: View, position: Int) {
                var loc=LatLng.from(locationData[position].y, locationData[position].x)
                val styles = kakaomap!!.getLabelManager()?.addLabelStyles(LabelStyles.from(LabelStyle.from(
                    R.drawable.clicklocation
                )))
                val options1 = LabelOptions.from(loc).setStyles(styles)
                if(searchMarker==null){ //이미 찍힌 마크가 없을 때
                    searchMarker=layer?.addLabel(options1)
                }else{
                    layer?.remove(searchMarker)
                    searchMarker=layer?.addLabel(options1) //마크 삭제 후 새로 추가
                }
                //지도 이동
                if(kakaomap!=null){
                    var cameraUpdate= CameraUpdateFactory.newCenterPosition(loc, zoomlevel)
                    kakaomap!!.moveCamera(cameraUpdate, CameraAnimation.from(500, true, true))

                }else{
                    Toast.makeText(context,"알 수 없는 오류가 발생했습니다. 재시도 해주세요.",Toast.LENGTH_SHORT).show()
                }
            }
            //검색 리스트의 경로 추가 버튼 클릭 시
            override fun onButtonClick(v: View, position: Int) {
                var loc=LatLng.from(locationData[position].y, locationData[position].x)
                viewLifecycleOwner.lifecycleScope.async {
                   routelistAdapter.list=loadMyList() //어댑터에 데이터 연결
                   dialog=showDialog()
                }
            }
        })
        routelistAdapter.setItemClickListener(object: MyDocumentAdapter.OnItemClickListener {
            //내 경로 리스트의 추가 버튼 클릭 시 이벤트 구현
            override fun onClick(v: View, position: Int) {
                dialog.dismiss()
                Toast.makeText(context,"추가 클릭 성공",Toast.LENGTH_SHORT).show()
            }
        })

        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        viewLifecycleOwner.lifecycleScope.async {
            listAdapter.list=locationData
            binding.recyclerView2.adapter = listAdapter
            binding.recyclerView2.layoutManager = LinearLayoutManager(context)
        }
        super.onViewCreated(view, savedInstanceState)
    }
     override fun onPause() {
        super.onPause()
         //fusedLocationProviderClient.removeLocationUpdates(locationCallback)
    }
    fun Context.hideKeyboard(view: View) {
        val inputMethodManager =
            getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
    }

    private fun searchKeyword(text:String){
        val retrofit = Retrofit.Builder() // Retrofit 구성
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        val api = retrofit.create(KakaoAPI::class.java) // 통신 인터페이스를 객체로 생성
        val call = api.getSearchKeyword(API_KEY,text,userX,userY) // 검색 조건 입력
        // API 서버에 요청
        call.enqueue(object: Callback<ResultSearchKeyword> {
            override fun onResponse(call: Call<ResultSearchKeyword>, response: Response<ResultSearchKeyword>) {
// 통신 성공
                addItemsAndMarkers(response.body())
            }

            override fun onFailure(call: Call<ResultSearchKeyword>, t: Throwable) {
// 통신 실패
                Log.w("LocalSearch", "통신 실패: ${t.message}")
            }
        })
    }


    private fun addItemsAndMarkers(searchResult: ResultSearchKeyword?) {
        if (!searchResult?.documents.isNullOrEmpty()) {
// 검색 결과 있음
            locationData.clear() // 리스트 초기화
           //layers
            for (document in searchResult!!.documents) {
            // 결과를 리사이클러 뷰에 추가
                val item = SearchLocation(document.place_name,
                    document.road_address_name,document.x.toDouble(),
                    document.y.toDouble())
                locationData.add(item)
            }
            listAdapter.list=locationData
            listAdapter.notifyDataSetChanged()
        } else {
// 검색 결과 없음
            Toast.makeText(this.context, "검색 결과가 없습니다", Toast.LENGTH_SHORT).show()
        }
    }
    suspend fun loadMyList(): MutableList<MyRouteDocument> {
        var list= mutableListOf<MyRouteDocument>()
        return try {
            val myList = myDb.get().await()
            if(!myList.isEmpty){
                for (doc in myList.documents) {
                    //var id = fr.data?.get("id").toString() //친구 id
                    //val fr_data = db.collection("user").document(id).get().await()
                   // var load = Friend(fr_data.data?.get("nickname").toString(), id)
                 //   data.add(load)
                   list.add(MyRouteDocument(doc.data?.get("docName").toString(),doc.id))
                }
            }else{
                Toast.makeText(context,"아직 경로가 없습니다. 새로운 경로를 만들어주세요.",Toast.LENGTH_SHORT).show()
            }
            list
        } catch (e: FirebaseException) {
            Log.d("list_test", "error")
            list
        }
    }

    private fun showDialog():AlertDialog{ //다이어로그로 팝업창 구현
        val dBinding = RecyclerviewDialogBinding.inflate(layoutInflater)
        val dialogBuild = AlertDialog.Builder(context).setView(dBinding.root)
        dBinding.listView.adapter = routelistAdapter
        dBinding.listView.layoutManager = LinearLayoutManager(context)
        val dialog = dialogBuild.show()
        return dialog
    }


    @SuppressLint("MissingPermission")
    private fun getLocation() {
        fusedLocationProviderClient.getCurrentLocation(PRIORITY_HIGH_ACCURACY, null)
            .addOnSuccessListener { success: Location? ->
                success?.let { location ->
                    userX=location.longitude.toString()
                    userY=location.latitude.toString()
                    startpositon= LatLng.from(location.latitude, location.longitude)
                    binding.progressBar.visibility=View.GONE
                    binding.mapViewId.start(lifecycleCallback, readyCallback)
                    Log.d("Map3init", "getLocation 호출" + startpositon.toString())
                }
            }
            .addOnFailureListener { fail ->

            }
    }
    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment MenuFragment3.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            MenuFragment3().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
        const val BASE_URL = "https://dapi.kakao.com/"
        const val API_KEY = "KakaoAK 95ce0663a74fa2702f06bed9a3025342"
        private var instance: MenuFragment3? = null
        fun getInstance():MenuFragment3?{
            return instance
        }
    }
}