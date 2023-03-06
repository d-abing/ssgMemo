package com.example.ssgmemo.common

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Rect
import android.graphics.Typeface
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.text.*
import android.text.style.AlignmentSpan
import android.text.style.StyleSpan
import android.text.style.UnderlineSpan
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doOnTextChanged
import com.example.ssgmemo.*
import com.example.ssgmemo.adapter.SpinnerAdapter
import com.example.ssgmemo.callback.CallbackListener
import com.example.ssgmemo.databinding.ActivityWriteBinding
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds


class WriteActivity : AppCompatActivity(), CallbackListener {
    private lateinit var binding: ActivityWriteBinding
    val helper = SqliteHelper(this, "ssgMemo", 1)
    lateinit var mAdView : AdView
    private val ctgrList = ArrayList<SpinnerModel>()
    private val fontSizeList = listOf("20", "22", "24", "26", "28", "30")
    private val backKeyHandler = BackKeyHandler(this)
    var backFlag = false

    @SuppressLint("MissingInflatedId", "WrongViewCast")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWriteBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        // 설정 state
        val fontSize = intent.getStringExtra("fontSize")
        val vibration = intent.getStringExtra("vibration")
        
        // view
        val content = findViewById<TextView>(R.id.writeContent)

        // spinner
        var ctgr = 0
        ctgrList.add(0, SpinnerModel(R.drawable.closed_box, "미분류"))
        for (i in helper.selectCtgrMap().values.toMutableList()) {
            val spinnerModel = SpinnerModel(R.drawable.closed_box, i)
            ctgrList.add(spinnerModel)
        }


        fun <K, V> getKey(map: Map<K, V>, target: V): K { return map.keys.first { target == map[it] } }

        // 설정 반영
        if (fontSize.equals("ON")) {
            binding.writeTitle.textSize = 24f
            content.textSize = 24f
            binding.category.adapter = SpinnerAdapter(this, R.layout.item_spinner2, ctgrList)
        } else {
            binding.category.adapter = SpinnerAdapter(this, R.layout.item_spinner, ctgrList)
        }

        binding.category.onItemSelectedListener = object: AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {}
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val category = binding.category.getItemAtPosition(position) as SpinnerModel
                if( category.name != "미분류") {
                    ctgr = getKey(helper.selectCtgrMap(), category.name)
                } else {
                    ctgr = 0
                }
            }
        }

        binding.spinner.adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, fontSizeList.toMutableList())
        binding.spinner.onItemSelectedListener = object: AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {}
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val contentFontSize = binding.spinner.getItemAtPosition(position)
            }
        }

        // 저장
        binding.saveMemo.setOnClickListener {
            if (content.text.toString().isNotEmpty()) {
                var memo: Memo
                var mTitle = ""
                if (binding.writeTitle.text.toString() == "") mTitle = "빈 제목"
                else mTitle = binding.writeTitle.text.toString()
                var priority = 0

                if(vibration.equals("ON")) {
                    val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                    vibrator.vibrate(VibrationEffect.createOneShot(200, 50))
                }

                // 카테고리가 있으며 첫 글이 아닌 경우 (마지막 우선순위 +1) 부여
                if (helper.checkTopMemo(ctgr!!) !=null){
                    priority = helper.checkTopMemo(ctgr!!)!! + 1
                }

                memo = Memo(
                    null,
                    mTitle,
                    content.text.toString(),
                    System.currentTimeMillis(),
                    ctgr,
                    priority
                )
                helper.insertMemo(memo)
                binding.writeTitle.setText("")
                content.text = ""
                binding.spinner.setSelection(0)

                binding.saveMemo.setImageResource(R.drawable.save2)
                val handler = android.os.Handler()
                handler.postDelayed( Runnable { binding.saveMemo.setImageResource(R.drawable.save1) }, 200 ) // 0.5초 후에 다시 닫아주기
            }
        }

        // content 높이 조절
        val display = this.applicationContext?.resources?.displayMetrics
        val deviceHeight = display?.heightPixels
        val layoutParams = content.layoutParams
        layoutParams.height = deviceHeight?.times(0.75)!!.toInt()
        content.layoutParams = layoutParams

        // onbackpressed 플래그 조절
        content.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(arg0: Editable) {
                if ( !content.text.toString().trim().equals("") && content.text.isNotEmpty() ) {
                    backFlag = true
                } else {
                    backFlag = false
                }
            }
        })


        var startKeyboardHeight: Int = 0

        binding.writeLayout.viewTreeObserver.addOnGlobalLayoutListener {
            // 현재 레이아웃의 크기
            val rect = Rect()
            binding.writeLayout.getWindowVisibleDisplayFrame(rect)

            // 키보드의 높이
            val screenHeight = binding.writeLayout.rootView.height
            val keyboardHeight = screenHeight - rect.bottom

            startKeyboardHeight = if(startKeyboardHeight < keyboardHeight) keyboardHeight else startKeyboardHeight

            // EditText의 크기 조정
            val layoutParams = content.layoutParams
            layoutParams.height = screenHeight - keyboardHeight - content.y.toInt() - 300
            content.layoutParams = layoutParams

            if (startKeyboardHeight > keyboardHeight) {
                binding.adView.visibility = View.VISIBLE
                binding.fontBar.visibility = View.GONE
            } else {
                binding.adView.visibility = View.GONE
                binding.fontBar.visibility = View.VISIBLE
            }
        }

        content.doOnTextChanged { _, _, _, _ ->
            // EditText의 텍스트가 변경될 때마다 실행
            val cursorPosition = content.selectionStart
            val cursorLineIndex = content.layout.getLineForOffset(cursorPosition)
            val lastLineIndex = content.layout.getLineForOffset(content.length())
            var x = cursorLineIndex - 9
            var scrollY = 64 + 76 * x

            if ( cursorLineIndex > 8 && cursorLineIndex == lastLineIndex) {
                val scrollAmount = content.layout.getLineBottom(content.lineCount - 1) - content.height + 65
                content.scrollTo(0, scrollAmount)
            } else if ( cursorLineIndex > 8 && !(content.scrollY >= scrollY && content.scrollY <= scrollY + 608)) {
                val scrollAmount = content.layout.getLineBottom(cursorLineIndex - 1) - content.height + 110
                content.scrollTo(0, scrollY)
            }
        }

        // 드래그한 텍스트를 저장할 변수
        var isSelected: Boolean = false

        // EditText에 터치 리스너 등록
        content.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_UP -> {
                    if (content.hasSelection()) {
                        isSelected = true
                    }
                    false
                }
                else -> false
            }
        }

        var isBold = false
        var isItalic = false

        val textWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                if (isBold) {
                    s?.setSpan(StyleSpan(Typeface.BOLD), s.length - 1, s.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                }
                if (isItalic) {
                    s?.setSpan(StyleSpan(Typeface.ITALIC), s.length - 1, s.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                }
            }
        }

        content.addTextChangedListener(textWatcher)

        binding.bold.setOnClickListener {
            val start = content.selectionStart
            val end = content.selectionEnd
            Log.d("test다", "bold체 클릭")

            if (start == end) { // 드래그하지 않은 경우
                if (!isBold) {
                    // bold 설정
                    isBold = true
                    val start = content.length()
                    content.append(" ")
                    binding.writeContent.setSelection(start, start + 1)
                } else {
                    // bold 설정 제거
                    isBold = false
                }
            } else { // 드래그한 경우
                Log.d("test다", "드래그함")

                val start = content.selectionStart
                val end = content.selectionEnd
                val spans = binding.writeContent.text!!.getSpans(start, end, StyleSpan::class.java)

                if (spans.isNotEmpty()) {
                    for (span in spans) {
                        if (span.style == Typeface.BOLD) {
                            binding.writeContent.text!!.removeSpan(span)
                            binding.writeContent.text!!.setSpan(
                                StyleSpan(Typeface.NORMAL),
                                start,
                                end,
                                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                            )
                        } else {
                            binding.writeContent.text!!.removeSpan(span)
                            binding.writeContent.text!!.setSpan(
                                StyleSpan(Typeface.BOLD),
                                start,
                                end,
                                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                            )
                        }
                    }
                } else {
                    binding.writeContent.text!!.setSpan(
                        StyleSpan(Typeface.BOLD),
                        start,
                        end,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                }
            }
        }



        binding.italic.setOnClickListener {
            if (isSelected) {
                // 드래그한 텍스트가 있을 때 italic체로 변경
                val start = content.selectionStart
                val end = content.selectionEnd
                val ssb = SpannableStringBuilder(content.text)
                ssb.setSpan(StyleSpan(Typeface.ITALIC), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                content.text = ssb
            }
        }

        binding.underline.setOnClickListener {
            if (isSelected) {
                // 드래그한 텍스트가 있을 때 underline체로 변경
                val start = content.selectionStart
                val end = content.selectionEnd
                val ssb = SpannableStringBuilder(content.text)
                ssb.setSpan(UnderlineSpan(), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                content.text = ssb
            }
        }

        binding.leftAlign.setOnClickListener {
            // 현재 커서 위치를 가져옵니다.
            val selectionStart = content.selectionStart
            val layout = content.layout
            val line = layout.getLineForOffset(selectionStart)

            // 현재 커서 위치의 줄의 시작점과 끝점을 가져옵니다.
            val lineStart = layout.getLineStart(line)
            val lineEnd = layout.getLineEnd(line)

            val ssb = SpannableStringBuilder(content.text)
            ssb.setSpan( AlignmentSpan.Standard(Layout.Alignment.ALIGN_NORMAL), lineStart, lineEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            content.text = ssb
        }

        binding.centerAlign.setOnClickListener {
            // 현재 커서 위치를 가져옵니다.
            val selectionStart = content.selectionStart
            val layout = content.layout
            val line = layout.getLineForOffset(selectionStart)

            // 현재 커서 위치의 줄의 시작점과 끝점을 가져옵니다.
            val lineStart = layout.getLineStart(line)
            val lineEnd = layout.getLineEnd(line)

            val ssb = SpannableStringBuilder(content.text)
            ssb.setSpan( AlignmentSpan.Standard(Layout.Alignment.ALIGN_CENTER), lineStart, lineEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            content.text = ssb
        }

        binding.rightAlign.setOnClickListener {
            // 현재 커서 위치를 가져옵니다.
            val selectionStart = content.selectionStart
            val layout = content.layout
            val line = layout.getLineForOffset(selectionStart)

            // 현재 커서 위치의 줄의 시작점과 끝점을 가져옵니다.
            val lineStart = layout.getLineStart(line)
            val lineEnd = layout.getLineEnd(line)

            val ssb = SpannableStringBuilder(content.text)
            ssb.setSpan( AlignmentSpan.Standard(Layout.Alignment.ALIGN_OPPOSITE), lineStart, lineEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            content.text = ssb
        }


        // 이미지버튼 클릭 리스너
        binding.checklist.setOnClickListener {


        }

        // 광고
        MobileAds.initialize(this) {}
        mAdView = findViewById<AdView>(R.id.adView)
        val adRequest = AdRequest.Builder().build()
        mAdView.loadAd(adRequest)
    }

    override fun onBackPressed() {
       if(backFlag) {
           backKeyHandler.onBackPressed()
       } else {
           super.onBackPressed()
       }
    }
}