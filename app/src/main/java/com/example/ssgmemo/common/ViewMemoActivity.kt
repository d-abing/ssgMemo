package com.example.ssgmemo.common

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.ssgmemo.Memo
import com.example.ssgmemo.R
import com.example.ssgmemo.SqliteHelper
import com.example.ssgmemo.adapter.RecyclerSwipeAdapter
import com.example.ssgmemo.callback.CallbackListener
import com.example.ssgmemo.callback.ItemTouchHelperCallback
import com.example.ssgmemo.databinding.ActivityViewMemoBinding
import com.example.ssgmemo.fragment.DeleteFragment
import com.example.ssgmemo.fragment.MemoDeleteFragment
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds

class ViewMemoActivity : AppCompatActivity(), CallbackListener{
    private lateinit var binding: ActivityViewMemoBinding
    val helper = SqliteHelper(this, "ssgMemo", 1)
    lateinit var adapter: RecyclerSwipeAdapter
    private lateinit var itemTouchHelperCallback: ItemTouchHelperCallback
    private lateinit var title: String
    var mode : Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        binding = ActivityViewMemoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        title = intent.getStringExtra("idx").toString()
        val ctgrName = intent.getStringExtra("ctgrname")
        // list
        val memoList = helper.selectMemoList(title!!)
        adapter = RecyclerSwipeAdapter(this)
        itemTouchHelperCallback = ItemTouchHelperCallback(adapter)

        adapter.callbackListener = this
        adapter.fontSize = intent.getStringExtra("fontSize").toString()
        adapter.vibration = intent.getStringExtra("vibration").toString()
        adapter.helper = helper
        itemTouchHelperCallback.setClamp(130f)
        ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(binding.recyclerContent1)
        binding.recyclerContent1.adapter = adapter
        binding.ctgrTitle.text = ctgrName
        adapter.itemList = helper.selectMemoList(title!!)
        Log.d("test다11","${adapter.itemList}")

        if(adapter.itemList.isEmpty()){
            binding.msgText.visibility = View.VISIBLE
        }

        binding.recyclerContent1.apply {
            layoutManager = LinearLayoutManager(applicationContext)
            adapter = adapter
            setOnTouchListener { _, _ ->
                itemTouchHelperCallback.removePreviousClamp(this)
                false
            }
        }

        var modeChange = false

        binding.selectBtn.setOnClickListener {
            if (!modeChange) {
                adapter.mode = 1
                adapter.selectedList = mutableListOf()
                adapter.selectAll = false
                adapter.notifyDataSetChanged()
                binding.deleteLayout.visibility = View.VISIBLE
                modeChange = true
            } else {
                adapter.mode = 0
                adapter.selectedList = mutableListOf()
                adapter.selectAll = false
                adapter.notifyDataSetChanged()
                binding.deleteLayout.visibility = View.INVISIBLE
                modeChange = false
            }
        }

        binding.selectAll.setOnClickListener {
            if (adapter.selectAll == false){
                adapter.selectAll = true
                adapter.selectedList = memoList
                Log.d("test다", "${adapter.selectedList}")
            }else if(adapter.selectAll == true){
                adapter.selectAll = false
                adapter.selectedList = mutableListOf()
                Log.d("test다", "${adapter.selectedList}")
            }
            adapter.notifyDataSetChanged()
        }

        binding.deleteSelected.setOnClickListener {
            Log.d("test다", "${adapter.selectedList}")
            for(selectedList in adapter.selectedList){
                helper.deleteContent(selectedList)
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
        // 메모 삭제시 clamp 되어있음
        // 메모 들어갔다가 나오면 삭제 클릭 리스너 클릭 불가
        binding.recyclerContent1.apply {
            layoutManager = LinearLayoutManager(applicationContext)
            adapter = adapter
            itemTouchHelperCallback.removePreviousClamp(this)
        }
        adapter.itemList = helper.selectMemoList(title)

        adapter.notifyDataSetChanged()
    }

    override fun fragmentOpen(memoCtgr: Int, memoidx: String) {
        super.fragmentOpen(memoCtgr, memoidx)
        if(memoCtgr == 0){
            val deleteFragment = DeleteFragment(this)
            val bundle:Bundle = Bundle()
            bundle.putString("memoidx",memoidx)
            deleteFragment.arguments = bundle
            deleteFragment.show(supportFragmentManager, "memoDelete")
        }else{
            val memoDeleteFragment = MemoDeleteFragment(this)
            val bundle:Bundle = Bundle()
            bundle.putString("memoidx",memoidx)
            memoDeleteFragment.arguments = bundle
            memoDeleteFragment.show(supportFragmentManager, "memoDelete")
        }
    }

    override fun deleteMemo(memoidx: String) {
        super.deleteMemo(memoidx)
        val memo:Memo = helper.selectMemo(memoidx)
        helper.deleteContent(memo)
        adapter.itemList = helper.selectMemoList(memo.ctgr.toString())
        if(adapter.itemList.isEmpty()){
            binding.msgText.visibility = View.VISIBLE
        }
        binding.recyclerContent1.apply {
            layoutManager = LinearLayoutManager(applicationContext)
            adapter = adapter
            itemTouchHelperCallback.removePreviousClamp(this)
        }
        adapter.notifyDataSetChanged()
    }

    override fun deleteCtgr(memoidx: String) {
        super.deleteCtgr(memoidx)
        val memo:Memo = helper.selectMemo(memoidx)
        helper.deleteMemoCtgr(memoidx)
        adapter.itemList = helper.selectMemoList(memo.ctgr.toString())
        if(adapter.itemList.isEmpty()){
            binding.msgText.visibility = View.VISIBLE
        }
        binding.recyclerContent1.apply {
            layoutManager = LinearLayoutManager(applicationContext)
            adapter = adapter
            itemTouchHelperCallback.removePreviousClamp(this)
        }
        adapter.notifyDataSetChanged()
    }
    fun getIdx(memoList: MutableList<Memo>):MutableList<String>{
        var result: MutableList<String> = mutableListOf()
        for (memoList in memoList){
            result.add(memoList.idx.toString())
        }
        return  result
    }
}