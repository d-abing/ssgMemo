package com.example.ssgmemo

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.ssgmemo.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
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