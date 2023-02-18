package com.example.ssgmemo.fragment

import android.graphics.Color
import android.os.Bundle

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.example.ssgmemo.callback.CallbackListener
import com.example.ssgmemo.databinding.FragmentMemoDeleteBinding

class MemoDeleteFragment(var listener: CallbackListener) : DialogFragment() {

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
        binding = FragmentMemoDeleteBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val bundle:Bundle? = arguments
        val memoidx: String? = bundle?.getString("memoidx")
        var ctgrSelected:Boolean = false
        var memoSelected:Boolean = false
        binding.deleteCtgrTxt.setOnClickListener {
            ctgrSelected = if(ctgrSelected){
                binding.deleteCtgrTxt.setTextColor(Color.parseColor("#BDBBBB"))
                false
            }else{
                binding.deleteCtgrTxt.setTextColor(Color.parseColor("#41AFE1"))
                binding.deleteMemoTxt.setTextColor(Color.parseColor("#BDBBBB"))
                memoSelected = false
                true
            }
        }
        binding.deleteMemoTxt.setOnClickListener {
            memoSelected = if(memoSelected){
                binding.deleteMemoTxt.setTextColor(Color.parseColor("#BDBBBB"))
                false
            }else{
                binding.deleteMemoTxt.setTextColor(Color.parseColor("#41AFE1"))
                binding.deleteCtgrTxt.setTextColor(Color.parseColor("#BDBBBB"))
                ctgrSelected = false
                true
            }
        }
        binding.dialogMemoDeleteNo.setOnClickListener {
            dismiss()
        }
        binding.dialogMemoDeleteYes.setOnClickListener {
            if (ctgrSelected){
                listener.deleteCtgr(memoidx!!)
                dismiss()
            }else if (memoSelected){
                listener.deleteMemo(memoidx!!)
                dismiss()
            }else{
                var text = "하나를 선택해 주세요."
                val duration = Toast.LENGTH_SHORT
                val toast = Toast.makeText(getActivity(), text, duration)
                toast.show()
            }

        }
    }

}