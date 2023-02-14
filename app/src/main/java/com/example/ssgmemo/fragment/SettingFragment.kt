package com.example.ssgmemo.fragment

import android.content.Context
import android.media.Image
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.viewpager2.widget.ViewPager2
import com.example.ssgmemo.Memo
import com.example.ssgmemo.R
import com.example.ssgmemo.adapter.ViewPagerAdapter
import com.example.ssgmemo.adapter.ViewPagerAdapter2
import com.example.ssgmemo.common.MainActivity
import com.example.ssgmemo.databinding.FragmentSettingBinding

class SettingFragment() : Fragment(),  MainActivity.onBackPressedListener {
    lateinit var mainActivity: MainActivity
    var pagerAdapter: ViewPagerAdapter2? = null
    var descriptionImageList: MutableList<Int>? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is MainActivity)	mainActivity = context
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentSettingBinding.inflate(inflater, container, false)
        binding.btnSetting2.setOnClickListener { onBackPressed() }

        // 앱 설정 확인 후 switch에 적용
        if(mainActivity.getVibrationState().equals("ON")) {
            binding.switchVibrate.isChecked = true
        }
        if(mainActivity.getFontSizeSetting().equals("ON")) {
            binding.switchFontSize.isChecked = true
        }

        // switch ChangeListener
        binding.switchVibrate.setOnCheckedChangeListener { compoundButton, ischecked ->
            if (ischecked) {
                mainActivity.setVibrationState("ON")
            } else {
                mainActivity.setVibrationState("OFF")
            }
        }
        binding.switchFontSize.setOnCheckedChangeListener { compoundButton, ischecked ->
            if(ischecked) {
                mainActivity.setFontSizeState("ON")
            } else {
                mainActivity.setFontSizeState("OFF")
            }
        }

        pagerAdapter = ViewPagerAdapter2()
        descriptionImageList = mutableListOf()
        descriptionImageList!!.add(R.drawable.opened_box)
        pagerAdapter!!.listData.addAll(descriptionImageList!!)      // pagerAdapter에 추가
        binding.viewPager.adapter = pagerAdapter        // viewpager에 pagerAdapter 등록

        return binding.root
    }

    // 뒤로 가기
    override fun onBackPressed() {
        requireActivity().supportFragmentManager.beginTransaction().remove(this).commit()
        //requireActivity().supportFragmentManager.popBackStack()
    }
}