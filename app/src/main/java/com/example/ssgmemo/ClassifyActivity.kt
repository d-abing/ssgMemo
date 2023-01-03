package com.example.ssgmemo

import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.example.ssgmemo.databinding.ActivityClassifyBinding

class ClassifyActivity : AppCompatActivity() {
    private lateinit var binding: ActivityClassifyBinding
    val helper = SqliteHelper(this, "ssgMemo", 1)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityClassifyBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 메모 list
        val memoList = helper.selectMemoList()
        var index = 0
        if ( memoList.isNotEmpty() ) {
            binding.memoTitle.text = memoList.elementAt(index).title
            binding.btnNext.setOnClickListener {
                index++
                if (index != memoList.size ) {
                    binding.memoTitle.text = memoList.elementAt(index).title
                } else {
                    index--
                    Toast.makeText(this@ClassifyActivity,"마지막 메모입니다.", Toast.LENGTH_SHORT).show()
                }
            }

            binding.btnPrevious.setOnClickListener {
                index--
                if (index != -1) {
                    binding.memoTitle.text = memoList.elementAt(index).title
                } else {
                    index++
                    Toast.makeText(this@ClassifyActivity,"첫 번째 메모입니다.", Toast.LENGTH_SHORT).show()
                }
            }
        }



        // 카테고리 list
        val adapter = RecyclerAdapter()
        adapter.helper = helper

        adapter.listData.addAll(helper.selectCtgrList())
        // helper.selectMemo()의 리턴값인 리스트를 통째로 listData 리스트에 넣음
        binding.recyclerCtgr1.adapter = adapter
        // 화면에서 보여줄 RecyclerView인 recyclerMemo의 어댑터로 위에서 만든 adapter를 지정
        binding.recyclerCtgr1.layoutManager = GridLayoutManager(this, 4)

        binding.btnSave.setOnClickListener {
            if (binding.ctgrName.text.toString().isNotEmpty()) {
                val ctgr = Ctgr(null, binding.ctgrName.text.toString(), System.currentTimeMillis())
                // ctgr 테이블에 저장할 레코드를 Ctgr형 인스턴스 ctgr로 생성
                helper.insertCtgr(ctgr)	// 새로운 레코드를 ctgr 테이블에 insert

                adapter.listData.clear()
                // 새로운 레코드가 추가되면 새롭게 select 해오므로 기존에 리스트에 남아있는 값들을 없앰
                adapter.listData.addAll(helper.selectCtgrList())
                adapter.notifyDataSetChanged()
                binding.ctgrName.setText("")
            }
        }

        binding.btnDel.setOnClickListener {
            helper?.deleteCtgr()	// ctgr 테이블의 레코드 삭제
            adapter.listData.clear()
            adapter.notifyDataSetChanged()		// 어댑터 갱신
        }

        // memomo
        var startX = 0f
        var startY = 0f

        val displayX = windowManager.defaultDisplay.width.toFloat()
        val displayY = windowManager.defaultDisplay.height.toFloat()
        val centerX: Float = displayX / 2
        val centerY: Float = displayY / 2

        binding.memo.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    startX = event.x
                    startY = event.y
                    Log.d("start xy","${startX},${startY}")
                }

                MotionEvent.ACTION_MOVE -> {
                    val movedX:Float= event.x - startX
                    val movedY:Float= event.y - startY

                    v.x = v.x + movedX
                    v.y = v.y + movedY
                    Log.d("event xy","${v.x},${v.y}")
                }
                MotionEvent.ACTION_UP -> {
                }
            }
            true
        }
    }
}