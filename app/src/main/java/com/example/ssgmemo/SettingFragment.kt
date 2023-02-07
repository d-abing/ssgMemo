package com.example.ssgmemo

import android.content.Context
import android.media.AudioManager
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.ssgmemo.databinding.FragmentSettingBinding

class SettingFragment() : Fragment() {

    var mainActivity: MainActivity? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is MainActivity)	mainActivity = context
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = FragmentSettingBinding.inflate(inflater, container, false)
        binding.btnSetting2.setOnClickListener { mainActivity?.goBack(this) }

        if(mainActivity?.getVibrationSetting().equals("ON")) {
            binding.switchVibrate.isChecked = true
        }
        if(mainActivity?.getFontSizeSetting().equals("ON")) {
            binding.switchFontSize.isChecked = true
        }

        binding.switchVibrate.setOnCheckedChangeListener { compoundButton, ischecked ->
            if (ischecked) {
                mainActivity?.setVibrationSetting("ON")
            } else {
                mainActivity?.setVibrationSetting("OFF")
            }
        }
        binding.switchFontSize.setOnCheckedChangeListener { compoundButton, ischecked ->
            if(ischecked) {
                mainActivity?.setFontSizeSetting("ON")
            } else {
                mainActivity?.setFontSizeSetting("OFF")
            }
        }

        return binding.root
    }
}