package com.example.rootmap

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Context.LOCATION_SERVICE
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
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
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getSystemService
import androidx.core.content.contentValuesOf
import androidx.core.content.getSystemService
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.Friend
import com.example.rootmap.databinding.DialogLayoutBinding
import com.example.rootmap.databinding.FragmentMenu3Binding
import com.example.rootmap.databinding.RecyclerviewDialogBinding
import com.example.rootmap.databinding.RouteaddLayoutBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest.PRIORITY_HIGH_ACCURACY
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.firebase.Firebase
import com.google.firebase.FirebaseException
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.firestore
import com.kakao.vectormap.KakaoMap
import com.kakao.vectormap.KakaoMapReadyCallback
import com.kakao.vectormap.LatLng
import com.kakao.vectormap.MapGravity.TOP
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
import com.kakao.vectormap.route.RouteLine
import com.kakao.vectormap.route.RouteLineLayer
import com.kakao.vectormap.route.RouteLineOptions
import com.kakao.vectormap.route.RouteLinePattern
import com.kakao.vectormap.route.RouteLineSegment
import com.kakao.vectormap.route.RouteLineStyle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
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
    lateinit var locationManager: LocationManager
    private lateinit var makerList:MutableList<LatLng>
    var label = arrayOf<LodLabel>()
    var layerR: RouteLineLayer? = null
    var byLevelLine: RouteLine?=null

    var locationData: MutableList<SearchLocation> = mutableListOf()
    var loadListData: MutableList<MyLocation> = mutableListOf()
    var mapListData: MutableList<MyLocation> = mutableListOf()
    lateinit var listAdapter: RouteListAdapter
    lateinit var routelistAdapter: MyDocumentAdapter
    lateinit var myRouteListAdapter: ListLocationAdapter
    lateinit var swipeHelperCallbackRoute: SwapeManageAdapter
    lateinit var myDb: DocumentReference

    val db = Firebase.firestore
    private lateinit var currentId: String

    lateinit var searchText:String
    lateinit var userX:String
    lateinit var userY:String
    lateinit var clickLocationName:String
    lateinit var clickLocationAdress:GeoPoint
    var layer: LabelLayer?=null
    lateinit var dialog:AlertDialog
    lateinit var routeDialog:RecyclerviewDialogBinding
    lateinit var owner:String
    private var uivisivle:Int = 1

    var routeName:String?=null
    private val readyCallback = object: KakaoMapReadyCallback(){
        override fun onMapReady(kakaoMap: KakaoMap) {
            //현재 위치에 라벨
            kakaomap=kakaoMap
            kakaoMap.logo!!.setPosition(TOP, 20F, 20F ) // 카카오맵 API 사용시 주의사항 - 'kakao'로고 가리지 말것
            layer=kakaoMap.labelManager?.layer
            layerR= kakaoMap.routeLineManager?.getLayer()
            val style = kakaoMap.getLabelManager()?.addLabelStyles(LabelStyles.from(LabelStyle.from(
                R.drawable.userlocation
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
                if(poi.isPoi() && poi.getName().toIntOrNull() == null){
                    binding.recyclerView2.visibility=View.VISIBLE
                    binding.bottomButton.visibility=View.VISIBLE
                    binding.disButton.visibility=View.VISIBLE
                    runBlocking {
                        searchPoi(poi.getName(), kakaoMap.getCameraPosition()!!.position)
                    }
                    kakaomap!!.setPadding(0,0,0,800)
                    binding.disButton.setImageResource(R.drawable.search_list_down)
                    Log.d("Map3poiname", poi.getName())
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

    override fun onAttach(context: Context) {
        super.onAttach(context)
        locationManager = requireActivity().getSystemService(LOCATION_SERVICE) as LocationManager
    }

    val locationListener: LocationListener = object : LocationListener{
        override fun onLocationChanged(location: Location) {
            Log.d("Map3locationUpdate", "${location.latitude}, ${location.longitude}")
            Log.d("Map3locationUpdate", "kakaomap: $kakaomap, layer: $layer, currendtMarker: $currendtMarker")
            val updateLocation: LatLng = LatLng.from(location.latitude, location.longitude)
            if (kakaomap != null) {
                val style = kakaomap!!.getLabelManager()?.addLabelStyles(LabelStyles.from(LabelStyle.from(
                    R.drawable.userlocation
                )))
                val options = LabelOptions.from(updateLocation).setStyles(style)

                if (layer != null) {
                    if (currendtMarker != null)
                        layer?.remove(currendtMarker)
                    currendtMarker = layer?.addLabel(options)
                } else {
                    Log.d("Map3locationUpdate", "Layer is null")
                }
            }
            else {
                Log.d("Map3locationUpdate", "kakaomap is null")
            }
        }
    }

    private val lifecycleCallback = object: MapLifeCycleCallback(){
        override fun onMapDestroy() {
            locationManager.removeUpdates(locationListener)
            Log.d("Map3locationUpdate", "DelUpdates")
        }
        override fun onMapResumed() {
            super.onMapResumed()
            if (requestingLocationUpdates){
                if(ContextCompat.checkSelfPermission(requireActivity(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    locationManager.requestLocationUpdates(
                        LocationManager.GPS_PROVIDER,
                        1000L,
                        10.0f,
                        locationListener
                    )
                    Log.d("Map3locationUpdate", "ResumedUpdate")
                }
            }
        }

        override fun onMapPaused() {
            super.onMapPaused()
            locationManager.removeUpdates(locationListener)
            Log.d("Map3locationUpdate", "PausedUpdates")
        }

        override fun onMapError(p0: Exception?) {
            Toast.makeText(context,"map error!", Toast.LENGTH_SHORT).show()
            locationManager.removeUpdates(locationListener)
            Log.d("Map3locationUpdate", "ErrUpdates")
        }
    }
    init{
        instance = this
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            currentId = it.getString("id").toString()
        }
        fusedLocationProviderClient =
            LocationServices.getFusedLocationProviderClient(this.context)
        requireActivity().window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)
        requestFineLocationPermission(locationListener)
        requestingLocationUpdates = true

        if(ContextCompat.checkSelfPermission(requireActivity(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                1000L,
                10.0f,
                locationListener
            )
            Log.d("Map3locationUpdate", "onCreateUpdate")
        }
    }
    @SuppressLint("MissingPermission")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        myDb = Firebase.firestore.collection("user").document(currentId.toString())
        //여기부터 코드 작성
        locationPermission= registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {result->
            if (result.any { permission -> !permission.value }) {
                Toast.makeText(this.context, "위치 권한을 승인하여야 사용할 수 있습니다.", Toast.LENGTH_SHORT).show()
            }else{
                getLocation()
            }
        }
        viewLifecycleOwner.lifecycleScope.async{
            locationPermission.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION))
        }
        binding.listButton.setOnClickListener {
            viewLifecycleOwner.lifecycleScope.async{
                routelistAdapter.list=loadMyList() //어댑터에 데이터 연결
                routelistAdapter.mode="View"
                dialog=showDialog()
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
                            val style = kakaomap!!.getLabelManager()?.addLabelStyles(LabelStyles.from(LabelStyle.from(
                                R.drawable.userlocation
                            )))
                            val options = LabelOptions.from(curPosition).setStyles(style)
                            layer?.remove(currendtMarker)
                            currendtMarker = layer?.addLabel(options)
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
                binding.disButton.setImageResource(R.drawable.search_list_up)
                if(binding.recyclerView3.getVisibility() == View.GONE){
                    binding.bottomButton.visibility=View.GONE
                    kakaomap!!.setPadding(0,0,0,0)
                }
                Log.d("Map3padding", kakaomap!!.padding.toString())
            }
            else{
                binding.recyclerView2.visibility=View.VISIBLE
                binding.bottomButton.visibility=View.VISIBLE
                binding.disButton.setImageResource(R.drawable.search_list_down)
                kakaomap!!.setPadding(0,0,0,800)
                Log.d("Map3padding", kakaomap!!.padding.toString())
            }
        }
        binding.searchText.setOnEditorActionListener { v, actionId, event //키보드 엔터 사용시
            ->
            if (!binding.progressBar.isVisible) {
                context?.hideKeyboard(binding.root)
                searchText = binding.searchText.text.toString()
                if (searchText == "") {
                    Toast.makeText(context, "빈칸입니다.", Toast.LENGTH_SHORT).show()
                } else {
                    searchKeyword(searchText)
                    binding.recyclerView2.visibility = View.VISIBLE
                    binding.bottomButton.visibility = View.VISIBLE
                    binding.disButton.visibility = View.VISIBLE
                    kakaomap!!.setPadding(0, 0, 0, 800)
                    Log.d("Map3padding", kakaomap!!.padding.toString())
                }
            }
            true
        }
        //리스트들을 위한 어댑터
        val recycledViewPool=RecyclerView.RecycledViewPool().apply {
            setMaxRecycledViews(1,15)
        }
        listAdapter= RouteListAdapter()
        routelistAdapter= MyDocumentAdapter()
        routelistAdapter.userId=currentId
        myRouteListAdapter= ListLocationAdapter()
        binding.apply {
            recyclerView2.setHasFixedSize(true)
            recyclerView3.setHasFixedSize(true)
            recyclerView2.setItemViewCacheSize(10)
            recyclerView3.setItemViewCacheSize(10)
            recyclerView3.setRecycledViewPool(recycledViewPool)
        }
        val swipeHelperCallback = DragManageAdapter(myRouteListAdapter).apply {
            // 스와이프한 뒤 고정시킬 위치 지정
            setClamp(resources.displayMetrics.widthPixels.toFloat()/4)
        }
        ItemTouchHelper(swipeHelperCallback).attachToRecyclerView(binding.recyclerView3)
        // 구분선 추가
        binding.recyclerView3.addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
        binding.recyclerView2.addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
        binding.recyclerView3.setOnTouchListener { _, _ ->
            swipeHelperCallback.removePreviousClamp(binding.recyclerView3)
            false
        }
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
                    dialog=showDialog()
                }
            }

            override fun heartClick(v: View, position: Int) {
                TODO("Not yet implemented")
            }
        })
        //유저가 만든 경로 모음의 리스트
        routelistAdapter.setItemClickListener(object: MyDocumentAdapter.OnItemClickListener {
            //내 경로 리스트의 추가 버튼 클릭 시 이벤트 구현
            override fun onClick(v: View, position: Int) {
                var docId=routelistAdapter.list[position].docId
                owner=routelistAdapter.list[position].owner
                //해당 장소를 추가하기위해 새로운 팝업창 띄우기
                viewLifecycleOwner.lifecycleScope.async {
                    loadListData.clear()
                    loadMyRouteData(docId,owner)
                    loadListData.add(MyLocation(clickLocationName,clickLocationAdress,"","")) //해당 장소를 리스트에 추가
                    myRouteListAdapter.list=loadListData
                    showListDialog(docId,"add",owner)
                }
            }
            //경로 보기를 눌렀을 때 나오는 목록의 버튼(보기) 클릭시 // 경로 및 핀표시 추가
            override fun onListClick(v: View, position: Int) {
                dialog.dismiss()
                var docId=routelistAdapter.list[position].docId
                var docName=routelistAdapter.list[position].docName
                owner=routelistAdapter.list[position].owner

                viewLifecycleOwner.lifecycleScope.async {
                    loadListData.clear()
                    loadMyRouteData(docId,owner)
                    myRouteListAdapter.docId=docId
                    myRouteListAdapter.list=loadListData
                    myRouteListAdapter.notifyDataSetChanged()

                    binding.recyclerView3.adapter = myRouteListAdapter
                    //롱클릭 드래그로 순서 이동가능
                    kakaomap!!.setPadding(0,0,0,800)
                    makeLine(kakaomap!!,loadListData)
                }
                binding.apply {
                    listButton.visibility = View.INVISIBLE
                    listCloseButton.visibility = View.VISIBLE
                    routeSaveButton.visibility= View.VISIBLE
                    routeNameText.visibility= View.VISIBLE
                    recyclerView3.visibility = View.VISIBLE
                    bottomButton.visibility = View.VISIBLE
                    fullButton.visibility = View.VISIBLE
                    routeNameText.setHint(docName)
                    routeNameText.setText("")
                }
                binding.routeSaveButton.setOnClickListener {
                    var text = ""
                    if (binding.routeNameText.text.isBlank()){
                        text = docName
                    } else {
                        text = binding.routeNameText.text.toString()
                    }
                    val originalData= returnDb(owner).document(docId)
                    originalData.update(hashMapOf("tripname" to text,"routeList" to loadListData)).addOnSuccessListener {
                        Toast.makeText(context,"성공적으로 저장하였습니다.",Toast.LENGTH_SHORT).show()
                    }
                    if(text!=docName){
                        CoroutineScope(Dispatchers.IO).launch {
                            val frList=originalData.get().await().data?.get("shared") as List<String>
                            frList.forEach {
                                Firebase.firestore.collection("user").document(it).collection("sharedList").document(docId).update("docName",text)
                            }
                        }
                        routelistAdapter.notifyDataSetChanged()
                    }
                }
            }
            //삭제 버튼을 눌렀을 때 DB에서 삭제하는 기능
            override fun deleteDoc(v: View, position: Int) {
                if(routelistAdapter.list[position].owner==currentId){
                    showDeleteDialog(position)
                    db.collection("route").document(routelistAdapter.list[position].docId).delete()
                }
                else{
                    swipeHelperCallbackRoute.removeClamp(routeDialog.listView)
                    Toast.makeText(this@MenuFragment3.context, "경로 생성자만이 삭제할 수 있습니다.", Toast.LENGTH_SHORT).show()
                }
            }

            override fun shareDoc(v: View, position: Int) {
                viewLifecycleOwner.lifecycleScope.launch {
                    val data = loadFriendData()
                    val docId = routelistAdapter.list[position].docId
                    val docName = routelistAdapter.list[position].docName

                    // 경로 생성자 확인
                    if (routelistAdapter.list[position].owner == currentId) {
                        showFriendDialog(data, docId, docName)
                    } else {
                        swipeHelperCallbackRoute.removeClamp(routeDialog.listView)
                        Toast.makeText(this@MenuFragment3.context, "경로 생성자만이 공유할 수 있습니다.", Toast.LENGTH_SHORT).show()
                    }
                }
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
                kakaomap!!.setPadding(0,0,0,0)
            }
            for(doc in label)
                doc.remove()
            layerR?.remove(byLevelLine)
        }

        binding.fullButton.setOnClickListener {
            if(uivisivle == 1) { //ui 끄기
                binding.fullButton.setImageResource(R.drawable.setui)
                binding.imageView2.visibility = View.GONE
                binding.searchText.visibility = View.GONE
                binding.recyclerView2.visibility = View.GONE
                binding.recyclerView3.visibility = View.GONE
                binding.bottomButton.visibility = View.GONE
                binding.routeNameText.visibility = View.GONE
                binding.routeSaveButton.visibility = View.GONE
                binding.listCloseButton.visibility = View.GONE
                binding.disButton.visibility = View.GONE
                kakaomap!!.setPadding(0,0,0,0)
                uivisivle = 0
            } else{ //ui 켜기
                binding.fullButton.setImageResource(R.drawable.offui)
                binding.disButton.setImageResource(R.drawable.search_list_up)
                binding.imageView2.visibility = View.VISIBLE
                binding.searchText.visibility = View.VISIBLE
                binding.recyclerView3.visibility = View.VISIBLE
                binding.bottomButton.visibility = View.VISIBLE
                binding.routeNameText.visibility = View.VISIBLE
                binding.routeSaveButton.visibility = View.VISIBLE
                binding.listCloseButton.visibility = View.VISIBLE
                binding.disButton.visibility = View.VISIBLE
                kakaomap!!.setPadding(0,0,0,800)
                uivisivle = 1
            }
        }
        val selectedAddress = activity?.intent?.getStringExtra("selected_address")
        selectedAddress?.let {
            binding.searchText.setText(it)
            searchKeyword(it)
        }
        arguments?.getString("selected_address")?.let { address ->
            binding.searchText.setText(address)
            searchKeyword(address)
        }

        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        viewLifecycleOwner.lifecycleScope.async {
            listAdapter.list=locationData
            binding.recyclerView2.adapter = listAdapter
            binding.recyclerView2.layoutManager = LinearLayoutManager(context)
        }
        binding.recyclerView3.layoutManager = LinearLayoutManager(context)
        super.onViewCreated(view, savedInstanceState)
    }
    private fun Context.hideKeyboard(view: View) {
        val inputMethodManager =
            getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
    }

    fun returnDb(owner:String):CollectionReference{
        return db.collection("user").document(owner).collection("route")
    }

    private fun makeLine(kakaomap: KakaoMap,list: MutableList<MyLocation>){
        if(kakaomap!=null){
            var cnt: Int = 1
            var labels= mutableListOf<LabelOptions>()
            var latLngList= mutableListOf<LatLng>()
            var segments = mutableListOf<RouteLineSegment>()
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
            // PolyLine이 앱을 더 무겁게 해서 RouteLine으로 변경.
            val styles = RouteLineStyle.from(20f, Color.GREEN, 1f, Color.WHITE).setZoomLevel(15)
                .setPattern(RouteLinePattern.from(activity?.baseContext, R.style.GreenRouteArrowLineStyle))

            val options: RouteLineOptions =
                RouteLineOptions.from(RouteLineSegment.from(latLngList, styles))
            byLevelLine = layerR?.addRouteLine(options)

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

    fun setTitleAndSearch(title: String, addr1: String? = null, addr2: String? = null) {
        binding.searchText.setText(title)
        Log.d("setTitleAndSearch", "Title: $title, Addr1: $addr1, Addr2: $addr2")
        searchKeyword(title, addr1, addr2) // 검색 키워드로 실제 검색 요청을 실행
    }

    private fun searchKeyword(query: String, fallbackQuery1: String? = null, fallbackQuery2: String? = null) {
        Log.d("searchKeyword", "Searching for: $query, with fallback1: $fallbackQuery1, fallback2: $fallbackQuery2")
        runBlocking {
            val retrofit = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
            val api = retrofit.create(KakaoAPI::class.java)
            val call = api.getSearchKeyword(API_KEY, query, userX, userY)
            call.enqueue(object : Callback<ResultSearchKeyword> {
                override fun onResponse(
                    call: Call<ResultSearchKeyword>,
                    response: Response<ResultSearchKeyword>
                ) {
                    if (response.isSuccessful) {
                        Log.d("searchKeyword", "Response: ${response.body()}")
                        if (response.body()?.documents.isNullOrEmpty()) {
                            Log.d("searchKeyword", "No results found for: $query")
                            if (fallbackQuery1 != null) {
                                Log.d("searchKeyword", "Retrying with fallback query1: $fallbackQuery1")
                                searchKeyword(fallbackQuery1, fallbackQuery2)
                            } else if (fallbackQuery2 != null) {
                                Log.d("searchKeyword", "Retrying with fallback query2: $fallbackQuery2")
                                searchKeyword(fallbackQuery2)
                            } else {
                                Toast.makeText(context, "검색 결과가 없습니다", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            addItemsAndMarkers(response.body())
                        }
                    } else {
                        Log.e("searchKeyword", "Response failed with code: ${response.code()}")
                        Toast.makeText(context, "검색 요청이 실패했습니다. 잠시 후 다시 시도해주세요.", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<ResultSearchKeyword>, t: Throwable) {
                    Log.e("searchKeyword", "Request failed: ${t.message}")
                    Toast.makeText(context, "검색 요청이 실패했습니다. 잠시 후 다시 시도해주세요.", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }

    private fun addItemsAndMarkers(searchResult: ResultSearchKeyword?) {
        runBlocking {
            if (!searchResult?.documents.isNullOrEmpty()) {
                locationData.clear()
                for (document in searchResult!!.documents) {
                    val item = SearchLocation(
                        document.place_name,
                        document.road_address_name, document.x.toDouble(),
                        document.y.toDouble()
                    )
                    locationData.add(item)
                }
                listAdapter.list = locationData
                listAdapter.notifyDataSetChanged()

                // 검색 결과가 있는 경우 recyclerView2를 보이게 설정
                binding.recyclerView2.visibility = View.VISIBLE
                binding.bottomButton.visibility = View.VISIBLE
                binding.disButton.visibility = View.VISIBLE
                kakaomap!!.setPadding(0, 0, 0, 800)

                // 첫 번째 아이템 클릭
                if (locationData.isNotEmpty()) {
                    val firstItem = locationData[0]
                    val firstLocation = LatLng.from(firstItem.y, firstItem.x)
                    val styles = kakaomap!!.getLabelManager()?.addLabelStyles(
                        LabelStyles.from(LabelStyle.from(R.drawable.clicklocation))
                    )
                    val options = LabelOptions.from(firstLocation).setStyles(styles)
                    if (searchMarker == null) {
                        searchMarker = layer?.addLabel(options)
                    } else {
                        layer?.remove(searchMarker)
                        searchMarker = layer?.addLabel(options)
                    }

                    // 지도 이동
                    if (kakaomap != null) {
                        val cameraUpdate = CameraUpdateFactory.newCenterPosition(firstLocation, zoomlevel)
                        kakaomap!!.moveCamera(cameraUpdate, CameraAnimation.from(500, true, true))
                    } else {
                        Toast.makeText(context, "알 수 없는 오류가 발생했습니다. 재시도 해주세요.", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                Toast.makeText(context, "검색 결과가 없습니다", Toast.LENGTH_SHORT).show()
                locationData.clear()
                listAdapter.list = locationData
                listAdapter.notifyDataSetChanged()

                // 검색 결과가 없는 경우 recyclerView2를 숨김
                binding.recyclerView2.visibility = View.GONE
                binding.bottomButton.visibility = View.GONE
                binding.disButton.visibility = View.GONE
                kakaomap!!.setPadding(0, 0, 0, 0)
            }
        }
    }

    private fun searchPoi(text:String, latLng: LatLng){
        val retrofit = Retrofit.Builder() // Retrofit 구성
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        val api = retrofit.create(KakaoAPI::class.java) // 통신 인터페이스를 객체로 생성
        var newtext = ""
        Log.d("Map3indexOf", text.indexOf("세대").toString())
        if (text.indexOf("세대") != -1 && text.indexOf("아파트") != -1) {
            val idx = text.indexOf("아파트")
            newtext = text.substring(0 until idx + 3)
            Log.d("Map3apart", newtext)
        } else {
            newtext = text
        }
        val x = latLng.longitude.toString()
        val y = latLng.latitude.toString()
        val call = api.getSearchKeyword(API_KEY, newtext, x, y) // 검색 조건 입력
        // API 서버에 요청
        call.enqueue(object : Callback<ResultSearchKeyword> {
            override fun onResponse(
                call: Call<ResultSearchKeyword>,
                response: Response<ResultSearchKeyword>
            ) {
// 통신 성공
                addItemsAndMarkers(response.body())
            }

            override fun onFailure(call: Call<ResultSearchKeyword>, t: Throwable) {
// 통신 실패
                Log.w("LocalSearch", "통신 실패: ${t.message}")
            }
        })
    }

    private suspend fun loadMyList(): MutableList<MyRouteDocument> {
        var list= mutableListOf<MyRouteDocument>()
        return try {
            val myList = returnDb(currentId).get().await()
            if(!myList.isEmpty){
                myList.forEach {
                    list.add(MyRouteDocument(it.data?.get("tripname").toString(),it.id,currentId))
                }
            }
            val sharedList=db.collection("user").document(currentId).collection("sharedList").get().await()
            if(!sharedList.isEmpty){
                sharedList.forEach {
                    list.add(MyRouteDocument(it.data?.get("docName").toString(),it.data?.get("docId").toString(),it.data?.get("created").toString()))
                }
            }
            list
        } catch (e: FirebaseException) {
            Log.d("list_test", "error")
            list
        }
    }
    suspend fun loadMyRouteData(id:String,owner:String): Boolean {
        //경로내의 여행지 리스트들을 가져오는 함수
        var dataList= mutableListOf<Map<String,*>>()
        return try {
            var data: MutableMap<*, *>
            db.collection("user").document(owner).collection("route").document(id).get().addOnSuccessListener { documents->
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
    private fun showDialog():AlertDialog{ //다이어로그로 팝업창 구현
        //boolean은 데이터의 유무-> true 있음, false 없음
        routeDialog = RecyclerviewDialogBinding.inflate(layoutInflater)
        val dialogBuild = AlertDialog.Builder(context).setView(routeDialog.root)
        dialogBuild.setTitle("내 여행 리스트")
        routeDialog.listView.adapter = routelistAdapter
        routeDialog.listView.layoutManager = LinearLayoutManager(context)
        swipeHelperCallbackRoute = SwapeManageAdapter(routelistAdapter).apply {
            // 스와이프한 뒤 고정시킬 위치 지정
            setClamp(resources.displayMetrics.widthPixels.toFloat()/4)
        }
        ItemTouchHelper(swipeHelperCallbackRoute).attachToRecyclerView(routeDialog.listView)
        routeDialog.listView.setOnTouchListener { _, _ ->
            swipeHelperCallbackRoute.removePreviousClamp(routeDialog.listView)
            false
        }
        if(routelistAdapter.list.isNullOrEmpty()){
            routeDialog.checkText.apply {
                text="아직 경로가 없습니다. 새로운 경로를 만들어주세요."
                visibility=View.VISIBLE
            }
        }
        routeDialog.addTripRouteText.text="새로운 경로 생성"
        routeDialog.addTripRouteText.setOnClickListener {
            showListDialog("","make",currentId)
        }
        val dialog = dialogBuild.show()
        return dialog
    }


    private fun showDeleteDialog(position: Int){
        val dBinding = DialogLayoutBinding.inflate(layoutInflater)
        dBinding.wButton.text = "취소" //다이어로그의 텍스트 변경
        dBinding.bButton.text = "확인"
        dBinding.content.text = "해당 경로를 삭제하시겠습니까?"
        val dialogBuild = AlertDialog.Builder(context).setView(dBinding.root)
        val dialog = dialogBuild.show() //다이어로그 창 띄우기
        dBinding.bButton.setOnClickListener {
            //검정 버튼의 기능 구현 ↓
            var docId=routelistAdapter.list[position].docId
            returnDb(currentId).document(docId).delete()
            routelistAdapter.list.removeAt(position)
            routelistAdapter.notifyItemRemoved(position)
            dialog.dismiss()
        }
        dBinding.wButton.setOnClickListener {//취소버튼
            //회색 버튼의 기능 구현 ↓
            swipeHelperCallbackRoute.removeClamp(routeDialog.listView)
            dialog.dismiss()
        }
    }

    private fun showFriendDialog(friendList: MutableList<Friend>, docId: String, docName: String): AlertDialog {
        myDb = Firebase.firestore.collection("user").document(currentId.toString())
        val dBinding = RecyclerviewDialogBinding.inflate(layoutInflater)
        val dialogBuild = AlertDialog.Builder(context).setView(dBinding.root)
        dialogBuild.setTitle("공유할 친구 목록")
        var myFriendAdapter = FriendAdapter()
        myFriendAdapter.run {
            mode = "RouteShare"
            myid = currentId
            list = friendList
        }
        dBinding.run {
            listView.adapter = myFriendAdapter
            listView.layoutManager = LinearLayoutManager(context)
            addTripRouteText.text = "공유하기"
        }
        val dialog = dialogBuild.show()
        dBinding.addTripRouteText.setOnClickListener {
            // 체크된 친구와 여행경로 공유
            val checkFriends = myFriendAdapter.mChecked.toList()

            if (checkFriends.isEmpty()) {
                // 체크된 친구가 없을 때
                Toast.makeText(context, "공유할 친구를 선택하세요", Toast.LENGTH_SHORT).show()
            } else {
                // 체크된 친구를 shared에 저장(자신의 DB데이터에)
                myDb.collection("route").document(docId).update("shared", checkFriends).addOnSuccessListener {
                    Toast.makeText(context, "${checkFriends.joinToString(", ")}님에게 경로 공유 완료", Toast.LENGTH_SHORT).show()
                    dialog.dismiss()
                }
                // 체크된 친구의 sharedList에 추가
                checkFriends.forEach {
                    Firebase.firestore.collection("user").document(it).collection("sharedList").document(docId).set(
                        hashMapOf("created" to currentId, "docId" to docId, "docName" to docName)
                    )
                }
            }
        }
        return dialog
    }


    suspend fun loadFriendData(): MutableList<Friend> {
        var friendList= mutableListOf<Friend>()
        return try {
            val fr_add = myDb.collection("friend").whereEqualTo("state", "2").get().await()
            for (fr in fr_add.documents) {
                var id = fr.data?.get("id").toString() //친구 id
                val fr_data = Firebase.firestore.collection("user").document(id).get().await()
                var load = Friend(fr_data.data?.get("nickname").toString(), id)
                friendList.add(load)
            }
            Log.d("list_test", "try")
            friendList
        } catch (e: FirebaseException) {
            Log.d("list_test", "error")
            friendList
        }
    }

    private fun showListDialog(docId:String,mode:String,owner: String):AlertDialog{ //다이어로그로 팝업창 구현
        var dBinding = RouteaddLayoutBinding.inflate(layoutInflater)
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
                swipeHelperCallbackRoute.removePreviousClamp(routeDialog.listView)
                false
            }
        }
        val dialog = dialogBuild.show()
        dBinding.cancleButton2.setOnClickListener { //다이어로그의 취소버튼 클릭
            dialog.dismiss()
        }
        dBinding.saveButton2.setOnClickListener {//다이어로그의 완료버튼 클릭
            //데이터 저장 후
            context?.hideKeyboard(dBinding.root) //키보드내리기
            var text=dBinding.editTextText.text.toString()
            if(mode=="make"){//즉, 새로운 여행 경로 만들기 모드
                if(text==""){ //제목이 비었으면
                    Toast.makeText(context,"제목을 입력하세요.",Toast.LENGTH_SHORT).show()
                }else{
                    //아래 형식으로 저장
                    var sharedList= listOf<String>()
                    returnDb(currentId).document().set(hashMapOf("tripname" to text,"routeList" to loadListData,"created" to currentId,"shared" to sharedList)).addOnSuccessListener {
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
                returnDb(owner).document(docId).update(hashMapOf("tripname" to text,"routeList" to loadListData)).addOnSuccessListener {
                    dialog.dismiss()
                    Toast.makeText(context,"성공적으로 저장하였습니다.",Toast.LENGTH_SHORT).show()
                }
            }
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

    private fun requestFineLocationPermission(listener: LocationListener){
        if (ContextCompat.checkSelfPermission(
                requireActivity(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // 권한이 없을 경우, 사용자에게 요청
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                PERMISSION_FINE_LOCATION
            )
        } else {
            // 권한이 이미 있을 경우, 위치 정보를 사용할 수 있음
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            PERMISSION_FINE_LOCATION -> {
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    // 권한이 부여되면 위치 정보를 사용할 수 있음
                } else {
                    // 권한이 거부되면, 기능 사용 불가
                    Toast.makeText(context, "위치서비스 권한이 거부되어 지도를 사용하실수 없습니다.", Toast.LENGTH_SHORT).show()
                }
                return
            }
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
        private const val PERMISSION_FINE_LOCATION = 100
        private var instance: MenuFragment3? = null
        fun getInstance():MenuFragment3?{
            return instance
        }
    }
}