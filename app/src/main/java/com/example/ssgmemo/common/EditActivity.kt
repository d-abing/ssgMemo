package com.example.ssgmemo.common

import android.R
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import com.example.ssgmemo.Memo
import com.example.ssgmemo.SqliteHelper
import com.example.ssgmemo.databinding.ActivityWriteBinding
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds

class EditActivity : AppCompatActivity() {
    private lateinit var binding: ActivityWriteBinding
    lateinit var mAdView : AdView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val helper = SqliteHelper(this, "ssgMemo", 1)
        binding = ActivityWriteBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val memoIdx = intent.getStringExtra("memoIdx") as String
        val fontSize = intent.getStringExtra("fontSize") as String
        var memo = helper.selectMemo(memoIdx)
        val ctgrList:MutableList<String> =  helper.selectCtgrMap().values.toMutableList()
        var ctgr:Int? = memo.ctgr
        var priority:Int? = memo.priority

        // content 높이 조절
        val display = this.applicationContext?.resources?.displayMetrics
        val deviceHeight = display?.heightPixels
        val layoutParams = binding.writeContent.layoutParams
        layoutParams.height = deviceHeight?.times(0.75)!!.toInt()
        binding.writeContent.layoutParams = layoutParams

        ctgrList.add(0,"미분류")
        // 수정시 버튼 숨김 및 기존 정보 불러오기
        binding.saveContent.setImageResource(com.example.ssgmemo.R.drawable.modify2)
        binding.saveContent.layoutParams.width = 80
        binding.saveContent.layoutParams.height = 80
        binding.writeTitle.setText(memo.title)
        binding.writeContent.setText(memo.content)
        if (fontSize.equals("ON")) {
            binding.writeTitle.textSize = 24f
            binding.writeContent.textSize = 24f
        }
        binding.category.adapter = ArrayAdapter(this, R.layout.simple_list_item_1, ctgrList)
        binding.category.setSelection(ctgr!!)
        binding.category.onItemSelectedListener = object: AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
            // 스피너의 값이 변경될 때 실행
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                // 선택된 값이 미분류가 아니면
                if(binding.category.getItemAtPosition(position).toString() !="미분류") {
                    val value = binding.category.getItemAtPosition(position)
                    // 카테고리 이름.. = 벨류 값...
                    ctgr = getKey(helper.selectCtgrMap(), value)
                } else{
                    ctgr = 0
                    priority = null
                }
            }
            fun <K, V> getKey(map: Map<K, V>, target: V): K {
                return map.keys.first { target == map[it] };
            }
        }

        binding.saveContent.setOnClickListener {
            // 메모 내용 업데이트 + 카테고리 변경시 우선순위 변경
            var mTitle = memo.title
            var mContent = memo.content
            var checkdiff1 = false
            var checkdiff2 = false
            var checkdiff3 = false

            if ( binding.writeTitle.text.toString() != mTitle ) {
                mTitle = if (binding.writeTitle.text.toString() == ""){
                    "빈 제목"
                }else{
                    binding.writeTitle.text.toString()
                }
                checkdiff1 = true
            }
            if ( binding.writeContent.text.toString() != mContent ) {
                mContent = binding.writeContent.text.toString()
                checkdiff2 = true
            }
            // 카테고리가 변경되었을 때 우선순위 +1 부여
            if(ctgr != memo.ctgr){
                priority = if(helper.checkTopMemo(ctgr!!) != null){
                    helper.checkTopMemo(ctgr!!)!! +1
                }else{ 0 }
                checkdiff3 = true
            }
            // 제목, 내용, 카테고리 하나라도 변경되었으면 db업뎃
            if (checkdiff1||checkdiff2||checkdiff3){
                val memo_after = Memo(memo.idx, mTitle, mContent, System.currentTimeMillis(),ctgr,priority)
                helper.updateMemo(memo_after, checkdiff3, memo.ctgr!!, memo.priority as Int)
            }
            binding.saveContent.setImageResource(com.example.ssgmemo.R.drawable.modify1)
            val handler = Handler()
            handler.postDelayed( Runnable { binding.saveContent.setImageResource(com.example.ssgmemo.R.drawable.modify2)}, 200) // 0.5초 후에 다시 닫아주기

            finish()
        }

        // 광고
        MobileAds.initialize(this) {}
        mAdView = findViewById<AdView>(com.example.ssgmemo.R.id.adView)
        val adRequest = AdRequest.Builder().build()
        mAdView.loadAd(adRequest)

    }
}