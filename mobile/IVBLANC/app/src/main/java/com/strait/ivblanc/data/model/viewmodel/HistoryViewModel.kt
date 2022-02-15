package com.strait.ivblanc.data.model.viewmodel

import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.strait.ivblanc.data.model.dto.*
import com.strait.ivblanc.data.model.response.ClothesResponse
import com.strait.ivblanc.data.model.response.FriendListResponse
import com.strait.ivblanc.data.model.response.FriendResponse
import com.strait.ivblanc.data.model.response.HistoryResponse
import com.strait.ivblanc.data.repository.FriendRepository
import com.strait.ivblanc.data.repository.HistoryRepository
import com.strait.ivblanc.util.MultiPartUtil
import com.strait.ivblanc.util.Resource
import com.strait.ivblanc.util.Status
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MultipartBody

class HistoryViewModel: ViewModel() {
    val historyRepository = HistoryRepository()

    private val _historyResponseStatus = MutableLiveData<Resource<*>>()
    val historyResponseStatus: LiveData<Resource<*>>
        get() = _historyResponseStatus

    private val _historyListLiveData = MutableLiveData<List<History>>()
    val historyListLiveData : LiveData<List<History>>
        get() = _historyListLiveData

    private val totalHistoryList = mutableListOf<History>()

    fun getAllHistory() = viewModelScope.launch {
        //히스토리 리스트를 가져옴
        getHistory()
        Log.d("bbbb", "getAllHistory: " + totalHistoryList)
    }

    fun getAllHistoryThisMonth() = viewModelScope.launch {
        //히스토리 리스트를 가져옴
        getHistoryMonth()
        Log.d("bbbb", "getHistoryThisMonth: " + totalHistoryList)
    }

    fun getAllHistorySelectedMonth(year: Int, month: Int) = viewModelScope.launch {
        //히스토리 리스트를 가져옴
        getHistoryMonthYear(year, month)
        Log.d("bbbb", "getHistoryMonthYear: " + totalHistoryList)
    }

    suspend fun getHistory() = withContext(Dispatchers.IO) {
        val result: Resource<HistoryResponse> = historyRepository.getAllHistory()
        _historyResponseStatus.postValue(result)
        if (result.status == Status.SUCCESS) {
            result.data!!.dataSet!!.forEach {
                if (!totalHistoryList.contains(it)) {
                    totalHistoryList.add(it)
                }
            }
        }
    }

    suspend fun getHistoryMonth() = withContext(Dispatchers.IO){
        val result: Resource<HistoryResponse> = historyRepository.getHistoryThisMonth()
        _historyResponseStatus.postValue(result)
        if (result.status == Status.SUCCESS) {
            result.data!!.dataSet!!.forEach {
                if (!totalHistoryList.contains(it)) {
                    totalHistoryList.add(it)
                }
            }
        }
    }

    suspend fun getHistoryMonthYear(year:Int, month:Int) = withContext(Dispatchers.IO){
        val result: Resource<HistoryResponse> = historyRepository.getHistoryByMonth(year, month)
        _historyResponseStatus.postValue(result)
        if (result.status == Status.SUCCESS) {
            result.data!!.dataSet!!.forEach {
                if (!totalHistoryList.contains(it)) {
                    totalHistoryList.add(it)
                }
            }
        }
        _historyListLiveData.postValue(totalHistoryList)
    }

    suspend fun addHistoryPhotos(historyId: Int, absolutePathList: List<String>) = viewModelScope.launch {
        setLoading()
        withContext(Dispatchers.IO) {
            val result = historyRepository.addHistoryPhotos(
                MultiPartUtil.makeMultiPartBody("historyId", historyId.toString())
                , MultiPartUtil.makeMultiPartBodyFileArray("photoList", absolutePathList, "image/*")
            )
            _historyResponseStatus.postValue(result)
        }
    }

    private fun setLoading() = _historyResponseStatus.postValue(Resource.loading(null))

}