package com.example.ssgmemo

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import com.example.ssgmemo.databinding.ActivitySearchBinding

class SearchActivity : AppCompatActivity(), CallbackListener {
    private lateinit var binding: ActivitySearchBinding
    val helper = SqliteHelper(this, "ssgMemo", 1)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySearchBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val recyclerAdapter = RecyclerAdapter(this, this)
        var where = ""
        var orderby = ""
        var keyword = ""

        var conditionList1: MutableList<String> = arrayListOf("제목", "내용", "제목+내용")
        val conditionList2: MutableList<String> = arrayListOf("최신순", "오래된순")

        binding.spinner4.adapter =  ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, conditionList1)
        binding.spinner4.onItemSelectedListener = object: AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                where = binding.spinner4.getItemAtPosition(position).toString()
            }
        }

        binding.spinner2.adapter = ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, conditionList2)
        binding.spinner2.onItemSelectedListener = object: AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                orderby = binding.spinner2.getItemAtPosition(position).toString()
                Log.d("로그", "확인")
                recyclerAdapter.listData.clear()
                recyclerAdapter.listData.addAll(helper.selectSearchList(keyword, where, orderby))
                recyclerAdapter.notifyDataSetChanged()
                binding.keyword.setText("")

            }
        }



        recyclerAdapter.helper = helper
        recyclerAdapter.listData.addAll(helper.selectSearchList(binding.keyword.text.toString(), where, orderby))
        binding.recyclerSearch.adapter = recyclerAdapter

        binding.imageButton.setOnClickListener{
            if (binding.keyword.text.toString().isNotEmpty()) {
                recyclerAdapter.listData.clear()
                recyclerAdapter.listData.addAll(helper.selectSearchList(binding.keyword.text.toString(), where, orderby))
                recyclerAdapter.notifyDataSetChanged()
                keyword = binding.keyword.text.toString()
                binding.keyword.setText("")
            }
        }
    }

    override fun callback(cidx: Long) {
        TODO("Not yet implemented")
    }
}