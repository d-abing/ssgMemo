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
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
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
	private var selectedItemPosition = -1
	private var selectedlayout: ConstraintLayout? = null
	private var selected1: BackPressEditText? = null
	private var selected2: TextView? = null
	private var selected3: ImageButton? = null

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
		holder.setIsRecyclable(false)
		if(parentName.equals("recyclerCtgr1")){
			val ctgr: Ctgr = listData[position] as Ctgr
			holder.setCtgr(ctgr)
		} else if (parentName.equals("recyclerCtgr2")){
			val layout = holder.itemView.findViewById<ConstraintLayout>(R.id.ctgr_item)
			val delete = holder.itemView.findViewById<ImageButton>(R.id.delete)
			val txtCtgr2 = holder.itemView.findViewById<BackPressEditText>(R.id.txtCtgr2)
			val txtCtgr3 = holder.itemView.findViewById<TextView>(R.id.txtCtgr3)

			if (position == selectedItemPosition) {
				delete.visibility = View.VISIBLE
				txtCtgr2.visibility = View.VISIBLE
				txtCtgr3.visibility = View.INVISIBLE
			} else {
				delete.visibility = View.INVISIBLE
				txtCtgr2.visibility = View.INVISIBLE
				txtCtgr3.visibility = View.VISIBLE
			}

			layout.setOnLongClickListener {
				val currentPosition = holder.adapterPosition
				if (selectedItemPosition == currentPosition) {
					selectedItemPosition = -1
					selected1?.visibility = View.INVISIBLE
					selected2?.visibility = View.INVISIBLE
					selected3?.visibility = View.VISIBLE
					selectedlayout = null
				}else {
					// Item New Selected
					if (selectedItemPosition >= 0 || selectedlayout != null) {
						selected1?.visibility = View.INVISIBLE
						selected2?.visibility = View.VISIBLE
						selected3?.visibility = View.INVISIBLE
					}

					selectedItemPosition = currentPosition
					selectedlayout = layout
					selected1 = txtCtgr2
					selected2 = txtCtgr3
					selected3 = delete
				}
				if(vibration.equals("ON")) {
					vibrator?.vibrate(VibrationEffect.createOneShot(200, 50))
				}
				delete.visibility = View.VISIBLE
				txtCtgr2.visibility = View.VISIBLE
				txtCtgr3.visibility = View.INVISIBLE
				txtCtgr2.isEnabled = true
				txtCtgr2.requestFocus()
				txtCtgr2.setSelection(txtCtgr2.length())
				callbackListener.openKeyBoard(txtCtgr2)
				return@setOnLongClickListener true
			}

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
				binding.memoCount.text = helper!!.getMemoListSize(ctgr.idx)

				if (ctgr.name == "미분류" || ctgr.name == "+" || ctgr.name == "휴지통") {
					itemView.setOnLongClickListener {return@setOnLongClickListener false}
				}

				if (ctgr.name == "+"){
					binding.ctgrBtn.setBackgroundResource(R.drawable.ctgrback2)
					binding.memoCount.visibility = View.INVISIBLE
				} else if (ctgr.name == "미분류") {
					binding.ctgrBtn.setBackgroundResource(R.drawable.ctgrback3)
				} else if (ctgr.name == "휴지통") {
					binding.ctgrBtn.setBackgroundResource(R.drawable.ctgrback4)
				} else {
					binding.ctgrBtn.setBackgroundResource(R.drawable.ctgrback1)
				}
				binding.delete.setOnClickListener {
					// 해당 crgr에 메모가 존재하는지 판단.
					if(helper?.isCtgrMemoExist(ctgr.idx.toString()) == true){
						callbackListener.fragmentOpen("delete@#",ctgr.idx.toString())

					}else{
						// 아니면 바로 삭제
						val unclassifyCtgr = Ctgr(0, "미분류", 11111111, 0)
						val ctgrAddBtn = Ctgr(null,"+",11111111, 0)
						val deleteBtn = Ctgr(-1,"휴지통",11111111, 0)
						helper?.deleteCtgr(ctgr.idx.toString())
						listData = helper?.selectCtgrList() as MutableList<Any>
						if (helper?.isUnknownMemoExist()!!){
							listData.add(0,unclassifyCtgr)
						}
						listData.add(ctgrAddBtn)
						listData.add(deleteBtn)
						notifyDataSetChanged()
					}
					binding.delete.visibility = View.INVISIBLE
					binding.txtCtgr2.visibility = View.INVISIBLE
					binding.txtCtgr3.visibility = View.VISIBLE
				}
				// 수정 중 뒤로가기 클릭시
				binding.txtCtgr2.setOnBackPressListener(object : BackPressEditText.OnBackPressListener{
					override fun onBackPress() {
						// 중복 체크 미분류, +
						val ctgrName = binding.txtCtgr2.text.toString().trim()
						if(ctgrName != "미분류" && ctgrName != "delete@#" && ctgrName != "+" && ctgrName != "휴지통"){
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
						if(ctgrName != "미분류" && ctgrName != "delete@#" && ctgrName != "+" && ctgrName != "휴지통"){
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
						notifyDataSetChanged()
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
