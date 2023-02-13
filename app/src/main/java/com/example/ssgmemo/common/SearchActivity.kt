package com.example.ssgmemo.common

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import com.example.ssgmemo.SqliteHelper
import com.example.ssgmemo.adapter.RecyclerAdapter
import com.example.ssgmemo.callback.CallbackListener
import com.example.ssgmemo.databinding.ActivitySearchBinding

class SearchActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySearchBinding
    val helper = SqliteHelper(this, "ssgMemo", 1)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySearchBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val recyclerAdapter = RecyclerAdapter(this)
        recyclerAdapter.fontSize =  intent.getStringExtra("fontSize")
        var where = ""          // sql where 조건
        var orderby = ""        // sql orderby 조건
        var keyword = ""        // sql where의 keyword

        var conditionList1: MutableList<String> = arrayListOf("제목", "내용", "제목+내용")
        val conditionList2: MutableList<String> = arrayListOf("최신순", "오래된순")

        // <"제목", "내용", "제목+내용">
        binding.spinner4.adapter =  ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, conditionList1)
        binding.spinner4.onItemSelectedListener = object: AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {}
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                // where 조건 변경
                where = binding.spinner4.getItemAtPosition(position).toString()
            }
        }

        // <"최신순", "오래된순">
        binding.spinner2.adapter = ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, conditionList2)
        binding.spinner2.onItemSelectedListener = object: AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {}
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                // order by 조건 바꿔서 select
                orderby = binding.spinner2.getItemAtPosition(position).toString()
                recyclerAdapter.listData.clear()
                showDataList(recyclerAdapter, keyword, where, orderby)
                recyclerAdapter.notifyDataSetChanged()

            }
        }

        recyclerAdapter.helper = helper
        showDataList(recyclerAdapter, keyword, where, orderby)
        binding.recyclerSearch.adapter = recyclerAdapter

        // 키워드 입력 후 돋보기 클릭
        binding.btnSearch.setOnClickListener{
            keyword = binding.keyword.text.toString()
            if (keyword.isNotEmpty()) {
                recyclerAdapter.listData.clear()
                showDataList(recyclerAdapter, keyword, where, orderby)
                recyclerAdapter.notifyDataSetChanged()
                binding.keyword.setText("")
            }
        }


        binding.btnCancel.setOnClickListener {
            recyclerAdapter.listData.clear()
            keyword = ""
            showDataList(recyclerAdapter, keyword, where, orderby)
            recyclerAdapter.notifyDataSetChanged()
        }
    }

    fun showDataList(recyclerAdapter: RecyclerAdapter, keyword: String, where: String, orderby: String) {
        val data = helper.selectSearchList(keyword, where, orderby)
        recyclerAdapter.listData.addAll(helper.selectSearchList(keyword, where, orderby))
        if(data!!.isEmpty()) {
            binding.recyclerSearch.visibility = View.INVISIBLE
            binding.emptyText2.visibility = View.VISIBLE
        } else {
            binding.recyclerSearch.visibility = View.VISIBLE
            binding.emptyText2.visibility = View.INVISIBLE
        }
    }
}