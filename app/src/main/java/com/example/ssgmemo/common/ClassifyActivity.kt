package com.example.ssgmemo.common

import android.content.Context
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.viewpager2.widget.ViewPager2
import com.example.ssgmemo.*
import com.example.ssgmemo.adapter.RecyclerAdapter
import com.example.ssgmemo.adapter.ViewPagerAdapter
import com.example.ssgmemo.callback.CallbackListener
import com.example.ssgmemo.databinding.ActivityClassifyBinding
import com.example.ssgmemo.fragment.CtgrAddFragment
import com.example.ssgmemo.fragment.MemoDeleteFragment
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds

class ClassifyActivity : AppCompatActivity(), CallbackListener {
    private lateinit var binding: ActivityClassifyBinding
    private val helper = SqliteHelper(this, "ssgMemo", 1)
    private lateinit var mAdView : AdView

    private var pagerAdapter: ViewPagerAdapter? = null  // memoList 출력
    private var recyclerAdapter: RecyclerAdapter? = null// ctgrList 출력
    private var memoList: MutableList<Memo>? = null     // 분류 메뉴에 들어올 때의 memoList
    private var memoList2: MutableList<Memo>? = null    // 분류로 인해 변경된 memoList
    private var midx: Long? = null                      // 현재 보고 있는 메모의 midx 값
    private var tmp_position: Int = 0                   // viewpager의 현재 위치

    private var vibration: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityClassifyBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 설정 state
        val fontSize = intent.getStringExtra("fontSize")
        vibration = intent.getStringExtra("vibration")

        // < 메모 list >
        pagerAdapter = ViewPagerAdapter()
        memoList = helper.selectUnclassifiedMemoList()  // 분류되지 않은 memoList
        pagerAdapter!!.fontSize = fontSize
        pagerAdapter!!.listData.addAll(memoList!!)      // pagerAdapter에 추가
        binding.viewpager.adapter = pagerAdapter        // viewpager에 pagerAdapter 등록
        binding.viewpager.registerOnPageChangeCallback( object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                if ( memoList!!.isNotEmpty() ) {
                    if (memoList2 != null && memoList2!!.isNotEmpty()) { // 현재 보고 있는 메모의 midx값과 viewpager상의 현재 위치를 받아내기 위함
                        midx = memoList2!![position].idx
                        tmp_position = position

                    } else {
                        midx = memoList!![position].idx
                        tmp_position = position
                    }
                    // Log.d("midx", "페이지 변경됨, midx : $midx")
                    // Log.d("midx", "페이지 변경됨, tmp_position : $tmp_position")
                }
            }
            override fun onPageScrollStateChanged(state: Int) { super.onPageScrollStateChanged(state) }
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) { super.onPageScrolled(position, positionOffset, positionOffsetPixels) }
        })

        if(memoList!!.isEmpty()) { // memoList가 비어있을 경우 "분류할 메모가 없습니다" 출력
            binding.viewpager.visibility = View.INVISIBLE
            binding.emptyText.visibility = View.VISIBLE
            binding.btnDelete.visibility = View.GONE
        } else {
            binding.viewpager.visibility = View.VISIBLE
            binding.emptyText.visibility = View.INVISIBLE
        }

        binding.btnDelete.setOnClickListener{
            fragmentOpen(helper.selectMemo(midx.toString()).ctgr!!, midx.toString())
        }

        // < 카테고리 list >
        recyclerAdapter = RecyclerAdapter(this)
        recyclerAdapter!!.callbackListener = this
        recyclerAdapter!!.fontSize = fontSize
        recyclerAdapter!!.helper = helper
        val ctgrAddBtn = Ctgr(null,"+",11111111, 0)
        recyclerAdapter!!.listData.addAll(helper.selectCtgrList())
        recyclerAdapter!!.listData.add(ctgrAddBtn)
        binding.recyclerCtgr1.adapter = recyclerAdapter
        binding.recyclerCtgr1.layoutManager = GridLayoutManager(this, 4)

        // 광고
        MobileAds.initialize(this) {}
        mAdView = findViewById<AdView>(R.id.sizeup)
        val adRequest = AdRequest.Builder().build()
        mAdView.loadAd(adRequest)
    }

    override fun fragmentOpen(item: String, ctgridx:String?) {
        if(item == "+"){
            CtgrAddFragment(this).show(supportFragmentManager, "CtgrAdd")
        }
    }

    override fun fragmentOpen(memoCtgr: Int, memoidx: String) {
        super.fragmentOpen(memoCtgr, memoidx)
        val ctgrDeleteFragment = MemoDeleteFragment(this)
        val bundle = Bundle()
        bundle.putString("memoidx", memoidx)
        bundle.putString("memoCtgr", memoCtgr.toString())
        ctgrDeleteFragment.arguments = bundle
        ctgrDeleteFragment.show(supportFragmentManager, "memoDelete")
    }

    override fun moveCtgr(memoidx: Long?, ctgr: Long) { // 카테고리 이동
        super.moveCtgr(memoidx, ctgr)
        if (memoList!!.isNotEmpty()) {
            val memo:Memo = helper.selectMemo(memoidx.toString())
            helper.updateMemoCtgr(memoidx, ctgr, helper.getTopPriority(ctgr.toInt()) + 1)
            helper.updatePriority(memo.ctgr.toLong())
            pagerAdapter!!.listData.clear()
            memoList2 = helper.selectUnclassifiedMemoList()                                 // 삭제로 인해 변경된 memoList 가져오기
            pagerAdapter!!.listData.addAll(memoList2!!)
            pagerAdapter!!.notifyDataSetChanged()

            if(memoList2 != null && memoList2!!.isNotEmpty()) {                             // 삭제 후에도 분류할 memoList가 남아있으면
                if (memoList2!!.size > tmp_position) { // 마지막 메모가 아니라면
                    // Log.d("midx", "마지막메모x")
                } else { // 마지막 메모라면
                    tmp_position = 0
                    // Log.d("midx", "마지막메모o")
                }
                midx = memoList2!![tmp_position].idx
                // Log.d("midx", "tmp_position : $tmp_position")
                // Log.d("midx", "midx : $midx")
            } else {                                                                        // 삭제후 분류할 메모리스트가 남아있지 않을 경우
                // memoList가 비어있을 경우 "분류할 메모가 없습니다" 출력
                binding.viewpager.visibility = View.INVISIBLE
                binding.emptyText.visibility = View.VISIBLE
                memoList!!.clear()
            }
        }
    }

    override fun callback(cidx: Long) {                                                     // RecyclerAdapter에서 호출되는 callback 함수
        if (vibration!!.equals("ON")) {
            val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            vibrator.vibrate(VibrationEffect.createOneShot(200, 50))
        }
        moveCtgr(midx, cidx)
    }

    override fun addCtgr(ctgrName: String) {
        val ctgr = Ctgr(null,ctgrName,System.currentTimeMillis(), 0)
        val ctgrAddBtn = Ctgr(null,"+",11111111, 0)

        if (!helper.checkDuplicationCtgr(ctgrName)){
            helper.insertCtgr(ctgr)
            val ctgrList = helper.selectCtgrList() as MutableList<Any>
            ctgrList.add(ctgrAddBtn)
            recyclerAdapter!!.listData = ctgrList
            recyclerAdapter!!.notifyDataSetChanged()
        }else{
            var text = if( ctgrName == "미분류" || ctgrName == "delete@#" || ctgrName == "+" || ctgrName == "휴지통" ) {
                "사용할 수 없는 이름입니다."
            } else {
                "이미 존재하는 카테고리 입니다."
            }
            val duration = Toast.LENGTH_SHORT
            val toast = Toast.makeText(applicationContext, text, duration)
            toast.show()
        }
    }
}