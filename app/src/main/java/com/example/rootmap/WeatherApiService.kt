package com.example.rootmap

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.http.Url

interface WeatherApiService {
    @GET
    fun getUltraSrtNcst(
        @Url url: String
    ): Call<WeatherResponse>

    @GET
    fun getVilageFcst(
        @Url url: String
    ): Call<WeatherResponse>
}
