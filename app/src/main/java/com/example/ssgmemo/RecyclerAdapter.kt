package com.example.ssgmemo

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.location.GnssAntennaInfo.Listener
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.get
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.example.ssgmemo.databinding.RecyclerContentItem1Binding
import com.example.ssgmemo.databinding.RecyclerCtgrViewItemBinding
import com.example.ssgmemo.databinding.RecyclerViewItemBinding
import java.text.SimpleDateFormat

class RecyclerAdapter(val context: Context): RecyclerView.Adapter<RecyclerAdapter.Holder>() {
	var listData = mutableListOf<Any>()
	var helper: SqliteHelper? = null
	var parentName : String? = null



	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
		parentName = parent.resources.getResourceEntryName(parent.id).toString()
		var binding: ViewBinding? =null
		if (parentName.equals("recyclerCtgr1")){
			Log.d("text","$parentName")
			binding =
				RecyclerViewItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
		} else if (parentName.equals("recyclerCtgr2")){
			binding =
				RecyclerCtgrViewItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
		} else if(parentName.equals("recyclerContent1")){
			binding =
				RecyclerContentItem1Binding.inflate(LayoutInflater.from(parent.context), parent, false)

		}


		return Holder(binding)
	}

	override fun onBindViewHolder(holder: Holder, position: Int) {
		if (parentName.equals("recyclerCtgr1") || parentName.equals("recyclerCtgr2")){
			val ctgr:Ctgr = listData.get(position) as Ctgr
			holder.setCtgr(ctgr)
		} else {
			val resultMemo:Memo = listData.get(position) as Memo
			holder.setMemo(resultMemo)
		}
	}

	@SuppressLint("ResourceType")
	override fun getItemCount(): Int {
		return listData.size
	}

	inner class Holder(val binding: ViewBinding?): RecyclerView.ViewHolder(binding?.root!!) {

		fun setCtgr(ctgr: Ctgr) {
			if (parentName.equals("recyclerCtgr1")) {
				(binding as RecyclerViewItemBinding).txtCtgr.text = ctgr.name
				binding.imageView.setImageResource(R.drawable.box)

			} else if (parentName.equals("recyclerCtgr2")) {
				(binding as RecyclerCtgrViewItemBinding).txtCtgr2.text = ctgr.name
				itemView.setOnClickListener {
					val intent = Intent(context, ViewContentActivity::class.java)
					intent.putExtra("title", "${ctgr.idx}")
					intent.putExtra("ctgrname", "${ctgr.name}")
					context.startActivity(intent)
				}
			}
		}
		fun setMemo(resultMemo: Memo) {
			(binding as RecyclerContentItem1Binding).titleItem.text = resultMemo.title
			itemView.setOnClickListener {
				Log.d("ggggg","${resultMemo.idx}")
			}
		}
	}
}
