package com.example.rootmap

import android.graphics.Color
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.rootmap.databinding.ActivityRouteMapViewBinding
import com.google.firebase.Firebase
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.firestore
import com.kakao.vectormap.KakaoMap
import com.kakao.vectormap.KakaoMapReadyCallback
import com.kakao.vectormap.LatLng
import com.kakao.vectormap.MapLifeCycleCallback
import com.kakao.vectormap.camera.CameraAnimation
import com.kakao.vectormap.camera.CameraUpdateFactory
import com.kakao.vectormap.shape.MapPoints
import com.kakao.vectormap.shape.PolylineOptions
import com.kakao.vectormap.shape.PolylineStyle


class RouteMapViewActivity : AppCompatActivity() {
    val db = Firebase.firestore
    var kakaomap: KakaoMap? = null
    lateinit var myDb: CollectionReference
    private val duration = 500
    var cnt: Int = 0

    private val readyCallback = object : KakaoMapReadyCallback() {
        override fun onMapReady(kakaoMap: KakaoMap) {
            kakaomap = kakaoMap
            val shapeManager = kakaoMap.shapeManager
            var list: MutableList<LatLng> = mutableListOf()
            var dataList = mutableListOf<Map<String, *>>()
            var data: MutableMap<*, *>
            var glist = mutableListOf<GeoPoint>()

            myDb = db.collection("user").document(intent.getStringExtra("id").toString())
                .collection("route")
            myDb.document(intent.getStringExtra("routeId").toString())
                .get()
                .addOnSuccessListener { result ->
                        list.clear()
                        data = result.data as MutableMap<*, *>
                        dataList.addAll(data["routeList"] as List<Map<String, *>>)
                        dataList.forEach {
                            glist.add(it["position"] as GeoPoint)
                        }
                        for (doc in glist) {
                            list.add(LatLng.from(doc.latitude, doc.longitude))
                            cnt++
                        }
                        var areaPolyline = shapeManager!!.layer.addPolyline(getAreaOptions(list)!!)
                        if(cnt!=0) {
                            kakaoMap.moveCamera(
                                CameraUpdateFactory.newCenterPosition(
                                    list[0], 15
                                ),
                                CameraAnimation.from(duration)
                            )
                        } else {
                            kakaoMap.moveCamera(
                                CameraUpdateFactory.newCenterPosition(
                                    LatLng.from(37.5642135,127.0016985), 15
                                ),
                                CameraAnimation.from(duration)
                            )
                            Toast.makeText(this@RouteMapViewActivity, "경로가 없습니다.", Toast.LENGTH_SHORT).show()
                        }

                    }
                }


        }

    private val lifecycleCallback = object : MapLifeCycleCallback() {
        override fun onMapDestroy() {
            Toast.makeText(this@RouteMapViewActivity, "뒤로가기", Toast.LENGTH_SHORT).show()
        }

        override fun onMapError(p0: Exception?) {
            Toast.makeText(this@RouteMapViewActivity, "map error", Toast.LENGTH_SHORT).show()
        }

    }


    private lateinit var binding: ActivityRouteMapViewBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRouteMapViewBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        binding.mapViewId.start(lifecycleCallback, readyCallback)

    }

    fun getAreaOptions(list: MutableList<LatLng>): PolylineOptions? {
        return PolylineOptions.from(
            MapPoints.fromLatLng(list),
            PolylineStyle.from(10f, Color.parseColor("#80ff2c35"), 3f, Color.RED)
        )
    }
}



