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
			if (parentName.equals("recyclerCtgr1")) { // <??????>
				(binding as RecyclerClassifyCtgrBinding).txtCtgr.text = ctgr.name
				if (fontSize.equals("ON")) {
					binding.txtCtgr.textSize = 20f
				}
				binding.box.setImageResource(R.drawable.closed_box)
				binding.cidx.text = ctgr.idx.toString()
				binding.cidx.visibility = View.INVISIBLE
				itemView.setOnClickListener {
					binding.box.setImageResource(R.drawable.opened_box) // ?????? ????????? ????????????
					val handler = android.os.Handler()
					handler.postDelayed(
						Runnable { binding.box.setImageResource(R.drawable.closed_box) },
						500
					) // 0.5??? ?????? ?????? ????????????

					callbackListener.callback(
						binding.cidx.text.toString().toLong()
					) // cidx?????? ??????????????? ??????
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

			} else if (parentName.equals("recyclerCtgr2")) { // <??????>
				(binding as RecyclerViewCtgrBinding).txtCtgr2.setText(ctgr.name)
				if (fontSize.equals("ON")) {
					binding.txtCtgr3.textSize = 30f
				}
				binding.txtCtgr3.text = ctgr.name
				binding.delete.visibility = View.INVISIBLE
				binding.txtCtgr2.visibility = View.INVISIBLE
				binding.txtCtgr3.visibility = View.VISIBLE
				binding.memoCount.text = helper!!.getMemoListSize(ctgr.idx)

				if (ctgr.name == "?????????" || ctgr.name == "+" || ctgr.name == "?????????") {
					itemView.setOnLongClickListener {return@setOnLongClickListener false}
				}

				if (ctgr.name == "+"){
					binding.ctgrBtn.setBackgroundResource(R.drawable.ctgrback2)
					binding.memoCount.visibility = View.INVISIBLE
				} else if (ctgr.name == "?????????") {
					binding.ctgrBtn.setBackgroundResource(R.drawable.ctgrback3)
				} else if (ctgr.name == "?????????") {
					binding.ctgrBtn.setBackgroundResource(R.drawable.ctgrback4)
				} else {
					binding.ctgrBtn.setBackgroundResource(R.drawable.ctgrback1)
				}
				binding.delete.setOnClickListener {
					// ?????? crgr??? ????????? ??????????????? ??????.
					if(helper?.isCtgrMemoExist(ctgr.idx.toString()) == true){
						callbackListener.fragmentOpen("delete@#",ctgr.idx.toString())

					}else{
						// ????????? ?????? ??????
						val unclassifyCtgr = Ctgr(0, "?????????", 11111111, 0)
						val ctgrAddBtn = Ctgr(null,"+",11111111, 0)
						val deleteBtn = Ctgr(-1,"?????????",11111111, 0)
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
				// ?????? ??? ???????????? ?????????
				binding.txtCtgr2.setOnBackPressListener(object : BackPressEditText.OnBackPressListener{
					override fun onBackPress() {
						// ?????? ?????? ?????????, +
						val ctgrName = binding.txtCtgr2.text.toString().trim()
						if(ctgrName != "?????????" && ctgrName != "delete@#" && ctgrName != "+" && ctgrName != "?????????"){
							if (!helper!!.checkDuplicationCtgr(ctgrName)) {
								// ?????? ????????????
								helper?.updateCtgrName(
									ctgr.idx.toString(),
									binding.txtCtgr2.text.toString()
								)
								// ????????? ????????? ??? ??????
								binding.txtCtgr3.text = binding.txtCtgr2.text
								ctgr.name = binding.txtCtgr2.text.toString()
								// ????????? ?????? ??????
								listData[adapterPosition] = ctgr
								this@RecyclerAdapter.notifyDataSetChanged()
								// ???????????? ???????????? ????????? ????????? ??????
//								binding.txtCtgr2.visibility = View.INVISIBLE
//								binding.txtCtgr3.visibility = View.VISIBLE
							}else if(ctgrName == binding.txtCtgr3.text){
								this@RecyclerAdapter.notifyDataSetChanged()
							}else{
								val text = "?????? ????????? ?????????."
								val duration = Toast.LENGTH_SHORT
								val toast = Toast.makeText(context, text, duration)
								toast.show()
							}
						}else{
							val text = "????????? ??? ?????? ???????????????."
							val duration = Toast.LENGTH_SHORT
							val toast = Toast.makeText(context, text, duration)
							toast.show()
						}
					}
				})
				// ?????? ?????? ??? ?????? ?????? ???
				binding.txtCtgr2.setOnKeyListener { view, i, keyEvent ->
					if (i == KeyEvent.KEYCODE_ENTER && keyEvent.action == KeyEvent.ACTION_UP) {
						val ctgrName = binding.txtCtgr2.text.toString().trim()
						if(ctgrName != "?????????" && ctgrName != "delete@#" && ctgrName != "+" && ctgrName != "?????????"){
							if (!helper!!.checkDuplicationCtgr(ctgrName)){
								binding.delete.visibility = View.INVISIBLE
								// ?????? ????????????
								helper?.updateCtgrName(
									ctgr.idx.toString(),
									binding.txtCtgr2.text.toString())
								// ????????? ????????? ??? ??????
								binding.txtCtgr3.text = binding.txtCtgr2.text
								ctgr.name = binding.txtCtgr2.text.toString()
								// ????????? ?????? ??????
								listData[adapterPosition] = ctgr
								callbackListener.closeKeyBoard()
								this@RecyclerAdapter.notifyDataSetChanged()
							}else if(ctgrName == binding.txtCtgr3.text){
								callbackListener.closeKeyBoard()
								this@RecyclerAdapter.notifyDataSetChanged()
							}else{
								val text = "?????? ????????? ?????????."
								val duration = Toast.LENGTH_SHORT
								val toast = Toast.makeText(context, text, duration)
								toast.show()
							}
						}else{
							val text = "????????? ??? ?????? ???????????????."
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

			val t_dateFormat = SimpleDateFormat("M??? d???", Locale("ko", "KR"))
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
