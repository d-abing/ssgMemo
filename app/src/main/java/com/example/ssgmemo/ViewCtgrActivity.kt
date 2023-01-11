package com.example.ssgmemo

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.GridLayoutManager
import com.example.ssgmemo.databinding.ActivityViewCtgrBinding

class ViewCtgrActivity : AppCompatActivity() {
    private lateinit var binding: ActivityViewCtgrBinding
    val helper = SqliteHelper(this, "ssgMemo", 1)

    @SuppressLint("NotifyDataSetChanged")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityViewCtgrBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val adapter = RecyclerAdapter(this)
        val unknownCtgr = Ctgr(0, "미분류", 11111111,null)

        adapter.helper = helper
        binding.textView2
        adapter.listData.addAll(helper.selectCtgrList())
        if (helper.isUnknownMemoExist()){
            adapter.listData.add(unknownCtgr)
        }
        // helper.selectMemo()의 리턴값인 리스트를 통째로 listData 리스트에 넣음
        binding.recyclerCtgr2.adapter = adapter
        // 화면에서 보여줄 RecyclerView인 recyclerMemo의 어댑터로 위에서 만든 adapter를 지정
        binding.recyclerCtgr2.layoutManager = GridLayoutManager(this, 2)

        binding.button.setOnClickListener {
            val adapter = RecyclerAdapter(this)
            adapter.test()

            binding.recyclerCtgr2.adapter = adapter
            // 화면에서 보여줄 RecyclerView인 recyclerMemo의 어댑터로 위에서 만든 adapter를 지정
            binding.recyclerCtgr2.layoutManager = GridLayoutManager(this, 2)
            Log.d("결과","00")
            adapter.notifyDataSetChanged()
        }

    }
}