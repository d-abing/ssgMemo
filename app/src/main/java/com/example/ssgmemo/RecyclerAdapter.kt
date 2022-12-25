package com.example.ssgmemo

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.ssgmemo.databinding.RecyclerViewItemBinding
import java.text.SimpleDateFormat

class RecyclerAdapter: RecyclerView.Adapter<RecyclerAdapter.Holder>() {
	var listData = mutableListOf<Ctgr>()
	var helper: SqliteHelper? = null
	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
		val binding =
			RecyclerViewItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
		return Holder(binding)
	}

	override fun onBindViewHolder(holder: Holder, position: Int) {
		val ctgr = listData.get(position)
		holder.setCtgr(ctgr)
	}

	override fun getItemCount(): Int {
		return listData.size
	}

	inner class Holder(val binding: RecyclerViewItemBinding): RecyclerView.ViewHolder(binding.root) {

		var mCtgr: Ctgr? = null
		fun setCtgr(ctgr: Ctgr) {
			binding.txtCtgr.text = "${ctgr.name}"
			binding.imageView.setImageResource(R.drawable.box)
			this.mCtgr = ctgr
			// 메모 삭제시 추가되는 부분으로 삭제할 메모 객체를 mMemo에 저장
		}
	}
}
