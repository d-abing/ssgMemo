package com.example.ssgmemo

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.GridLayoutManager
import com.example.ssgmemo.databinding.ActivityViewCtgrBinding

class ViewCtgrActivity : AppCompatActivity() {
    private lateinit var binding: ActivityViewCtgrBinding
    val helper = SqliteHelper(this, "ssgMemo", 1)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityViewCtgrBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // list
        val memoList = helper.selectMemoList()
        val adapter = RecyclerAdapter()
        adapter.helper = helper

        adapter.listData.addAll(helper.selectCtgrList())
        // helper.selectMemo()의 리턴값인 리스트를 통째로 listData 리스트에 넣음
        binding.recyclerCtgr2.adapter = adapter
        // 화면에서 보여줄 RecyclerView인 recyclerMemo의 어댑터로 위에서 만든 adapter를 지정
        binding.recyclerCtgr2.layoutManager = GridLayoutManager(this, 2)


    }
}