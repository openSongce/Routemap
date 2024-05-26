package com.example.rootmap

import kotlin.math.*
object GridConverter {
    data class MapParameter(
        val Re: Double, // 지구 반경(km)
        val grid: Double, // 격자 간격(km)
        val slat1: Double, // 투영 위도1(degree)
        val slat2: Double, // 투영 위도2(degree)
        val olon: Double, // 기준점 경도(degree)
        val olat: Double, // 기준점 위도(degree)
        val xo: Double, // 기준점 X좌표(GRID)
        val yo: Double, // 기준점 Y좌표(GRID)
        var first: Int = 0
    )

    private val map = MapParameter(
        Re = 6371.00877,
        grid = 5.0,
        slat1 = 30.0,
        slat2 = 60.0,
        olon = 126.0,
        olat = 38.0,
        xo = 43.0,
        yo = 136.0
    )

    private fun lamcproj(lon: Double, lat: Double, code: Int): Pair<Double, Double> {
        val PI = Math.PI
        val DEGRAD = PI / 180.0
        val RADDEG = 180.0 / PI

        var re = map.Re / map.grid
        val slat1 = map.slat1 * DEGRAD
        val slat2 = map.slat2 * DEGRAD
        val olon = map.olon * DEGRAD
        val olat = map.olat * DEGRAD

        val sn = tan(PI * 0.25 + slat2 * 0.5) / tan(PI * 0.25 + slat1 * 0.5)
        val snLog = ln(cos(slat1) / cos(slat2)) / ln(sn)
        val sf = tan(PI * 0.25 + slat1 * 0.5)
        val sfPow = sf.pow(snLog) * cos(slat1) / snLog
        val ro = tan(PI * 0.25 + olat * 0.5)
        val roRe = re * sfPow / ro.pow(snLog)

        return if (code == 0) {
            // 위경도 -> 격자 좌표 변환
            val ra = tan(PI * 0.25 + lat * DEGRAD * 0.5)
            val raRe = re * sfPow / ra.pow(snLog)
            var theta = lon * DEGRAD - olon
            if (theta > PI) theta -= 2.0 * PI
            if (theta < -PI) theta += 2.0 * PI
            theta *= snLog
            val x = (raRe * sin(theta) + map.xo + 0.5)
            val y = (roRe - raRe * cos(theta) + map.yo + 0.5)
            Pair(x, y)
        } else {
            // 격자 좌표 -> 위경도 변환
            val xn = lon - map.xo
            val yn = roRe - lat + map.yo
            val ra = sqrt(xn * xn + yn * yn)
            val alat = 2.0 * atan((re * sfPow / ra).pow(1.0 / snLog)) - PI * 0.5
            val theta = atan2(xn, yn) / snLog + olon
            val lonResult = theta * RADDEG
            val latResult = alat * RADDEG
            Pair(lonResult, latResult)
        }
    }
    fun convert(lat: Double, lon: Double): Pair<Int, Int> {
        val (x, y) = lamcproj(lon, lat, 0)
        return Pair(x.toInt(), y.toInt())
    }
}
