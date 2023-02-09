package com.example.ssgmemo

import android.app.Activity
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
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
        var adapter = RecyclerSwipeAdapter(this)
        adapter.fontSize = intent.getStringExtra("fontSize").toString()
        adapter.helper = helper
        adapter.itemList = helper.selectMemoList(ctgrName!!)
        val itemTouchHelperCallback = ItemTouchHelperCallback(adapter)
        itemTouchHelperCallback.setClamp(150f)
        ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(binding.recyclerContent1)
        binding.recyclerContent1.adapter = adapter
        binding.ctgrTitle.text = ctgrName

        if (title == "0"){
            adapter.itemList.addAll(unknownMemoList)
        }else{
            adapter.itemList.addAll(memoList)
        }
        if(adapter.itemList.isEmpty()){
            binding.msgText.visibility = View.VISIBLE
        }

        binding.recyclerContent1.apply {
            layoutManager = LinearLayoutManager(applicationContext)
            adapter = adapter
//            addItemDecoration(ItemDecoration())

            setOnTouchListener { _, _ ->
                itemTouchHelperCallback.removePreviousClamp(this)
                false
            }
        }
    }

    override fun onRestart() {
        super.onRestart()
        val helper = SqliteHelper(this, "ssgMemo", 1)
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
        itemTouchHelperCallback.setClamp(150f)
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