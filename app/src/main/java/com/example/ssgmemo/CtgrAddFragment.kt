package com.example.ssgmemo

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.example.ssgmemo.databinding.FragmentCtgrAddBinding

class CtgrAddFragment(val Callback:CallbackListener) : DialogFragment(){

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //false로 설정해 주면 화면밖 혹은 뒤로가기 버튼시 다이얼로그라 dismiss 되지 않는다.
        isCancelable = true
    }

    private lateinit var binding: FragmentCtgrAddBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentCtgrAddBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.editTextTextPersonName.requestFocus()
        binding.dialogTvNo.setOnClickListener {
            dismiss()
        }
        binding.dialogTvYes.setOnClickListener {
            Callback.addCtgr(binding.editTextTextPersonName.text.toString())
            dismiss()
        }
    }

}