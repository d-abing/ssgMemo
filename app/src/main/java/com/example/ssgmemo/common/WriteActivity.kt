package com.example.ssgmemo.common

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.Rect
import android.graphics.Typeface
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.text.*
import android.text.style.AbsoluteSizeSpan
import android.text.style.ImageSpan
import android.text.style.StyleSpan
import android.text.style.UnderlineSpan
import android.util.Log
import android.util.TypedValue
import android.view.Gravity
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doOnTextChanged
import com.example.ssgmemo.*
import com.example.ssgmemo.adapter.SpinnerAdapter
import com.example.ssgmemo.callback.CallbackListener
import com.example.ssgmemo.databinding.ActivityClassifyBinding.inflate
import com.example.ssgmemo.databinding.ActivityWriteBinding
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds


class WriteActivity : AppCompatActivity(), CallbackListener {
    private lateinit var binding: ActivityWriteBinding
    private val helper = SqliteHelper(this, "ssgMemo", 1)
    private lateinit var mAdView : AdView
    private val backKeyHandler = BackKeyHandler(this)

    private val ctgrList = ArrayList<SpinnerModel>()
    private lateinit var fontSizeList: List<String>

    private var backFlag = false
    private var isBold = false
    private var isItalic = false
    private var isUnderline = false
    private var ischecked = false

    @SuppressLint("MissingInflatedId", "WrongViewCast")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWriteBinding.inflate(layoutInflater)
        setContentView(binding.root)


        // fontSize용 변수
        var selectedIndex : Int = 0
        var textFontSize : Int = 20
        
        // 설정 state
        val fontSize = intent.getStringExtra("fontSize")
        val vibration = intent.getStringExtra("vibration")

        // category spinner
        var mCtgr = 0
        ctgrList.add(0, SpinnerModel(R.drawable.closed_box, "미분류"))
        for (i in helper.selectCtgrMap().values.toMutableList()) {
            val spinnerModel = SpinnerModel(R.drawable.closed_box, i)
            ctgrList.add(spinnerModel)
        }

        fun <K, V> getKey(map: Map<K, V>, target: V): K { return map.keys.first { target == map[it] } }

        // 설정 반영
        if (fontSize.equals("ON")) {
            binding.writeTitle.textSize = 24f
            binding.writeContent.textSize = 24f
            binding.category.adapter = SpinnerAdapter(this, R.layout.item_spinner2, ctgrList) // -----------------반영이 안됨
            fontSizeList = listOf("24", "26", "28", "30", "32", "34")
        } else {
            binding.category.adapter = SpinnerAdapter(this, R.layout.item_spinner, ctgrList)
            fontSizeList = listOf("20", "22", "24", "26", "28", "30")
        }

        binding.category.onItemSelectedListener = object: AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {}
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val category = binding.category.getItemAtPosition(position) as SpinnerModel
                if( category.name != "미분류") {
                    mCtgr = getKey(helper.selectCtgrMap(), category.name)
                } else {
                    mCtgr = 0
                }
            }
        }

        // fontSize spinner
        binding.fontSize.adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, fontSizeList.toMutableList())
        binding.fontSize.onItemSelectedListener = object: AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {}
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                changeFontSize(binding.fontSize.getItemAtPosition(position).toString())
                textFontSize = binding.fontSize.getItemAtPosition(position).toString().toInt()
                selectedIndex = position
            }
        }

        // fontSize spinner 옆 사이즈 조절 버튼
        binding.sizedown.setOnClickListener { // 사이즈 다운
            if ( selectedIndex != 0 ) {
                binding.fontSize.setSelection(selectedIndex - 1)
            }
        }

        binding.sizeup.setOnClickListener { // 사이즈 업
            if ( selectedIndex != fontSizeList.size - 1 ) {
                binding.fontSize.setSelection(selectedIndex + 1)
            }
        }

        // 저장 (saveMemo) ----------------글 스타일 설정 같이 저장되도록 변경해야 함
        binding.saveMemo.setOnClickListener {
            if (binding.writeContent.text.toString().isNotEmpty()) {
                val memo: Memo
                val mTitle = if (binding.writeTitle.text.toString() == "") "빈 제목" else binding.writeTitle.text.toString()
                val mContent = binding.writeContent.text.toString()
                val mDate = System.currentTimeMillis()
                var mPriority = if (helper.checkTopMemo(mCtgr!!) != null) { helper.checkTopMemo(mCtgr!!)!! + 1 } else 0
                var mStatus = 0

                if(vibration.equals("ON")) {
                    val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                    vibrator.vibrate(VibrationEffect.createOneShot(200, 50))
                }

                memo = Memo(null, mTitle, mContent, mDate, mCtgr, mPriority, mStatus)
                helper.insertMemo(memo)

                binding.writeTitle.setText("")
                binding.writeContent.setText("")
                binding.category.setSelection(0)

                binding.saveMemo.setImageResource(R.drawable.save2)
                val handler = android.os.Handler()
                handler.postDelayed( Runnable { binding.saveMemo.setImageResource(R.drawable.save1) }, 200 ) // 0.5초 후에 다시 닫아주기
            }
        }

        // onbackpressed 플래그 조절
        binding.writeContent.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(arg0: Editable) {
                if ( !binding.writeContent.text.toString().trim().equals("") && binding.writeContent.text!!.isNotEmpty() ) {
                    backFlag = true
                } else {
                    backFlag = false
                }
            }
        })

        // writeContent의 크기를 유동적으로 설정
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
            val layoutParams = binding.writeContent.layoutParams
            layoutParams.height = screenHeight - keyboardHeight - binding.writeContent.y.toInt() - 300
            binding.writeContent.layoutParams = layoutParams

            if (startKeyboardHeight > keyboardHeight) {
                binding.adView.visibility = View.VISIBLE
                binding.fontBar.visibility = View.GONE
            } else {
                binding.adView.visibility = View.GONE
                binding.fontBar.visibility = View.VISIBLE
            }
        }

        // writeContent의 텍스트가 변경될 때마다 스크롤 변경
        binding.writeContent.doOnTextChanged { _, _, _, _ ->

            val cursorPosition = binding.writeContent.selectionStart
            val cursorLineIndex = binding.writeContent.layout.getLineForOffset(cursorPosition)
            val lastLineIndex = binding.writeContent.layout.getLineForOffset(binding.writeContent.length())


            when (textFontSize) {
                20 -> scrollChange(cursorLineIndex, lastLineIndex, 9, 86, 82)
                22 -> scrollChange(cursorLineIndex, lastLineIndex, 8, 60, 88)
                24 -> scrollChange(cursorLineIndex, lastLineIndex, 8, 116, 94)
                26 -> scrollChange(cursorLineIndex, lastLineIndex, 7, 85, 102)
                28 -> scrollChange(cursorLineIndex, lastLineIndex, 7, 135, 108)
                30 -> scrollChange(cursorLineIndex, lastLineIndex, 6, 70, 114)
                32 -> scrollChange(cursorLineIndex, lastLineIndex, 6, 125, 122)
                34 -> scrollChange(cursorLineIndex, lastLineIndex, 6, 169, 128)
            }
        }

        // writeContent의 텍스트가 변경될 때마다 스타일 적용
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
                if (isUnderline) {
                    s?.setSpan(UnderlineSpan(), s.length - 1, s.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                }
            }
        }
        binding.writeContent.addTextChangedListener(textWatcher)

        // 텍스트를 지울 때 span 에러가 발생하지 않도록 처리
        binding.writeContent.setOnKeyListener { _, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_DEL && event.action == KeyEvent.ACTION_DOWN) {
                isBold = false
                isItalic = false
                isUnderline = false
                return@setOnKeyListener false
            }
            false
        }

        // font Style 버튼 클릭 이벤트
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
            binding.writeContent.gravity = Gravity.LEFT
        }

        binding.centerAlign.setOnClickListener {
            binding.writeContent.gravity = Gravity.CENTER_HORIZONTAL
        }

        binding.rightAlign.setOnClickListener {
            binding.writeContent.gravity = Gravity.RIGHT
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
//            checkList.setGetIndexListener {}
            checkList.setOnClickListener { view ->
                // clickedIndex 인덱스 정보를 저장한 변수
                val clickedIndex = binding.inputContent.indexOfChild(view)
                Log.d("0123","${clickedIndex}")
            }
            if(ischecked){
                binding.inputContent.addView(checkBoxItem,0)

            }else{
                binding.inputContent.removeViewAt(0)
            }
        }

        // 광고
        MobileAds.initialize(this) {}
        mAdView = findViewById<AdView>(R.id.adView)
        val adRequest = AdRequest.Builder().build()
        mAdView.loadAd(adRequest)
    }

    fun scrollChange(cursorLineIndex:Int, lastLineIndex: Int, maxline: Int, base: Int, multi: Int) {
        // 글 입력 시 스크롤 변경
        var x = cursorLineIndex - maxline
        var scrollY = base + multi * x

        if ( cursorLineIndex > (maxline - 1) && cursorLineIndex == lastLineIndex) {
            val scrollAmount = binding.writeContent.layout.getLineBottom(binding.writeContent.lineCount - 1) - binding.writeContent.height + 65
            binding.writeContent.scrollTo(0, scrollAmount)
        } else if ( cursorLineIndex > (maxline - 1) && !(binding.writeContent.scrollY >= scrollY && binding.writeContent.scrollY <= scrollY + multi * (maxline - 1))) {
            binding.writeContent.scrollTo(0, scrollY)
        } else if ( cursorLineIndex <= (maxline - 1)) {
            binding.writeContent.scrollTo(0, 0)
        }
    }

    fun fontStyleChange(fontKind: String) {
        // B, I, U 폰트 스타일 변경
        val start =  binding.writeContent.selectionStart
        val end =  binding.writeContent.selectionEnd

        if (start == end) { // 드래그하지 않은 경우
            setNextFontStyle(fontKind)
        } else { // 드래그한 경우
            setSeletedFontStyle(fontKind, start, end)
        }
    }

    fun setNextFontStyle(fontKind: String) {
        // 드래그하지 않은 경우 폰트 스타일
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
        // 드래그한 경우 폰트 스타일
        val spans = binding.writeContent.text!!.getSpans(start, end, StyleSpan::class.java)
        val underlines = binding.writeContent.text!!.getSpans(start, end, UnderlineSpan::class.java)

        if (spans.isNotEmpty() && underlines.isNotEmpty()) { // B,I : O / U : O
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
        } else if (spans.isNotEmpty()) { // B,I : O / U : X
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
        } else if (underlines.isNotEmpty()) { // B,I : X / U : O
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
        } else { // B,I : X / U : X
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

    fun checkStyle(span: StyleSpan, start: Int, end: Int, style: Int) { // 이미 B,I 속성이 설정되어 있을 때 속성 확인 후 bold_italic 적용 여부 선택
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

    fun setNextSelection(){ // 드래그 하지 않은 경우 폰트 스타일을 바꾸기 위해서 공백문자열을 삽입해야 함
        val start = binding.writeContent.length()
        binding.writeContent.append(" ")
        binding.writeContent.setSelection(start, start + 1)
    }

    fun changeFontSize(fontSize: String) {
        // font size 변경
        val start =  binding.writeContent.selectionStart
        val end =  binding.writeContent.selectionEnd

        if (start == end) { // 드래그 안한 경우
            binding.writeContent.textSize = fontSize.toFloat()
        } else { // 드래그한 경우
            setSeletedFontSize(fontSize, start, end)
        }
    }

    fun setSeletedFontSize(fontSize: String, start: Int, end: Int) {
        // 드래그 하지 않은 경우 폰트 사이즈
        val originalSpans = binding.writeContent.text!!.getSpans(start, end, AbsoluteSizeSpan::class.java)

        // 선택한 텍스트에서 기존에 AbsoluteSizeSpan 스팬을 찾아서 제거
        for (span in originalSpans) {
            binding.writeContent.text!!.removeSpan(span)
        }

        // 새로운 AbsoluteSizeSpan 스팬을 적용
        binding.writeContent.text!!.setSpan(AbsoluteSizeSpan(dpToPx(fontSize.toInt())), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
    }

    fun dpToPx(dp: Int) : Int {
        val metrics = resources.displayMetrics
        val px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp.toFloat(), metrics).toInt()
        return px
    }


    override fun onBackPressed() {
       if(backFlag) {
           backKeyHandler.onBackPressed() // 텍스트가 있는 경우 [저장하지 않은 메모는 사라집니다] 출력
       } else {
           super.onBackPressed()
       }
    }
}