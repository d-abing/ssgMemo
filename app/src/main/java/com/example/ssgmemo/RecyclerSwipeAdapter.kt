package com.example.ssgmemo

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.example.ssgmemo.databinding.RecyclerCtgrViewItemBinding

class RecyclerSwipeAdapter(): RecyclerView.Adapter<RecyclerSwipeAdapter.Holder>(),ItemTouchHelperListener {
    lateinit var helper: SqliteHelper
    lateinit var itemList: MutableList<Any>
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerSwipeAdapter.Holder {
        var binding =
            RecyclerCtgrViewItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return Holder(binding)
    }
    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.bind(itemList.get(position))
    }

    override fun getItemCount(): Int {
        return itemList.size
    }
    inner class Holder(val binding: RecyclerCtgrViewItemBinding): RecyclerView.ViewHolder(binding?.root!!){
        fun bind(item: Any) {
            if (item is Ctgr){
                binding.txtCtgr2.setText(item.name)
            } else {
                item as Memo
            }

        }
    }

    override fun onItemMove(from: Int, to: Int) : Boolean {
        val data = itemList[from]
        //리스트 갱신
        itemList.removeAt(from)
        itemList.add(to,data)

        // from에서 to 위치로 아이템 위치 변경
        notifyItemMoved(from,to)
        return true
    }

    // 아이템 스와이프되면 호출되는 메소드
    override fun onItemSwipe(position: Int) {
        // 리스트 아이템 삭제
        itemList.removeAt(position)
        notifyItemRemoved(position)
    }
}