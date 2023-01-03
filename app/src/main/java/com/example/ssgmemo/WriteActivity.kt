package com.example.ssgmemo

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*

class WriteActivity : AppCompatActivity() {
    val helper = SqliteHelper(this, "ssgMemo", 1)
    @SuppressLint("MissingInflatedId", "WrongViewCast")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_write)
        val spinner = findViewById<Spinner>(R.id.category)
        val title = findViewById<TextView>(R.id.writeTitle)
        val content = findViewById<TextView>(R.id.content)
        val btnSave = findViewById<ImageButton>(R.id.saveContent)
        var ctgr:Int? = null

        val ctgrList:MutableList<String> =  helper.selectCtgrMap().keys.toMutableList()
        ctgrList.add(0,"카테고리")
        spinner.adapter = ArrayAdapter<String>(this,android.R.layout.simple_list_item_1, ctgrList)
        spinner.onItemSelectedListener = object: AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if(spinner.getItemAtPosition(position).toString() !="카테고리") {
                    val value = spinner.getItemAtPosition(position).toString()
                    ctgr = helper.selectCtgrMap()[value] as Int?
                    Log.d("test","${ctgr}")
                }else{
                    ctgr = null
                }
            }
        }

        btnSave.setOnClickListener {
            if (content.text.toString().isNotEmpty()){
                val memo = Memo(null, title.text.toString(), content.text.toString(), System.currentTimeMillis(),ctgr)
                helper.insertMemo(memo)
                title.text = ""
                content.text = ""
                spinner.setSelection(0)

            }
        }

    }
}