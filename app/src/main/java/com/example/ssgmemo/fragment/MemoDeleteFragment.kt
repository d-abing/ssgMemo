package com.example.ssgmemo.fragment

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Vibrator
import android.util.Log

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.example.ssgmemo.SqliteHelper
import com.example.ssgmemo.callback.CallbackListener
import com.example.ssgmemo.databinding.FragmentMemoDeleteBinding

class MemoDeleteFragment(var listener: CallbackListener) : DialogFragment() {

    var helper: SqliteHelper? = null
    private lateinit var binding: FragmentMemoDeleteBinding

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
        binding = FragmentMemoDeleteBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val bundle:Bundle? = arguments
        val memoidx: String? = bundle?.getString("memoidx") // 해당 메모의 idx 리스트라면 = 첫번째 idx 값만을 가져옴
        val memoCtgr:String? = bundle?.getString("memoCtgr") // 해당 메모의 ctgr 미분류 = 0
        val isList: Boolean? = bundle?.getBoolean("isList") // 리스트인지 아닌지.

        if(memoCtgr!!.toInt() == -1) {
            binding.deleteMemoMsg.text = "메모가 삭제되면 복원할 수 없습니다"
        }

        // 버튼 기본 값
        binding.dialogMemoDeleteYes.setOnClickListener {
            var text = "하나를 선택해 주세요"
            val duration = Toast.LENGTH_SHORT
            val toast = Toast.makeText(getActivity(), text, duration)
            toast.show()
        }
        binding.dialogMemoDeleteNo.setOnClickListener {
            dismiss()
        }

        binding.dialogMemoDeleteYes.setOnClickListener {
            if (memoCtgr!!.toInt() == -1 ) {
                if (isList!!) {
                    // 선택된 메모가 리스트라면 리스트 전체 삭제
                    listener.deleteMemoList()
                    dismiss()

                } else {
                    // 리스트가 아니라면 해당 메모만 삭제
                    listener.deleteMemo(memoidx!!)
                    dismiss()
                }
            } else {
                if (isList!!) {
                    // 선택된 메모가 리스트라면 리스트 전체 삭제
                    listener.moveCtgrList(memoCtgr!!.toLong(), -1)
                    dismiss()

                } else {
                    // 리스트가 아니라면 해당 메모만 삭제
                    Log.d("test다", "${memoidx}")
                    listener.moveCtgr(memoidx!!.toLong(), -1)
                    dismiss()
                }
            }
        }
    }
}