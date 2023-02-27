package com.example.ssgmemo.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.example.ssgmemo.*
import com.example.ssgmemo.callback.CallbackListener
import com.example.ssgmemo.common.EditActivity
import com.example.ssgmemo.common.ViewMemoActivity
import com.example.ssgmemo.databinding.RecyclerViewCtgrBinding
import com.example.ssgmemo.databinding.RecyclerSearchMemoBinding
import com.example.ssgmemo.databinding.RecyclerClassifyCtgrBinding
import java.text.SimpleDateFormat
import java.util.*


class RecyclerAdapter(val context: Context): RecyclerView.Adapter<RecyclerAdapter.Holder>() {
	var listData = mutableListOf<Any>()
	var helper: SqliteHelper? = null
	var parentName : String? = null
	var fontSize : String? = null
	var vibration : String? = null
	var vibrator: Vibrator? = null
	lateinit var callbackListener: CallbackListener

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
		parentName = parent.resources.getResourceEntryName(parent.id).toString()
		var binding: ViewBinding? =null
		if (parentName.equals("recyclerCtgr1")){
			Log.d("text","$parentName")
			binding =
				RecyclerClassifyCtgrBinding.inflate(LayoutInflater.from(parent.context), parent, false)
		} else if (parentName.equals("recyclerCtgr2")){
			binding =
				RecyclerViewCtgrBinding.inflate(LayoutInflater.from(parent.context), parent, false)
		} else if(parentName.equals("recyclerSearch")){
			binding =
				RecyclerSearchMemoBinding.inflate(LayoutInflater.from(parent.context), parent, false)
		}
		return Holder(binding)
	}

	override fun onBindViewHolder(holder: Holder, position: Int) {
		if (parentName.equals("recyclerCtgr1") || parentName.equals("recyclerCtgr2")){
			val ctgr: Ctgr = listData[position] as Ctgr
			holder.setCtgr(ctgr)
		} else {
			val memo: Memo = listData[position] as Memo
			holder.getMemo(memo)
		}
	}

	@SuppressLint("ResourceType")
	override fun getItemCount(): Int {
		return listData.size
	}

	inner class Holder(val binding: ViewBinding?): RecyclerView.ViewHolder(binding?.root!!) {
		@SuppressLint("NotifyDataSetChanged")
		fun setCtgr(ctgr: Ctgr) {
			if (parentName.equals("recyclerCtgr1")) { // <분류>
				(binding as RecyclerClassifyCtgrBinding).txtCtgr.text = ctgr.name
				if (fontSize.equals("ON")) {
					binding.txtCtgr.textSize = 20f
				}
				binding.box.setImageResource(R.drawable.closed_box)
				binding.cidx.text = ctgr.idx.toString()
				binding.cidx.visibility = View.INVISIBLE
				itemView.setOnClickListener {
					binding.box.setImageResource(R.drawable.opened_box) // 닫힌 상자를 열어주고
					val handler = android.os.Handler()
					handler.postDelayed(
						Runnable { binding.box.setImageResource(R.drawable.closed_box) },
						500
					) // 0.5초 후에 다시 닫아주기

					callbackListener.callback(
						binding.cidx.text.toString().toLong()
					) // cidx값을 액티비티로 전송
				}


				if (ctgr.name == "+"){
					binding.txtCtgr.visibility = View.INVISIBLE
					binding.box.setImageResource(R.drawable.add_ctgr)
					itemView.setOnClickListener {
						callbackListener.fragmentOpen(ctgr.name, null)
					}
				} else {
					binding.txtCtgr.visibility = View.VISIBLE
				}

			} else if (parentName.equals("recyclerCtgr2")) { // <보기>
				(binding as RecyclerViewCtgrBinding).txtCtgr2.setText(ctgr.name)
				if (fontSize.equals("ON")) {
					binding.txtCtgr3.textSize = 30f
				}
				binding.txtCtgr3.text = ctgr.name
				binding.delete.visibility = View.INVISIBLE
				binding.txtCtgr2.visibility = View.INVISIBLE
				binding.txtCtgr3.visibility = View.VISIBLE

				if (ctgr.name == "+"){
					binding.ctgrBtn.setBackgroundResource(R.drawable.ctgrback2)
				} else if (ctgr.name == "미분류") {
					binding.ctgrBtn.setBackgroundResource(R.drawable.ctgrback3)
				} else {
					binding.ctgrBtn.setBackgroundResource(R.drawable.ctgrback1)
				}
				if (ctgr.name != "미분류" && ctgr.name != "+") {
					itemView.setOnLongClickListener {

						if(vibration.equals("ON")) {
							vibrator?.vibrate(VibrationEffect.createOneShot(200, 50))
						}

						binding.delete.visibility = View.VISIBLE
						binding.txtCtgr3.visibility = View.INVISIBLE
						binding.txtCtgr2.visibility = View.VISIBLE
						binding.txtCtgr2.isEnabled = true
						binding.txtCtgr2.requestFocus()
						binding.txtCtgr2.setSelection(binding.txtCtgr2.length())
						callbackListener.openKeyBoard(binding.txtCtgr2)
						return@setOnLongClickListener true
					}
					binding.delete.setOnClickListener {
						callbackListener.fragmentOpen("delete@#",ctgr.idx.toString())
						binding.delete.visibility = View.INVISIBLE
						binding.txtCtgr2.visibility = View.INVISIBLE
						binding.txtCtgr3.visibility = View.VISIBLE
					}
				}else{
					itemView.setOnLongClickListener {return@setOnLongClickListener false}
				}
				// 수정 중 뒤로가기 클릭시
				binding.txtCtgr2.setOnBackPressListener(object : BackPressEditText.OnBackPressListener{
					override fun onBackPress() {
						// 중복 체크 미분류, +
						val ctgrName = binding.txtCtgr2.text.toString().trim()
						if(ctgrName != "미분류" && ctgrName != "delete@#" && ctgrName != "+"){
							if (!helper!!.checkDuplicationCtgr(ctgrName)) {
								// 이름 업데이트
								helper?.updateCtgrName(
									ctgr.idx.toString(),
									binding.txtCtgr2.text.toString()
								)
								// 플레인 텍스트 값 변경
								binding.txtCtgr3.text = binding.txtCtgr2.text
								ctgr.name = binding.txtCtgr2.text.toString()
								// 데이터 변경 알림
								listData[adapterPosition] = ctgr
								this@RecyclerAdapter.notifyDataSetChanged()
								// 에디터와 플레인을 번갈아 가면서 노출
//								binding.txtCtgr2.visibility = View.INVISIBLE
//								binding.txtCtgr3.visibility = View.VISIBLE
							}else if(ctgrName == binding.txtCtgr3.text){
								this@RecyclerAdapter.notifyDataSetChanged()
							}else{
								val text = "이미 사용중 입니다."
								val duration = Toast.LENGTH_SHORT
								val toast = Toast.makeText(context, text, duration)
								toast.show()
							}
						}else{
							val text = "사용할 수 없는 이름입니다."
							val duration = Toast.LENGTH_SHORT
							val toast = Toast.makeText(context, text, duration)
							toast.show()
						}
					}
				})
				// 수정 완료 후 엔터 클릭 시
				binding.txtCtgr2.setOnKeyListener { view, i, keyEvent ->
					if (i == KeyEvent.KEYCODE_ENTER && keyEvent.action == KeyEvent.ACTION_UP) {
						val ctgrName = binding.txtCtgr2.text.toString().trim()
						if(ctgrName != "미분류" && ctgrName != "delete@#" && ctgrName != "+"){
							if (!helper!!.checkDuplicationCtgr(ctgrName)){
								binding.delete.visibility = View.INVISIBLE
								// 이름 업데이트
								helper?.updateCtgrName(
									ctgr.idx.toString(),
									binding.txtCtgr2.text.toString())
								// 플레인 텍스트 값 변경
								binding.txtCtgr3.text = binding.txtCtgr2.text
								ctgr.name = binding.txtCtgr2.text.toString()
								// 데이터 변경 알림
								listData[adapterPosition] = ctgr
								callbackListener.closeKeyBoard()
								this@RecyclerAdapter.notifyDataSetChanged()
							}else if(ctgrName == binding.txtCtgr3.text){
								callbackListener.closeKeyBoard()
								this@RecyclerAdapter.notifyDataSetChanged()
							}else{
								val text = "이미 사용중 입니다."
								val duration = Toast.LENGTH_SHORT
								val toast = Toast.makeText(context, text, duration)
								toast.show()
							}
						}else{
							val text = "사용할 수 없는 이름입니다."
							val duration = Toast.LENGTH_SHORT
							val toast = Toast.makeText(context, text, duration)
							toast.show()
						}
						return@setOnKeyListener true
					}
					return@setOnKeyListener false
				}
				if (ctgr.name == "+") {
					itemView.setOnClickListener {
						binding.delete.visibility = View.INVISIBLE
						callbackListener.fragmentOpen(
							ctgr.name,null
						)
					}
				}else{
					itemView.setOnClickListener {
						binding.delete.visibility = View.INVISIBLE
						val intent = Intent(context, ViewMemoActivity::class.java)
						intent.putExtra("idx", "${ctgr.idx}")
						intent.putExtra("ctgrname", "${ctgr.name}")
						intent.putExtra("fontSize", "$fontSize")
						intent.putExtra("vibration", "$vibration")
						context.startActivity(intent)
					}
				}
			}
		}

		fun getMemo(memo: Memo) {
			(binding as RecyclerSearchMemoBinding).searchTitle.text = memo.title
			binding.searchContent.text = memo.content

			val t_dateFormat = SimpleDateFormat("M월 d일", Locale("ko", "KR"))
			val str_date = t_dateFormat.format(Date(memo.datetime))
			binding.searchDate.text = str_date

			if (fontSize.equals("ON")) {
				binding.searchTitle.textSize = 24f
				binding.searchContent.textSize = 20f
				binding.searchDate.textSize = 20f
			}

			itemView.setOnClickListener {
				val intent = Intent(context, EditActivity::class.java)
				intent.putExtra("memoIdx", "${memo.idx}")
				intent.putExtra("fontSize", "$fontSize")
				context.startActivity(intent)
			}
		}

	}
}
