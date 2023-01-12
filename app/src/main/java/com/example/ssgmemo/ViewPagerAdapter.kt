package com.example.ssgmemo

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import androidx.viewpager.widget.PagerAdapter.POSITION_NONE
import com.example.ssgmemo.databinding.MemoListItemBinding


class ViewPagerAdapter() : RecyclerView.Adapter<ViewPagerAdapter.Holder>() {
    var listData = mutableListOf<Memo>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) : Holder {
        var binding: ViewBinding = MemoListItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)

        return Holder(binding)
    }

    override fun getItemCount(): Int = listData.size

    override fun onBindViewHolder(holder: Holder, position: Int) {
        val memo:Memo = listData.get(position) as Memo
        holder.setMemo(memo)
    }

    inner class Holder(val binding: ViewBinding): RecyclerView.ViewHolder(binding?.root!!) {
        fun setMemo(memo: Memo) {
            (binding as MemoListItemBinding).memoTitle.text = memo.title
            binding.memoContent.text = memo.content
        }
    }
}