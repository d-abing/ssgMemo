package com.example.ssgmemo

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.util.Log
import androidx.appcompat.widget.ThemedSpinnerAdapter.Helper
import androidx.core.view.get
import com.example.ssgmemo.databinding.ActivityEditBinding

class EditActivity : AppCompatActivity() {
    private lateinit var binding: ActivityEditBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val helper = SqliteHelper(this, "ssgMemo", 1)
        binding = ActivityEditBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val memoIdx = intent.getStringExtra("memoIdx") as String
        val memo = helper.selectMemo(memoIdx)

        binding.editCategory.setSelection(memo.ctgr as Int)
        binding.editTitle.setText(memo.title)
        binding.editContent.setText(memo.content)

        binding.editSave.setOnClickListener {
            memo.title = binding.editTitle.text.toString()
            memo.content = binding.editContent.text.toString()
            memo.datetime = System.currentTimeMillis()
            val a = helper.updateMemo(memo)
            Log.d("결과값","${a}")
            val intent = Intent(this, ViewCtgrActivity::class.java)
            startActivity(intent)
            finish()
        }


    }
}