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
import com.example.ssgmemo.databinding.FragmentCompleteBinding
import com.example.ssgmemo.databinding.FragmentMemoDeleteBinding

class CompleteFragment(var listener: CallbackListener) : DialogFragment() {

    var helper: SqliteHelper? = null
    private lateinit var binding: FragmentCompleteBinding

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
        binding = FragmentCompleteBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val bundle: Bundle? = arguments
        val idx: Long? = bundle?.getLong("idx")
        Log.d("idxxxxx","$idx")
        binding.dialogMemoCompleteNo.setOnClickListener {
            dismiss()
        }

        binding.dialogMemoCompleteYes.setOnClickListener {
            listener.completeMemo(idx!!)
            dismiss()
        }
    }
}