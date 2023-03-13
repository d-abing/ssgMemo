package com.example.ssgmemo.common

import android.content.Context
import android.os.Bundle
import android.os.Vibrator
import android.util.Log
import android.util.TypedValue
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doOnTextChanged
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.ssgmemo.BackPressEditText
import com.example.ssgmemo.Ctgr
import com.example.ssgmemo.R
import com.example.ssgmemo.SqliteHelper
import com.example.ssgmemo.adapter.RecyclerAdapter
import com.example.ssgmemo.callback.CallbackListener
import com.example.ssgmemo.databinding.ActivityViewCtgrBinding
import com.example.ssgmemo.fragment.CtgrAddFragment
import com.example.ssgmemo.fragment.CtgrDeleteFragment
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds

class ViewCtgrActivity : AppCompatActivity(), CallbackListener {
    // 바인딩 및 어댑터 지연 초기화
    private lateinit var binding: ActivityViewCtgrBinding
    lateinit var adapter: RecyclerAdapter
    val helper = SqliteHelper(this, "ssgMemo", 1)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityViewCtgrBinding.inflate(layoutInflater)
        setContentView(binding.root)

    // 변수 선언
        // ctgr 관련 변수
        val unclassifyCtgr = Ctgr(0, "미분류", 11111111, 0)
        val ctgrAddBtn = Ctgr(null,"+",11111111, 0)
        val deleteBtn = Ctgr(-1,"휴지통",11111111, 0)

        // 검색 및 정렬 어댑터 관련 변수
        val recyclerAdapter = RecyclerAdapter(this)

        // 검색 및 정렬 관련 변수
        var where = "제목+내용"          // sql where 조건
        var orderby = "최신순"          // sql orderby 조건
        var keyword = ""               // sql where의 keyword
        var conditionList1: MutableList<String> = arrayListOf("제목+내용", "제목", "내용")
        val conditionList2: MutableList<String> = arrayListOf("최신순", "오래된순")

        // display 조정 변수
        val dividerItemDecoration = DividerItemDecoration(binding.recyclerSearch.context, LinearLayoutManager(this).orientation)
        val display = this.applicationContext?.resources?.displayMetrics
        val deviceHeight = display?.heightPixels
        val layoutParams1 = binding.recyclerSearch.layoutParams
        val layoutParams2 = binding.emptyText4.layoutParams

        // 기타
        var flag = false
        var fontSize = intent.getStringExtra("fontSize")

        // 검색 관련 어댑터 초기화
        recyclerAdapter.helper = helper
        binding.recyclerSearch.addItemDecoration(dividerItemDecoration)
        binding.recyclerSearch.adapter = recyclerAdapter
        showDataList(recyclerAdapter, keyword, where, orderby)
        layoutParams1.height = deviceHeight?.times(0.81)!!.toInt()
        layoutParams2.height = deviceHeight?.times(0.81)!!.toInt()
        binding.recyclerSearch.layoutParams = layoutParams1
        binding.emptyText4.layoutParams = layoutParams2

        // ctgr뷰 어댑터 초기화
        adapter = RecyclerAdapter(this)
        adapter.callbackListener = this
        adapter.vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        adapter.helper = helper
        adapter.listData = helper.selectCtgrList().toMutableList()
        adapter.fontSize = intent.getStringExtra("fontSize")
        adapter.vibration = intent.getStringExtra("vibration")
        recyclerAdapter.fontSize = fontSize
        if (helper.isUnknownMemoExist()){
            adapter.listData.add(0,unclassifyCtgr)
        }
        adapter.listData.add(ctgrAddBtn)
        adapter.listData.add(deleteBtn)

        // helper.selectMemo()의 리턴값인 리스트를 통째로 listData 리스트에 넣음
        binding.recyclerCtgr2.adapter = adapter

        // 화면에서 보여줄 RecyclerView인 recyclerMemo의 어댑터로 위에서 만든 adapter를 지정
        binding.recyclerCtgr2.layoutManager = GridLayoutManager(this, 2)

        // <"제목", "내용", "제목+내용">
        if(fontSize.equals("ON")) {
            binding.spinner3.adapter = ArrayAdapter(this, R.layout.spinner_layout, conditionList1)
            binding.keyword.textSize = 20f
        } else binding.spinner3.adapter =  ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, conditionList1)
        binding.spinner3.onItemSelectedListener = object: AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {}
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                // where 조건 변경
                where = binding.spinner3.getItemAtPosition(position).toString()
            }
        }

        // <"최신순", "오래된순">
        if(fontSize.equals("ON"))  binding.spinner5.adapter = ArrayAdapter(this, R.layout.spinner_layout, conditionList2)
        else binding.spinner5.adapter = ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, conditionList2)
        binding.spinner5.onItemSelectedListener = object: AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {}
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                // order by 조건 바꿔서 select
                orderby = binding.spinner5.getItemAtPosition(position).toString()
                recyclerAdapter.listData.clear()
                showDataList(recyclerAdapter, keyword, where, orderby)

            }
        }
        // 리스너
        binding.btnFilter.setOnClickListener {
            if (flag == false) {
                binding.spinner5.visibility = View.VISIBLE
                binding.spinner3.visibility = View.VISIBLE
                binding.recyclerCtgr2.margin(top = 48F)
                binding.recyclerSearch.margin(top = 60F)
                binding.emptyText4.margin(top = 60F)
                layoutParams1.height = deviceHeight.times(0.76).toInt()
                layoutParams2.height = deviceHeight.times(0.76).toInt()
                binding.recyclerSearch.layoutParams = layoutParams1
                binding.emptyText4.layoutParams = layoutParams2
                flag = true
            } else {
                binding.spinner5.visibility = View.GONE
                binding.spinner3.visibility = View.GONE
                binding.recyclerCtgr2.margin(top = 0F)
                binding.recyclerSearch.margin(top = 20F)
                binding.emptyText4.margin(top = 20F)
                layoutParams1.height = deviceHeight.times(0.81).toInt()
                layoutParams2.height = deviceHeight.times(0.81).toInt()
                binding.recyclerSearch.layoutParams = layoutParams1
                binding.emptyText4.layoutParams = layoutParams2
                flag = false
            }
        }

        binding.viewCtgrLayout.viewTreeObserver.addOnGlobalLayoutListener {
            if (binding.keyword.text!!.isNotEmpty()) {
                binding.recyclerSearch.visibility = View.VISIBLE
                binding.recyclerCtgr2.visibility = View.INVISIBLE
            } else {
                binding.recyclerCtgr2.visibility = View.VISIBLE
                binding.recyclerSearch.visibility = View.INVISIBLE
                binding.emptyText4.visibility = View.INVISIBLE
            }
        }

        binding.keyword.doOnTextChanged { _, _, _, _ ->
            keyword = binding.keyword.text.toString()
            recyclerAdapter.listData.clear()
            showDataList(recyclerAdapter, keyword, where, orderby)
            false
        }

        // 광고
        MobileAds.initialize(this) {}
        val mAdView = findViewById<AdView>(R.id.sizeup)
        val adRequest = AdRequest.Builder().build()
        mAdView.loadAd(adRequest)
    }

    override fun onRestart() {
        super.onRestart()
        val ctgrAddBtn = Ctgr(null,"+",11111111, 0)
        val unclassifyCtgr = Ctgr(0, "미분류", 11111111, 0)
        val deleteBtn = Ctgr(-1,"휴지통",11111111, 0)
        if (!helper.isUnknownMemoExist()){
            adapter.listData = helper.selectCtgrList().toMutableList()
            adapter.listData.add(ctgrAddBtn)
            adapter.listData.add(deleteBtn)
        } else{
            adapter.listData = helper.selectCtgrList().toMutableList()
            adapter.listData.add(ctgrAddBtn)
            adapter.listData.add(0,unclassifyCtgr)
            adapter.listData.add(deleteBtn)
        }
        adapter.notifyDataSetChanged()
    }

    override fun fragmentOpen(item: String, ctgridx: String?) {
        if(item == "+"){
            CtgrAddFragment(this).show(supportFragmentManager, "CtgrAdd")
        } else if (item == "delete@#"){
            val ctgrDeleteFragment = CtgrDeleteFragment(this)
            val bundle:Bundle = Bundle()
            bundle.putString("Ctgridx",ctgridx)
            ctgrDeleteFragment.arguments = bundle
            ctgrDeleteFragment.show(supportFragmentManager, "DeleteFragment1")
        }
    }

    // ctgr 추가
    override fun addCtgr(ctgrName: String) {
        val ctgr = Ctgr(null,ctgrName,System.currentTimeMillis(), 0)
        val unclassifyCtgr = Ctgr(0, "미분류", 11111111, 0)
        val ctgrAddBtn = Ctgr(null,"+",11111111, 0)
        val deleteBtn = Ctgr(-1,"휴지통",11111111, 0)

        // 첫 Ctgr의 이름이 "미분류" 라면 미분류와 + 버튼 사이에 존재 아니라면 0번째 부터
        if(ctgrName != "미분류" && ctgrName != "delete@#" && ctgrName != "+" && ctgrName != "휴지통"){
            if (!helper.checkDuplicationCtgr(ctgrName)){
                helper.insertCtgr(ctgr)
                adapter.listData = helper.selectCtgrList() as MutableList<Any>
                if (helper.isUnknownMemoExist()){
                    adapter.listData.add(0,unclassifyCtgr)
                }
                adapter.listData.add(ctgrAddBtn)
                adapter.listData.add(deleteBtn)
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

    // ctgr 삭제
    override fun deleteCtgr(ctgridx: String) {
        super.deleteCtgr(ctgridx)
        val unclassifyCtgr = Ctgr(0, "미분류", 11111111, 0)
        val ctgrAddBtn = Ctgr(null,"+",11111111, 0)
        val deleteBtn = Ctgr(-1,"휴지통",11111111, 0)

        helper.deleteCtgr(ctgridx)
        adapter.listData = helper.selectCtgrList() as MutableList<Any>
        if (helper.isUnknownMemoExist()){
            adapter.listData.add(0,unclassifyCtgr)
        }
        adapter.listData.add(ctgrAddBtn)
        adapter.listData.add(deleteBtn)
        adapter.notifyDataSetChanged()
    }

    // ctgr 이동
    override fun moveCtgrList(oldctgr: Long, ctgr: Long){
        var sortedList = helper.selectMemoList(oldctgr.toString()).sortedBy { it.priority }
        for( memo in sortedList){
            helper.moveContent(memo, ctgr)
        }
        deleteCtgr(oldctgr.toString())
        adapter.notifyDataSetChanged()
    }

    // 키보드 관련
    override fun onTouchEvent(event: MotionEvent): Boolean {
        val imm: InputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(currentFocus?.windowToken, 0)
        return true
    }
    override fun openKeyBoard(view: View) {
        val imm: InputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(view,0)
    }
    override fun closeKeyBoard() {
        val imm: InputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(currentFocus?.windowToken, 0)
    }

    // 검색 결과
    fun showDataList(recyclerAdapter: RecyclerAdapter, keyword: String, where: String, orderby: String) {
        val data = helper.selectSearchList(keyword, where, orderby)
        Log.d("test다", "$data")
        recyclerAdapter.listData.addAll(helper.selectSearchList(keyword, where, orderby))
        if(data.isEmpty()) {
            binding.recyclerSearch.visibility = View.INVISIBLE
            binding.emptyText4.visibility = View.VISIBLE
        } else {
            binding.recyclerSearch.visibility = View.VISIBLE
            binding.emptyText4.visibility = View.INVISIBLE
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

