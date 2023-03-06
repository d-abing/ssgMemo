package com.example.ssgmemo.common

import android.app.Activity
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
    var backFlag = false

    // 설정 state
    var vibration =  MyApplication.prefs.getString("vibration", "")
    var fontSize = MyApplication.prefs.getString("fontSize", "")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 설정 fragment
        binding.btnSetting1.setOnClickListener {
            setFragment()
            backFlag = true
        }

        // memomo 이동 좌표
        var startX = 0f
        var startY = 0f

        // 디스플레이 크기
        val display = this.applicationContext?.resources?.displayMetrics
        val deviceHeight = display?.heightPixels
        val deviceWidth = display?.widthPixels

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
                    goMenu<WriteActivity>(v, v.x, v.y, deviceWidth!!, 480, 1164, 640, WriteActivity::class.java)
                    goMenu<ClassifyActivity>(v, v.x, v.y, 120, -420, 1164, 640, ClassifyActivity::class.java)
                    goMenu<ViewCtgrActivity>(v, v.x, v.y, 570, 60, deviceHeight!!, 1080, ViewCtgrActivity::class.java)
                    goMenu<SearchActivity>(v, v.x, v.y, 550, 70, 670, 0, SearchActivity::class.java)
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

    // 각 Activity로 이동
    fun <T>goMenu(v: View, x: Float, y: Float,
                  range1: Int, range2: Int, range3: Int, range4: Int,
                  targetActivity: Class<T>) {

        if ( x < range1 && x > range2 && y < range3 && y > range4 ) {
            goStartState(v)
            val intent = Intent(this, targetActivity)
            intent.putExtra("fontSize", "$fontSize")
            intent.putExtra("vibration", "$vibration")
            startActivity(intent)
        }
    }

    // memomo 시작 위치로 이동
    fun goStartState(v: View) {
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
        if (backFlag) {
            val fragmentList = supportFragmentManager.fragments
            for (fragment in fragmentList) {
                if (fragment is onBackPressedListener) {
                    (fragment as onBackPressedListener).onBackPressed()
                    return
                }
            }
        } else {
            super.onBackPressed()
        }
    }

    // 설정 fragment
    private fun setFragment() {
        val settingFragment = SettingFragment()
        supportFragmentManager.beginTransaction().add(R.id.frameLayout, settingFragment).commit()
    }

    // 진동 state Setter, Getter
    fun setVibrationState(vibrationState: String) {
        // 앱 설정에 등록
        MyApplication.prefs.setString("vibration", "$vibrationState")
        vibration = vibrationState
    }

    fun getVibrationState(): String{
        return vibration
    }

    // 폰트 사이즈 state Setter, Getter
    fun setFontSizeState(fontSizeState: String) {
        // 앱 설정에 등록
        MyApplication.prefs.setString("fontSize", "$fontSizeState")
        fontSize = fontSizeState
    }

    fun getFontSizeSetting(): String{
        return fontSize
    }
}