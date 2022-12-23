package com.example.ssgmemo

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipDescription
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.DragEvent
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import com.example.ssgmemo.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.top.setOnDragListener(dragListener)
        binding.bottom.setOnDragListener(dragListener)
        binding.left.setOnDragListener(dragListener)
        binding.right.setOnDragListener(dragListener)
        binding.imageView.setOnLongClickListener {
            val clipText = "this is our ClipData text"
            val item = ClipData.Item(clipText)
            val mimeTypes = arrayOf(ClipDescription.MIMETYPE_TEXT_PLAIN)
            val data = ClipData(clipText, mimeTypes, item)

            val dragShadowBuilder = View.DragShadowBuilder(it)
            it.startDragAndDrop(data, dragShadowBuilder, it, 0)

            it.visibility = View.INVISIBLE
            true
        }
        var startX = 0f
        var startY = 0f


//        binding.imageView.setOnTouchListener(
//        object: OnSwipeTouchListener(this@MainActivity) {
//            override fun onSwipeLeft() {
//                Toast.makeText(this@MainActivity,"왼쪽으로",Toast.LENGTH_SHORT).show()
//            }
//            override fun onSwipeRight() {
//                Toast.makeText(this@MainActivity,"오른쪽으로",Toast.LENGTH_SHORT).show()
//            }
//            override fun onSwipeTop() {
//                Toast.makeText(this@MainActivity,"위로",Toast.LENGTH_SHORT).show()
//            }
//            override fun onSwipeBottom() {
//                Toast.makeText(this@MainActivity,"아래로",Toast.LENGTH_SHORT).show()
//            }
//        })


//        binding.imageView.setOnTouchListener { v, event ->
//            when (event.action) {
//                MotionEvent.ACTION_DOWN -> {
//                    startX = event.x
//                    startY = event.y
//                    Log.d("start xy","${startX},${startY}")
//                }
//
//                MotionEvent.ACTION_MOVE -> {
//                    val movedX:Float= event.x - startX
//                    val movedY:Float= event.y - startY
//
//                    v.x = v.x + movedX
//                    v.y = v.y + movedY
//                    Log.d("event xy","${v.x},${v.y}")
//                }
//                MotionEvent.ACTION_UP -> {
//                    if(v.x > 500){
//                        Toast.makeText(this@MainActivity,"오른쪽으로",Toast.LENGTH_SHORT).show()
//                    }
//                    if(v.x<0){
//                        Toast.makeText(this@MainActivity,"왼쪽으로",Toast.LENGTH_SHORT).show()
//                    }
////                    v.x =  v.x - (event.x - startX)
////                    v.y =  v.y - (event.y - startY)
////                    Log.d("up xy","${event.x},${startX}")
//
//                }
//            }
//            true
//        }
    }
    val dragListener = View.OnDragListener{view, event ->
        when(event.action){
            DragEvent.ACTION_DRAG_STARTED ->{
                event.clipDescription.hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN)
            }
            DragEvent.ACTION_DRAG_ENTERED ->{
                view.invalidate()
                true
            }
            DragEvent.ACTION_DRAG_LOCATION -> true
            DragEvent.ACTION_DRAG_EXITED ->{
                view.invalidate()
                true
            }
            DragEvent.ACTION_DROP ->{
                val item = event.clipData.getItemAt(0)
                val dragData = item.text
                Toast.makeText(this,dragData,Toast.LENGTH_SHORT).show()

                val v = event.localState as View
                val owner = v.parent as ViewGroup
                owner.removeView(v)
                val destination = view as LinearLayout
                destination.addView(v)
                v.visibility = View.VISIBLE
                true
            }
            DragEvent.ACTION_DRAG_ENTERED ->{
                view.invalidate()
                true
            }
            else -> false
        }
    }

}