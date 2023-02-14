package com.example.ssgmemo.common

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
import android.view.MotionEvent
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.ssgmemo.*
import com.example.ssgmemo.databinding.ActivityMainBinding
import com.example.ssgmemo.fragment.SettingFragment
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    lateinit var mAdView : AdView

    // 설정 state
    var vibration =  MyApplication.prefs.getString("vibration", "")
    var fontSize = MyApplication.prefs.getString("fontSize", "")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 설정 fragment
        binding.btnSetting1.setOnClickListener { setFragment() }

        // memomo 이동 좌표
        var startX = 0f
        var startY = 0f

        // memomo 이동
        binding.memomo.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    startX = event.x
                    startY = event.y
                }

                MotionEvent.ACTION_MOVE -> {
                    val movedX:Float= event.x - startX
                    val movedY:Float= event.y - startY

                    v.x = v.x + movedX
                    v.y = v.y + movedY

                    Log.d("event xy","${v.x},${v.y}")
                }

                MotionEvent.ACTION_UP -> {
                    if(v.x > 480 && 640 < v.y && v.y < 1164){ // 쓰기
                        goMenu(v)
                        val intent = Intent(this, WriteActivity::class.java)
                        intent.putExtra("fontSize", "$fontSize")
                        startActivity(intent)
                    }
                    if(v.x < 120 && 640 < v.y && v.y < 1164){ // 분류
                        goMenu(v)
                        val intent = Intent(this, ClassifyActivity::class.java)
                        intent.putExtra("fontSize", "$fontSize")
                        intent.putExtra("vibration", "$vibration")
                        startActivity(intent)
                    }
                    if(v.y > 1080 && v.x > 60 && v.x < 570){ // 보기
                        goMenu(v)
                        val intent = Intent(this, ViewCtgrActivity::class.java)
                        intent.putExtra("fontSize", "$fontSize")
                        startActivity(intent)
                    }
                    if(v.y < 670 && v.x > 70 && v.x < 550){ // 검색
                        goMenu(v)
                        val intent = Intent(this, SearchActivity::class.java)
                        intent.putExtra("fontSize", "$fontSize")
                        startActivity(intent)
                    }
                }
            }
            true
        }

        // 광고
        MobileAds.initialize(this) {}
        mAdView = findViewById<AdView>(R.id.adView)
        val adRequest = AdRequest.Builder().build()
        mAdView.loadAd(adRequest)
    }

    fun goMenu(v: View) {
        vibrate()
        v.x = 317.20898f
        v.y = 884.44336f
    }

    // 진동
    fun vibrate() {
        if(vibration.equals("ON")) {
            val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            vibrator.vibrate(VibrationEffect.createOneShot(200, 50))
        }
    }

    // 설정 fragment 뒤로가기 시 닫기 구현
    interface onBackPressedListener {
        fun onBackPressed()
    }

    override fun onBackPressed(){
        val fragmentList = supportFragmentManager.fragments
        for (fragment in fragmentList) {
            if (fragment is onBackPressedListener) {
                (fragment as onBackPressedListener).onBackPressed()
                return
            }
        }
    }

    // 설정 fragment
    private fun setFragment() {
        val settingFragment = SettingFragment()
        supportFragmentManager.beginTransaction().add(R.id.frameLayout, settingFragment).commit()
    }

    // 진동 state Set, Get
    fun setVibrationState(vibrationState: String) {
        // 앱 설정에 등록
        MyApplication.prefs.setString("vibration", "$vibrationState")
        vibration = vibrationState
    }

    fun getVibrationState(): String{
        return vibration
    }

    // 폰트 사이즈 state Set, Get
    fun setFontSizeState(fontSizeState: String) {
        // 앱 설정에 등록
        MyApplication.prefs.setString("fontSize", "$fontSizeState")
        fontSize = fontSizeState
    }

    fun getFontSizeSetting(): String{
        return fontSize
    }
}