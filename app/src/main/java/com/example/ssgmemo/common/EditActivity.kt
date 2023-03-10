package com.example.ssgmemo.common

import android.R
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
import android.util.Log
import android.util.TypedValue
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doOnTextChanged
import com.example.ssgmemo.BackPressEditText
import com.example.ssgmemo.Memo
import com.example.ssgmemo.SpinnerModel
import com.example.ssgmemo.SqliteHelper
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
    var readmode = true
    var scroll = false
    val SOFT_KEYBOARD_HEIGHT_DP_THRESHOLD = 128

    private val fontSizeList = listOf("20", "22", "24", "26", "28", "30")

    val helper = SqliteHelper(this, "ssgMemo", 1)
    lateinit var mAdView : AdView
    private lateinit var binding: ActivityWriteBinding
    private val ctgrList = ArrayList<SpinnerModel>()
    var memoIdx: String = "1"
    var memo = Memo(null,"","",1111111,0,0)
    var ctgr: Int = 0
    var priority: Int = 0

    var ischecked = false
    var isBold = false
    var isItalic = false
    var isUnderline = false
    var isLeftAlign = false
    var isCenterAlign = false
    var isRightAlign = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityWriteBinding.inflate(layoutInflater)
        setContentView(binding.root)

        var selectedIndex2 : Int = 0
        var textFontSize : Int = 20

        val content = findViewById<TextView>(com.example.ssgmemo.R.id.writeContent)

        memoIdx = intent.getStringExtra("memoIdx") as String
        val fontSize = intent.getStringExtra("fontSize") as String
        val ctgrMap = helper.selectCtgrMap()

        memo = helper.selectMemo(memoIdx)
        ctgr = memo.ctgr
        priority = memo.priority

        binding.category.margin(top = 70F)

        ctgrList.add(0, SpinnerModel(com.example.ssgmemo.R.drawable.closed_box, "미분류"))
        for (i in helper.selectCtgrMap().values.toMutableList()) {
            val spinnerModel = SpinnerModel(com.example.ssgmemo.R.drawable.closed_box, i)
            ctgrList.add(spinnerModel)
        }

        binding.writeContent.isFocusableInTouchMode = false
        binding.btnCopy.visibility = View.VISIBLE
        binding.btnShare.visibility = View.VISIBLE
        binding.btnDelete.visibility = View.VISIBLE

        binding.btnShare.setOnClickListener {
            val sendIntent: Intent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, "${binding.writeTitle.text.toString() + "\n\n" + binding.writeContent.text.toString()}")
                type = "text/plain"
            }

            val shareIntent = Intent.createChooser(sendIntent, null)
            startActivity(shareIntent)

        }

        binding.btnCopy.setOnClickListener {
            copyToClipboard(binding.writeContent.text.toString())
            Toast.makeText(this, "클립보드에 복사되었습니다", Toast.LENGTH_SHORT).show()
        }

        binding.btnDelete.setOnClickListener {
            fragmentOpen(memo.ctgr!!.toString(),memo.idx.toString(),false)
        }

        // 수정시 버튼 숨김 및 기존 정보 불러오기
        binding.date.visibility = View.VISIBLE
        val t_dateFormat = SimpleDateFormat("yyyy년 M월 d일\n a hh:mm:ss 수정", Locale("ko", "KR"))
        val str_date = t_dateFormat.format(Date(memo.datetime))
        binding.date.text = str_date
        binding.saveMemo.setImageResource(com.example.ssgmemo.R.drawable.read)
        binding.writeTitle.setText(memo.title)
        binding.writeContent.setText(memo.content)
        if (fontSize.equals("ON")) {
            binding.date.textSize = 20f
            binding.writeTitle.textSize = 24f
            binding.writeContent.textSize = 24f
            binding.category.adapter = SpinnerAdapter(this, com.example.ssgmemo.R.layout.item_spinner2, ctgrList)
        } else {
            binding.category.adapter = SpinnerAdapter(this, com.example.ssgmemo.R.layout.item_spinner, ctgrList)
        }

        var selectedIndex : Int = 0
        for (i in ctgrList) {
            if (i.name == ctgrMap[ctgr]) {
                selectedIndex = ctgrList.indexOf(i)
            }
        }

        binding.category.setSelection(selectedIndex)
        binding.category.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }

            // 스피너의 값이 변경될 때 실행
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                val category = binding.category.getItemAtPosition(position) as SpinnerModel
                // 선택된 값이 미분류가 아니면
                if (category.name != "미분류") {
                    ctgr = getKey(helper.selectCtgrMap(), category.name)
                } else {
                    ctgr = 0
                }
            }

            fun <K, V> getKey(map: Map<K, V>, target: V): K {
                return map.keys.first { target == map[it] }
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

        binding.saveMemo.setOnClickListener {
            if (readmode) {
                readmode = false
                binding.saveMemo.setImageResource(com.example.ssgmemo.R.drawable.modify)
                binding.writeContent.isFocusableInTouchMode = true
            } else {
                readmode = true
                binding.saveMemo.setImageResource(com.example.ssgmemo.R.drawable.read)
                binding.writeContent.isFocusableInTouchMode = false
                binding.writeContent.isFocusable = false
                softkeyboardHide()
            }
        }

        binding.writeContent.setOnClickListener {
            if (!scroll) {
                readmode = false
                binding.saveMemo.setImageResource(com.example.ssgmemo.R.drawable.modify)
                binding.writeContent.isFocusableInTouchMode = true
            }
            scroll = false
        }

        binding.writeContent.setOnKeyListener { view, i, keyEvent ->
            if (i == KeyEvent.KEYCODE_ENTER && keyEvent.action == KeyEvent.ACTION_UP && ischecked){
                val mainLayout = binding.writeLayout
                val newLayout = LayoutInflater.from(this).inflate(com.example.ssgmemo.R.layout.item_edittext_checkbox, null)

                mainLayout.addView(newLayout)
                return@setOnKeyListener true
            }
            false
        }

        binding.writeContent.setOnScrollChangeListener { view, i, i2, i3, i4 ->
            scroll = true
            readmode = true
            binding.saveMemo.setImageResource(com.example.ssgmemo.R.drawable.read)
            binding.writeContent.isFocusableInTouchMode = false
            binding.writeContent.isFocusable = false
            softkeyboardHide()
        }

        binding.writeContent.setOnBackPressListener(object : BackPressEditText.OnBackPressListener {
            override fun onBackPress() {
                if ( isKeyboardShown(binding.writeContent.rootView) ) {
                    readmode = true
                    binding.saveMemo.setImageResource(com.example.ssgmemo.R.drawable.read)
                    binding.writeContent.isFocusableInTouchMode = false
                    binding.writeContent.isFocusable = false
                }
            }
        })

        binding.fontBar.visibility = View.GONE
        binding.adView.visibility = View.VISIBLE

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
            layoutParams.height = screenHeight - keyboardHeight - content.y.toInt() - 310
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
        binding.checklist.setOnClickListener {
            ischecked = !ischecked
        }

        // 광고
        MobileAds.initialize(this) {}
        mAdView = findViewById<AdView>(com.example.ssgmemo.R.id.adView)
        val adRequest = AdRequest.Builder().build()
        mAdView.loadAd(adRequest)

    }

    override fun onBackPressed() {
        super.onBackPressed()
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
        if (ctgr != memo.ctgr) {
            priority = if (helper.checkTopMemo(ctgr!!) != null) {
                helper.checkTopMemo(ctgr!!)!! + 1
            } else {
                0
            }
            checkdiff3 = true
        }

        // 제목, 내용, 카테고리 하나라도 변경되었으면 db업뎃
        if (checkdiff1 || checkdiff2 || checkdiff3) {

            val memo_after = Memo(
                memo.idx,
                mTitle,
                mContent,
                System.currentTimeMillis(),
                ctgr,
                priority
            )
            helper.updateMemo(memo_after, checkdiff3, memo.ctgr!!, memo.priority as Int)
        }

        if (checkdiff3) {
            helper.updatePriority(memo.ctgr.toLong())
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val imm: InputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(currentFocus?.windowToken, 0)
        readmode = true
        binding.saveMemo.setImageResource(com.example.ssgmemo.R.drawable.read)
        binding.writeContent.isFocusableInTouchMode = false
        binding.writeContent.isFocusable = false
        return true
    }

    private fun isKeyboardShown(rootView: View): Boolean {
        val r = Rect()
        rootView.getWindowVisibleDisplayFrame(r)
        val dm = rootView.resources.displayMetrics
        val heightDiff = rootView.bottom - r.bottom

        return heightDiff > SOFT_KEYBOARD_HEIGHT_DP_THRESHOLD * dm.density
    }

    fun softkeyboardHide() {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(binding.writeContent.windowToken, 0)
    }

    fun Context.copyToClipboard(text: String) {
        val clipboardManager =
            getSystemService(AppCompatActivity.CLIPBOARD_SERVICE) as ClipboardManager
        val clipData = ClipData.newPlainText("label", text)
        clipboardManager.setPrimaryClip(clipData)
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


    override fun fragmentOpen(memoCtgr: String, memoidx: String, isList:Boolean) {
        super.fragmentOpen(memoCtgr, memoidx, isList)
        // 리스트 인지 하나인지 미분류인지 아닌지...
        val deleteFragment = MemoDeleteFragment(this)
        val bundle:Bundle = Bundle()
        bundle.putString("memoCtgr",memoCtgr)
        bundle.putString("memoidx",memoidx)
        bundle.putBoolean("isList",isList)
        deleteFragment.arguments = bundle
        deleteFragment.show(supportFragmentManager, "memoDelete")
    }
}