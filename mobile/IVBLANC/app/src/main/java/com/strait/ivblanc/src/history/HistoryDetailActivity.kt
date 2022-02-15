package com.strait.ivblanc.src.history

import android.content.Intent
import android.location.Geocoder
import android.os.Bundle
import com.bumptech.glide.Glide
import com.strait.ivblanc.config.BaseActivity
import com.strait.ivblanc.data.model.dto.History
import com.strait.ivblanc.databinding.ActivityHistoryDetailBinding

import android.location.Address
import android.util.Log
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.strait.ivblanc.R
import com.strait.ivblanc.adapter.HistoryDetailRecyclerViewAdapter

import java.io.IOException


class HistoryDetailActivity : BaseActivity<ActivityHistoryDetailBinding>(
    ActivityHistoryDetailBinding::inflate) {
    lateinit var history: History
    lateinit var historyDetailRecyclerViewAdapter: HistoryDetailRecyclerViewAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        intent.getParcelableExtra<History>("history")?.let {
            history = it
        } ?: finish()
        Log.d("HISTORY_DETAIL", "onCreate called, historyId = " + history.historyId)
        setHistoryInfo()
        setClickListeners()
        setRecyclerView()
    }

    private fun setClickListeners() {
        binding.imageViewHistoryDetailClose.setOnClickListener {
            finish()
        }
        binding.imageViewHistoryDetailEdit.setOnClickListener {
            val intent = Intent(this, HistoryEditActivity::class.java)
                .putExtra("history", history)
                .putExtra("location", getLocation())
            startActivity(intent)
        }
    }

    private fun setHistoryInfo() {
        if(history.styleUrl != null) {
            Glide.with(this).load(history.styleUrl).into(binding.imageViewHistoryDetailStyle)
        }

        binding.textViewHistoryDetailDate.text = history.date
        binding.textViewHistoryDetailSubject.text = history.subject
        binding.textViewHistoryDetailLocation.text = getLocation()
        binding.textViewHistoryDetailTemperature.text = history.temperature_high.toString() + "°C/" + history.temperature_low.toString() + "°C"
        binding.textViewHistoryDetailText.text = history.text

        if(history.photos.size == 0)
            binding.recyclerViewHistoryDetailPhoto.visibility = View.GONE



        if(history.weather == "맑음"){
            binding.imageViewHistoryDetailWeather.setImageResource(R.drawable.icon_weather_sunny_48);
        } else if(history.weather == "흐림"){
            binding.imageViewHistoryDetailWeather.setImageResource(R.drawable.icon_weather_cloudy_64);
        } else if(history.weather == "눈"){
            binding.imageViewHistoryDetailWeather.setImageResource(R.drawable.icon_weather_snowy_48);
        } else if(history.weather == "비"){
            binding.imageViewHistoryDetailWeather.setImageResource(R.drawable.icon_weather_rainy_48);
        }
    }

    private fun getLocation() : String {

        val geocoder = Geocoder(this)

        var list: List<Address>? = null
        try {
            val latitude = history.location
            val longitude = history.field
            list = geocoder.getFromLocation(
                latitude,  // 위도
                longitude,  // 경도
                10
            ) // 얻어올 값의 개수
        } catch (e: IOException) {
            e.printStackTrace()
            Log.e("test", "입출력 오류 - 서버에서 주소변환시 에러발생")
        }

//        Log.d("GEO", "latitude = " + history.location + ", longitude = " + history.field + ", list_size = " + list!!.size)

        if (list != null && !list.isEmpty()) {
            var cut = list.get(0).toString().split("\"")
            return cut[1]
        }
        return "알 수 없는 장소"
    }

    private fun setRecyclerView(){
        historyDetailRecyclerViewAdapter = HistoryDetailRecyclerViewAdapter()
        historyDetailRecyclerViewAdapter.apply {
            data = history.photos
        }
        binding.recyclerViewHistoryDetailPhoto.apply {
            adapter = historyDetailRecyclerViewAdapter
            layoutManager = LinearLayoutManager(this@HistoryDetailActivity, RecyclerView.HORIZONTAL, false)
        }
    }
}