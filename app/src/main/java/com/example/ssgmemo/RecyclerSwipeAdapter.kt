package com.example.ssgmemo

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.ssgmemo.databinding.RecyclerContentItem1Binding

class RecyclerSwipeAdapter(val context: Context): RecyclerView.Adapter<RecyclerSwipeAdapter.Holder>(),ItemTouchHelperListener {
    lateinit var helper: SqliteHelper
    lateinit var itemList: MutableList<Memo>
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerSwipeAdapter.Holder {
        var binding =
            RecyclerContentItem1Binding.inflate(LayoutInflater.from(parent.context), parent, false)
        return Holder(binding)
    }
    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.bind(itemList[position])
    }

    override fun getItemCount(): Int {
        return itemList.size
    }
    inner class Holder(val binding: RecyclerContentItem1Binding): RecyclerView.ViewHolder(binding?.root!!){
        fun bind(item: Memo) {

            binding.titleItem.text = item.title
            itemView.setOnClickListener {
                val intent = Intent(context, EditActivity::class.java)
                intent.putExtra("memoIdx", "${item.idx}")
                context.startActivity(intent)
            }
        }
    }

    override fun onItemMove(from: Int, to: Int) : Boolean {
        val data = itemList[from]
        //리스트 갱신
        helper.switchPriority(itemList[to],data)
        for(i in itemList){
            Log.d("아ㅓ이템","${i.priority}")
        }
        itemList.removeAt(from)
        itemList.add(to,data)

        // from에서 to 위치로 아이템 위치 변경
        notifyItemMoved(from,to)
        return true
    }

    // 아이템 스와이프되면 호출되는 메소드
    override fun onItemSwipe(position: Int) {
        // 리스트 아이템 삭제
        helper.deleteContent(itemList[position].idx)
        itemList.removeAt(position)
        notifyItemRemoved(position)
    }

}