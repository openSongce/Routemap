package com.example.rootmap

import android.Manifest
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.rootmap.databinding.FragmentMenu3Binding
import com.kakao.vectormap.KakaoMap
import com.kakao.vectormap.KakaoMapReadyCallback
import com.kakao.vectormap.LatLng
import com.kakao.vectormap.MapLifeCycleCallback
import com.kakao.vectormap.MapView
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
    private lateinit var mapview: MapView
    private var zoomlevel = 17
    lateinit var locationPermission: ActivityResultLauncher<Array<String>>
    private lateinit var locationService: LocationService
    lateinit var startpositon:LatLng

    //프래그먼트의 binding
    val binding by lazy { FragmentMenu3Binding.inflate(layoutInflater) }

    private val readyCallback = object: KakaoMapReadyCallback(){
        override fun onMapReady(p0: KakaoMap) {}
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
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
      //  binding= FragmentMenu3Binding.inflate(inflater, container, false)
        locationService = LocationService(requireContext())
        //여기부터 코드 작성
         locationPermission= registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {result->
            if (result.any { permission -> !permission.value }) {
                Toast.makeText(this.context, "위치 권한을 승인하여야 사용할 수 있습니다.", Toast.LENGTH_SHORT).show()
            }else{
                fetchLocation()
            }
        }
        mapview = binding.mapView.findViewById(R.id.map_view)
        viewLifecycleOwner.lifecycleScope.async{
           locationPermission.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION))
          // val style = KakaoMap.getLabelManager()?.addLabelStyles(LabelStyles.from(LabelStyle.from(R.drawable.ic_mark)))

        }
        binding.addButton.setOnClickListener {
            Toast.makeText(context,"클릭",Toast.LENGTH_SHORT).show()
        }
        binding.locationButton.setOnClickListener {

        }
        //
        return binding.root
    }
    private fun fetchLocation(){
        locationService.fetchLocation { latitude, longitude ->
            startpositon=LatLng.from(latitude,longitude)
            Log.d("test","${latitude}, ${longitude}")
            binding.progressBar.visibility=View.GONE
            mapview.start(lifecycleCallback, readyCallback)
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