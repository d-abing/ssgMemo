package com.example.ssgmemo.fragment

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.AdapterView
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.example.ssgmemo.R
import com.example.ssgmemo.SpinnerModel
import com.example.ssgmemo.SqliteHelper
import com.example.ssgmemo.adapter.SpinnerAdapter
import com.example.ssgmemo.callback.CallbackListener
import com.example.ssgmemo.common.ViewMemoActivity
import com.example.ssgmemo.databinding.FragmentMemoDeleteBinding
import com.example.ssgmemo.databinding.FragmentMemoMoveBinding

class MemoMoveFragment(var listener: CallbackListener) : DialogFragment() {

    private lateinit var binding: FragmentMemoMoveBinding
    var helper: SqliteHelper? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //false로 설정해 주면 화면밖 혹은 뒤로가기 버튼시 다이얼로그라 dismiss 되지 않는다.
        isCancelable = true
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog?.window?.requestFeature(Window.FEATURE_NO_TITLE)
        binding = FragmentMemoMoveBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val bundle:Bundle? = arguments
        val memoCtgr:String? = bundle?.getString("memoCtgr") // 해당 메모의 ctgr 미분류 = 0
        val memoidx: String? = bundle?.getString("memoidx") // 해당 메모의 idx 리스트라면 = 첫번째 idx 값만을 가져옴
        val isList: Boolean? = bundle?.getBoolean("isList") // 리스트인지 아닌지.

        // 버튼 기본 값
        binding.dialogMemoMoveNo.setOnClickListener {
            dismiss()
        }

        fun <K, V> getKey(map: Map<K, V>, target: V): K { return map.keys.first { target == map[it] } }

        var ctgr: Long = 0
        val ctgrList = ArrayList<SpinnerModel>()
        ctgrList.add(0, SpinnerModel(R.drawable.closed_box, "미분류"))
        for (i in helper!!.selectCtgrMap().values.toMutableList()) {
            val spinnerModel = SpinnerModel(R.drawable.closed_box, i)
            ctgrList.add(spinnerModel)
        }

       var currentIndex = 0
        for (i in ctgrList) {
            var currentCtgrName = if (helper!!.selectCtgrName(memoCtgr) != null) helper!!.selectCtgrName(memoCtgr) else {"미분류"}
            if (i.name.equals(currentCtgrName)) {
                currentIndex = ctgrList.indexOf(i)
            }
        }

        ctgrList.removeAt(currentIndex)

        if( ctgrList.isNotEmpty()) {
            binding.category2.adapter =
                SpinnerAdapter(requireContext(), R.layout.item_spinner, ctgrList)
        } else {
            binding.moveMemoMsg.setText("이동할 카테고리가 없습니다")
            binding.category2.visibility = View.GONE
        }

        binding.category2.onItemSelectedListener = object: AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {}
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val category = binding.category2.getItemAtPosition(position) as SpinnerModel
                if( category.name != "미분류") {
                    ctgr = getKey(helper!!.selectCtgrMap(), category.name).toLong()
                } else {
                    ctgr = 0
                }
            }
        }

        binding.dialogMemoMoveYes.setOnClickListener {
            if(isList!!) {
                // 메모가 리스트 라면
                listener.moveCtgrList(memoCtgr!!.toLong(), ctgr)
                dismiss()
            } else {
                listener.moveCtgr(memoidx!!.toLong(), ctgr)
                dismiss()
            }
        }
    }
}