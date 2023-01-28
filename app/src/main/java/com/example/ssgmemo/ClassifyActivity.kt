package com.example.ssgmemo

import android.content.Context
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.viewpager2.widget.ViewPager2
import com.example.ssgmemo.databinding.ActivityClassifyBinding

class ClassifyActivity : AppCompatActivity(), CallbackListener {
    lateinit var binding: ActivityClassifyBinding
    val helper = SqliteHelper(this, "ssgMemo", 1)
    var midx: Long? = null
    var pagerAdapter: ViewPagerAdapter? = null // pagerAdapter 생성
    var memoList: MutableList<Memo>? = null
    var memoList2: MutableList<Memo>? = null
    var tmp_position: Int = 0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityClassifyBinding.inflate(layoutInflater) // 바인딩
        setContentView(binding.root)

        // < 메모 list >
        pagerAdapter = ViewPagerAdapter()
        memoList = helper.selectUnclassifiedMemoList() // 분류되지 않은 memoList 가져오기
        pagerAdapter!!.listData.addAll(memoList!!)
        binding.viewpager.adapter = pagerAdapter // memoList 뷰페이저에 pagerAdapter 등록
        binding.viewpager.registerOnPageChangeCallback( object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                if(memoList2 != null && memoList2!!.isNotEmpty()) {
                    midx = memoList2!![position].idx
                    tmp_position = position

                } else {
                    midx = memoList!![position].idx
                    tmp_position = position
                }
                // Log.d("midx", "페이지 변경됨, midx : $midx")
                // Log.d("midx", "페이지 변경됨, tmp_position : $tmp_position")
            }

            override fun onPageScrollStateChanged(state: Int) {
                super.onPageScrollStateChanged(state)
            }

            override fun onPageScrolled(position: Int,
                                        positionOffset: Float,
                                        positionOffsetPixels: Int) {
                super.onPageScrolled(position, positionOffset, positionOffsetPixels)
            }
        })


        // < 카테고리 list >
        val adapter = RecyclerAdapter(this, this)
        adapter.helper = helper
        adapter.listData.addAll(helper.selectCtgrList())
        // helper.selectMemo()의 리턴값인 리스트를 통째로 listData 리스트에 넣음
        binding.recyclerCtgr1.adapter = adapter
        // 화면에서 보여줄 RecyclerView인 recyclerMemo의 어댑터로 위에서 만든 adapter를 지정
        binding.recyclerCtgr1.layoutManager = GridLayoutManager(this, 4)

        binding.btnSave.setOnClickListener {
            if (binding.ctgrName.text.toString().isNotEmpty()) {
                val ctgr = Ctgr(null, binding.ctgrName.text.toString(), System.currentTimeMillis()) // [수정희망]
                // ctgr 테이블에 저장할 레코드를 Ctgr형 인스턴스 ctgr로 생성
                helper.insertCtgr(ctgr)    // 새로운 레코드를 ctgr 테이블에 insert

                adapter.listData.clear()
                // 새로운 레코드가 추가되면 새롭게 select 해오므로 기존에 리스트에 남아있는 값들을 없앰
                adapter.listData.addAll(helper.selectCtgrList())
                adapter.notifyDataSetChanged()
                binding.ctgrName.setText("")
            }
        }
    }

    override fun callback(cidx: Long) {
        val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator;
        vibrator.vibrate(VibrationEffect.createOneShot(200, 50));

        if (memoList!!.isNotEmpty()) {
            var priority:Int = if (helper.checkTop(cidx.toInt()) == null) 0 else helper.checkTop(cidx.toInt())?.plus(1)!!
            helper.updateMemoCtgr(midx, cidx, priority)
            pagerAdapter!!.listData.clear()
            memoList2 = helper.selectUnclassifiedMemoList()
            pagerAdapter!!.listData.addAll(memoList2!!)
            pagerAdapter!!.notifyDataSetChanged()
            // Log.d("midx", "changed midx : $midx")

            if(memoList2 != null && memoList2!!.isNotEmpty()) {
                if (memoList2!!.size > tmp_position) { // 마지막 메모가 아니라면
                    // Log.d("midx", "마지막메모x")
                } else { // 마지막 메모라면
                    tmp_position = 0
                    // Log.d("midx", "마지막메모o")
                }
                midx = memoList2!![tmp_position].idx
                // Log.d("midx", "tmp_position : $tmp_position")
                // Log.d("midx", "midx : $midx")
            }
        }
    }
}