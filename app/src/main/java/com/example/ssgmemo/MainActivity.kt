package com.example.ssgmemo

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MotionEvent
import android.widget.Toast
import com.example.ssgmemo.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        var startX = 0f
        var startY = 0f

        binding.imageView.setOnTouchListener { v, event ->
            when (event.action) {

                MotionEvent.ACTION_DOWN -> {
                    startX = event.x
                    startY = event.y
                }

                MotionEvent.ACTION_MOVE -> {
                    val movedX: Float = event.x - startX
                    val movedY: Float = event.y - startY

                    v.x = v.x + movedX
                    v.y = v.y + movedY
                }
            }
            true
        }

        binding.SampleLayoutView1.setOnTouchListener(@SuppressLint("ClickableViewAccessibility")
        object: OnSwipeTouchListener(this@MainActivity) {
            override fun onSwipeLeft() {
                Toast.makeText(this@MainActivity,"왼쪽으로",Toast.LENGTH_SHORT).show()
            }
            override fun onSwipeRight() {
                Toast.makeText(this@MainActivity,"오른쪽으로",Toast.LENGTH_SHORT).show()
            }
            @SuppressLint("ClickableViewAccessibility")
            override fun onSwipeTop() {
                Toast.makeText(this@MainActivity,"위로",Toast.LENGTH_SHORT).show()
            }
            override fun onSwipeBottom() {
                Toast.makeText(this@MainActivity,"아래로",Toast.LENGTH_SHORT).show()
            }
        })
    }
}