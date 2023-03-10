package com.example.ssgmemo.adapter

import android.animation.ObjectAnimator
import android.content.Context
import android.content.Intent
import android.os.VibrationEffect
import android.util.Log
import android.os.Vibrator
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.RadioButton
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.example.ssgmemo.Memo
import com.example.ssgmemo.R
import com.example.ssgmemo.SqliteHelper
import com.example.ssgmemo.callback.CallbackListener
import com.example.ssgmemo.common.EditActivity
import com.example.ssgmemo.callback.ItemTouchHelperListener
import com.example.ssgmemo.databinding.RecyclerViewMemoBinding
import java.text.SimpleDateFormat
import java.util.*

class RecyclerSwipeAdapter(val context: Context): RecyclerView.Adapter<RecyclerSwipeAdapter.Holder>(),
    ItemTouchHelperListener {
    lateinit var helper: SqliteHelper
    lateinit var callbackListener: CallbackListener
    lateinit var itemList: MutableList<Memo>
    var fontSize: String = ""
    var vibration: String = ""
    var vibrator: Vibrator? = null
    lateinit var  binding: ViewBinding
    var mode = 0
    var selectAll = false
    var selectedList : MutableList<Memo> = mutableListOf()


    override fun onBindViewHolder(holder: Holder, position: Int, payloads: MutableList<Any>) {
        super.onBindViewHolder(holder, position, payloads)
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        binding =
            RecyclerViewMemoBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return Holder(binding as RecyclerViewMemoBinding)
    }
    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.setIsRecyclable(false)
        holder.bind(itemList[position])
    }

    override fun getItemCount(): Int {
        return itemList.size
    }

    inner class Holder(val binding: RecyclerViewMemoBinding): RecyclerView.ViewHolder(binding?.root!!) {
        fun bind(memo: Memo) {
            // 변수 선언
            val t_dateFormat = SimpleDateFormat("M월 d일", Locale("ko", "KR"))
            val str_date = t_dateFormat.format(Date(memo.datetime))
            var toggle_checked: Boolean = false
            // 아이템 초기화
            binding.searchTitle2.text = memo.title
            binding.searchContent2.text = memo.content
            binding.btnMerge.visibility = View.VISIBLE
            binding.toggleButton.visibility = View.GONE
            binding.searchDate2.text = str_date
            // 토글 버튼 클릭 리스너 초기화
            binding.toggleButton.setOnClickListener {
                binding.toggleButton.isChecked = !toggle_checked
                if (!toggle_checked) {
                    memo.sel = true
                    selectedList.add(memo)
                    if (selectedList.size == itemList.size) {
                        selectAll = true
                    }
                } else {
                    memo.sel = false
                    selectedList.remove(memo)
                    if (selectedList.isEmpty()) {
                        selectAll = false
                    }
                }
                toggle_checked = !toggle_checked
            }
            // 메모가 전체 선택일 때 또는 하나 선택 되었을 때 재활용해도 체크 유지
            if(memo.sel || selectAll){
                binding.toggleButton.isChecked = true
                toggle_checked = true
            }
            // 선택 모드일 때 재사용하는 아이템들 초기화
            if(mode == 1){
                binding.memoItem.translationX = 150f
                binding.btnMerge.visibility = View.GONE
                binding.toggleButton.visibility = View.VISIBLE
                // 아이템 클릭 리스너 초기화
                binding.memoItem.setOnClickListener {
                    binding.toggleButton.isChecked = !toggle_checked
                    if (!toggle_checked) {
                        memo.sel = true
                        selectedList.add(memo)
                        if (selectedList.size == itemList.size) {
                            selectAll = true
                        }
                    } else {
                        memo.sel = false
                        selectedList.remove(memo)
                        if (selectedList.isEmpty()) {
                            selectAll = false
                        }
                    }
                    toggle_checked = !toggle_checked
                }
            } else{
                // 아이템 클릭 리스너 초기화
                binding.memoItem.setOnClickListener {
                    val intent = Intent(context, EditActivity::class.java)
                    intent.putExtra("memoIdx", "${memo.idx}")
                    intent.putExtra("fontSize", "$fontSize")
                    intent.putExtra("vibration", "$vibration")
                    context.startActivity(intent)
                }
            }

            if (memo.priority!! > helper.getTopPriority(memo.ctgr) - 10) {
                binding.memoItem.setBackgroundResource(R.drawable.memoback2)
            } else {
                binding.memoItem.setBackgroundResource(R.drawable.memoback)
            }

            if (fontSize!!.equals("ON")) {
                binding.searchTitle2.textSize = 24f
                binding.searchContent2.textSize = 20f
                binding.searchDate2.textSize = 20f
            }

            binding.btnMerge.setOnClickListener {
                //머지
            }
        }
    }

    override fun onItemMove(from: Int, to: Int) : Boolean {

        if(vibration.equals("ON")) {
            vibrator?.vibrate(VibrationEffect.createOneShot(200, 50))
        }

        //리스트 갱신
        var priority_gap =itemList[from].priority!! - itemList[to].priority!!
        if (priority_gap == 1){
            itemList[to].priority = itemList[to].priority.plus(1)
            itemList[from].priority = itemList[from].priority.minus(1)
        }else if (priority_gap == -1){
            itemList[to].priority = itemList[to].priority.minus(1)
            itemList[from].priority = itemList[from].priority.plus(1)
        }
        val data = itemList[from]
        helper.movePriority(itemList[from], itemList[to])
        itemList.removeAt(from)
        itemList.add(to,data)

        // from에서 to 위치로 아이템 위치 변경
        notifyItemMoved(from,to)
        return true
    }
    override fun onItemDrag() {
        if(vibration.equals("ON")) {
            vibrator?.vibrate(VibrationEffect.createOneShot(200, 50))
        }
    }

}