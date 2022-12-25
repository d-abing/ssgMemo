package com.example.ssgmemo

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.ssgmemo.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        var startX = 0f
        var startY = 0f

        val displayX = windowManager.defaultDisplay.width.toFloat()
        val displayY = windowManager.defaultDisplay.height.toFloat()
        val centerX: Float = displayX / 2
        val centerY: Float = displayY / 2

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
                    if(v.x > 500 && 463 < v.y && v.y < 1147){
                        Toast.makeText(this@MainActivity,"쓰기",Toast.LENGTH_SHORT).show()
                        v.x = centerX - 230
                        v.y = centerY - 200
                    }
                    if(v.x < 0 && 463 < v.y && v.y < 1147){
                        v.x = centerX - 230
                        v.y = centerY - 200
                        val intent = Intent(this, ClassifyActivity::class.java)
                        startActivity(intent)
                    }
                    if(v.y > 1147 && v.x > 0 && v.x < 500){
                        Toast.makeText(this@MainActivity,"보기",Toast.LENGTH_SHORT).show()
                        v.x = centerX - 230
                        v.y = centerY - 200
                    }
                    if(v.y < 463 && v.x > 0 && v.x < 500){
                        v.x = centerX - 230
                        v.y = centerY - 200
                        val intent = Intent(this, StatisticsActivity::class.java)
                        startActivity(intent)
                    }
                }
            }
            true
        }
    }
}