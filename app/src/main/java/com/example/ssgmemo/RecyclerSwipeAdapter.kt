package com.example.ssgmemo

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.example.ssgmemo.databinding.RecyclerCtgrViewItemBinding

class RecyclerSwipeAdapter(var itemList: MutableList<Any>): RecyclerView.Adapter<RecyclerSwipeAdapter.Holder>() {
    lateinit var helper: SqliteHelper
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
                binding.txtCtgr2.text = item.name
            } else {
                item as Memo
            }

        }
    }


}