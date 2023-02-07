package com.example.ssgmemo

import android.content.Context
import android.content.Intent
import android.os.Build.VERSION_CODES.O
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
import android.view.MotionEvent
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.ssgmemo.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    var vibration = ""
    var fontSize = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator;
        var startX = 0f
        var startY = 0f

        binding.btnSetting.setOnClickListener {
            setFragment()
        }

        binding.memomo.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    startX = event.x
                    startY = event.y
                    Log.d("start xy","${startX},${startY}")
                }

                MotionEvent.ACTION_MOVE -> {
                    val movedX:Float= event.x - startX
                    val movedY:Float= event.y - startY

                    v.x = v.x + movedX
                    v.y = v.y + movedY
                    Log.d("event xy","${v.x},${v.y}")
                }
                MotionEvent.ACTION_UP -> {
                    if(v.x > 480 && 740 < v.y && v.y < 1290){
                        if(vibration.equals("ON")) {
                            vibrator.vibrate(VibrationEffect.createOneShot(200, 50));

                        }
                        //                     Toast.makeText(this@MainActivity,"쓰기",Toast.LENGTH_SHORT).show()
                        v.x = 317.20898f
                        v.y = 928.77344f
                        val intent = Intent(this, WriteActivity::class.java)
                        startActivity(intent)
                    }
                    if(v.x < 120 && 740 < v.y && v.y < 1290){
                        if(vibration.equals("ON")) {
                            vibrator.vibrate(VibrationEffect.createOneShot(200, 50));

                        }
//                      Toast.makeText(this@MainActivity,"분류",Toast.LENGTH_SHORT).show()
                        v.x = 317.20898f
                        v.y = 928.77344f
                        val intent = Intent(this, ClassifyActivity::class.java)
                        startActivity(intent)
                    }
                    if(v.y > 1220 && v.x > 60 && v.x < 560){
                        if(vibration.equals("ON")) {
                            vibrator.vibrate(VibrationEffect.createOneShot(200, 50));

                        }
//                      Toast.makeText(this@MainActivity,"보기",Toast.LENGTH_SHORT).show()
                        v.x = 317.20898f
                        v.y = 928.77344f
                        val intent = Intent(this, ViewCtgrActivity::class.java)
                        startActivity(intent)
                    }
                    if(v.y < 643 && v.x > 60 && v.x < 560){
                        if(vibration.equals("ON")) {
                            vibrator.vibrate(VibrationEffect.createOneShot(200, 50));

                        }
//                      Toast.makeText(this@MainActivity,"검색",Toast.LENGTH_SHORT).show()
                        v.x = 317.20898f
                        v.y = 928.77344f
                        val intent = Intent(this, SearchActivity::class.java)
                        startActivity(intent)
                    }
                }
            }
            true
        }
    }

    var settingFrg : Fragment? = null

    private fun setFragment() {
        val SettingFragment = SettingFragment()
        supportFragmentManager.beginTransaction().add(R.id.frameLayout, SettingFragment).commit()
    }

    fun goBack(settingFragment: SettingFragment) {
        settingFrg = settingFragment
        supportFragmentManager.beginTransaction().remove(settingFragment).commit()
    }

    fun setVibrationSetting(vibrationSetting: String) {
        vibration = vibrationSetting
    }

    fun getVibrationSetting(): String{
        return vibration
    }

    fun setFontSizeSetting(FontSizeSetting: String) {
        fontSize = FontSizeSetting
    }

    fun getFontSizeSetting(): String{
        return fontSize
    }
}