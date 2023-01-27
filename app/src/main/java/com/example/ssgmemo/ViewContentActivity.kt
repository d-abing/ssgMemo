package com.example.ssgmemo

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.ItemTouchHelper
import com.example.ssgmemo.databinding.ActivityViewContentBinding

class ViewContentActivity : AppCompatActivity(){
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
        val unknownMemoList = helper.selectMemoList("isnull")
        val adapter = RecyclerSwipeAdapter(this)

        adapter.helper = helper
        adapter.itemList = helper.selectMemoList(ctgrName!!)
        val itemTouchHelperCallback = ItemTouchHelperCallback(adapter)
        ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(binding.recyclerContent1)
        binding.recyclerContent1.adapter = adapter
        binding.ctgrTitle.text = ctgrName

        if (title == "0"){
            adapter.itemList.addAll(unknownMemoList)
        }else{
            adapter.itemList.addAll(memoList)
        }
    }
}