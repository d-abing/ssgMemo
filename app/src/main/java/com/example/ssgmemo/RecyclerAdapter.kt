package com.example.ssgmemo

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnKeyListener
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.example.ssgmemo.databinding.RecyclerContentItem1Binding
import com.example.ssgmemo.databinding.RecyclerCtgrViewItemBinding
import com.example.ssgmemo.databinding.RecyclerViewItemBinding


class RecyclerAdapter(val callbackListener: CallbackListener, val context: Context): RecyclerView.Adapter<RecyclerAdapter.Holder>() {
	var listData = mutableListOf<Any>()
	var helper: SqliteHelper? = null
	var parentName : String? = null
	var flag :Boolean = false


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
//	fun test (){
//		val binding = RecyclerContentItem1Binding.inflate(LayoutInflater.from(context))
//		val holder = Holder(binding)
//		Log.d("결과","${binding.titleItem.text}")
//		holder.test1()
//	}

	@SuppressLint("ResourceType")
	override fun getItemCount(): Int {
		return listData.size
	}

	inner class Holder(val binding: ViewBinding?): RecyclerView.ViewHolder(binding?.root!!) {


		fun setCtgr(ctgr: Ctgr) {
			if (parentName.equals("recyclerCtgr1")) {
				(binding as RecyclerViewItemBinding).txtCtgr.text = ctgr.name
				binding.box.setImageResource(R.drawable.closed_box)
				binding.cidx.text = ctgr.idx.toString()
				binding.cidx.visibility = View.INVISIBLE
				itemView.setOnClickListener {
					(binding as RecyclerViewItemBinding).box.setImageResource(R.drawable.opened_box) // 닫힌 상자를 열어주고
					val handler = android.os.Handler()
					handler.postDelayed( Runnable { binding.box.setImageResource(R.drawable.closed_box)}, 500) // 0.5초 후에 다시 닫아주기

					callbackListener.callback((binding as RecyclerViewItemBinding).cidx.text.toString().toLong()) // cidx값을 액티비티로 전송
				}
			} else if (parentName.equals("recyclerCtgr2")) {
				(binding as RecyclerCtgrViewItemBinding).txtCtgr2.setText(ctgr.name)
				binding.txtCtgr3.text = ctgr.name
				itemView.setOnLongClickListener {
					if(flag){
						binding.delete.visibility = View.INVISIBLE
						binding.repair.visibility = View.INVISIBLE
						flag=false
					}else{
						binding.delete.visibility = View.VISIBLE
						binding.repair.visibility = View.VISIBLE
						flag=true
					}
					binding.delete.setOnClickListener {
						helper?.deleteCtgr(ctgr.idx.toString())
						listData.clear()
						notifyDataSetChanged()
					}
					binding.repair.setOnClickListener {
						binding.txtCtgr3.visibility = View.INVISIBLE
						binding.txtCtgr2.visibility = View.VISIBLE
						binding.txtCtgr2.isEnabled = true
						binding.txtCtgr2.selectionEnd
					}
					return@setOnLongClickListener true
				}
				binding.txtCtgr2.setOnKeyListener(object : OnKeyListener{
					override fun onKey(p0: View?, p1: Int, p2: KeyEvent?): Boolean {
						Log.d("tttt","ttttt")
						if (p2 != null) {
							if (p2.action == KeyEvent.KEYCODE_ENTER && p2.action==KeyEvent.ACTION_UP){

								binding.txtCtgr2.isEnabled = false
								helper?.updateCtgrName(ctgr.idx.toString(),binding.txtCtgr2.text.toString())
								notifyDataSetChanged()
								return true
							}
						}
						return false
					}

				})

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

//		fun test1(){
//			(binding as RecyclerCtgrViewItemBinding).txtCtgr2.text
//			Log.d("결과","${binding.txtCtgr2.text}")
//
//		}
	}
}
