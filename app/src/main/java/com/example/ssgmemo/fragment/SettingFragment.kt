package com.example.ssgmemo.fragment

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.ssgmemo.common.MainActivity
import com.example.ssgmemo.databinding.FragmentSettingBinding

class SettingFragment() : Fragment(),  MainActivity.onBackPressedListener {
    lateinit var mainActivity: MainActivity

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

        return binding.root
    }

    // 뒤로 가기
    override fun onBackPressed() {
        requireActivity().supportFragmentManager.beginTransaction().remove(this).commit()
        //requireActivity().supportFragmentManager.popBackStack()
    }
}