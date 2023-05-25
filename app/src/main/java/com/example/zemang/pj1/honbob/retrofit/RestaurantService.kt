package com.example.zemang.pj1.honbob.retrofit

import retrofit2.Call
import retrofit2.http.GET

// for retrofit
interface RestaurantService {
    @GET("/v3/ceae0cb6-4558-4168-8ba3-8f928a07f703")
    fun getRestaurantList(): Call<RestaurantDto>
}