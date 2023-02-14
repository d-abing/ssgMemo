package com.example.ssgmemo.common

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.GridLayoutManager
import com.example.ssgmemo.Ctgr
import com.example.ssgmemo.R
import com.example.ssgmemo.SqliteHelper
import com.example.ssgmemo.adapter.RecyclerAdapter
import com.example.ssgmemo.callback.CallbackListener
import com.example.ssgmemo.databinding.ActivityViewCtgrBinding
import com.example.ssgmemo.fragment.CtgrAddFragment
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds

class ViewCtgrActivity : AppCompatActivity(), CallbackListener {
    private lateinit var binding: ActivityViewCtgrBinding
    val helper = SqliteHelper(this, "ssgMemo", 1)
    lateinit var adapter: RecyclerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityViewCtgrBinding.inflate(layoutInflater)
        setContentView(binding.root)

        adapter = RecyclerAdapter(this)
        adapter.callbackListener = this
        adapter.helper = helper
        val unclassifyCtgr = Ctgr(0, "미분류", 11111111)
        val ctgrAddBtn = Ctgr(null,"+",11111111)
        adapter.listData = helper.selectCtgrList().toMutableList()
        adapter.fontSize = intent.getStringExtra("fontSize")

        if (helper.isUnknownMemoExist()){
            adapter.listData.add(0,unclassifyCtgr)
        }
        adapter.listData.add(ctgrAddBtn)
        // helper.selectMemo()의 리턴값인 리스트를 통째로 listData 리스트에 넣음
        binding.recyclerCtgr2.adapter = adapter
        // 화면에서 보여줄 RecyclerView인 recyclerMemo의 어댑터로 위에서 만든 adapter를 지정
        binding.recyclerCtgr2.layoutManager = GridLayoutManager(this, 2)
        if(adapter.listData.isEmpty()){
            binding.msgCtgr.visibility = View.VISIBLE
        }

        // 광고
        MobileAds.initialize(this) {}
        val mAdView = findViewById<AdView>(R.id.adView)
        val adRequest = AdRequest.Builder().build()
        mAdView.loadAd(adRequest)
    }

    override fun callmsg() {
        binding.msgCtgr.visibility = View.VISIBLE
    }

    override fun fragmentOpen(item: String) {
        if(item == "+"){
            CtgrAddFragment(this).show(supportFragmentManager, "CtgrAdd")
        }
    }
    override fun addCtgr(ctgrName: String) {
        val ctgr = Ctgr(null,ctgrName,System.currentTimeMillis())
        val unclassifyCtgr = Ctgr(0, "미분류", 11111111)
        val ctgrAddBtn = Ctgr(null,"+",11111111)
        val index = if(adapter.listData.size<2){0}
        else{
            adapter.listData.size-1
        }
        // 첫 Ctgr의 이름이 "미분류" 라면 미분류와 + 버튼 사이에 존재 아니라면 0번째 부터
        if (!helper.checkDuplicationCtgr(ctgrName)){
            helper.insertCtgr(ctgr)
            adapter.listData = helper.selectCtgrList() as MutableList<Any>
            if (helper.isUnknownMemoExist()){
                adapter.listData.add(0,unclassifyCtgr)
            }
            adapter.listData.add(ctgrAddBtn)
            adapter.notifyDataSetChanged()
        }else{
            var text = if(ctgrName == "미분류"){"사용할 수 없는 이름입니다."}else{
                "이미 존재하는 카테고리 입니다."
            }
            val duration = Toast.LENGTH_SHORT
            val toast = Toast.makeText(applicationContext, text, duration)
            toast.show()
        }
    }

    override fun onRestart() {
        super.onRestart()
//        val unclassifyCtgr = Ctgr(0, "미분류", 11111111)
//        val ctgrAddBtn = Ctgr(null,"+",11111111)
//        if (helper.isUnknownMemoExist()){
//            adapter.listData.add(0,unclassifyCtgr)
//        }
//        adapter.listData.add(ctgrAddBtn)
//        adapter.listData = helper.selectCtgrList().toMutableList()
        adapter.notifyDataSetChanged()
    }
}