package com.example.ssgmemo

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
import android.view.MotionEvent
import androidx.appcompat.app.AppCompatActivity
import com.example.ssgmemo.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator;

        var startX = 0f
        var startY = 0f

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
                        vibrator.vibrate(VibrationEffect.createOneShot(200, 50));
 //                     Toast.makeText(this@MainActivity,"쓰기",Toast.LENGTH_SHORT).show()
                        v.x = 317.20898f
                        v.y = 928.77344f
                        val intent = Intent(this, WriteActivity::class.java)
                        startActivity(intent)
                    }
                    if(v.x < 120 && 740 < v.y && v.y < 1290){
                        vibrator.vibrate(VibrationEffect.createOneShot(200, 50));
//                      Toast.makeText(this@MainActivity,"분류",Toast.LENGTH_SHORT).show()
                        v.x = 317.20898f
                        v.y = 928.77344f
                        val intent = Intent(this, ClassifyActivity::class.java)
                        startActivity(intent)
                    }
                    if(v.y > 1293 && v.x > 60 && v.x < 560){
                        vibrator.vibrate(VibrationEffect.createOneShot(200, 50));
//                      Toast.makeText(this@MainActivity,"보기",Toast.LENGTH_SHORT).show()
                        v.x = 317.20898f
                        v.y = 928.77344f
                        val intent = Intent(this, ViewCtgrActivity::class.java)
                        startActivity(intent)
                    }
                    if(v.y > 250 && v.y < 700 && v.x > 60 && v.x < 560){
                        vibrator.vibrate(VibrationEffect.createOneShot(200, 50));
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
}