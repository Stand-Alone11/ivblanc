package com.strait.ivblanc.src.styleMaking

import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.viewModels
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.res.ResourcesCompat
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.strait.ivblanc.R
import com.strait.ivblanc.adapter.BottomSheetClothesRVAdapter
import com.strait.ivblanc.adapter.StyleEditorAdapter
import com.strait.ivblanc.adapter.StyleRecyclerViewAdapter
import com.strait.ivblanc.component.ItemTouchHelperCallback
import com.strait.ivblanc.config.BaseActivity
import com.strait.ivblanc.data.model.dto.Clothes
import com.strait.ivblanc.data.model.dto.Style
import com.strait.ivblanc.data.model.viewmodel.ClothesViewModel
import com.strait.ivblanc.data.model.viewmodel.MainViewModel
import com.strait.ivblanc.data.model.viewmodel.StyleViewModel
import com.strait.ivblanc.databinding.ActivityStyleMakingBinding
import com.strait.ivblanc.util.CaptureUtil
import com.strait.ivblanc.util.CategoryCode
import com.strait.ivblanc.util.Status

private const val TAG = "StyleMakingAct_debuk"
class StyleMakingActivity : BaseActivity<ActivityStyleMakingBinding>(ActivityStyleMakingBinding::inflate) {
    lateinit var style: Style
    lateinit var styleEditorAdapter: StyleEditorAdapter
    lateinit var recyclerViewAdapter: StyleRecyclerViewAdapter
    lateinit var bottomSheetRVAdapter: BottomSheetClothesRVAdapter
    lateinit var itemTouchHelper: ItemTouchHelper
    lateinit var bottomSheet: BottomSheetBehavior<ConstraintLayout>
    private val clothesViewModel: ClothesViewModel by viewModels()
    private val styleViewModel: StyleViewModel by viewModels()
    var focusImage:ImageView?=null
    lateinit var largeCategories: List<String>
    var smallCategories = listOf<Pair<Int, Int>>()
    private val itemClickListener = object :StyleEditorAdapter.ItemClickListener{

        override fun onClick(imageView: ImageView) {
            styleViewModel.changefocus(imageView)
        }

    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        init()
    }

    private fun init() {
        // clothesViewModel에서 모든 옷 요청
        clothesViewModel.getAllClothesWithCategory(CategoryCode.TOTAL)
        intent.getParcelableExtra<Style>("style")?.let {
            style = it
        }
        bottomSheet = BottomSheetBehavior.from(binding.constraintLayoutStyleMakingBottomSheet)
        setToolbar()
        setBottomSheetDropDown()
        setStyleEditorAdapter()
        setRecyclerView()
        setBottomSheetRecyclerView()
        setObserver()
    }

    private fun setObserver() {
        styleViewModel.styleResponseStatus.observe(this) {
            when(it.status) {
                Status.LOADING -> {

                }
                Status.ERROR -> {

                }
                Status.SUCCESS -> {

                }
            }
        }
        styleViewModel.focusedImage.observe(this){
            if(focusImage!=null && focusImage!=it){
                focusImage!!.background=null
            }
            focusImage=it
            focusImage!!.setBackgroundResource(R.drawable.kakao)
        }
    }

    private fun setToolbar() {
        val toolbar = binding.toolbarStyleMaking.toolbar
        toolbar.findViewById<TextView>(R.id.textView_toolbar).text = "스타일 만들기"
        val leadingIcon = toolbar.findViewById<ImageView>(R.id.imageView_toolbar_leadingIcon).apply {
            setImageDrawable(ResourcesCompat.getDrawable(resources, R.drawable.ic_back, null))
            setOnClickListener { finish() }
        }
        val trailingIcon = toolbar.findViewById<ImageView>(R.id.imageView_toolbar_trailingIcon).apply {
            setImageDrawable(ResourcesCompat.getDrawable(resources, R.drawable.ic_checked, null))
            setOnClickListener { requestAddStyle() }
        }
    }

    // Dropbox 관련 시작 -------------------------------
    private fun setBottomSheetDropDown() {
        largeCategories = getLargeCategoryString()
        // 초기화 시 대분류 전체, 소분류 전체로 세팅
        binding.autoCompleteTextViewClothesSelectBottomSheetFLargeCategory.setText(resources.getText(R.string.total))
        binding.autoCompleteTextViewClothesSelectBottomSheetFSmallCategory.setText(resources.getText(R.string.total))

        val largeCategoryAdapter = ArrayAdapter(this, R.layout.list_category_item, largeCategories)
        binding.autoCompleteTextViewClothesSelectBottomSheetFLargeCategory.setAdapter(largeCategoryAdapter)

        // 대분류가 바뀜에 따라 소분류 바꿈
        clothesViewModel.largeCategory.observe(this) {
            smallCategories = clothesViewModel.getSmallCategoriesByLargeCategory(it)
            val smallCategoryAdapter = ArrayAdapter(this, R.layout.list_category_item, getSmallCategoryStringByLargeCategory(it))
            binding.autoCompleteTextViewClothesSelectBottomSheetFSmallCategory.setAdapter(smallCategoryAdapter)
        }

        // 대분류 edittext observe -> viewModel에 대분류 카테고리 변경, 대분류 카테고리로 clothesUpdate
        // 대분류가 전체라면 소분류도 전체로 변경.
        binding.autoCompleteTextViewClothesSelectBottomSheetFLargeCategory.addTextChangedListener {
            val largeCategory = clothesViewModel.largeCategorySet.find { pair ->
                it.toString() == resources.getString(pair.second) && pair.first in 0..9
            }

            largeCategory?.let {
                binding.autoCompleteTextViewClothesSelectBottomSheetFSmallCategory.setText(resources.getText(R.string.total))
                clothesViewModel.setLargeCategory(it.first)
                clothesViewModel.updateClothesByCategory(it.first)
            }
        }

        // 소분류 edittext observe -> viewModel 소분류에 맞는 옷 필터링
        binding.autoCompleteTextViewClothesSelectBottomSheetFSmallCategory.addTextChangedListener {
            if(it.toString().isBlank() || it.toString().isEmpty()) {
                clothesViewModel.setSmallCategory(CategoryCode.UNSELECTED)
                return@addTextChangedListener
            }

            val smallCategory = smallCategories.find { pair ->
                val string = resources.getString(pair.second)
                it.toString() == string
            }

            smallCategory?.let { pair ->
                if(pair.first != CategoryCode.TOTAL_SMALL) {
                    clothesViewModel.updateClothesByCategory(pair.first)
                } else {
                    // TOTAL_SMALL이라면 대분류로 옷 분류
                    clothesViewModel.updateClothesByCategory(clothesViewModel.largeCategory.value!!)
                }
            }
        }
    }

    private fun getLargeCategoryString(): List<String> {
        val result = mutableListOf<String>()
        clothesViewModel.largeCategorySet.forEach {
            result.add(resources.getString(it.second))
        }
        return result.toList()
    }

    private fun getSmallCategoryStringByLargeCategory(largeCategory: Int): List<String> {
        val result = mutableListOf<String>()
        result.add(resources.getString(R.string.total))
        clothesViewModel.getSmallCategoriesByLargeCategory(largeCategory).forEach {
            result.add(resources.getString(it.second))
        }
        return result.toList()
    }
    // Dropbox 관련 끝 ------------------------------------------------------

    private fun setRecyclerView() {
        recyclerViewAdapter = StyleRecyclerViewAdapter(editorAdapter = styleEditorAdapter)
        itemTouchHelper = ItemTouchHelper(ItemTouchHelperCallback(recyclerViewAdapter))
        binding.recyclerViewStyleMakingClothes.apply {
            adapter = recyclerViewAdapter
            layoutManager = LinearLayoutManager(this@StyleMakingActivity, RecyclerView.VERTICAL, false)
        }
        itemTouchHelper.attachToRecyclerView(binding.recyclerViewStyleMakingClothes)
    }

    private fun setBottomSheetRecyclerView() {
        bottomSheetRVAdapter = BottomSheetClothesRVAdapter().apply { 
            itemClickListener = object: BottomSheetClothesRVAdapter.ItemClickListener {
                override fun onClick(clothes: Clothes) {
                    setClothesToEditor(clothes)
                }
            }
        }
        binding.recyclerViewStyleMakingBottomSheet.apply {
            adapter = bottomSheetRVAdapter
            layoutManager = GridLayoutManager(this@StyleMakingActivity, 3, RecyclerView.VERTICAL, false)
        }
        clothesViewModel.clothesList.observe(this) {
            bottomSheetRVAdapter.setData(it)
        }
    }

    private fun setStyleEditorAdapter() {
        styleEditorAdapter = StyleEditorAdapter(binding.constraintLayoutStyleMakingEdit).apply {
            itemClickListener = this@StyleMakingActivity.itemClickListener
        }
        styleEditorAdapter.addImageView(binding.imageViewStyleMakingTop)
        styleEditorAdapter.addImageView(binding.imageViewStyleMakingBottom)
        styleEditorAdapter.addImageView(binding.imageViewStyleMakingOuter)
        styleEditorAdapter.addImageView(binding.imageViewStyleMakingShoes)
        styleEditorAdapter.addImageView(binding.imageViewStyleMakingBag)
        styleEditorAdapter.addImageView(binding.imageViewStyleMakingHat)
        styleEditorAdapter.addImageView(binding.imageViewStyleMakingEtc)
        if(this::style.isInitialized) {
            style.styleDetails.forEach {
                styleEditorAdapter.addOrUpdateClothes(it.clothes)
            }
        }
    }
    
    private fun setClothesToEditor(clothes: Clothes) {
        if(this::styleEditorAdapter.isInitialized) {
            Log.d(TAG, "setClothesToEditor: ${clothes.clothesId}")
            styleEditorAdapter.addOrUpdateClothes(clothes)    
        }
        if(this::recyclerViewAdapter.isInitialized) {
            recyclerViewAdapter.addOrUpdateClothes(clothes)
        }
    }

    private var imageUri: Uri? = null
    // 스타일 생성 요청 시, Editor view 만큼 이미지 캡처 저장 후 등록 요청
    private fun requestAddStyle() {
        if(this::recyclerViewAdapter.isInitialized && recyclerViewAdapter.data.size > 0) {

            // 이미지 저장이 성공적으로 이뤄지면 imageUri 값 할당
            imageUri = CaptureUtil.saveCapture(binding.constraintLayoutStyleMakingEdit)?.let { uri ->

                // 이미지 저장이 이뤄진 후, contentResolver로 이미지 파일의 실제 경로를 이용하여 스타일 등록 요청
                CaptureUtil.getAbsolutePathFromImageUri(this, uri)?.let {
                    styleViewModel.addStyle(recyclerViewAdapter.data, it)
                }
                uri
            }
        }
    }
}