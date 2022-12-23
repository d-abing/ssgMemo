package com.example.ssgmemo

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
                    if(v.x > 500){
                        Toast.makeText(this@MainActivity,"오른쪽으로",Toast.LENGTH_SHORT).show()
                    }
                    if(v.x < 0){
                        Toast.makeText(this@MainActivity,"왼쪽으로",Toast.LENGTH_SHORT).show()
                    }
                    if(v.y > 1147){
                        Toast.makeText(this@MainActivity,"아래로",Toast.LENGTH_SHORT).show()
                    }
                    if(v.y < 463){
                        Toast.makeText(this@MainActivity,"위로",Toast.LENGTH_SHORT).show()
                    }
                }
            }
            true
        }
    }
}