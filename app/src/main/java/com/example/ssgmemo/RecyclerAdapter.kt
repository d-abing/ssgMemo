package com.example.ssgmemo

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.example.ssgmemo.databinding.RecyclerCtgrViewItemBinding
import com.example.ssgmemo.databinding.RecyclerViewItemBinding

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

		init {
			binding?.root!!.setOnClickListener {
				(binding as RecyclerViewItemBinding).imageView.setImageResource(R.drawable.opened_box)

				helper?.updateMemoCtgr( (binding as RecyclerViewItemBinding).midx.text.toString().toInt(),
					(binding as RecyclerViewItemBinding).cidx.text.toString().toInt())
				Toast.makeText(binding.root.context
					, "cidx=${(binding as RecyclerViewItemBinding).cidx.text}"
					, Toast.LENGTH_LONG).show()
			}
		}

		/*companion object {
			fun setMidx(midx: Int) {

			}
		}*/

		fun setCtgr(ctgr: Ctgr) {
			if (parentName.equals("recyclerCtgr1")){
				(binding as RecyclerViewItemBinding).txtCtgr.text = ctgr.name
				binding.imageView.setImageResource(R.drawable.closed_box)
				binding.midx.visibility = View.INVISIBLE
				binding.cidx.text = ctgr.idx.toString()
				binding.cidx.visibility = View.INVISIBLE
			} else if(parentName.equals("recyclerCtgr2")){
				(binding as RecyclerCtgrViewItemBinding).ctgrBtn.text =ctgr.name
			}


		}
	}
}
