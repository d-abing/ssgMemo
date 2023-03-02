package com.example.ssgmemo.common

import android.annotation.SuppressLint
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
import android.view.View
import android.widget.*
import com.example.ssgmemo.Memo
import com.example.ssgmemo.R
import com.example.ssgmemo.SpinnerModel
import com.example.ssgmemo.SqliteHelper
import com.example.ssgmemo.adapter.SpinnerAdapter
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView

class WriteActivity : AppCompatActivity() {
    val helper = SqliteHelper(this, "ssgMemo", 1)
    lateinit var mAdView : AdView
    private val ctgrList = ArrayList<SpinnerModel>()

    @SuppressLint("MissingInflatedId", "WrongViewCast")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_write)
        
        // 설정 state
        val fontSize = intent.getStringExtra("fontSize")
        val vibration = intent.getStringExtra("vibration")
        
        // view
        val spinner = findViewById<Spinner>(R.id.category)
        val title = findViewById<TextView>(R.id.writeTitle)
        val content = findViewById<TextView>(R.id.writeContent)
        val btnSave = findViewById<ImageButton>(R.id.saveContent)

        // spinner
        var ctgr = 0
        ctgrList.add(0, SpinnerModel(R.drawable.closed_box, "미분류"))
        for (i in helper.selectCtgrMap().values.toMutableList()) {
            val spinnerModel = SpinnerModel(R.drawable.closed_box, i)
            ctgrList.add(spinnerModel)
        }

        fun <K, V> getKey(map: Map<K, V>, target: V): K { return map.keys.first { target == map[it] } }

        // 설정 반영
        if (fontSize.equals("ON")) {
            title.textSize = 24f
            content.textSize = 24f
            spinner.adapter = SpinnerAdapter(this, R.layout.item_spinner2, ctgrList)
        } else {
            spinner.adapter = SpinnerAdapter(this, R.layout.item_spinner, ctgrList)
        }

        spinner.onItemSelectedListener = object: AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {}
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val category = spinner.getItemAtPosition(position) as SpinnerModel
                if( category.name != "미분류") {
                    ctgr = getKey(helper.selectCtgrMap(), category.name)
                } else {
                    ctgr = 0
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

                if(vibration.equals("ON")) {
                    val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                    vibrator.vibrate(VibrationEffect.createOneShot(200, 50))
                }

                // 카테고리가 있으며 첫 글이 아닌 경우 (마지막 우선순위 +1) 부여
                if (helper.checkTopMemo(ctgr!!) !=null){
                    priority = helper.checkTopMemo(ctgr!!)!! + 1
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



       // setFragment()
    }
   /* private fun setFragment() {
        val fontFragment: Fragment = FontFragment()
        val trans = supportFragmentManager.beginTransaction()
        trans.add(R.id.frameLayout, fontFragment)
        trans.commit()
    }*/
}