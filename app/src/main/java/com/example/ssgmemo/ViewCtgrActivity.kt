package com.example.ssgmemo

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import com.example.ssgmemo.databinding.ActivityViewCtgrBinding
import java.security.Provider.Service

class ViewCtgrActivity : AppCompatActivity(), CallbackListener {
    private lateinit var binding: ActivityViewCtgrBinding
    val helper = SqliteHelper(this, "ssgMemo", 1)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityViewCtgrBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val adapter = RecyclerAdapter(this,this)
        adapter.helper = helper
        val unknownCtgr = Ctgr(0, "미분류", 11111111)
        adapter.listData = helper.selectCtgrList().toMutableList()
        adapter.fontSize = intent.getStringExtra("fontSize")

        if (helper.isUnknownMemoExist()){
            adapter.listData.add(unknownCtgr)
        }
        // helper.selectMemo()의 리턴값인 리스트를 통째로 listData 리스트에 넣음
        binding.recyclerCtgr2.adapter = adapter
        // 화면에서 보여줄 RecyclerView인 recyclerMemo의 어댑터로 위에서 만든 adapter를 지정
        binding.recyclerCtgr2.layoutManager = GridLayoutManager(this, 2)
        if(adapter.listData.isEmpty()){
            binding.msgCtgr.visibility = View.VISIBLE
        }
    }

    override fun callmsg() {
        binding.msgCtgr.visibility = View.VISIBLE
    }

    override fun callback(cidx: Long) {
        TODO("Not yet implemented")
    }
}