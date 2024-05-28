package com.example.rootmap

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.rootmap.databinding.FragmentMenu3Binding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest.PRIORITY_HIGH_ACCURACY
import com.google.android.gms.location.LocationServices
import com.google.firebase.Firebase
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.firestore
import com.kakao.vectormap.KakaoMap
import com.kakao.vectormap.KakaoMapReadyCallback
import com.kakao.vectormap.LatLng
import com.kakao.vectormap.MapLifeCycleCallback
import com.kakao.vectormap.camera.CameraPosition
import com.kakao.vectormap.camera.CameraUpdateFactory
import com.kakao.vectormap.label.Label
import com.kakao.vectormap.label.LabelLayer
import com.kakao.vectormap.label.LabelOptions
import com.kakao.vectormap.label.LabelStyle
import com.kakao.vectormap.label.LabelStyles
import kotlinx.coroutines.async


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [MenuFragment3.newInstance] factory method to
 * create an instance of this fragment.
 */
class MenuFragment3 : Fragment() {
    private var zoomlevel = 17
    lateinit var locationPermission: ActivityResultLauncher<Array<String>>
    var startpositon:LatLng?=null
    lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    var kakaomap: KakaoMap?=null
    lateinit var startCamera: CameraPosition
    val routeData: MutableList<Route> = mutableListOf()
    //프래그먼트의 binding
    val binding by lazy { FragmentMenu3Binding.inflate(layoutInflater) }
    lateinit var listAdapter: RouteListAdapter
    val db = Firebase.firestore
    lateinit var myDb: CollectionReference
    private var currentId: String? = null
    lateinit var layers: LabelLayer
    var clickMarker: Label?=null
    var currendtMarker:Label?=null
    private val readyCallback = object: KakaoMapReadyCallback(){
        override fun onMapReady(kakaoMap: KakaoMap) {
            //현재 위치에 라벨
            kakaomap=kakaoMap
            var layer=kakaoMap.labelManager?.layer
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
                    R.drawable.mylocation
                )))
                val options1 = LabelOptions.from(latLng).setStyles(styles)
                if(clickMarker==null){ //이미 찍힌 마크가 없을 때
                    clickMarker=layer?.addLabel(options1)
                    Log.d("test","${latLng}")
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            currentId = it.getString("id")
        }
        fusedLocationProviderClient =
            LocationServices.getFusedLocationProviderClient(this.context)
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
                var cameraUpdate= CameraUpdateFactory.newCameraPosition(startCamera)
                var cameraZoom=CameraUpdateFactory.zoomTo(kakaomap!!.zoomLevel)
                kakaomap!!.moveCamera(cameraUpdate)
                kakaomap!!.moveCamera(cameraZoom)
            }
      }
        binding.searchText.setOnEditorActionListener{ v, actionId, event //키보드 엔터 사용시
            ->
            context?.hideKeyboard(binding.root)
            binding.recyclerView2.visibility=View.VISIBLE
            binding.disButton.visibility=View.VISIBLE
            binding.disButton.setOnClickListener {
                binding.recyclerView2.visibility=View.GONE
                binding.disButton.visibility=View.GONE
            }
            true
        }
        listAdapter= RouteListAdapter()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        viewLifecycleOwner.lifecycleScope.async {
            loadData()
            listAdapter.list=routeData
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
    @SuppressLint("MissingPermission")
    private fun getLocation() {
        fusedLocationProviderClient.getCurrentLocation(PRIORITY_HIGH_ACCURACY, null)
            .addOnSuccessListener { success: Location? ->
                success?.let { location ->
                    Log.d("location_test","${location.latitude}, ${location.longitude}")
                    startpositon= LatLng.from(location.latitude, location.longitude)
                    binding.progressBar.visibility=View.GONE
                    binding.mapViewId.start(lifecycleCallback, readyCallback)
                }
            }
            .addOnFailureListener { fail ->

            }
    }
    fun loadData(){
        for(i in 0..1){
            routeData.add(Route("이름","주소"))
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
    }
}