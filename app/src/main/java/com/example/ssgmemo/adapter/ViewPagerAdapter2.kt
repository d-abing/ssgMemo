package com.example.ssgmemo.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.example.ssgmemo.Memo
import com.example.ssgmemo.databinding.RecyclerDescriptionImageItemBinding
import com.example.ssgmemo.databinding.RecyclerMemolistItem2Binding
import com.example.ssgmemo.databinding.RecyclerMemolistItemBinding


class ViewPagerAdapter2() : RecyclerView.Adapter<ViewPagerAdapter2.Holder>() {
    var listData = mutableListOf<Int>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) : Holder {
        var binding: RecyclerDescriptionImageItemBinding = RecyclerDescriptionImageItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)

        return Holder(binding)
    }

    override fun getItemCount(): Int = listData.size

    override fun onBindViewHolder(holder: Holder, position: Int) {
        val image: Int = listData.get(position) as Int
        holder.setImage(image)
    }

    inner class Holder(val binding: RecyclerDescriptionImageItemBinding): RecyclerView.ViewHolder(binding?.root!!) {
        fun setImage(image: Int) {
            binding.descriptionImage.setImageResource(image)
        }
    }
}