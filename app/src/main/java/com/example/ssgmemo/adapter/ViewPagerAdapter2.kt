package com.example.ssgmemo.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.ssgmemo.databinding.RecyclerSettingImageBinding


class ViewPagerAdapter2() : RecyclerView.Adapter<ViewPagerAdapter2.Holder>() {
    var listData = mutableListOf<Int>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) : Holder {
        var binding: RecyclerSettingImageBinding = RecyclerSettingImageBinding.inflate(LayoutInflater.from(parent.context), parent, false)

        return Holder(binding)
    }

    override fun getItemCount(): Int = listData.size

    override fun onBindViewHolder(holder: Holder, position: Int) {
        val image: Int = listData.get(position) as Int
        holder.setImage(image)
    }

    inner class Holder(val binding: RecyclerSettingImageBinding): RecyclerView.ViewHolder(binding?.root!!) {
        fun setImage(image: Int) {
            binding.descriptionImage.setImageResource(image)
        }
    }
}