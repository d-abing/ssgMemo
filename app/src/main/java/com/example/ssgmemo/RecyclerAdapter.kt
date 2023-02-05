package com.example.ssgmemo

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.icu.text.Transliterator.Position
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.View.OnKeyListener
import android.view.ViewGroup
import androidx.core.view.MotionEventCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.example.ssgmemo.databinding.RecyclerContentItem1Binding
import com.example.ssgmemo.databinding.RecyclerCtgrViewItemBinding
import com.example.ssgmemo.databinding.RecyclerSearchItemBinding
import com.example.ssgmemo.databinding.RecyclerViewItemBinding
import java.text.SimpleDateFormat
import java.util.*


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
		} else if(parentName.equals("recyclerSearch")){
			binding =
				RecyclerSearchItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
		}
		return Holder(binding)
	}

	override fun onBindViewHolder(holder: Holder, position: Int) {
		if (parentName.equals("recyclerCtgr1") || parentName.equals("recyclerCtgr2")){
			val ctgr:Ctgr = listData.get(position) as Ctgr
			holder.setCtgr(ctgr)
		} else if (parentName.equals("recyclerSearch")) {
			val memo:Memo = listData.get(position) as Memo
			holder.getMemo(memo)
		} else {
			val resultMemo:Memo = listData.get(position) as Memo
			holder.getMemo(resultMemo)
		}
	}

	@SuppressLint("ResourceType")
	override fun getItemCount(): Int {
		return listData.size
	}

	inner class Holder(val binding: ViewBinding?): RecyclerView.ViewHolder(binding?.root!!) {

		@SuppressLint("NotifyDataSetChanged")
		fun setCtgr(ctgr: Ctgr) {
			if (parentName.equals("recyclerCtgr1")) {
				(binding as RecyclerViewItemBinding).txtCtgr.text = ctgr.name
				binding.box.setImageResource(R.drawable.closed_box)
				binding.cidx.text = ctgr.idx.toString()
				binding.cidx.visibility = View.INVISIBLE
				itemView.setOnClickListener {
					binding.box.setImageResource(R.drawable.opened_box) // 닫힌 상자를 열어주고
					val handler = android.os.Handler()
					handler.postDelayed( Runnable { binding.box.setImageResource(R.drawable.closed_box)}, 500) // 0.5초 후에 다시 닫아주기

					callbackListener.callback(binding.cidx.text.toString().toLong()) // cidx값을 액티비티로 전송
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
						listData.removeAt(adapterPosition)
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
				// 수정 완료 후 엔터 클릭 시
				binding.txtCtgr2.setOnKeyListener { view, i, keyEvent ->
					// 엔터 그리고 키 업일 때만 적용
					flag=false
					binding.delete.visibility = View.INVISIBLE
					binding.repair.visibility = View.INVISIBLE
					if(i == KeyEvent.KEYCODE_ENTER && keyEvent.action == KeyEvent.ACTION_UP){
						// db update 문
						helper?.updateCtgrName(ctgr.idx.toString(),binding.txtCtgr2.text.toString())
						// 플레인 텍스트 값 변경
						binding.txtCtgr3.text = binding.txtCtgr2.text
						ctgr.name = binding.txtCtgr2.text.toString()
						// 데이터 변경 알림
						listData.set(adapterPosition,ctgr)
						this@RecyclerAdapter.notifyDataSetChanged()
						// 에디터와 플레인을 번갈아 가면서 노출
						binding.txtCtgr2.visibility = View.INVISIBLE
						binding.txtCtgr3.visibility = View.VISIBLE
						return@setOnKeyListener true
					}
					return@setOnKeyListener false
				}
				itemView.setOnClickListener {
					flag=false
					binding.delete.visibility = View.INVISIBLE
					binding.repair.visibility = View.INVISIBLE
					val intent = Intent(context, ViewContentActivity::class.java)
					intent.putExtra("title", "${ctgr.idx}")
					intent.putExtra("ctgrname", "${ctgr.name}")
					context.startActivity(intent)
				}
			}
		}
		fun getMemo(memo: Memo) {
			(binding as RecyclerSearchItemBinding).searchTitle.text = memo.title
			binding.searchContent.text = memo.content

			val t_dateFormat = SimpleDateFormat("M월 d일", Locale("ko", "KR"))
			val str_date = t_dateFormat.format(Date(memo.datetime))
			binding.searchDate.text = str_date

			itemView.setOnClickListener {
				val intent = Intent(context, EditActivity::class.java)
				intent.putExtra("memoIdx", "${memo.idx}")
				context.startActivity(intent)
			}
		}
	}
}
