package com.example.ssgmemo.common

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Vibrator
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.ssgmemo.Memo
import com.example.ssgmemo.R
import com.example.ssgmemo.SqliteHelper
import com.example.ssgmemo.adapter.RecyclerSwipeAdapter
import com.example.ssgmemo.callback.CallbackListener
import com.example.ssgmemo.callback.ItemTouchHelperCallback
import com.example.ssgmemo.databinding.ActivityViewMemoBinding
import com.example.ssgmemo.fragment.MemoDeleteFragment
import com.example.ssgmemo.fragment.MemoMoveFragment
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
        adapter.vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        adapter.helper = helper
        itemTouchHelperCallback.setClamp(150f)
        ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(binding.recyclerContent1)
        val dividerItemDecoration = DividerItemDecoration(binding.recyclerContent1.context, LinearLayoutManager(this).orientation)
        binding.recyclerContent1.addItemDecoration(dividerItemDecoration)
        binding.recyclerContent1.adapter = adapter
        binding.ctgrTitle.text = ctgrName
        adapter.itemList = helper.selectMemoList(title!!)
        Log.d("test다11","${adapter.itemList}")

        val display = this.applicationContext?.resources?.displayMetrics
        val deviceHeight = display?.heightPixels
        val layoutParams1 = binding.recyclerContent1.layoutParams
        layoutParams1.height = deviceHeight?.times(0.82)!!.toInt()
        binding.recyclerContent1.layoutParams = layoutParams1

        if(adapter.itemList.isEmpty()){
            binding.msgText.visibility = View.VISIBLE
            binding.selectBtn.visibility = View.INVISIBLE
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
                adapter.selectedList.clear()
                adapter.selectAll = false
                adapter.notifyDataSetChanged()
                binding.selectLayout.visibility = View.VISIBLE
                modeChange = true
                itemTouchHelperCallback.setMode(1)
            } else {
                adapter.mode = 0
                adapter.selectedList.clear()
                adapter.selectAll = false
                adapter.notifyDataSetChanged()
                binding.selectLayout.visibility = View.INVISIBLE
                modeChange = false
                itemTouchHelperCallback.setMode(0)
            }
        }

        binding.selectAll.setOnClickListener {
            if (adapter.selectAll == false){
                adapter.selectAll = true
                adapter.selectedList = memoList
                Log.d("test다", "${adapter.selectedList}")
            }else if(adapter.selectAll == true){
                adapter.selectAll = false
                adapter.selectedList.clear()
                Log.d("test다", "${adapter.selectedList}")
            }
            adapter.notifyDataSetChanged()
        }

        binding.deleteSelected.setOnClickListener {
            if (adapter.selectedList.size == 1){
                val memoidx = adapter.selectedList[0].idx.toString()
                fragmentOpen(title, memoidx, false)
            }else if(adapter.selectedList.isEmpty()) {
                Toast.makeText(this,"선택된 값이 없습니다.",Toast.LENGTH_SHORT).show()
            }else{
                fragmentOpen(title, adapter.selectedList[0].idx.toString(), true)
            }
        }

        binding.moveSelected.setOnClickListener {
            if (adapter.selectedList.size == 1){
                val memoidx = adapter.selectedList[0].idx.toString()
                fragmentOpen(title, memoidx, false, 1)
            }else if(adapter.selectedList.isEmpty()) {
                Toast.makeText(this,"선택된 값이 없습니다.",Toast.LENGTH_SHORT).show()
            }else{
                fragmentOpen(title, adapter.selectedList[0].ctgr.toString(), true, 1)
            }
        }

        // 광고
        MobileAds.initialize(this) {}
        val mAdView = findViewById<AdView>(R.id.sizeup)
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

    override fun fragmentOpen(memoCtgr: String, memoidx: String, isList:Boolean) {
        super.fragmentOpen(memoCtgr, memoidx, isList)
        // 리스트 인지 하나인지 미분류인지 아닌지...
        val deleteFragment = MemoDeleteFragment(this)
        val bundle:Bundle = Bundle()
        bundle.putString("memoCtgr",memoCtgr)
        bundle.putString("memoidx",memoidx)
        bundle.putBoolean("isList",isList)
        deleteFragment.arguments = bundle
        deleteFragment.show(supportFragmentManager, "memoDelete")
    }

    fun fragmentOpen(memoCtgr: String, memoidx: String, isList:Boolean, move: Int) {
        super.fragmentOpen(memoCtgr, memoidx, isList)
        // 리스트 인지 하나인지 미분류인지 아닌지...
        val moveFragment = MemoMoveFragment(this)
        moveFragment.helper = helper
        val bundle:Bundle = Bundle()
        bundle.putString("memoCtgr",memoCtgr)
        bundle.putString("memoidx",memoidx)
        bundle.putBoolean("isList",isList)
        moveFragment.arguments = bundle
        moveFragment.show(supportFragmentManager, "memoMove")
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
    override fun deleteCtgrList() {
        super.deleteCtgrList()
        var sortedList = adapter.selectedList.sortedBy { it.priority }
        val cidx = adapter.selectedList.get(0).ctgr!!.toLong()
        for (list in sortedList){
            helper.deleteMemoCtgr(list.idx.toString())
        }
        helper.updatePriority(cidx)
        if(helper.selectMemoList(title).isEmpty()){
            binding.msgText.visibility = View.VISIBLE
        }
        adapter.itemList = helper.selectMemoList(title)
        binding.recyclerContent1.apply {
            layoutManager = LinearLayoutManager(applicationContext)
            adapter = adapter
            itemTouchHelperCallback.removePreviousClamp(this)
        }
        adapter.notifyDataSetChanged()
    }
    override fun deleteMemoList(){
        for(selectedList in adapter.selectedList){
            helper.deleteContent(selectedList)
        }
        adapter.itemList = helper.selectMemoList(title)
        if(adapter.itemList.isEmpty()){
            binding.msgText.visibility = View.VISIBLE
        }
        adapter.notifyDataSetChanged()
    }

    override fun moveCtgr(memoidx: Long?, ctgr: Long) {
        super.moveCtgr(memoidx, ctgr)
        val memo:Memo = helper.selectMemo(memoidx.toString())
        helper.updateMemoCtgr(memoidx, ctgr, helper.getTopPriority(ctgr.toInt()) + 1)
        helper.updatePriority(memo.ctgr.toLong())
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

    override fun moveCtgrList(oldctgr: Long, ctgr: Long){
        var sortedList = adapter.selectedList.sortedBy { it.priority }
        for( memo in sortedList){
            helper.moveContent(memo, ctgr)
        }
        helper.updatePriority(oldctgr)
        adapter.itemList = helper.selectMemoList(title)
        if(adapter.itemList.isEmpty()){
            binding.msgText.visibility = View.VISIBLE
        }
        adapter.notifyDataSetChanged()
    }
}