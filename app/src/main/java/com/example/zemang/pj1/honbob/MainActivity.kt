package com.example.zemang.pj1.honbob

import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.example.zemang.pj1.honbob.adapter.RestaurantListAdapter
import com.example.zemang.pj1.honbob.adapter.ViewPagerAdapter
import com.example.zemang.pj1.honbob.retrofit.RestaurantDto
import com.example.zemang.pj1.honbob.retrofit.RestaurantModel
import com.example.zemang.pj1.honbob.retrofit.RestaurantService
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.*
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.overlay.Overlay
import com.naver.maps.map.util.FusedLocationSource
import com.naver.maps.map.util.MarkerIcons
import com.naver.maps.map.widget.LocationButtonView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit

class MainActivity : AppCompatActivity(), OnMapReadyCallback, Overlay.OnClickListener {

    private lateinit var naverMap: NaverMap
    private lateinit var locationSource: FusedLocationSource

    private val mapView: MapView by lazy { findViewById(R.id.mapView) }

    private val viewPager: ViewPager2 by lazy { findViewById(R.id.houseViewPager) }
    private val viewPagerAdapter = ViewPagerAdapter(itemClicked = {
        onRestaurantModelClicked(restaurantModel = it)
    })

    private val recyclerView: RecyclerView by lazy { findViewById(R.id.recyclerView) }
    private val recyclerViewAdapter = RestaurantListAdapter()

    private val currentLocationButton: LocationButtonView by lazy { findViewById(R.id.currentLocationButton) }

    private val bottomSheetTitleTextView: TextView by lazy { findViewById(R.id.bottomSheetTitleTextView) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this)

        initRestaurantViewPager()
        initRestaurantRecyclerView()
    }

    override fun onMapReady(map: NaverMap) {
        naverMap = map
        naverMap.maxZoom = 18.0
        naverMap.minZoom = 10.0

        val cameraUpdate = CameraUpdate.scrollTo(LatLng(37.3860462, 126.6393889))
        naverMap.moveCamera(cameraUpdate)

        val uiSetting = naverMap.uiSettings
        uiSetting.isLocationButtonEnabled = false

        currentLocationButton.map = naverMap

        locationSource =
            FusedLocationSource(this@MainActivity, LOCATION_PERMISSION_REQUEST_CODE)
        naverMap.locationSource = locationSource

        getRestaurantListFromAPI()
    }

    private fun getRestaurantListFromAPI() {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://run.mocky.io")
            .addConverterFactory(retrofit2.converter.gson.GsonConverterFactory.create())
            .build()

        retrofit.create(RestaurantService::class.java).also {
            it.getRestaurantList()
                .enqueue(object : Callback<RestaurantDto> {
                    override fun onResponse(call: Call<RestaurantDto>, response: Response<RestaurantDto>) {
                        if (response.isSuccessful.not()) {
                            Log.d("Retrofit", "error1")
                            return
                        }

                        response.body()?.let { dto ->
                            updateMarker(dto.items)
                            viewPagerAdapter.submitList(dto.items)
                            recyclerViewAdapter.submitList(dto.items)
                            bottomSheetTitleTextView.text = "${dto.items.size}개의 맛집 리스트"
                        }
                    }

                    override fun onFailure(call: Call<RestaurantDto>, t: Throwable) {
                        Log.d("Retrofit", "error2")
                        Log.d("Retrofit", t.stackTraceToString())
                    }

                })
        }
    }

    private fun initRestaurantViewPager() {
        viewPager.adapter = viewPagerAdapter

        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)

                val selectedRestaurantModel = viewPagerAdapter.currentList[position]
                val cameraUpdate =
                    CameraUpdate.scrollTo(LatLng(selectedRestaurantModel.lat, selectedRestaurantModel.lng))
                        .animate(CameraAnimation.Easing)
                naverMap.moveCamera(cameraUpdate)
            }
        })
    }

    private fun initRestaurantRecyclerView() {
        recyclerView.adapter = recyclerViewAdapter
        recyclerView.layoutManager = LinearLayoutManager(this)
    }

    private fun updateMarker(Restaurants: List<RestaurantModel>) {
        Restaurants.forEach { Restaurant ->

            val marker = Marker()
            marker.position = LatLng(Restaurant.lat, Restaurant.lng)
            marker.onClickListener = this
            marker.map = naverMap
            marker.tag = Restaurant.id
            marker.icon = MarkerIcons.BLACK
            marker.iconTintColor = Color.YELLOW

        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode != LOCATION_PERMISSION_REQUEST_CODE)
            return

        if (locationSource.onRequestPermissionsResult(requestCode, permissions, grantResults)) {
            if (!locationSource.isActivated) {
                naverMap.locationTrackingMode = LocationTrackingMode.None
            }
            return
        }
    }


    override fun onClick(overlay: Overlay): Boolean {
        val selectedModel = viewPagerAdapter.currentList.firstOrNull {
            it.id == overlay.tag
        }
        selectedModel?.let {
            val position = viewPagerAdapter.currentList.indexOf(it)
            viewPager.currentItem = position
        }
        return true
    }

    private fun onRestaurantModelClicked(restaurantModel: RestaurantModel) {
        val intent = Intent()
            .apply {
                action = Intent.ACTION_SEND
                putExtra(
                    Intent.EXTRA_TEXT,
                    "${restaurantModel.title} ${restaurantModel.price} ",
                )
                type = "text/plain"
            }
        startActivity(Intent.createChooser(intent, null))
    }

    override fun onStart() {
        super.onStart()
        mapView.onStart()
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapView.onSaveInstanceState(outState)
    }

    override fun onStop() {
        super.onStop()
        mapView.onStop()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1000
    }
}