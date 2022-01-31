package com.strait.ivblanc.data.model.viewmodel

import android.app.Application
import android.content.res.Resources
import androidx.core.content.ContextCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.strait.ivblanc.R
import com.strait.ivblanc.adapter.ExpandableRecyclerViewAdapter
import com.strait.ivblanc.data.model.dto.Clothes
import com.strait.ivblanc.data.model.dto.PhotoItem
import com.strait.ivblanc.data.model.response.ClothesResponse
import com.strait.ivblanc.data.repository.ClothesRepository
import com.strait.ivblanc.util.CategoryCode
import com.strait.ivblanc.util.Resource
import com.strait.ivblanc.util.Status
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class MainViewModel: ViewModel() {
    private val clothesRepository = ClothesRepository()
    var currentCategory = CategoryCode.TOTAL
    private val totalClothesList = mutableListOf<Clothes>()

    private val _clothesResponseStatus = MutableLiveData<Resource<ClothesResponse>>()
    val clothesResponseStatus: LiveData<Resource<ClothesResponse>>
        get() = _clothesResponseStatus

    // ExpandableRecyclerView에서 observe하는 리스트
    private val _clothesListLiveData = MutableLiveData<List<PhotoItem<Clothes>>>()
    val clothesListLiveData : LiveData<List<PhotoItem<Clothes>>>
        get() = _clothesListLiveData

    // TODO: 2022/01/31 page 삭제
    suspend fun getAllClothes(page: Int) = withContext(Dispatchers.IO) {
        setLoading()
        val result: Resource<ClothesResponse> = clothesRepository.getAllClothes(page)
        _clothesResponseStatus.postValue(result)
        if(result.status == Status.SUCCESS) {
            totalClothesList.addAll(result.data!!.dataSet!!)
        }
    }

    fun getAllClothesWithCategory(category: Int) = viewModelScope.launch {
        getAllClothes(0)
        updateClothesByCategory(category)
    }

    /**
     * @param result
     * clothesRepository에서 받은 response
     */
    // TODO: 2022/01/31 paging이 사라져서 필요 없어짐
    private fun updateResult(result: Resource<ClothesResponse>) {
        //
        if(result.status == Status.SUCCESS) {
            result.data?.dataSet?.let {
                // 서버에서 받은 옷 목록이 비어있지 않을 때, _clothesListLiveData 내부의 list에 옷 목록 추가하여 post
                if(it.isNotEmpty()) {
                    val mutableList = getPhotoItemListFromLiveData()
                    for(clothes in it) {
                        mutableList.add(PhotoItem(ExpandableRecyclerViewAdapter.CHILD, content = clothes))
                    }
                    _clothesListLiveData.postValue(mutableList)
                }
            }
        }
    }

    // 대분류 카테고리로 전체 옷 필터링
    private fun getClothesListWithLargeCategory(category: Int): MutableList<Clothes> {
        val largeCategory = category.toString()[0].digitToInt()
        return totalClothesList.filter { clothes -> clothes.category.toString()[0].digitToInt() == largeCategory }.toMutableList()
    }

    // 소분류 카테고리로 옷 필터링
    private fun getClothesListWithSmallCategory(category: Int): MutableList<Clothes> {
        val filteredList = getClothesListWithLargeCategory(category)
        return filteredList.filter { clothes -> clothes.category == category }.toMutableList()
    }

    // 라이브 데이터가 비어있으면 빈 리스트 반환
    private fun getPhotoItemListFromLiveData():MutableList<PhotoItem<Clothes>> {
        _clothesListLiveData.value?.let { it ->
            return it.toMutableList()
        }
        return mutableListOf()
    }

    /**
     * 최근에 추가한 옷이 있으면
     * 최근에 추가한 옷 헤더 추가
     * 최근에 추가한 옷 child 추가
     *
     * 즐겨찾기한 옷이 있으면
     * 즐겨찾기한 옷 헤더 추가
     * 즐겨찾기한 옷 child 추가
     *
     * 최근에 추가한 옷, 즐겨찾기한 옷 둘중에 하나라도 있으면
     * -> 카테고리 별 옷 헤더 추가
     *
     * 카테고리 별 옷 child 추가
     */
    // TODO: 2022/01/31 ExpandableRecyclerViewAdapter 뷰타입 Divider 추가
    private fun makePhotoItemList(filteredList: MutableList<Clothes>): List<PhotoItem<Clothes>> {
        val photoItemList = mutableListOf<PhotoItem<Clothes>>()
        val recentlyCreatedClothesList = getCreatedRecentlyClothesList(filteredList)
        val favoriteClothesList = getFavoriteClothesList(filteredList)
        // 주석의 첫번째 조건
        if(recentlyCreatedClothesList.size != 0) {
            photoItemList.add(PhotoItem<Clothes>(ExpandableRecyclerViewAdapter.HEADER, "최근에 추가한 옷").apply{
                initInvisibleItems()
            })
            photoItemList.addAll(getPhotoItemList(recentlyCreatedClothesList))
        }
        // 주석의 두번째 조건
        if(favoriteClothesList.size != 0) {
            photoItemList.add(PhotoItem<Clothes>(ExpandableRecyclerViewAdapter.HEADER, "즐겨찾기한 옷").apply {
                initInvisibleItems()
            })
            photoItemList.addAll(getPhotoItemList(favoriteClothesList))
        }
        // 주석의 마지막 조건
        if(favoriteClothesList.size != 0 || recentlyCreatedClothesList.size != 0) {
            photoItemList.add(PhotoItem(ExpandableRecyclerViewAdapter.HEADER))
        }
        photoItemList.addAll(getPhotoItemList(filteredList))
        return photoItemList
    }

    // List<Clothes>를 List<PhotoItem<Clothes>>로 변환
    private fun <T> getPhotoItemList(list: MutableList<T>):List<PhotoItem<T>> {
        val result = mutableListOf<PhotoItem<T>>()
        list.forEach { clothes -> result.add(PhotoItem(ExpandableRecyclerViewAdapter.CHILD, content = clothes)) }
        return result
    }

    // 즐겨찾기 한 옷
    private fun getFavoriteClothesList(list: MutableList<Clothes>): MutableList<Clothes> {
        return list.filter { clothes -> clothes.favorite == 1 }.toMutableList()
    }

    // 최근 일주일 간 생성된 옷
    private fun getCreatedRecentlyClothesList(list: MutableList<Clothes>): MutableList<Clothes> {
        val today = System.currentTimeMillis()
        val oneWeekMillis = 1000 * 7 * 24 * 60 * 60
        return list.filter { clothes -> dateStringToMillis(clothes.createDate) > (today - oneWeekMillis)}.toMutableList()
    }

    // string to millis
    private fun dateStringToMillis(dateString: String): Long {
        val stringFormat = SimpleDateFormat("yyy-MM-dd'T'HH:mm:ss", Locale.KOREA)
        val date = stringFormat.parse(dateString)
        return date.time
    }


    /**
     * 대분류로 분류 후, 소분류로 분류
     * (대분류 전체인 경우 code는 10보다 작음)
     * List<PhotoItem<Clothes>> 생성 후
     * _clothesListLiveData 업데이트
     */
    // View에서 호출하는 카테고리 별 조회
    fun updateClothesByCategory(category: Int) {
        if(category == CategoryCode.TOTAL) {
            _clothesListLiveData.postValue(makePhotoItemList(totalClothesList))
        } else if(category < 10) {
            _clothesListLiveData.postValue(makePhotoItemList(getClothesListWithLargeCategory(category)))
        } else {
            _clothesListLiveData.postValue(makePhotoItemList(getClothesListWithSmallCategory(category)))
        }
    }

    private fun setLoading() = _clothesResponseStatus.postValue(Resource.loading(null))
}