package com.example.rootmap

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.graphics.Color
import android.location.Location
import android.location.LocationRequest
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.rootmap.databinding.FragmentMenu3Binding
import com.example.rootmap.databinding.RecyclerviewDialogBinding
import com.example.rootmap.databinding.RouteaddLayoutBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest.PRIORITY_HIGH_ACCURACY
import com.google.android.gms.location.LocationServices
import com.google.firebase.Firebase
import com.google.firebase.FirebaseException
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.GeoPoint
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
import com.kakao.vectormap.label.LabelTextStyle
import com.kakao.vectormap.label.LodLabel
import com.kakao.vectormap.shape.MapPoints
import com.kakao.vectormap.shape.Polyline
import com.kakao.vectormap.shape.PolylineOptions
import com.kakao.vectormap.shape.PolylineStyle
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
    //프래그먼트의 binding
    val binding by lazy { FragmentMenu3Binding.inflate(layoutInflater) }
    private var zoomlevel = 17
    lateinit var locationPermission: ActivityResultLauncher<Array<String>>
    var startpositon:LatLng?=null
    var curPosition: LatLng?=null
    lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    var kakaomap: KakaoMap?=null
    lateinit var startCamera: CameraPosition
    var clickMarker: Label?=null
    var currendtMarker:Label?=null
    var searchMarker:Label?=null
    private var requestingLocationUpdates = false
    private var locationRequest: LocationRequest? = null
    private val locationCallback: LocationCallback?=null
    private lateinit var makerList:MutableList<LatLng>
    var label = arrayOf<LodLabel>()
    var areaPolyline:Polyline?=null

    val locationData: MutableList<SearchLocation> = mutableListOf()
    val loadListData: MutableList<MyLocation> = mutableListOf()
    lateinit var listAdapter: RouteListAdapter
    lateinit var routelistAdapter: MyDocumentAdapter
    lateinit var myRouteListAdapter: ListLocationAdapter

    val db = Firebase.firestore
    lateinit var myDb: CollectionReference
    private var currentId: String? = null

    lateinit var searchText:String
    lateinit var userX:String
    lateinit var userY:String
    lateinit var clickLocationName:String
    lateinit var clickLocationAdress:GeoPoint
    lateinit var clickAdress:String
    var layer: LabelLayer?=null
    lateinit var dialog:AlertDialog
    lateinit var listdialog:AlertDialog
    var routeName:String?=null
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
        binding.listButton.setOnClickListener {
            viewLifecycleOwner.lifecycleScope.async{
                routelistAdapter.list=loadMyList() //어댑터에 데이터 연결
                routelistAdapter.mode="View"
                if(!routelistAdapter.list.isNullOrEmpty()){
                    dialog=showDialog(true)
                }else{
                    dialog=showDialog(false)
                }
            }
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
                            kakaomap!!.moveCamera(cameraUpdate, CameraAnimation.from(500, true, true))
                            Log.d("Map3cameraUpdate", cameraUpdate.position.toString())
                        }
                    }
                    .addOnFailureListener{
                        Toast.makeText(context,"알 수 없는 오류가 발생했습니다. 재시도 해주세요.",Toast.LENGTH_SHORT).show()
                    }
            }
      }
        binding.disButton.setOnClickListener {
            if (binding.recyclerView2.getVisibility() == View.VISIBLE){
                binding.recyclerView2.visibility=View.GONE
                if(binding.recyclerView3.getVisibility() == View.GONE){
                    binding.bottomButton.visibility=View.GONE
                    kakaomap!!.setPadding(0,0,0,0)
                }
                Log.d("Map3padding", kakaomap!!.padding.toString())
            }
            else{
                binding.recyclerView2.visibility=View.VISIBLE
                binding.bottomButton.visibility=View.VISIBLE
                kakaomap!!.setPadding(0,0,0,800)
                Log.d("Map3padding", kakaomap!!.padding.toString())
            }
        }
        binding.searchText.setOnEditorActionListener{ v, actionId, event //키보드 엔터 사용시
            ->
            if(!binding.progressBar.isVisible){
                context?.hideKeyboard(binding.root)
                searchText=binding.searchText.text.toString()
                if(searchText==""){
                    Toast.makeText(context, "빈칸입니다.", Toast.LENGTH_SHORT).show()
                }else{
                    binding.recyclerView2.visibility=View.VISIBLE
                    binding.bottomButton.visibility=View.VISIBLE
                    binding.disButton.visibility=View.VISIBLE
                    searchKeyword(searchText)
                    kakaomap!!.setPadding(0,0,0,800)
                    //kakaomap!!.setViewport(0, 0, widthPixel, heightPixel-900)
                    Log.d("Map3padding", kakaomap!!.padding.toString())
                }
            }
            true
        }
        //리스트들을 위한 어댑터
        listAdapter= RouteListAdapter()
        routelistAdapter= MyDocumentAdapter()
        myRouteListAdapter= ListLocationAdapter()
        myRouteListAdapter.myDb=myDb

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
                clickLocationAdress=GeoPoint(locationData[position].y, locationData[position].x)
                clickLocationName=locationData[position].name
                viewLifecycleOwner.lifecycleScope.async {
                   routelistAdapter.list=loadMyList() //어댑터에 데이터 연결
                   routelistAdapter.mode="Add"
                    if(!routelistAdapter.list.isNullOrEmpty()){
                        dialog=showDialog(true)
                    }else{
                        dialog=showDialog(false)
                    }
                }
            }
        })
        //유저가 만든 경로 모음의 리스트
        routelistAdapter.setItemClickListener(object: MyDocumentAdapter.OnItemClickListener {
            //내 경로 리스트의 추가 버튼 클릭 시 이벤트 구현
            override fun onClick(v: View, position: Int) {
               // dialog.dismiss()
                var docId=routelistAdapter.list[position].docId
                //해당 장소를 추가하기위해 새로운 팝업창 띄우기
                viewLifecycleOwner.lifecycleScope.async {
                    loadListData.clear()
                    loadMyRouteData(docId)
                    loadListData.add(MyLocation(clickLocationName,clickLocationAdress,"","")) //해당 장소를 리스트에 추가
                    myRouteListAdapter.list=loadListData
                    showListDialog(docId,"add")
                }
            }
            //경로 보기를 눌렀을 때 나오는 목록의 버튼(보기) 클릭시 // 경로 및 핀표시 추가
            override fun onListClick(v: View, position: Int) {
                dialog.dismiss()
                var docId=routelistAdapter.list[position].docId
                var docName=routelistAdapter.list[position].docName
                binding.listButton.visibility = View.INVISIBLE
                binding.listCloseButton.visibility = View.VISIBLE
                viewLifecycleOwner.lifecycleScope.async {
                    //Toast.makeText(context, "지도에서 보여주기", Toast.LENGTH_SHORT).show()
                    loadListData.clear()
                    loadMyRouteData(docId)
                    myRouteListAdapter.docId=docId
                    myRouteListAdapter.list=loadListData
/*
                    val intent = Intent(context, RouteMapViewActivity::class.java)
                    intent.putExtra("id", currentId)
                    intent.putExtra("routeId",docId)
                    startActivity(intent)

 */
                    if (loadListData.isNotEmpty()){
                        //아직 여행지 없다는 텍스트뷰 출력
                    }
                    binding.recyclerView3.adapter = myRouteListAdapter
                    binding.recyclerView3.layoutManager = LinearLayoutManager(context)
                    //롱클릭 드래그로 순서 이동가능
                    val swipeHelperCallback = DragManageAdapter(myRouteListAdapter).apply {
                        // 스와이프한 뒤 고정시킬 위치 지정
                        setClamp(resources.displayMetrics.widthPixels.toFloat()/4)
                    }
                    ItemTouchHelper(swipeHelperCallback).attachToRecyclerView(binding.recyclerView3)
                    // 구분선 추가
                    binding.recyclerView3.addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
                    binding.recyclerView3.setOnTouchListener { _, _ ->
                        swipeHelperCallback.removePreviousClamp(binding.recyclerView3)
                        false
                    }
                    kakaomap!!.setPadding(0,0,0,800)
                    makeLine(kakaomap!!,loadListData)
                }
                binding.apply {
                    routeSaveButton.visibility=View.VISIBLE
                    routeNameText.visibility=View.VISIBLE
                    recyclerView3.visibility = View.VISIBLE
                    bottomButton.visibility = View.VISIBLE
                    routeNameText.setText(docName)
                }
                binding.routeSaveButton.setOnClickListener {
                    var text=binding.routeNameText.text.toString()
                    myDb.document(docId).update(hashMapOf("tripname" to text,"routeList" to loadListData)).addOnSuccessListener {
                        Toast.makeText(context,"성공적으로 저장하였습니다.",Toast.LENGTH_SHORT).show()
                    }
                }

            }
            //삭제 버튼을 눌렀을 때 삭제하는 기능
            override fun deleteDoc(v: View, position: Int) {
                var docId=routelistAdapter.list[position].docId
                myDb.document(docId).delete()
            }
        })
        binding.listCloseButton.setOnClickListener{//지도에서경로보기 끄는버튼 // 경로지우기 추가
            binding.listCloseButton.visibility = View.GONE
            binding.listButton.visibility = View.VISIBLE
            binding.recyclerView3.visibility = View.GONE
            binding.routeNameText.visibility=View.GONE
            binding.routeSaveButton.visibility=View.GONE
            if (binding.recyclerView2.getVisibility() == View.GONE){
                binding.bottomButton.visibility = View.GONE
            }
            for(doc in label)
                doc.remove()
            kakaomap?.shapeManager?.getLayer()?.remove(areaPolyline)
        }
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
    fun Context.hideKeyboard(view: View) {
        val inputMethodManager =
            getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
    }

    private fun makeLine(kakaomap: KakaoMap,list: MutableList<MyLocation>){
        if(kakaomap!=null){
            var cnt: Int = 1
            var labels= mutableListOf<LabelOptions>()
            var latLngList= mutableListOf<LatLng>()
            var labelStyle= kakaomap!!.getLabelManager()?.addLabelStyles(LabelStyles.from(LabelStyle.from(
                R.drawable.clicklocation
            ).setTextStyles(
                LabelTextStyle.from(50, Color.parseColor("#0A3711")))))
            for(doc in list){
                var lanLng=LatLng.from(doc.position.latitude,doc.position.longitude)
                labels.add(LabelOptions.from(lanLng)
                    .setStyles(labelStyle).setTexts(cnt.toString()))
                latLngList.add(lanLng)
                cnt++
            }
            areaPolyline = kakaomap.shapeManager!!.layer.addPolyline(getAreaOptions(latLngList)!!)
            var testlayer= kakaomap!!.getLabelManager()?.getLodLayer();
            label = testlayer!!.addLodLabels(labels)
            //첫 위치 기준으로 카메라 이동
            kakaomap.moveCamera(
                CameraUpdateFactory.newCenterPosition(
                    LatLng.from(list[0].position.latitude,list[0].position.longitude), 15
                ),
                CameraAnimation.from(500)
            )
        }
    }
    private fun getAreaOptions(list: MutableList<LatLng>): PolylineOptions? {
        return PolylineOptions.from(
            MapPoints.fromLatLng(list),
            PolylineStyle.from(10f, Color.parseColor("#80ff2c35"), 3f, Color.RED)
        )
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
            Log.d("Map3searchlist", listAdapter.list.toString())
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
                   list.add(MyRouteDocument(doc.data?.get("tripname").toString(),doc.id))
                }
            }
            list
        } catch (e: FirebaseException) {
            Log.d("list_test", "error")
            list
        }
    }
   suspend fun loadMyRouteData(id:String): Boolean {
        var dataList= mutableListOf<Map<String,*>>()
        return try {
            var data: MutableMap<*, *>
            myDb.document(id).get().addOnSuccessListener { documents->
                routeName=documents.data?.get("tripname").toString()
                data=documents.data as MutableMap<*,*>
                dataList.addAll(data["routeList"] as List<Map<String,*>>)
                dataList.forEach{
                    loadListData.add(MyLocation(it["name"].toString(),it["position"] as GeoPoint,it["memo"] as String,it["spending"] as String))
                }
            }.await()
            true
        } catch (e: FirebaseException) {
            Log.d("list_test", "error")
            true
        }
    }
    private fun showDialog(boolean: Boolean):AlertDialog{ //다이어로그로 팝업창 구현
        //boolean은 데이터의 유무-> true 있음, false 없음
        val dBinding = RecyclerviewDialogBinding.inflate(layoutInflater)
        val dialogBuild = AlertDialog.Builder(context).setView(dBinding.root)
        dialogBuild.setTitle("내 여행 리스트")
        dBinding.listView.adapter = routelistAdapter
        dBinding.listView.layoutManager = LinearLayoutManager(context)
        val swipeHelperCallback = SwapeManageAdapter(routelistAdapter).apply {
            // 스와이프한 뒤 고정시킬 위치 지정
            setClamp(resources.displayMetrics.widthPixels.toFloat()/4)
        }
        ItemTouchHelper(swipeHelperCallback).attachToRecyclerView(dBinding.listView)
        dBinding.listView.setOnTouchListener { _, _ ->
            swipeHelperCallback.removePreviousClamp(dBinding.listView)
            false
        }
        if(!boolean){
            dBinding.checkText.apply {
                text="아직 경로가 없습니다. 새로운 경로를 만들어주세요."
                visibility=View.VISIBLE
            }
        }
        dBinding.addTripRouteText.text="새로운 경로 생성"
        dBinding.addTripRouteText.setOnClickListener {
            showListDialog("","make")
        }
        val dialog = dialogBuild.show()
        return dialog
    }

    private fun showListDialog(docId:String,mode:String):AlertDialog{ //다이어로그로 팝업창 구현
        val dBinding = RouteaddLayoutBinding.inflate(layoutInflater)
        val dialogBuild = AlertDialog.Builder(context).setView(dBinding.root)
        dialogBuild.setCancelable(false)
        if(mode=="make"){ //즉, 새로운 여행 경로 만들기 모드
            dialogBuild.setTitle("새로운 여행 루트 만들기")
            dBinding.apply {
                editTextText.hint="제목을 입력하세요."
                dialogListView.visibility=View.GONE
                linearLayout5.visibility=View.GONE
            }
            loadListData.clear()
        }else{ //여행 경로에 장소 추가 모드
            dBinding.editTextText.setText(routeName) //여행 이름 띄우기
        }
        dBinding.dialogListView.apply {
            adapter = myRouteListAdapter
            layoutManager = LinearLayoutManager(context)
            //롱클릭 드래그로 순서 이동가능
            val swipeHelperCallback = DragManageAdapter(myRouteListAdapter).apply {
                // 스와이프한 뒤 고정시킬 위치 지정
                setClamp(resources.displayMetrics.widthPixels.toFloat()/4)
            }
            ItemTouchHelper(swipeHelperCallback).attachToRecyclerView(dBinding.dialogListView)
            // 구분선 추가
            addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
            dBinding.dialogListView.setOnTouchListener { _, _ ->
                swipeHelperCallback.removePreviousClamp(dBinding.dialogListView)
                false
            }
        }
        val dialog = dialogBuild.show()
        dBinding.cancleButton2.setOnClickListener { //다이어로그의 취소버튼 클릭
            dialog.dismiss()
        }
        dBinding.saveButton2.setOnClickListener {//다이어로그의 완료버튼 클릭
            //데이터 저장 후
            var text=dBinding.editTextText.text.toString()
            if(mode=="make"){//즉, 새로운 여행 경로 만들기 모드
                if(text==""){ //제목이 비었으면
                    Toast.makeText(context,"제목을 입력하세요.",Toast.LENGTH_SHORT).show()
                }else{
                    //아래 형식으로 저장
                    myDb.document().set(hashMapOf("tripname" to text,"routeList" to loadListData)).addOnSuccessListener {
                        dialog.dismiss()
                        Toast.makeText(context,"성공적으로 저장하였습니다.",Toast.LENGTH_SHORT).show()
                    }
                    viewLifecycleOwner.lifecycleScope.async {
                        routelistAdapter.list=loadMyList()
                        routelistAdapter.notifyDataSetChanged()
                    }
                    dialog.dismiss()
                }
            }else{//여행 경로에 장소 추가 모드
                myDb.document(docId).update(hashMapOf("tripname" to text,"routeList" to loadListData)).addOnSuccessListener {
                    dialog.dismiss()
                    Toast.makeText(context,"성공적으로 저장하였습니다.",Toast.LENGTH_SHORT).show()
                }
            }
            context?.hideKeyboard(dBinding.root) //키보드내리기
        }
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