package com.example.ssgmemo.common

import android.content.Context
import android.os.Bundle
import android.os.Vibrator
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.getSystemService
import androidx.recyclerview.widget.GridLayoutManager
import com.example.ssgmemo.Ctgr
import com.example.ssgmemo.R
import com.example.ssgmemo.SqliteHelper
import com.example.ssgmemo.adapter.RecyclerAdapter
import com.example.ssgmemo.callback.CallbackListener
import com.example.ssgmemo.databinding.ActivityViewCtgrBinding
import com.example.ssgmemo.fragment.CtgrAddFragment
import com.example.ssgmemo.fragment.DeleteFragment
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
        adapter.vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        adapter.helper = helper
        val unclassifyCtgr = Ctgr(0, "미분류", 11111111)
        val ctgrAddBtn = Ctgr(null,"+",11111111)
        adapter.listData = helper.selectCtgrList().toMutableList()
        adapter.fontSize = intent.getStringExtra("fontSize")
        adapter.vibration = intent.getStringExtra("vibration")

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

    override fun openKeyBoard(view: View) {
        val imm: InputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(view,0)
    }
    override fun closeKeyBoard() {
        val imm: InputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(currentFocus?.windowToken, 0)
    }

    override fun callmsg() {
        binding.msgCtgr.visibility = View.VISIBLE
    }

    override fun fragmentOpen(item: String, ctgridx: String?) {
        if(item == "+"){
            CtgrAddFragment(this).show(supportFragmentManager, "CtgrAdd")
        }else if(item == "delete@#"){
            val deleteFragment = DeleteFragment(this)
            val bundle:Bundle = Bundle()
            bundle.putString("Ctgridx",ctgridx)
            deleteFragment.arguments = bundle
            deleteFragment.show(supportFragmentManager, "CtgrDelete")
        }
    }
    override fun addCtgr(ctgrName: String) {
        val ctgr = Ctgr(null,ctgrName,System.currentTimeMillis())
        val unclassifyCtgr = Ctgr(0, "미분류", 11111111)
        val ctgrAddBtn = Ctgr(null,"+",11111111)

        // 첫 Ctgr의 이름이 "미분류" 라면 미분류와 + 버튼 사이에 존재 아니라면 0번째 부터
        if(ctgrName != "미분류" && ctgrName != "delete@#" && ctgrName != "+"){
            if (!helper.checkDuplicationCtgr(ctgrName)){
                helper.insertCtgr(ctgr)
                adapter.listData = helper.selectCtgrList() as MutableList<Any>
                if (helper.isUnknownMemoExist()){
                    adapter.listData.add(0,unclassifyCtgr)
                }
                adapter.listData.add(ctgrAddBtn)
                adapter.notifyDataSetChanged()
            }else{
                val text = "이미 사용중 입니다."
                val duration = Toast.LENGTH_SHORT
                val toast = Toast.makeText(applicationContext, text, duration)
                toast.show()
            }
        }else{
            val text = "사용할 수 없는 이름입니다."
            val duration = Toast.LENGTH_SHORT
            val toast = Toast.makeText(applicationContext, text, duration)
            toast.show()
        }


    }

    override fun deleteCtgr(ctgridx: String) {
        super.deleteCtgr(ctgridx)
        val unclassifyCtgr = Ctgr(0, "미분류", 11111111)
        val ctgrAddBtn = Ctgr(null,"+",11111111)

        helper.deleteCtgr(ctgridx)
        adapter.listData = helper.selectCtgrList() as MutableList<Any>
        if (helper.isUnknownMemoExist()){
            adapter.listData.add(0,unclassifyCtgr)
        }
        adapter.listData.add(ctgrAddBtn)
        adapter.notifyDataSetChanged()
    }

    override fun deleteMemoFromCtgr(cidx: String) {
        super.deleteMemoFromCtgr(cidx)
        helper.deleteMemoFromCtgr(cidx)
        deleteCtgr(cidx)
    }

    override fun onRestart() {
        super.onRestart()
        val ctgrAddBtn = Ctgr(null,"+",11111111)
        val unclassifyCtgr = Ctgr(0, "미분류", 11111111)
        if (!helper.isUnknownMemoExist()){
            adapter.listData = helper.selectCtgrList().toMutableList()
            adapter.listData.add(ctgrAddBtn)
        } else{
            adapter.listData = helper.selectCtgrList().toMutableList()
            adapter.listData.add(ctgrAddBtn)
            adapter.listData.add(0,unclassifyCtgr)
        }
        adapter.notifyDataSetChanged()

    }

}

