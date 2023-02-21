package com.example.ssgmemo.common

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.TypedValue
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.core.view.marginTop
import com.example.ssgmemo.R
import com.example.ssgmemo.SqliteHelper
import com.example.ssgmemo.adapter.RecyclerAdapter
import com.example.ssgmemo.callback.CallbackListener
import com.example.ssgmemo.databinding.ActivitySearchBinding
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds

class SearchActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySearchBinding
    lateinit var mAdView : AdView

    val helper = SqliteHelper(this, "ssgMemo", 1)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySearchBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val recyclerAdapter = RecyclerAdapter(this)
        var fontSize = intent.getStringExtra("fontSize")
        recyclerAdapter.fontSize = fontSize
        var where = "제목+내용"          // sql where 조건
        var orderby = "최신순"          // sql orderby 조건
        var keyword = ""               // sql where의 keyword
        var flag = false

        var conditionList1: MutableList<String> = arrayListOf("제목+내용", "제목", "내용")
        val conditionList2: MutableList<String> = arrayListOf("최신순", "오래된순")

        // <"제목", "내용", "제목+내용">
        if(fontSize.equals("ON")) binding.spinner4.adapter = ArrayAdapter(this, R.layout.spinner_layout, conditionList1)
        else binding.spinner4.adapter =  ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, conditionList1)
        binding.spinner4.onItemSelectedListener = object: AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {}
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                // where 조건 변경
                where = binding.spinner4.getItemAtPosition(position).toString()
            }
        }


        // <"최신순", "오래된순">
        if(fontSize.equals("ON"))  binding.spinner2.adapter = ArrayAdapter(this, R.layout.spinner_layout, conditionList2)
        else binding.spinner2.adapter = ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, conditionList2)
        binding.spinner2.onItemSelectedListener = object: AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {}
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                // order by 조건 바꿔서 select
                orderby = binding.spinner2.getItemAtPosition(position).toString()
                recyclerAdapter.listData.clear()
                showDataList(recyclerAdapter, keyword, where, orderby)

            }
        }

        // 키워드 입력 후 엔터
        binding.keyword.setOnEditorActionListener { v, actionId, event ->
            keyword = binding.keyword.text.toString()
            if (keyword.isNotEmpty()) {
                recyclerAdapter.listData.clear()
                showDataList(recyclerAdapter, keyword, where, orderby)
                binding.keyword.setText("")
            }
            false
        }

        recyclerAdapter.helper = helper
        showDataList(recyclerAdapter, keyword, where, orderby)
        binding.recyclerSearch.adapter = recyclerAdapter


        binding.btnCancel.setOnClickListener {
            recyclerAdapter.listData.clear()
            keyword = ""
            showDataList(recyclerAdapter, keyword, where, orderby)
            binding.keyword.setText("")
        }

        val display = this.applicationContext?.resources?.displayMetrics
        val deviceHeight = display?.heightPixels
        val layoutParams1 = binding.recyclerSearch.layoutParams
        val layoutParams2 = binding.emptyText2.layoutParams
        layoutParams1.height = deviceHeight?.times(0.81)!!.toInt()
        layoutParams2.height = deviceHeight?.times(0.81)!!.toInt()
        binding.recyclerSearch.layoutParams = layoutParams1
        binding.emptyText2.layoutParams = layoutParams2

        binding.btnFilter.setOnClickListener {
            if (flag == false) {
                binding.spinner2.visibility = View.VISIBLE
                binding.spinner4.visibility = View.VISIBLE
                binding.recyclerSearch.margin(top = 79F)
                binding.emptyText2.margin(top = 79F)
                layoutParams1.height = deviceHeight.times(0.75).toInt()
                layoutParams2.height = deviceHeight.times(0.75).toInt()
                binding.recyclerSearch.layoutParams = layoutParams1
                binding.emptyText2.layoutParams = layoutParams2
                flag = true
            } else {
                binding.spinner2.visibility = View.GONE
                binding.spinner4.visibility = View.GONE
                binding.recyclerSearch.margin(top = 32F)
                binding.emptyText2.margin(top = 32F)
                layoutParams1.height = deviceHeight.times(0.81).toInt()
                layoutParams2.height = deviceHeight.times(0.81).toInt()
                binding.recyclerSearch.layoutParams = layoutParams1
                binding.emptyText2.layoutParams = layoutParams2
                flag = false
            }
        }



        // 광고
        MobileAds.initialize(this) {}
        mAdView = findViewById<AdView>(R.id.adView)
        val adRequest = AdRequest.Builder().build()
        mAdView.loadAd(adRequest)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val imm: InputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(currentFocus?.windowToken, 0)
        return true
    }

    fun showDataList(recyclerAdapter: RecyclerAdapter, keyword: String, where: String, orderby: String) {
        val data = helper.selectSearchList(keyword, where, orderby)
        recyclerAdapter.listData.addAll(helper.selectSearchList(keyword, where, orderby))
        if(data.isEmpty()) {
            binding.recyclerSearch.visibility = View.INVISIBLE
            binding.emptyText2.visibility = View.VISIBLE
        } else {
            binding.recyclerSearch.visibility = View.VISIBLE
            binding.emptyText2.visibility = View.INVISIBLE
        }
        recyclerAdapter.notifyDataSetChanged()
    }

    fun View.margin(left: Float? = null, top: Float? = null, right: Float? = null, bottom: Float? = null) {
        layoutParams<ViewGroup.MarginLayoutParams> {
            left?.run { leftMargin = dpToPx(this) }
            top?.run { topMargin = dpToPx(this) }
            right?.run { rightMargin = dpToPx(this) }
            bottom?.run { bottomMargin = dpToPx(this) }
        }
    }

    inline fun <reified T : ViewGroup.LayoutParams> View.layoutParams(block: T.() -> Unit) {
        if (layoutParams is T) block(layoutParams as T)
    }

    fun View.dpToPx(dp: Float): Int = context.dpToPx(dp)
    fun Context.dpToPx(dp: Float): Int = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, resources.displayMetrics).toInt()
}