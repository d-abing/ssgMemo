package com.example.ssgmemo

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.GridLayoutManager
import com.example.ssgmemo.databinding.ActivityViewContentBinding

class ViewContentActivity : AppCompatActivity() {
    private lateinit var binding: ActivityViewContentBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        val helper = SqliteHelper(this, "ssgMemo", 1)
        super.onCreate(savedInstanceState)
        binding = ActivityViewContentBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val title = intent.getStringExtra("title")
        val ctgrName = intent.getStringExtra("ctgrname")
        // list
        val memoList = helper.selectMemoList(title!!)
        val adapter = RecyclerAdapter(this)
        adapter.helper = helper
        adapter.listData.addAll(memoList)
        binding.recyclerContent1.adapter = adapter
        binding.ctgrTitle.text = ctgrName


        Log.d("왜","${adapter.listData}")







    }
}