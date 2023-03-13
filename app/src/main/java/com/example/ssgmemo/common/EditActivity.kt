package com.example.ssgmemo.common

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Rect
import android.graphics.Typeface
import android.os.Bundle
import android.text.*
import android.text.style.AbsoluteSizeSpan
import android.text.style.AlignmentSpan
import android.text.style.StyleSpan
import android.text.style.UnderlineSpan
import android.util.TypedValue
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doOnTextChanged
import com.example.ssgmemo.*
import com.example.ssgmemo.adapter.SpinnerAdapter
import com.example.ssgmemo.callback.CallbackListener
import com.example.ssgmemo.databinding.ActivityWriteBinding
import com.example.ssgmemo.fragment.MemoDeleteFragment
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import java.text.SimpleDateFormat
import java.util.*

class EditActivity : AppCompatActivity(), CallbackListener {
    private lateinit var binding: ActivityWriteBinding
    private val helper = SqliteHelper(this, "ssgMemo", 1)
    private lateinit var mAdView : AdView

    private val ctgrList = ArrayList<SpinnerModel>()
    private lateinit var fontSizeList: List<String>

    private val SOFT_KEYBOARD_HEIGHT_DP_THRESHOLD = 128

    private var isBold = false
    private var isItalic = false
    private var isUnderline = false

    // edit용 변수
    private var memoIdx = "1"
    private var memo = Memo(null,"","",1111111,0,0, 0)
    private var mCtgr: Int = 0
    private var mPriority: Int = 0
    private var readmode = true
    private var scroll = false
    private var more = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWriteBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // fontSize용 변수
        var selectedIndex = 0
        var textFontSize = 20

        // 설정 state
        val fontSize = intent.getStringExtra("fontSize")
        memoIdx = intent.getStringExtra("memoIdx") as String

        // edit용 설정 및 메모 정보 불러오기
        memo = helper.selectMemo(memoIdx)
        mCtgr = memo.ctgr
        mPriority = memo.priority

        // binding.date.visibility = View.VISIBLE
        val dateFormat = SimpleDateFormat("yyyy년 M월 d일\n a hh:mm:ss 수정", Locale("ko", "KR"))
        binding.date.text = dateFormat.format(Date(memo.datetime))
        binding.saveMemo.setImageResource(com.example.ssgmemo.R.drawable.more)
        binding.btnMode.visibility = View.VISIBLE
        binding.btnDelete.visibility = View.VISIBLE
        binding.writeTitle.setText(memo.title)
        binding.writeContent.setText(memo.content)
        binding.writeContent.isFocusableInTouchMode = false
        binding.fontBar.visibility = View.GONE
        binding.adView.visibility = View.VISIBLE

        // ctgrList
        val ctgrMap = helper.selectCtgrMap()
        ctgrList.add(0, SpinnerModel(com.example.ssgmemo.R.drawable.closed_box, "미분류"))
        for (i in helper.selectCtgrMap().values.toMutableList()) {
            val spinnerModel = SpinnerModel(com.example.ssgmemo.R.drawable.closed_box, i)
            ctgrList.add(spinnerModel)
        }

        // 설정 반영
        if (fontSize.equals("ON")) {
            binding.date.textSize = 20f
            binding.writeTitle.textSize = 24f
            binding.writeContent.textSize = 24f
            binding.category.adapter = SpinnerAdapter(this, com.example.ssgmemo.R.layout.item_spinner2, ctgrList)
            fontSizeList = listOf("24", "26", "28", "30", "32", "34")
        } else {
            binding.category.adapter = SpinnerAdapter(this, com.example.ssgmemo.R.layout.item_spinner, ctgrList)
            fontSizeList = listOf("20", "22", "24", "26", "28", "30")
        }

        // category spinner
        var selectedIndex2 = 0
        for (i in ctgrList) {
            if (i.name == ctgrMap[mCtgr]) {
                selectedIndex2 = ctgrList.indexOf(i)
            }
        }
        binding.category.setSelection(selectedIndex2)
        binding.category.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {}
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val category = binding.category.getItemAtPosition(position) as SpinnerModel
                if (category.name != "미분류") {
                    mCtgr = getKey(helper.selectCtgrMap(), category.name)
                } else {
                    mCtgr = 0
                }
            }
            fun <K, V> getKey(map: Map<K, V>, target: V): K { return map.keys.first { target == map[it] } }
        }

        // mode button
        binding.btnMode.setOnClickListener {
            if (readmode) {
                changeToModify()
            } else {
                changeToRead()
            }
        }

        // writeContent에 mode 적용, 해제
        binding.writeContent.setOnClickListener {
            if (!scroll) {
                changeToModify()
            }
            scroll = false
        }

        // writeContent에서 스크롤 했을 때 scroll 값 변경
        binding.writeContent.setOnScrollChangeListener { view, i, i2, i3, i4 ->
            if (readmode == true) {
                scroll = true
                changeToRead()
            }
        }

        // writeContent에서 뒤로가기 했을 때
        binding.writeContent.setOnBackPressListener(object : BackPressEditText.OnBackPressListener {
            override fun onBackPress() {
                if ( isKeyboardShown(binding.writeContent.rootView) ) {
                    readmode = true
                    binding.btnMode.setImageResource(com.example.ssgmemo.R.drawable.read)
                    binding.writeContent.isFocusableInTouchMode = false
                }
            }
        })

        // 더보기
        binding.saveMemo.setOnClickListener {
            if (more) {
                more = false
                ObjectAnimator.ofFloat(binding.btnCopy,"translationY", -96f).apply {
                    addListener(object : AnimatorListenerAdapter() {
                        override fun onAnimationEnd(animation: Animator?) {
                            // 애니메이션이 시작될 때 호출되는 콜백 메서드
                            binding.btnCopy.visibility = View.INVISIBLE
                        }
                    })
                    start()
                }
                ObjectAnimator.ofFloat(binding.moreButton,"translationY", -96f).apply {
                    addListener(object : AnimatorListenerAdapter() {
                        override fun onAnimationEnd(animation: Animator?) {
                            // 애니메이션이 종료될 때 호출되는 콜백 메서드
                            binding.moreButton.visibility = View.INVISIBLE
                        }
                    })
                    start()
                }

            } else {
                more = true
                ObjectAnimator.ofFloat(binding.moreButton,"translationY", 0f).apply {
                    addListener(object : AnimatorListenerAdapter() {
                        override fun onAnimationStart(animation: Animator?) {
                            // 애니메이션이 시작될 때 호출되는 콜백 메서드
                            binding.moreButton.visibility = View.VISIBLE
                        }
                    })
                    start()
                }
                ObjectAnimator.ofFloat(binding.btnCopy,"translationY", 0f).apply {
                    addListener(object : AnimatorListenerAdapter() {
                        override fun onAnimationStart(animation: Animator?) {
                            // 애니메이션이 시작될 때 호출되는 콜백 메서드
                            binding.btnCopy.visibility = View.VISIBLE
                        }
                    })
                    start()
                }
            }
        }

        // 삭제
        binding.btnDelete.setOnClickListener {
            fragmentOpen(memo.ctgr!!.toString(),memo.idx.toString(),false)
        }

        // 공유
        binding.btnShare.setOnClickListener {
            val sendIntent: Intent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, "${binding.writeTitle.text.toString() + "\n\n" + binding.writeContent.text.toString()}")
                type = "text/plain"
            }

            val shareIntent = Intent.createChooser(sendIntent, null)
            startActivity(shareIntent)
        }

        // 복사
        binding.btnCopy.setOnClickListener {
            copyToClipboard(binding.writeContent.text.toString())
            Toast.makeText(this, "클립보드에 복사되었습니다", Toast.LENGTH_SHORT).show()
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

        // 광고
        MobileAds.initialize(this) {}
        mAdView = findViewById<AdView>(R.id.adView)
        val adRequest = AdRequest.Builder().build()
        mAdView.loadAd(adRequest)

        if(memo.ctgr == -1) {
            binding.btnMode.visibility = View.GONE
            binding.btnShare.visibility = View.GONE
            // binding.date.visibility = View.GONE
            binding.fontBar.visibility = View.GONE
            binding.writeTitle.isFocusableInTouchMode = false
            binding.writeContent.isClickable = false
            binding.writeContent.isFocusable = false
            binding.category.margin(top = 16F)
            binding.saveMemo.setImageResource(com.example.ssgmemo.R.drawable.reset)
            binding.saveMemo.setOnClickListener {
                moveCtgr(memo.idx, mCtgr.toLong())
            }
            binding.writeContent.setOnClickListener {  }
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        if (memo.ctgr != -1) {
            var mTitle = memo.title
            var mContent = memo.content
            var checkdiff1 = false
            var checkdiff2 = false
            var checkdiff3 = false

            if (binding.writeTitle.text.toString() != mTitle) {
                mTitle = if (binding.writeTitle.text.toString() == "") {
                    "빈 제목"
                } else {
                    binding.writeTitle.text.toString()
                }
                checkdiff1 = true
            }
            if (binding.writeContent.text.toString() != mContent) {
                mContent = binding.writeContent.text.toString()
                checkdiff2 = true
            }
            // 카테고리가 변경되었을 때 우선순위 +1 부여
            if (mCtgr != memo.ctgr) {
                mPriority = if (helper.checkTopMemo(mCtgr!!) != null) {
                    helper.checkTopMemo(mCtgr!!)!! + 1
                } else {
                    0
                }
                checkdiff3 = true
            }

            // 제목, 내용, 카테고리 하나라도 변경되었으면 db업뎃
            if (checkdiff1 || checkdiff2 || checkdiff3) {

                val memo_after = Memo(memo.idx, mTitle, mContent, System.currentTimeMillis(), mCtgr, mPriority, 0)
                helper.updateMemo(memo_after, checkdiff3, memo.ctgr!!, memo.priority as Int)
            }

            if (checkdiff3) {
                helper.updatePriority(memo.ctgr.toLong())
            }
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean { // 다른 곳 클릭 시 readMode ON
        val imm: InputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(currentFocus?.windowToken, 0)
        changeToRead()
        return true
    }

    private fun isKeyboardShown(rootView: View): Boolean { // 키보드가 열려있는지 확인
        val r = Rect()
        rootView.getWindowVisibleDisplayFrame(r)
        val dm = rootView.resources.displayMetrics
        val heightDiff = rootView.bottom - r.bottom

        return heightDiff > SOFT_KEYBOARD_HEIGHT_DP_THRESHOLD * dm.density
    }

    fun softkeyboardHide() { // 키보드 숨기기
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(binding.writeContent.windowToken, 0)
    }

    fun Context.copyToClipboard(text: String) { // 클립보드에 복사
        val clipboardManager =
            getSystemService(AppCompatActivity.CLIPBOARD_SERVICE) as ClipboardManager
        val clipData = ClipData.newPlainText("label", text)
        clipboardManager.setPrimaryClip(clipData)
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

    fun View.margin(left: Float? = null, top: Float? = null, right: Float? = null, bottom: Float? = null) {
        layoutParams<ViewGroup.MarginLayoutParams> {
            left?.run { leftMargin = dpToPx(this) }
            top?.run { topMargin = dpToPx(this) }
            right?.run { rightMargin = dpToPx(this) }
            bottom?.run { bottomMargin = dpToPx(this) }
        }
    }

    inline fun <reified T : ViewGroup.LayoutParams> View.layoutParams(block: T.() -> Unit) {
        if (layoutParams is T) block(layoutParams as T)
    }

    fun View.dpToPx(dp: Float): Int = context.dpToPx(dp)
    fun Context.dpToPx(dp: Float): Int = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, resources.displayMetrics).toInt()


    override fun fragmentOpen(memoCtgr: String, memoidx: String, isList:Boolean) { // memoDeleteFragment 오픈
        super.fragmentOpen(memoCtgr, memoidx, isList)
        val deleteFragment = MemoDeleteFragment(this)
        val bundle:Bundle = Bundle()
        bundle.putString("memoCtgr",memoCtgr)
        bundle.putString("memoidx",memoidx)
        bundle.putBoolean("isList",isList)
        deleteFragment.arguments = bundle
        deleteFragment.show(supportFragmentManager, "memoDelete")
    }

    override fun moveCtgr(memoidx: Long?, ctgr: Long) { // 카테고리 이동
        super.moveCtgr(memoidx, ctgr)
        val memo:Memo = helper.selectMemo(memoidx.toString())
        helper.updateMemoCtgr(memoidx, ctgr, helper.getTopPriority(ctgr.toInt()) + 1)
        helper.updatePriority(memo.ctgr.toLong())
        this.finish()
    }

    fun changeToModify() { // 수정모드로 변경
        readmode = false
        binding.btnMode.setImageResource(com.example.ssgmemo.R.drawable.modify)
        binding.writeContent.isFocusableInTouchMode = true
    }

    fun changeToRead() { // 읽기모드로 변경
        readmode = true
        binding.btnMode.setImageResource(com.example.ssgmemo.R.drawable.read)
        binding.writeContent.isFocusableInTouchMode = false
        binding.writeContent.clearFocus()
        softkeyboardHide()
    }
}