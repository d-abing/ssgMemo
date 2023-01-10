package com.example.ssgmemo

import android.content.Intent
import android.os.Looper
import android.annotation.SuppressLint
import android.content.Context
import android.location.GnssAntennaInfo.Listener
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat.startActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.get
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.example.ssgmemo.databinding.RecyclerContentItem1Binding
import com.example.ssgmemo.databinding.RecyclerCtgrViewItemBinding
import com.example.ssgmemo.databinding.RecyclerViewItemBinding
import java.util.logging.Handler


class RecyclerAdapter(val callbackListener: CallbackListener, val context: Context): RecyclerView.Adapter<RecyclerAdapter.Holder>() {
	var listData = mutableListOf<Any>()
	var helper: SqliteHelper? = null
	var parentName : String? = null

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
		parentName = parent.resources.getResourceEntryName(parent.id).toString()
		Log.d("text","$parentName")
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

		init {
			binding?.root!!.setOnClickListener { // 아이템 클릭 시
				(binding as RecyclerViewItemBinding).box.setImageResource(R.drawable.opened_box) // 닫힌 상자를 열어주고
				// 시간지나면 박스가 다시 닫혔으면 좋겠는데..

				callbackListener.callback((binding as RecyclerViewItemBinding).cidx.text.toString().toLong()) // cidx값을 액티비티로 전송
			}
		}

		fun setCtgr(ctgr: Ctgr) {
			if (parentName.equals("recyclerCtgr1")) {
				(binding as RecyclerViewItemBinding).txtCtgr.text = ctgr.name
				binding.box.setImageResource(R.drawable.closed_box)
				binding.cidx.text = ctgr.idx.toString()
				binding.cidx.visibility = View.INVISIBLE
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
