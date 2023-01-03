package com.example.ssgmemo

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.get
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.example.ssgmemo.databinding.RecyclerCtgrViewItemBinding
import com.example.ssgmemo.databinding.RecyclerViewItemBinding
import java.text.SimpleDateFormat

class RecyclerAdapter: RecyclerView.Adapter<RecyclerAdapter.Holder>() {
	var listData = mutableListOf<Ctgr>()
	var helper: SqliteHelper? = null
	var parentName : String? = null
	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
		parentName = parent.resources.getResourceEntryName(parent.id).toString()
		Log.d("text","$parentName")
		var binding: ViewBinding? =null
		if (parentName.equals("recyclerCtgr1")){
			binding =
				RecyclerViewItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
		} else if (parentName.equals("recyclerCtgr2")){
			binding =
				RecyclerCtgrViewItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
		}

		return Holder(binding)
	}

	override fun onBindViewHolder(holder: Holder, position: Int) {
		val ctgr = listData.get(position)
		holder.setCtgr(ctgr)
	}

	override fun getItemCount(): Int {
		return listData.size
	}

	inner class Holder(val binding: ViewBinding?): RecyclerView.ViewHolder(binding?.root!!) {

		var mCtgr: Ctgr? = null
		fun setCtgr(ctgr: Ctgr) {
			if (parentName.equals("recyclerCtgr1")){
				(binding as RecyclerViewItemBinding).txtCtgr.text = ctgr.name
				binding.imageView.setImageResource(R.drawable.box)
				this.mCtgr = ctgr
			} else if(parentName.equals("recyclerCtgr2")){
				(binding as RecyclerCtgrViewItemBinding).ctgrBtn.text =ctgr.name
				this.mCtgr = ctgr
			}

			// 메모 삭제시 추가되는 부분으로 삭제할 메모 객체를 mMemo에 저장
		}
	}
}
