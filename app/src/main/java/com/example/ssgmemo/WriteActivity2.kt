package com.example.ssgmemo

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
import android.view.MotionEvent
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import com.example.ssgmemo.databinding.ActivityMainBinding
import com.example.ssgmemo.databinding.ActivityWrite2Binding

class WriteActivity2 : AppCompatActivity() {
    private lateinit var binding: ActivityWrite2Binding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_write2)
        binding = ActivityWrite2Binding.inflate(layoutInflater)
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
                    if(v.y > 1070){
                        vibrator.vibrate(VibrationEffect.createOneShot(200, 50));
//                      Toast.makeText(this@MainActivity,"생각모으기",Toast.LENGTH_SHORT).show()
                        val intent = Intent(this, MainActivity::class.java)
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);    //인텐트 플래그 설정
                        startActivity(intent)
                    }
                    if(v.y < 472){
                        vibrator.vibrate(VibrationEffect.createOneShot(200, 50));
//                      Toast.makeText(this@MainActivity,"생각버리기",Toast.LENGTH_SHORT).show()
                        val intent = Intent(this, MainActivity::class.java)
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);    //인텐트 플래그 설정
                        startActivity(intent)
                    }
                }
            }
            true
        }
    }
}