package com.example.rootmap

import retrofit2.Retrofit
import retrofit2.converter.simplexml.SimpleXmlConverterFactory

object RetrofitClientInstance {

    private var retrofit: Retrofit? = null
    private val BASE_URL = "https://apis.data.go.kr/B551011/KorService1/"

    fun getRetrofitInstance(): Retrofit {
        if (retrofit == null) {
            retrofit = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(SimpleXmlConverterFactory.create())
                .build()
        }
        return retrofit!!
    }
}
