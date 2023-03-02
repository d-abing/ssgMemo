package com.example.ssgmemo.common

import android.R
import android.content.Context
import android.graphics.Rect
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.marginTop
import com.example.ssgmemo.BackPressEditText
import com.example.ssgmemo.Memo
import com.example.ssgmemo.SpinnerModel
import com.example.ssgmemo.SqliteHelper
import com.example.ssgmemo.adapter.SpinnerAdapter
import com.example.ssgmemo.databinding.ActivityWriteBinding
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import java.text.SimpleDateFormat
import java.util.*

class EditActivity : AppCompatActivity() {
    var readmode = true
    var scroll = false
    val SOFT_KEYBOARD_HEIGHT_DP_THRESHOLD = 128

    private lateinit var binding: ActivityWriteBinding
    lateinit var mAdView: AdView
    private val ctgrList = ArrayList<SpinnerModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val helper = SqliteHelper(this, "ssgMemo", 1)
        binding = ActivityWriteBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val memoIdx = intent.getStringExtra("memoIdx") as String
        val fontSize = intent.getStringExtra("fontSize") as String
        val vibration = intent.getStringExtra("vibration")

        var memo = helper.selectMemo(memoIdx)
        val ctgrMap = helper.selectCtgrMap()
        var ctgr: Int? = memo.ctgr
        var priority: Int? = memo.priority

        ctgrList.add(0, SpinnerModel(com.example.ssgmemo.R.drawable.closed_box, "미분류"))
        for (i in helper.selectCtgrMap().values.toMutableList()) {
            val spinnerModel = SpinnerModel(com.example.ssgmemo.R.drawable.closed_box, i)
            ctgrList.add(spinnerModel)
        }

        binding.writeContent.isFocusableInTouchMode = false

        // content 높이 조절
        val display = this.applicationContext?.resources?.displayMetrics
        val deviceHeight = display?.heightPixels
        val layoutParams = binding.writeContent.layoutParams
        layoutParams.height = deviceHeight?.times(0.7)!!.toInt()
        binding.writeContent.layoutParams = layoutParams

        // 수정시 버튼 숨김 및 기존 정보 불러오기
        Log.d("test다", "$ctgrList")
        binding.date.visibility = View.VISIBLE
        val t_dateFormat = SimpleDateFormat("yyyy년 M월 d일 a h:m:s", Locale("ko", "KR"))
        val str_date = t_dateFormat.format(Date(memo.datetime))
        binding.date.text = str_date
        binding.saveContent.setImageResource(com.example.ssgmemo.R.drawable.read)
        binding.writeTitle.setText(memo.title)
        binding.writeContent.setText(memo.content)
        if (fontSize.equals("ON")) {
            binding.date.textSize = 20f
            binding.writeTitle.textSize = 24f
            binding.writeContent.textSize = 24f
            binding.category.adapter = SpinnerAdapter(this, com.example.ssgmemo.R.layout.item_spinner2, ctgrList)
        } else {
            binding.category.adapter = SpinnerAdapter(this, com.example.ssgmemo.R.layout.item_spinner, ctgrList)
        }

        var selectedIndex : Int = 0
        for (i in ctgrList) {
            if (i.name == ctgrMap[ctgr]) {
                selectedIndex = ctgrList.indexOf(i)
            }
        }

        binding.category.setSelection(selectedIndex)
        binding.category.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }

            // 스피너의 값이 변경될 때 실행
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                val category = binding.category.getItemAtPosition(position) as SpinnerModel
                // 선택된 값이 미분류가 아니면
                if (category.name != "미분류") {
                    ctgr = getKey(helper.selectCtgrMap(), category.name)
                } else {
                    ctgr = 0
                }
            }

            fun <K, V> getKey(map: Map<K, V>, target: V): K {
                return map.keys.first { target == map[it] }
            }
        }

        binding.saveContent.setOnClickListener {
            if (readmode) {
                readmode = false
                binding.saveContent.setImageResource(com.example.ssgmemo.R.drawable.modify)
                binding.writeContent.isFocusableInTouchMode = true
            } else {
                readmode = true
                binding.saveContent.setImageResource(com.example.ssgmemo.R.drawable.read)
                binding.writeContent.isFocusableInTouchMode = false
                binding.writeContent.isFocusable = false
                softkeyboardHide()
            }
        }

        binding.writeContent.setOnClickListener {
            if (!scroll) {
                readmode = false
                binding.saveContent.setImageResource(com.example.ssgmemo.R.drawable.modify)
                binding.writeContent.isFocusableInTouchMode = true
            }
            scroll = false
        }

        binding.writeContent.setOnScrollChangeListener { view, i, i2, i3, i4 ->
            scroll = true
            readmode = true
            binding.saveContent.setImageResource(com.example.ssgmemo.R.drawable.read)
            binding.writeContent.isFocusableInTouchMode = false
            binding.writeContent.isFocusable = false
            softkeyboardHide()
        }
        

        binding.writeContent.setOnBackPressListener(object : BackPressEditText.OnBackPressListener {
            override fun onBackPress() {
                if ( isKeyboardShown(binding.writeContent.rootView) ) {
                    readmode = true
                    binding.saveContent.setImageResource(com.example.ssgmemo.R.drawable.read)
                    binding.writeContent.isFocusableInTouchMode = false
                    binding.writeContent.isFocusable = false
                }
                var mTitle = memo.title
                var mContent = memo.content
                var checkdiff1 = false
                var checkdiff2 = false
                var checkdiff3 = false

                if (binding.writeTitle.text.toString() != mTitle) {
                    mTitle = if (binding.writeTitle.text.toString() == "") {
                        "빈 제목"
                    } else {
                        binding.writeTitle.text.toString()
                    }
                    checkdiff1 = true
                }
                if (binding.writeContent.text.toString() != mContent) {
                    mContent = binding.writeContent.text.toString()
                    checkdiff2 = true
                }
                // 카테고리가 변경되었을 때 우선순위 +1 부여
                if (ctgr != memo.ctgr) {
                    priority = if (helper.checkTopMemo(ctgr!!) != null) {
                        helper.checkTopMemo(ctgr!!)!! + 1
                    } else {
                        0
                    }
                    checkdiff3 = true
                }
                // 제목, 내용, 카테고리 하나라도 변경되었으면 db업뎃
                if (checkdiff1 || checkdiff2 || checkdiff3) {
                    if (vibration.equals("ON")) {
                        val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                        vibrator.vibrate(VibrationEffect.createOneShot(200, 50))
                    }

                    val memo_after = Memo(
                        memo.idx,
                        mTitle,
                        mContent,
                        System.currentTimeMillis(),
                        ctgr,
                        priority
                    )
                    Log.d("test다","$memo")
                    Log.d("test다","$memo_after")
                    helper.updateMemo(memo_after, checkdiff3, memo.ctgr!!, memo.priority as Int)
                }
            }
        })

        // 광고
        MobileAds.initialize(this) {}
        mAdView = findViewById<AdView>(com.example.ssgmemo.R.id.adView)
        val adRequest = AdRequest.Builder().build()
        mAdView.loadAd(adRequest)

    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val imm: InputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(currentFocus?.windowToken, 0)
        readmode = true
        binding.saveContent.setImageResource(com.example.ssgmemo.R.drawable.read)
        binding.writeContent.isFocusableInTouchMode = false
        binding.writeContent.isFocusable = false
        return true
    }

    private fun isKeyboardShown(rootView: View): Boolean {
        val r = Rect()
        rootView.getWindowVisibleDisplayFrame(r)
        val dm = rootView.resources.displayMetrics
        val heightDiff = rootView.bottom - r.bottom

        return heightDiff > SOFT_KEYBOARD_HEIGHT_DP_THRESHOLD * dm.density
    }

    fun softkeyboardHide() {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(binding.writeContent.windowToken, 0)
    }

}