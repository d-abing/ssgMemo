package com.example.ssgmemo.common

import android.annotation.SuppressLint
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.*
import com.example.ssgmemo.Memo
import com.example.ssgmemo.R
import com.example.ssgmemo.SqliteHelper
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView

class WriteActivity : AppCompatActivity() {
    val helper = SqliteHelper(this, "ssgMemo", 1)
    lateinit var mAdView : AdView

    @SuppressLint("MissingInflatedId", "WrongViewCast")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_write)
        
        // 설정 state
        var fontSize = intent.getStringExtra("fontSize")
        
        // view
        val spinner = findViewById<Spinner>(R.id.category)
        val title = findViewById<TextView>(R.id.writeTitle)
        val content = findViewById<TextView>(R.id.writeContent)
        val btnSave = findViewById<ImageButton>(R.id.saveContent)

        // spinner
        var ctgr = 0
        val ctgrList:MutableList<String> =  helper.selectCtgrMap().values.toMutableList()

        fun <K, V> getKey(map: Map<K, V>, target: V): K { return map.keys.first { target == map[it] } }

        ctgrList.add(0,"미분류")
        if(fontSize.equals("ON"))  spinner.adapter = ArrayAdapter(this, R.layout.spinner_layout, ctgrList)
        else spinner.adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, ctgrList)
        spinner.onItemSelectedListener = object: AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {}
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if( spinner.getItemAtPosition(position).toString() != "미분류") {
                    val value = spinner.getItemAtPosition(position)
                    ctgr = getKey(helper.selectCtgrMap(), value)
                }
            }
        }

        // 저장
        btnSave.setOnClickListener {
            if (content.text.toString().isNotEmpty()) {
                var memo: Memo
                var mTitle = ""
                if (title.text.toString() == "") mTitle = "빈 제목"
                else mTitle = title.text.toString()
                var priority = 0

                // 카테고리가 있으며 첫 글이 아닌 경우 (마지막 우선순위 +1) 부여
                if (helper.checkTop(ctgr!!) !=null){
                    priority = helper.checkTop(ctgr!!)!! + 1
                }

                memo = Memo(
                    null,
                    mTitle,
                    content.text.toString(),
                    System.currentTimeMillis(),
                    ctgr,
                    priority
                )
                helper.insertMemo(memo)
                title.text = ""
                content.text = ""
                spinner.setSelection(0)

                btnSave.setImageResource(R.drawable.save2)
                val handler = android.os.Handler()
                handler.postDelayed( Runnable { btnSave.setImageResource(R.drawable.save1) }, 200 ) // 0.5초 후에 다시 닫아주기
            }
        }

        // content 높이 조절
        val display = this.applicationContext?.resources?.displayMetrics
        val deviceHeight = display?.heightPixels
        val layoutParams = content.layoutParams
        layoutParams.height = deviceHeight?.times(0.75)!!.toInt()
        content.layoutParams = layoutParams

        // 광고
        MobileAds.initialize(this) {}
        mAdView = findViewById<AdView>(R.id.adView)
        val adRequest = AdRequest.Builder().build()
        mAdView.loadAd(adRequest)

        // 설정 반영
        if (fontSize.equals("ON")) {
            title.textSize = 24f
            content.textSize = 24f
        }

       // setFragment()
    }
   /* private fun setFragment() {
        val fontFragment: Fragment = FontFragment()
        val trans = supportFragmentManager.beginTransaction()
        trans.add(R.id.frameLayout, fontFragment)
        trans.commit()
    }*/
}