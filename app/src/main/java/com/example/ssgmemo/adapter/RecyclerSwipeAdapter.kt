package com.example.ssgmemo.adapter

import android.content.Context
import android.content.Intent
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.example.ssgmemo.Memo
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
    override fun onBindViewHolder(holder: Holder, position: Int, payloads: MutableList<Any>) {
        super.onBindViewHolder(holder, position, payloads)
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        binding =
            RecyclerViewMemoBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return Holder(binding as RecyclerViewMemoBinding)
    }
    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.bind(itemList[position])
    }

    override fun getItemCount(): Int {
        return itemList.size
    }
    inner class Holder(val binding: RecyclerViewMemoBinding): RecyclerView.ViewHolder(binding?.root!!) {
        fun bind(memo: Memo) {
            binding.searchTitle2.text = memo.title
            binding.searchContent2.text = memo.content

            val t_dateFormat = SimpleDateFormat("M월 d일", Locale("ko", "KR"))
            val str_date = t_dateFormat.format(Date(memo.datetime))
            binding.searchDate2.text = str_date

            if (fontSize!!.equals("ON")) {
                binding.searchTitle2.textSize = 24f
                binding.searchContent2.textSize = 20f
                binding.searchDate2.textSize = 20f
            }

            binding.memoItem.setOnClickListener {
                val intent = Intent(context, EditActivity::class.java)
                intent.putExtra("memoIdx", "${memo.idx}")
                intent.putExtra("fontSize", "$fontSize")
                intent.putExtra("vibration", "$vibration")
                context.startActivity(intent)
            }

            binding.task.setOnClickListener {
                // 프레그먼트 오픈
                callbackListener.fragmentOpen(memo.ctgr!!,memo.idx.toString())
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
            itemList[to].priority = itemList[to].priority?.plus(1)
            itemList[from].priority = itemList[from].priority?.minus(1)
        }else if (priority_gap == -1){
            itemList[to].priority = itemList[to].priority?.minus(1)
            itemList[from].priority = itemList[from].priority?.plus(1)
        }
        val data = itemList[from]
        helper.movePriority(itemList[from], itemList[to])
        itemList.removeAt(from)
        itemList.add(to,data)

        // from에서 to 위치로 아이템 위치 변경
        notifyItemMoved(from,to)
        return true
    }
    // 아이템 스와이프되면 호출되는 메소드
//    override fun onItemSwipe(position: Int) {
//        // 리스트 아이템 삭제
//        helper.deleteContent(itemList[position])
//        itemList.removeAt(position)
//        notifyItemRemoved(position)
//    }

}
