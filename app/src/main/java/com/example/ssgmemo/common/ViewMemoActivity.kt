package com.example.ssgmemo.common

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.ssgmemo.R
import com.example.ssgmemo.SqliteHelper
import com.example.ssgmemo.adapter.RecyclerSwipeAdapter
import com.example.ssgmemo.callback.ItemTouchHelperCallback
import com.example.ssgmemo.databinding.ActivityViewContentBinding
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds

class ViewMemoActivity : AppCompatActivity(){
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
        val unknownMemoList = helper.selectMemoList("0")
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

        // 광고
        MobileAds.initialize(this) {}
        val mAdView = findViewById<AdView>(R.id.adView)
        val adRequest = AdRequest.Builder().build()
        mAdView.loadAd(adRequest)
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
        val unknownMemoList = helper.selectMemoList("0")
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