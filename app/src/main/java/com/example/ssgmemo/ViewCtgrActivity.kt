package com.example.ssgmemo

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.example.ssgmemo.databinding.ActivityViewCtgrBinding

class ViewCtgrActivity : AppCompatActivity(), CallbackListener {
    private lateinit var binding: ActivityViewCtgrBinding
    val helper = SqliteHelper(this, "ssgMemo", 1)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityViewCtgrBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val adapter = RecyclerSwipeAdapter(this)
        val unknownCtgr = Ctgr(0, "미분류", 11111111)
        val itemTouchHelper = ItemTouchHelper(object :ItemTouchHelper.Callback(){
            override fun getMovementFlags(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder
            ): Int {
                TODO("Not yet implemented")
            }

            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                TODO("Not yet implemented")
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                TODO("Not yet implemented")
            }


        })

        adapter.helper = helper
        binding.textView2
        adapter.itemList = helper.selectCtgrList().toMutableList()
        if (helper.isUnknownMemoExist()){
            adapter.itemList.add(unknownCtgr)
        }
        // helper.selectMemo()의 리턴값인 리스트를 통째로 listData 리스트에 넣음
        binding.recyclerCtgr2.adapter = adapter
        // 화면에서 보여줄 RecyclerView인 recyclerMemo의 어댑터로 위에서 만든 adapter를 지정
        binding.recyclerCtgr2.layoutManager = GridLayoutManager(this, 2)
        itemTouchHelper.attachToRecyclerView(binding.recyclerCtgr2)

//        편집 버튼 클릭시 어뎁터 다시 적용
//        binding.button.setOnClickListener {
//            val adapter = RecyclerAdapter(this,this)
//            adapter.test()
//
//            binding.recyclerCtgr2.adapter = adapter
//            // 화면에서 보여줄 RecyclerView인 recyclerMemo의 어댑터로 위에서 만든 adapter를 지정
//            binding.recyclerCtgr2.layoutManager = GridLayoutManager(this, 2)
//
//
//
//            Log.d("결과","00")
//            adapter.notifyDataSetChanged()
//        }



    }

    override fun callback(cidx: Long) {
        TODO("Not yet implemented")
    }
}