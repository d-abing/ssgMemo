package com.example.ssgmemo

import android.content.Intent
import android.os.Looper
import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.example.ssgmemo.databinding.RecyclerContentItem1Binding
import com.example.ssgmemo.databinding.RecyclerCtgrViewItemBinding
import com.example.ssgmemo.databinding.RecyclerViewItemBinding


class RecyclerAdapter(val context: Context): RecyclerView.Adapter<RecyclerAdapter.Holder>() {
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
	fun test (){
		val binding = RecyclerContentItem1Binding.inflate(LayoutInflater.from(context))
		val holder = Holder(binding)
		Log.d("결과","${binding.titleItem.text}")
		holder.test1()
	}

	@SuppressLint("ResourceType")
	override fun getItemCount(): Int {
		return listData.size
	}

	inner class Holder(val binding: ViewBinding?): RecyclerView.ViewHolder(binding?.root!!) {

		init {
			var isOpen:Boolean = false
			val myThread = Thread {Thread.sleep(3000)
			}
			binding?.root!!.setOnClickListener { // 아이템 클릭 시
				(binding as RecyclerViewItemBinding).imageView.setImageResource(R.drawable.opened_box) // 닫힌 상자를 열어주고
				if (!isOpen){
					isOpen = true
					myThread.start()
					isOpen = false
					binding.imageView.setImageResource(R.drawable.closed_box)
					notifyDataSetChanged()
				}
				helper?.updateMemoCtgr((binding as RecyclerViewItemBinding).midx.text.toString().toLong(),
					(binding as RecyclerViewItemBinding).cidx.text.toString().toLong()) // Memo의 Ctgr 없데이트

				Toast.makeText(binding.root.context
					, "cidx=${(binding as RecyclerViewItemBinding).cidx.text}"
					, Toast.LENGTH_LONG).show()
			}
		}

		fun setCtgr(ctgr: Ctgr) {
			if (parentName.equals("recyclerCtgr1")) {
				(binding as RecyclerViewItemBinding).txtCtgr.text = ctgr.name
				binding.imageView.setImageResource(R.drawable.closed_box)
				binding.midx.visibility = View.INVISIBLE
				binding.midx.text = ctgr.midx.toString()
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
				val intent = Intent(context, EditActivity::class.java)
				intent.putExtra("memoIdx", "${resultMemo.idx}")
				context.startActivity(intent)
			}
		}

		fun test1(){
			(binding as RecyclerCtgrViewItemBinding).txtCtgr2.text = ctgr.name
			Log.d("결과","${binding.titleItem.text}")

		}
	}
}
