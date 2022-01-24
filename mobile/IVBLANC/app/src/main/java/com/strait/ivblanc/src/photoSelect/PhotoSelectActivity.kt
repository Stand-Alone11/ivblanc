package com.strait.ivblanc.src.photoSelect

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.strait.ivblanc.config.BaseActivity
import com.strait.ivblanc.databinding.ActivityPhotoSelectBinding

class PhotoSelectActivity : BaseActivity<ActivityPhotoSelectBinding>(ActivityPhotoSelectBinding::inflate) {
    lateinit var viewPager: ViewPager2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewPager = binding.viewpagerPhotoSelectA

        val viewPagerAdapter = ScreenSlidePagerAdapter(this)

        viewPager.adapter = viewPagerAdapter
    }

    private inner class ScreenSlidePagerAdapter(fa: FragmentActivity) : FragmentStateAdapter(fa) {
        override fun getItemCount(): Int = 2

        override fun createFragment(position: Int): Fragment = AlbumFragment()
    }
}