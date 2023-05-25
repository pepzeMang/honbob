package com.example.zemang.pj1.honbob.retrofit

// for retrofit
data class RestaurantModel(
    val id: Int,
    val title: String,
    val price: String,
    val lat: Double,
    val lng: Double,
    val imgUrl: String
)
