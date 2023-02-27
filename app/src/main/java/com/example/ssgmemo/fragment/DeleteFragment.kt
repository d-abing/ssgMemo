package com.example.ssgmemo.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.example.ssgmemo.Memo
import com.example.ssgmemo.callback.CallbackListener
import com.example.ssgmemo.databinding.FragmentCtgrDeleteBinding

class DeleteFragment (var listener:CallbackListener) : DialogFragment(){

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
        binding = FragmentCtgrDeleteBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val bundle:Bundle? = arguments
        val ctgridx: String? = bundle?.getString("Ctgridx")
        val memoidx: String? = bundle?.getString("memoidx")

        if (ctgridx== null && memoidx == null){
            binding.deleteMsg.text = "선택된 메모가 삭제 됩니다."
        }else{
            binding.deleteMsg.text = "메모가 삭제됩니다"
        }
        binding.dialogDeleteNo.setOnClickListener {
            dismiss()
        }
        binding.dialogDeleteYes.setOnClickListener {
            if(ctgridx== null && memoidx == null){
                listener.selectedListDel()
            }else if (ctgridx != null){
                listener.deleteCtgr(ctgridx!!)
            }else if(ctgridx == null){
                listener.deleteMemo(memoidx!!)
            }
            dismiss()
        }
    }
}