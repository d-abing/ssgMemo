package com.example.ssgmemo

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.fragment.app.Fragment
import com.example.ssgmemo.databinding.RecyclerViewItemBinding

class WriteActivity : AppCompatActivity() {
    val helper = SqliteHelper(this, "ssgMemo", 1)
    @SuppressLint("MissingInflatedId", "WrongViewCast")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_write)
        val spinner = findViewById<Spinner>(R.id.category)
        val title = findViewById<TextView>(R.id.writeTitle)
        val content = findViewById<TextView>(R.id.writeContent)
        val btnSave = findViewById<ImageButton>(R.id.saveContent)
        var ctgr:Int? = null

        val ctgrList:MutableList<String> =  helper.selectCtgrMap().values.toMutableList()

        fun <K, V> getKey(map: Map<K, V>, target: V): K {
            return map.keys.first { target == map[it] };
        }

        ctgrList.add(0,"미분류")
        spinner.adapter = ArrayAdapter<String>(this,android.R.layout.simple_list_item_1, ctgrList)
        spinner.onItemSelectedListener = object: AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if(spinner.getItemAtPosition(position).toString() !="미분류") {
                    val value = spinner.getItemAtPosition(position)
                    // 카테고리 이름.. = 벨류 값...
                    ctgr = getKey(helper.selectCtgrMap(), value)
                    Log.d("값값","${ctgr}")

                }else{
                    ctgr = null
                }
            }
        }

        btnSave.setOnClickListener {
            if (content.text.toString().isNotEmpty()){
                var mTitle = ""
                if ( title.text.toString() == "" ) {
                    mTitle = "빈 제목"
                } else {
                    mTitle = title.text.toString()
                }
                val memo = Memo(null, mTitle, content.text.toString(), System.currentTimeMillis(),ctgr,null)
                helper.insertMemo(memo)
                title.text = ""
                content.text = ""
                spinner.setSelection(0)

                btnSave.setImageResource(R.drawable.save2)
                val handler = android.os.Handler()
                handler.postDelayed( Runnable { btnSave.setImageResource(R.drawable.save1)}, 200) // 0.5초 후에 다시 닫아주기

            }
        }
//        setFragment()
    }
/*
    private fun setFragment() {
        val fontFragment: Fragment = FontFragment()
        val trans = supportFragmentManager.beginTransaction()
        trans.add(R.id.frameLayout, fontFragment)
        trans.commit()
    }*/
}