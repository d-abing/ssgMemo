package com.example.ssgmemo.common

import android.content.Context
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.viewpager2.widget.ViewPager2
import com.example.ssgmemo.*
import com.example.ssgmemo.adapter.RecyclerAdapter
import com.example.ssgmemo.adapter.ViewPagerAdapter
import com.example.ssgmemo.callback.CallbackListener
import com.example.ssgmemo.databinding.ActivityClassifyBinding
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds

class ClassifyActivity : AppCompatActivity(), CallbackListener {
    lateinit var binding: ActivityClassifyBinding
    lateinit var mAdView : AdView

    val helper = SqliteHelper(this, "ssgMemo", 1)
    var pagerAdapter: ViewPagerAdapter? = null
    var memoList: MutableList<Memo>? = null     // 분류 메뉴에 들어올 때의 memoList
    var memoList2: MutableList<Memo>? = null    // 분류로 인해 변경된 memoList
    var midx: Long? = null                      // 현재 보고 있는 메모의 midx 값
    var tmp_position: Int = 0                   // viewpager의 현재 위치
    var vibration: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityClassifyBinding.inflate(layoutInflater)
        setContentView(binding.root)
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

            override fun onPageScrollStateChanged(state: Int) {
                super.onPageScrollStateChanged(state)
            }

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
                super.onPageScrolled(position, positionOffset, positionOffsetPixels)
            }
        })

        if(memoList!!.isEmpty()) { // memoList가 비어있을 경우 "분류할 메모가 없습니다" 출력
            binding.viewpager.visibility = View.INVISIBLE
            binding.emptyText.visibility = View.VISIBLE
        } else {
            binding.viewpager.visibility = View.VISIBLE
            binding.emptyText.visibility = View.INVISIBLE
        }

        // < 카테고리 list >
        val recyclerAdapter = RecyclerAdapter(this)
        recyclerAdapter.callbackListener = this
        recyclerAdapter.fontSize = fontSize
        recyclerAdapter.helper = helper
        recyclerAdapter.listData.addAll(helper.selectCtgrList())                            // ctgrList를 recyclerAdapter에 추가
        binding.recyclerCtgr1.adapter = recyclerAdapter                                     // recyclerCtgr1에 recyclerAdapter 등록
        binding.recyclerCtgr1.layoutManager = GridLayoutManager(this, 4)   // layout을 그리드 4span으로 지정

        // 광고
        MobileAds.initialize(this) {}
        mAdView = findViewById<AdView>(R.id.adView)
        val adRequest = AdRequest.Builder().build()
        mAdView.loadAd(adRequest)

        /*binding.btnCtgrAdd.setOnClickListener {                                             // 카테고리 추가 기능
            if (binding.ctgrName.text.toString().isNotEmpty()) {
                val ctgr = Ctgr(null, binding.ctgrName.text.toString(), System.currentTimeMillis())
                helper.insertCtgr(ctgr)
                recyclerAdapter.listData.clear()
                recyclerAdapter.listData.addAll(helper.selectCtgrList())
                recyclerAdapter.notifyDataSetChanged()
                binding.ctgrName.setText("")
            }
        }*/
    }

    override fun callback(cidx: Long) {                                                     // RecyclerAdapter에서 호출되는 callback 함수
        // item(ctgr) 클릭시 호출

        if (vibration!!.equals("ON")) {
            val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            vibrator.vibrate(VibrationEffect.createOneShot(200, 50))
        }

        if (memoList!!.isNotEmpty()) {                                                      // memoList가 비어있지 않을 때만 수행
            helper.updateMemoCtgr(midx, cidx, helper.checkTop(cidx.toInt())?.plus(1)) // 현재 보고 있는 memo의 ctgr값 업데이트 (분류)
            pagerAdapter!!.listData.clear()
            memoList2 = helper.selectUnclassifiedMemoList()                                 // 분류로 인해 변경된 memoList 가져오기
            pagerAdapter!!.listData.addAll(memoList2!!)
            pagerAdapter!!.notifyDataSetChanged()
            // Log.d("midx", "changed midx : $midx")

            if(memoList2 != null && memoList2!!.isNotEmpty()) {                             // 분류후에도 분류할 memoList가 남아있으면
                if (memoList2!!.size > tmp_position) { // 마지막 메모가 아니라면
                    // Log.d("midx", "마지막메모x")
                } else { // 마지막 메모라면
                    tmp_position = 0
                    // Log.d("midx", "마지막메모o")
                }
                midx = memoList2!![tmp_position].idx
                // Log.d("midx", "tmp_position : $tmp_position")
                // Log.d("midx", "midx : $midx")
            } else {                                                                        // 분류후 분류할 메모리스트가 남아있지 않을 경우
                // memoList가 비어있을 경우 "분류할 메모가 없습니다" 출력
                binding.viewpager.visibility = View.INVISIBLE
                binding.emptyText.visibility = View.VISIBLE
                memoList!!.clear()
            }
        }
    }
}