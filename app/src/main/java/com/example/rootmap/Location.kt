package com.example.rootmap

import android.location.Geocoder
import com.google.firebase.firestore.GeoPoint
import com.kakao.vectormap.LatLng

data class SearchLocation(var name:String, var adress:String,var x:Double,var y:Double)

data class MyRouteDocument(var docName:String,var docId:String)

data class MyLocation(var name:String, var position: GeoPoint,var memo:String,var spending:String)
