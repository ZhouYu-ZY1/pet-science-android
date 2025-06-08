package com.zhouyu.pet_science.activities

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.amap.api.location.AMapLocationClient
import com.amap.api.location.AMapLocationClientOption
import com.amap.api.maps.AMap
import com.amap.api.maps.CameraUpdateFactory
import com.amap.api.maps.MapView
import com.amap.api.maps.model.LatLng
import com.amap.api.maps.model.Marker
import com.amap.api.maps.model.MarkerOptions
import com.amap.api.maps.model.MyLocationStyle
import com.amap.api.services.core.LatLonPoint
import com.amap.api.services.core.PoiItem
import com.amap.api.services.geocoder.GeocodeResult
import com.amap.api.services.geocoder.GeocodeSearch
import com.amap.api.services.geocoder.RegeocodeQuery
import com.amap.api.services.geocoder.RegeocodeResult
import com.amap.api.services.poisearch.PoiResult
import com.amap.api.services.poisearch.PoiSearch
import com.zhouyu.pet_science.R
import com.zhouyu.pet_science.activities.base.BaseActivity
import com.zhouyu.pet_science.databinding.ActivityMapBinding
import com.zhouyu.pet_science.utils.InputUtils

class MapActivity : BaseActivity(), GeocodeSearch.OnGeocodeSearchListener, PoiSearch.OnPoiSearchListener {
    private lateinit var binding: ActivityMapBinding
    private lateinit var aMap: AMap
    private lateinit var locationClient: AMapLocationClient
    private lateinit var geocodeSearch: GeocodeSearch
    private lateinit var searchAdapter: SearchResultAdapter

    private var currentMarker: Marker? = null
    private var currentLatLng: LatLng? = null
    private var currentAddress: String = ""
    private var currentProvince: String = ""
    private var currentCity: String = ""
    private var currentDistrict: String = ""

    private val REQUEST_CODE_LOCATION = 100
    private var searchPoiItems = mutableListOf<PoiItem>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMapBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setTopBarView(binding.layoutMap, true)

        // 初始化视图
        binding.rvSearchResult.setPadding(
            binding.rvSearchResult.left,
            binding.rvSearchResult.top,
            binding.rvSearchResult.right,
            binding.layoutBottomBar.height
        )

        binding.map.onCreate(savedInstanceState)

        // 初始化地图
        if (::aMap.isInitialized.not()) {
            aMap = binding.map.map
            aMap.mapType = AMap.MAP_TYPE_NORMAL

            // 设置定位蓝点
            val myLocationStyle = MyLocationStyle()
            myLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_LOCATE)
            aMap.myLocationStyle = myLocationStyle
            aMap.isMyLocationEnabled = true
            aMap.uiSettings.isMyLocationButtonEnabled = true; //设置默认定位按钮是否显示，非必需设置。
        }
        
        // 初始化地理编码搜索
        geocodeSearch = GeocodeSearch(this)
        geocodeSearch.setOnGeocodeSearchListener(this)
        
        // 初始化搜索结果列表
        setupSearchResultList()
        
        // 设置搜索相关监听
        setupSearchListeners()
        
        // 检查权限
        checkPermissions()
        
        // 设置地图点击事件
        aMap.setOnMapClickListener { latLng ->
            updateMarkerAndAddress(latLng)
        }
        aMap.setOnPOIClickListener{
            // 点击POI时，更新地址信息
            updateMarkerAndAddress(it.coordinate)
        }
//        aMap.setOnMarkerClickListener{
//            // 点击标记时，更新地址信息
//            MyToast.show("点击了标记")
//            currentLatLng?.let { updateMarkerAndAddress(it) }
//            true
//        }
//        aMap.setOnPolylineClickListener {
//            // 点击折线时，更新地址信息
//            MyToast.show("点击了折线")
//            updateMarkerAndAddress(it.getNearestLatLng())
//        }
//        aMap.setOnInfoWindowClickListener {
//            // 点击信息窗口时，更新地址信息
//            MyToast.show("点击了信息窗口")
//            currentLatLng?.let { updateMarkerAndAddress(it) }
//        }
        aMap.setOnMapLoadedListener{
            // 地图加载完成后，开始定位
            startLocation()
        }

        // 设置确认按钮点击事件
        binding.btnConfirm.setOnClickListener {
            if (currentLatLng != null && currentAddress.isNotEmpty()) {
                val intent = Intent()
                intent.putExtra("address", currentAddress)
                intent.putExtra("province", currentProvince)
                intent.putExtra("city", currentCity)
                intent.putExtra("district", currentDistrict)
                intent.putExtra("latitude", currentLatLng?.latitude ?: 0.0)
                intent.putExtra("longitude", currentLatLng?.longitude ?: 0.0)
                setResult(RESULT_OK, intent)
                finish()
            } else {
                Toast.makeText(this, "请先选择位置", Toast.LENGTH_SHORT).show()
            }
        }

        // 返回按钮
        binding.btnBack.setOnClickListener {
            finish()
        }
    }
    
    private fun setupSearchResultList() {
        searchAdapter = SearchResultAdapter(searchPoiItems) { poiItem ->
            // 隐藏键盘
            InputUtils.inputHide(this, binding.etSearch)

            // 点击搜索结果项的处理
            val latLng = LatLng(poiItem.latLonPoint.latitude, poiItem.latLonPoint.longitude)
            updateMarkerAndAddress(latLng)
            // 隐藏搜索结果列表
            binding.rvSearchResult.visibility = View.GONE
        }

        binding.rvSearchResult.apply {
            layoutManager = LinearLayoutManager(this@MapActivity)
            adapter = searchAdapter
        }
    }

    private fun setupSearchListeners() {
        // 搜索按钮点击事件
        binding.btnCancelSearch.setOnClickListener {
            // 隐藏键盘
            InputUtils.inputHide(this, binding.etSearch)
            binding.etSearch.setText("")

            binding.rvSearchResult.visibility = View.GONE
            binding.btnCancelSearch.visibility = View.GONE
        }

        // 输入框回车搜索
        binding.etSearch.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: android.text.Editable?) {
                // 清空搜索结果
                performSearch()
            }
        })
    }
    
    private fun performSearch() {
        val keyword = binding.etSearch.text.toString().trim()
        if (keyword.isEmpty()) {
            return
        }

        binding.btnCancelSearch.visibility = View.VISIBLE

        // 执行POI搜索
        searchPOI(keyword)
    }
    
    private fun searchPOI(keyword: String) {
        val query = PoiSearch.Query(keyword, "", "")
        query.pageSize = 20 // 设置每页最多返回多少条POI
        query.pageNum = 0 // 设置查询第几页，从0开始
        
        // 设置搜索区域为当前地图中心点附近
        val poiSearch = PoiSearch(this, query)
        currentLatLng?.let {
            poiSearch.bound = PoiSearch.SearchBound(LatLonPoint(it.latitude, it.longitude), 5000) // 设置周边搜索的中心点和半径
        }
        
        poiSearch.setOnPoiSearchListener(this)
        poiSearch.searchPOIAsyn() // 异步搜索
    }

    private fun updateMarkerAndAddress(latLng: LatLng) {
        // 更新标记
        currentLatLng = latLng
        if (currentMarker == null) {
            currentMarker = aMap.addMarker(MarkerOptions().position(latLng).title("所选位置"))
        } else {
            currentMarker?.position = latLng
        }
        
        // 移动相机
        aMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 17f))
        
        // 逆地理编码获取地址
        val query = RegeocodeQuery(
            LatLonPoint(latLng.latitude, latLng.longitude),
            200f,
            GeocodeSearch.AMAP
        )
        geocodeSearch.getFromLocationAsyn(query)
    }
    
    // 实现PoiSearch.OnPoiSearchListener接口的方法
    @SuppressLint("NotifyDataSetChanged")
    override fun onPoiSearched(result: PoiResult?, rCode: Int) {
        if (rCode == 1000) {
            if (result != null && result.pois != null && result.pois.size > 0) {
                searchPoiItems.clear()
                searchPoiItems.addAll(result.pois)
                searchAdapter.notifyDataSetChanged()
                binding.rvSearchResult.visibility = View.VISIBLE
            } else {
                Toast.makeText(this, "未找到相关位置", Toast.LENGTH_SHORT).show()
                binding.rvSearchResult.visibility = View.GONE
            }
        } else {
            Toast.makeText(this, "搜索失败，错误码: $rCode", Toast.LENGTH_SHORT).show()
            binding.rvSearchResult.visibility = View.GONE
        }
    }
    
    override fun onPoiItemSearched(item: PoiItem?, rCode: Int) {
        // 单个POI搜索回调，本例中不需要实现
    }
    
    // 搜索结果适配器
    inner class SearchResultAdapter(
        private val poiItems: List<PoiItem>,
        private val onItemClick: (PoiItem) -> Unit
    ) : RecyclerView.Adapter<SearchResultAdapter.ViewHolder>() {
        
        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val tvTitle: TextView = view.findViewById(R.id.tv_poi_title)
            val tvAddress: TextView = view.findViewById(R.id.tv_poi_address)
        }
        
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_search_result, parent, false)
            return ViewHolder(view)
        }
        
        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val poiItem = poiItems[position]
            holder.tvTitle.text = poiItem.title
            holder.tvAddress.text = poiItem.snippet
            
            holder.itemView.setOnClickListener {
                onItemClick(poiItem)
            }
        }
        
        override fun getItemCount() = poiItems.size
    }
    
    private fun checkPermissions() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                REQUEST_CODE_LOCATION
            )
        } else {
            initLocation()
        }
    }
    
    private fun initLocation() {
        try {
            AMapLocationClient.updatePrivacyShow(this, true, true)
            AMapLocationClient.updatePrivacyAgree(this, true)
            
            locationClient = AMapLocationClient(applicationContext)
            
            val option = AMapLocationClientOption()
            option.locationMode = AMapLocationClientOption.AMapLocationMode.Hight_Accuracy
            option.isOnceLocation = true
            option.isNeedAddress = true
            locationClient.setLocationOption(option)
            
            locationClient.setLocationListener { location ->
                if (location != null && location.errorCode == 0) {
                    // 定位成功
                    val latLng = LatLng(location.latitude, location.longitude)
                    updateMarkerAndAddress(latLng)
                } else {
                    // 定位失败
                    Log.e("AmapError", "location Error, ErrCode:" +
                            location?.errorCode + ", errInfo:" +
                            location?.errorInfo)
                    Toast.makeText(this, "定位失败，请手动选择位置", Toast.LENGTH_SHORT).show()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    private fun startLocation() {
        if (::locationClient.isInitialized) {
            locationClient.startLocation()
        }
    }
    
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_LOCATION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                initLocation()
                startLocation()
            } else {
                Toast.makeText(this, "需要定位权限才能获取位置", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    override fun onRegeocodeSearched(result: RegeocodeResult?, rCode: Int) {
        if (rCode == 1000) {
            val regeocodeAddress = result?.regeocodeAddress
            if (regeocodeAddress != null) {
                currentProvince = regeocodeAddress.province ?: ""
                currentCity = regeocodeAddress.city ?: ""
                currentDistrict = regeocodeAddress.district ?: ""

                currentAddress = regeocodeAddress.formatAddress.replace(currentProvince, "")
                    .replace(currentCity, "")
                    .replace(currentDistrict, "")
                binding.tvAddress.text = regeocodeAddress.formatAddress
            }
        } else {
            Toast.makeText(this, "获取地址信息失败", Toast.LENGTH_SHORT).show()
        }
    }
    
    override fun onGeocodeSearched(geocodeResult: GeocodeResult?, rCode: Int) {
        // 不需要实现
    }
    
    override fun onResume() {
        super.onResume()
        binding.map.onResume()
    }

    override fun onPause() {
        super.onPause()
        binding.map.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        binding.map.onDestroy()
        if (::locationClient.isInitialized) {
            locationClient.stopLocation()
            locationClient.onDestroy()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        binding.map.onSaveInstanceState(outState)
    }
}