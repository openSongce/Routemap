package com.example.rootmap

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context.LOCATION_SERVICE
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.rootmap.databinding.FragmentMenu3Binding
import com.google.android.gms.location.LocationRequest.PRIORITY_HIGH_ACCURACY
import com.google.android.gms.location.LocationServices
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


    @SuppressLint("MissingPermission")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        //  binding= FragmentMenu3Binding.inflate(inflater, container, false)
        //여기부터 코드 작성
        locationPermission= registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {result->
            if (result.any { permission -> !permission.value }) {
                Toast.makeText(this.context, "위치 권한을 승인하여야 사용할 수 있습니다.", Toast.LENGTH_SHORT).show()
            }else{
                getLocation()
            }
        }
        //mapview = binding.mapView.findViewById(R.id.map_view)
        mapview=binding.mapViewId
        viewLifecycleOwner.lifecycleScope.async{
            locationPermission.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION))
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
    @SuppressLint("MissingPermission")
    private fun getLocation() {
        val fusedLocationProviderClient =
            LocationServices.getFusedLocationProviderClient(this.context)

        fusedLocationProviderClient.getCurrentLocation(PRIORITY_HIGH_ACCURACY, null)
            .addOnSuccessListener { success: Location? ->
                success?.let { location ->
                    Log.d("GmapViewFragment2","${location.latitude}, ${location.longitude}")
                    startpositon= LatLng.from(location.latitude, location.longitude)
                     binding.progressBar.visibility=View.GONE
                    mapview.start(lifecycleCallback, readyCallback)
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
    }
}