package com.example.ssgmemo.fragment

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.example.ssgmemo.callback.CallbackListener
import com.example.ssgmemo.databinding.FragmentCtgrDeleteBinding

class CtgrDeleteFragment (var listener:CallbackListener) : DialogFragment() {

    private lateinit var binding: FragmentCtgrDeleteBinding

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
        binding = FragmentCtgrDeleteBinding.inflate(inflater, container, false)
        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d("as1d23","1da56")
        val bundle: Bundle? = arguments
        val ctgridx: String? = bundle?.getString("Ctgridx")
        var ctgrSelected: Boolean = false // ctgr만 지울 것인가
        var memoSelected: Boolean = false // 내부의 메모도 함께 지울 것 인가.

        binding.deleteOnlyCtgr.setOnClickListener {
            ctgrSelected = if (ctgrSelected) {
                binding.deleteOnlyCtgr.setTextColor(Color.parseColor("#BDBBBB"))
                false
            } else {
                binding.deleteOnlyCtgr.setTextColor(Color.parseColor("#41AFE1"))
                binding.deleteAlsoMemo.setTextColor(Color.parseColor("#BDBBBB"))
                memoSelected = false
                true
            }
        }
        binding.deleteAlsoMemo.setOnClickListener {
            memoSelected = if (memoSelected) {
                binding.deleteAlsoMemo.setTextColor(Color.parseColor("#BDBBBB"))
                false
            } else {
                binding.deleteAlsoMemo.setTextColor(Color.parseColor("#41AFE1"))
                binding.deleteOnlyCtgr.setTextColor(Color.parseColor("#BDBBBB"))
                ctgrSelected = false
                true
            }
        }
        binding.dialogDeleteNo.setOnClickListener {
            dismiss()
        }
        binding.dialogDeleteYes.setOnClickListener {
            if (ctgridx != null) {
                if (ctgrSelected) {
                    listener.deleteCtgr(ctgridx!!)
                } else if (memoSelected) {
                    listener.deleteMemoFromCtgr(ctgridx!!)
                } else {
                    Toast.makeText(activity, "하나를 선택해 주세요.", Toast.LENGTH_SHORT).show()
                }
                Toast.makeText(activity, "잘못된 접근입니다.", Toast.LENGTH_SHORT).show()
            }
            dismiss()
        }
    }
}
