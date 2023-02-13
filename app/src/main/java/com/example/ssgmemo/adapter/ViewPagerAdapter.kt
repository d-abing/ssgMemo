package com.example.ssgmemo.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.example.ssgmemo.Memo
import com.example.ssgmemo.databinding.RecyclerMemolistItemBinding


class ViewPagerAdapter() : RecyclerView.Adapter<ViewPagerAdapter.Holder>() {
    var listData = mutableListOf<Memo>()
    var fontSize: String? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) : Holder {
        var binding: ViewBinding = RecyclerMemolistItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)

        return Holder(binding)
    }

    override fun getItemCount(): Int = listData.size

    override fun onBindViewHolder(holder: Holder, position: Int) {
        val memo: Memo = listData.get(position) as Memo
        holder.setMemo(memo)
    }

    inner class Holder(val binding: ViewBinding): RecyclerView.ViewHolder(binding?.root!!) {
        fun setMemo(memo: Memo) {
            (binding as RecyclerMemolistItemBinding).memoTitle.text = memo.title
            binding.memoContent.text = memo.content

            if (fontSize.equals("ON")) {
                binding.memoTitle.textSize = 40f
                binding.memoContent.textSize = 30f
            }
        }
    }
}