package com.example.ssgmemo.common

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Rect
import android.graphics.Typeface
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.text.*
import android.text.style.AbsoluteSizeSpan
import android.text.style.AlignmentSpan
import android.text.style.StyleSpan
import android.text.style.UnderlineSpan
import android.util.Log
import android.util.TypedValue
import android.view.KeyEvent
import android.view.LayoutInflater
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

    var ischecked = false
    var isBold = false
    var isItalic = false
    var isUnderline = false
    var isLeftAlign = false
    var isCenterAlign = false
    var isRightAlign = false

    @SuppressLint("MissingInflatedId", "WrongViewCast")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWriteBinding.inflate(layoutInflater)
        setContentView(binding.root)

        var selectedIndex : Int = 0
        var textFontSize : Int = 20
        
        // 설정 state
        val fontSize = intent.getStringExtra("fontSize")
        val vibration = intent.getStringExtra("vibration")

        // view
        val content = findViewById<TextView>(R.id.writeContent)
        val inputContent = binding.inputContent

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

        binding.fontSize.adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, fontSizeList.toMutableList())
        binding.fontSize.onItemSelectedListener = object: AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {}
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                changeFontSize(binding.fontSize.getItemAtPosition(position).toString())
                textFontSize = binding.fontSize.getItemAtPosition(position).toString().toInt()
                selectedIndex = position
            }
        }

        binding.sizedown.setOnClickListener {
            if ( selectedIndex != 0 ) {
                binding.fontSize.setSelection(selectedIndex - 1)
            }
        }

        binding.sizeup.setOnClickListener {
            if ( selectedIndex != fontSizeList.size - 1 ) {
                binding.fontSize.setSelection(selectedIndex + 1)
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
                binding.category.setSelection(0)

                binding.saveMemo.setImageResource(R.drawable.save2)
                val handler = android.os.Handler()
                handler.postDelayed( Runnable { binding.saveMemo.setImageResource(R.drawable.save1) }, 200 ) // 0.5초 후에 다시 닫아주기
            }
        }

        //


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
            // 스크롤 변경
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


        val textWatcher = object : TextWatcher {
            //
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                if (isBold) {
                    s?.setSpan(StyleSpan(Typeface.BOLD), s.length - 1, s.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                }
                if (isItalic) {
                    s?.setSpan(StyleSpan(Typeface.ITALIC), s.length - 1, s.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                }
                if (isUnderline) {
                    s?.setSpan(UnderlineSpan(), s.length - 1, s.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                }

                // s?.setSpan(AbsoluteSizeSpan(dpToPx(textFontSize.toInt())),  s.length - 1, s.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            }
        }

        content.addTextChangedListener(textWatcher)

        binding.bold.setOnClickListener {
            fontStyleChange("bold")
        }

        binding.italic.setOnClickListener {
            fontStyleChange("italic")
        }

        binding.underline.setOnClickListener {
            fontStyleChange("underline")
        }

        binding.leftAlign.setOnClickListener {
            alignChange("leftAlign")
        }

        binding.centerAlign.setOnClickListener {
            alignChange("centerAlign")
        }

        binding.rightAlign.setOnClickListener {
            alignChange("rightAlign")
        }


        // 체크박스...
        // 외부에서 뷰를 초기호 하면 추가할 때 같은 객체를 추가하기 때문에 에러발생
        binding.checklist.setOnClickListener {
            ischecked = !ischecked
            val checkBoxItem = LayoutInflater.from(this).inflate(R.layout.item_edittext_checkbox, null)
            val checkList = checkBoxItem.findViewById<BackPressEditText>(R.id.checkList)
            // 체크리스트에서 엔터를 눌렀을 경우
            checkList.setOnKeyListener { view, i, keyEvent ->
                if (i == KeyEvent.KEYCODE_ENTER && keyEvent.action == KeyEvent.ACTION_UP && ischecked){
                    val mainLayout = binding.inputContent
                    val newLayout = LayoutInflater.from(this).inflate(R.layout.item_edittext_checkbox, null)
                    mainLayout.addView(newLayout,0)
                    return@setOnKeyListener true
                }
                false
            }
            checkList.setGetIndexListener {}
            checkList.setOnClickListener { view ->
                // clickedIndex 인덱스 정보를 저장한 변수
                val clickedIndex = inputContent.indexOfChild(view)
                Log.d("0123","${clickedIndex}")
            }
            if(ischecked){
                inputContent.addView(checkBoxItem,0)

            }else{
                inputContent.removeViewAt(0)
            }
        }

        // 광고
        MobileAds.initialize(this) {}
        mAdView = findViewById<AdView>(R.id.adView)
        val adRequest = AdRequest.Builder().build()
        mAdView.loadAd(adRequest)
    }

    fun fontStyleChange(fontKind: String) {
        val start =  binding.writeContent.selectionStart
        val end =  binding.writeContent.selectionEnd

        if (start == end) { // 드래그하지 않은 경우
            setNextFontStyle(fontKind)
        } else { // 드래그한 경우
            setSeletedFontStyle(fontKind, start, end)
        }
    }

    fun setNextFontStyle(fontKind: String) {
        // 드래그하지 않은 경우
        when (fontKind) {
            "bold" ->
                if (!isBold) {
                    isBold = true
                    setNextSelection()
                } else {
                    isBold = false
                }

            "italic" ->
                if (!isItalic) {
                    isItalic = true
                    setNextSelection()
                } else {
                    isItalic = false
                }

            "underline" ->
                if (!isUnderline) {
                    isUnderline = true
                    setNextSelection()
                } else {
                    isUnderline = false
                }
        }
    }

    fun setSeletedFontStyle(fontKind: String, start: Int, end: Int) {
        // 드래그한 경우
        val spans = binding.writeContent.text!!.getSpans(start, end, StyleSpan::class.java)
        val underlines = binding.writeContent.text!!.getSpans(start, end, UnderlineSpan::class.java)

        if (spans.isNotEmpty() && underlines.isNotEmpty()) {
            for (span in spans) {
                for (underline in underlines) {
                    when (fontKind) {
                        "bold" ->
                            checkStyle(span, start, end, Typeface.BOLD)

                        "italic" ->
                            checkStyle(span, start, end, Typeface.ITALIC)

                        "underline" ->
                            binding.writeContent.text!!.removeSpan(underline)
                    }
                }
            }
        } else if (spans.isNotEmpty()) {
            for (span in spans) {
                when (fontKind) {
                    "bold" ->
                        checkStyle(span, start, end, Typeface.BOLD)

                    "italic" ->
                        checkStyle(span, start, end, Typeface.ITALIC)

                    "underline" ->
                        binding.writeContent.text!!.setSpan(UnderlineSpan(), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

                }
            }
        } else if (underlines.isNotEmpty()) {
            for (underline in underlines) {
                when (fontKind) {
                    "bold" ->
                        binding.writeContent.text!!.setSpan(StyleSpan(Typeface.BOLD), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                    "italic" ->
                        binding.writeContent.text!!.setSpan(StyleSpan(Typeface.ITALIC), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                    "underline" ->
                        binding.writeContent.text!!.removeSpan(underline)
                }
            }
        } else {
            when (fontKind) {
                "bold" ->
                    binding.writeContent.text!!.setSpan(StyleSpan(Typeface.BOLD), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

                "italic" ->
                    binding.writeContent.text!!.setSpan(StyleSpan(Typeface.ITALIC), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

                "underline" ->
                    binding.writeContent.text!!.setSpan(UnderlineSpan(), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            }
        }
    }

    fun checkStyle(span: StyleSpan, start: Int, end: Int, style: Int) {
        binding.writeContent.text!!.removeSpan(span)
        if (span.style == Typeface.BOLD_ITALIC){ // 현재 스타일이 bold_italic일때
            when (style) {
                Typeface.BOLD -> binding.writeContent.text!!.setSpan(StyleSpan(Typeface.ITALIC), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                Typeface.ITALIC -> binding.writeContent.text!!.setSpan(StyleSpan(Typeface.BOLD), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            }
        } else if ( span.style != style ) { // 두 스타일 모두 적용
            binding.writeContent.text!!.setSpan(StyleSpan(Typeface.BOLD_ITALIC), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
    }

    fun setNextSelection(){
        val start = binding.writeContent.length()
        binding.writeContent.append(" ")
        binding.writeContent.setSelection(start, start + 1)
    }

    fun alignChange(alignKind: String) {
        // 현재 커서 위치를 가져옵니다.
        val selectionStart = binding.writeContent.selectionStart
        val layout = binding.writeContent.layout
        val line = layout.getLineForOffset(selectionStart)

        // 현재 커서 위치의 줄의 시작점과 끝점을 가져옵니다.
        val lineStart = layout.getLineStart(line)
        val lineEnd = layout.getLineEnd(line)

        // 현재 커서가 위치한 줄이 비어 있는 경우 공백 문자로 채워줍니다.
        if (lineStart == lineEnd) {
            binding.writeContent.text!!.insert(lineStart, " ")
            binding.writeContent.setSelection(lineStart + 1)
        }

        when (alignKind) {
            "leftAlign" ->
                binding.writeContent.text!!.setSpan(
                    AlignmentSpan.Standard(Layout.Alignment.ALIGN_NORMAL),
                    lineStart,
                    lineEnd,
                    Spannable.SPAN_INCLUSIVE_INCLUSIVE
                )
            "centerAlign" ->
                binding.writeContent.text!!.setSpan(
                    AlignmentSpan.Standard(Layout.Alignment.ALIGN_CENTER),
                    lineStart,
                    lineEnd,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            "rightAlign" ->
                binding.writeContent.text!!.setSpan(
                    AlignmentSpan.Standard(Layout.Alignment.ALIGN_OPPOSITE),
                    lineStart,
                    lineEnd,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
        }
        binding.writeContent.setSelection(selectionStart)
    }

    fun changeFontSize(fontSize: String) {
        val start =  binding.writeContent.selectionStart
        val end =  binding.writeContent.selectionEnd

        if (start == end) { // 드래그하지 않은 경우
            setNextFontSize(fontSize)
        } else { // 드래그한 경우
            setSeletedFontSize(fontSize, start, end)
        }
    }

    fun setNextFontSize(fontSize: String) {
       setNextSelection()
    }

    fun setSeletedFontSize(fontSize: String, start: Int, end: Int) {
        val originalSpans = binding.writeContent.text!!.getSpans(start, end, AbsoluteSizeSpan::class.java)

        // 선택한 텍스트에서 기존에 AbsoluteSizeSpan 스팬을 찾아서 제거합니다.
        for (span in originalSpans) {
            binding.writeContent.text!!.removeSpan(span)
        }

        // 새로운 AbsoluteSizeSpan 스팬을 적용합니다.
        binding.writeContent.text!!.setSpan(AbsoluteSizeSpan(dpToPx(fontSize.toInt())), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
    }

    fun dpToPx(dp: Int) : Int {
        val metrics = resources.displayMetrics
        val px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp.toFloat(), metrics).toInt()
        return px
    }


    override fun onBackPressed() {
       if(backFlag) {
           backKeyHandler.onBackPressed()
       } else {
           super.onBackPressed()
       }
    }
}