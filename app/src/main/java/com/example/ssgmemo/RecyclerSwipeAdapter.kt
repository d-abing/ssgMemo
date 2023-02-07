package com.example.ssgmemo

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.ssgmemo.databinding.RecyclerContentItem1Binding
import com.example.ssgmemo.databinding.RecyclerSearchItemBinding
import java.text.SimpleDateFormat
import java.util.*

class RecyclerSwipeAdapter(val context: Context): RecyclerView.Adapter<RecyclerSwipeAdapter.Holder>(),ItemTouchHelperListener {
    lateinit var helper: SqliteHelper
    lateinit var itemList: MutableList<Memo>
    var fontSize: String? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerSwipeAdapter.Holder {
        var binding =
            RecyclerSearchItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return Holder(binding)
    }
    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.bind(itemList[position])
    }

    override fun getItemCount(): Int {
        return itemList.size
    }
    inner class Holder(val binding: RecyclerSearchItemBinding): RecyclerView.ViewHolder(binding?.root!!) {
        fun bind(memo: Memo) {

            binding.searchTitle.text = memo.title
            binding.searchContent.text = memo.content

            val t_dateFormat = SimpleDateFormat("M월 d일", Locale("ko", "KR"))
            val str_date = t_dateFormat.format(Date(memo.datetime))
            binding.searchDate.text = str_date

            if (fontSize!!.equals("ON")) {
                binding.searchTitle.textSize = 24f
                binding.searchContent.textSize = 20f
                binding.searchDate.textSize = 20f
            }

            itemView.setOnClickListener {
                val intent = Intent(context, EditActivity::class.java)
                intent.putExtra("memoIdx", "${memo.idx}")
                intent.putExtra("fontSize", "$fontSize")
                context.startActivity(intent)
            }
        }
    }

    override fun onItemMove(from: Int, to: Int) : Boolean {

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
    override fun onItemSwipe(position: Int) {
        // 리스트 아이템 삭제
        helper.deleteContent(itemList[position])
        itemList.removeAt(position)
        notifyItemRemoved(position)
    }

}