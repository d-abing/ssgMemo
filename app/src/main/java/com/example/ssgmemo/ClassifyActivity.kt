package com.example.ssgmemo

import android.R
import android.content.Context
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.text.method.ScrollingMovementMethod
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.GridLayoutManager
import com.example.ssgmemo.databinding.ActivityClassifyBinding
import com.example.ssgmemo.databinding.RecyclerViewItemBinding

class ClassifyActivity : AppCompatActivity() {
    private lateinit var binding: ActivityClassifyBinding
    val helper = SqliteHelper(this, "ssgMemo", 1)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityClassifyBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator;
        val adapter = RecyclerAdapter()

        // 메모 list

        var memoList : MutableList<Memo> = helper.selectMemoList()
        var index = 0

        binding.btnPrevious.visibility = View.INVISIBLE // 첫 글에서는 이전으로 가기 버튼 안보이게
        if (memoList.size <= 1) binding.btnNext.visibility = View.INVISIBLE // 글이 하나 이하면 다음으로 가기 버튼 안보이게

        if ( memoList.isNotEmpty() ) { // 메모리스트가 비어있지 않으면

            binding.memoTitle.text = memoList.elementAt(index).title  // memoList의 첫 번째 글 제목이 표시되도록
            binding.memoContent.text = memoList.elementAt(index).content // memoList의 첫 번째 글 내용이 표시되도록

            binding.btnNext.setOnClickListener { // 다음 버튼을 누르면
                vibrator.vibrate(VibrationEffect.createOneShot(200, 50)); // 진동
                index++
                if (index <= memoList.size - 1 ) {
                    binding.btnPrevious.visibility = View.VISIBLE // 이전으로 가기 버튼 보이게
                    binding.memoTitle.text = memoList.elementAt(index).title // 다음 글 제목 보여주기
                    binding.memoContent.text = memoList.elementAt(index).content // 다음 내용 보여주기

                    helper.updateItemCtgr(memoList.elementAt(index).idx)    // 새로운 레코드를 ctgr 테이블에 insert
                    adapter.listData.clear()
                    adapter.listData.addAll(helper.selectCtgrList())

                    if (index == memoList.size - 1) {
                        binding.btnNext.visibility = View.INVISIBLE // 마지막 글일 경우 다음으로 가기 버튼 안보이게
                    }
                }
            }

            binding.btnPrevious.setOnClickListener {
                vibrator.vibrate(VibrationEffect.createOneShot(200, 50)); // 진동
                index--
                binding.btnNext.visibility = View.VISIBLE // 다음으로 가기 버튼 보이게
                if ( memoList.size -1 > index && index >= 0) {
                    binding.memoTitle.text = memoList.elementAt(index).title // 이전 글 제목 보여주기
                    binding.memoContent.text = memoList.elementAt(index).content // 이전 글 내용 보여주기

                    if (index == 0) {
                        binding.btnPrevious.visibility = View.INVISIBLE // 처음 글일 경우 이전으로 가기 버튼 안보이게
                    }
                }
            }

            binding.memoContent.setMovementMethod(ScrollingMovementMethod()) // 글 내용이 길어질 경우 스크롤이 가능하도록 설정
        }


        // 카테고리 list

        adapter.helper = helper

        adapter.listData.addAll(helper.selectCtgrList())
        // helper.selectMemo()의 리턴값인 리스트를 통째로 listData 리스트에 넣음
        binding.recyclerCtgr1.adapter = adapter
        // 화면에서 보여줄 RecyclerView인 recyclerMemo의 어댑터로 위에서 만든 adapter를 지정
        binding.recyclerCtgr1.layoutManager = GridLayoutManager(this, 4)

        binding.btnSave.setOnClickListener {
            if (binding.ctgrName.text.toString().isNotEmpty()) {
                val ctgr = Ctgr(null, binding.ctgrName.text.toString(), System.currentTimeMillis(), memoList.elementAt(0).idx )
                // ctgr 테이블에 저장할 레코드를 Ctgr형 인스턴스 ctgr로 생성
                helper.insertCtgr(ctgr)    // 새로운 레코드를 ctgr 테이블에 insert

                adapter.listData.clear()
                // 새로운 레코드가 추가되면 새롭게 select 해오므로 기존에 리스트에 남아있는 값들을 없앰
                adapter.listData.addAll(helper.selectCtgrList())
                adapter.notifyDataSetChanged()
                binding.ctgrName.setText("")
            }
        }

        binding.btnDel.setOnClickListener {
            helper?.deleteCtgr()   // ctgr 테이블의 레코드 삭제
            adapter.listData.clear()
            adapter.notifyDataSetChanged()    // 어댑터 갱신
        }

    }
}