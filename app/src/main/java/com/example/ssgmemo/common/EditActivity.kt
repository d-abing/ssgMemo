package com.example.ssgmemo.common

import android.R
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Rect
import android.graphics.Typeface
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.text.Layout
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.AlignmentSpan
import android.text.style.StyleSpan
import android.text.style.UnderlineSpan
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.marginTop
import androidx.core.widget.doOnTextChanged
import com.example.ssgmemo.BackPressEditText
import com.example.ssgmemo.Memo
import com.example.ssgmemo.SpinnerModel
import com.example.ssgmemo.SqliteHelper
import com.example.ssgmemo.adapter.SpinnerAdapter
import com.example.ssgmemo.databinding.ActivityWriteBinding
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import java.text.SimpleDateFormat
import java.util.*

class EditActivity : AppCompatActivity() {
    var readmode = true
    var scroll = false
    val SOFT_KEYBOARD_HEIGHT_DP_THRESHOLD = 128

    val helper = SqliteHelper(this, "ssgMemo", 1)
    lateinit var mAdView : AdView
    private lateinit var binding: ActivityWriteBinding
    private val ctgrList = ArrayList<SpinnerModel>()
    var memoIdx: String = "1"
    var memo = Memo(null,"","",1111111,0,0)
    var ctgr: Int = 0
    var priority: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityWriteBinding.inflate(layoutInflater)
        setContentView(binding.root)


        val content = findViewById<TextView>(com.example.ssgmemo.R.id.writeContent)

        memoIdx = intent.getStringExtra("memoIdx") as String
        val fontSize = intent.getStringExtra("fontSize") as String
        val ctgrMap = helper.selectCtgrMap()

        memo = helper.selectMemo(memoIdx)
        ctgr = memo.ctgr
        priority = memo.priority

        ctgrList.add(0, SpinnerModel(com.example.ssgmemo.R.drawable.closed_box, "미분류"))
        for (i in helper.selectCtgrMap().values.toMutableList()) {
            val spinnerModel = SpinnerModel(com.example.ssgmemo.R.drawable.closed_box, i)
            ctgrList.add(spinnerModel)
        }

        binding.writeContent.isFocusableInTouchMode = false
        binding.btnCopy.visibility = View.VISIBLE
        binding.btnShare.visibility = View.VISIBLE

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

        // content 높이 조절
        val display = this.applicationContext?.resources?.displayMetrics
        val deviceHeight = display?.heightPixels
        val layoutParams = binding.writeContent.layoutParams
        layoutParams.height = deviceHeight?.times(0.7)!!.toInt()
        binding.writeContent.layoutParams = layoutParams

        // 수정시 버튼 숨김 및 기존 정보 불러오기
        binding.date.visibility = View.VISIBLE
        val t_dateFormat = SimpleDateFormat("yyyy년 M월 d일 a hh:mm:ss 수정", Locale("ko", "KR"))
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

        binding.saveMemo.setOnClickListener {
            if (readmode) {
                readmode = false
                binding.saveMemo.setImageResource(com.example.ssgmemo.R.drawable.modify)
                binding.writeContent.isFocusableInTouchMode = true
                binding.fontBar.visibility = View.VISIBLE
                binding.adView.visibility = View.GONE
            } else {
                readmode = true
                binding.saveMemo.setImageResource(com.example.ssgmemo.R.drawable.read)
                binding.writeContent.isFocusableInTouchMode = false
                binding.writeContent.isFocusable = false
                softkeyboardHide()
                binding.fontBar.visibility = View.GONE
                binding.adView.visibility = View.VISIBLE
            }
        }

        binding.writeContent.setOnClickListener {
            if (!scroll) {
                readmode = false
                binding.saveMemo.setImageResource(com.example.ssgmemo.R.drawable.modify)
                binding.writeContent.isFocusableInTouchMode = true
                binding.fontBar.visibility = View.VISIBLE
                binding.adView.visibility = View.GONE
            }
            scroll = false
        }

        binding.writeContent.setOnScrollChangeListener { view, i, i2, i3, i4 ->
            scroll = true
            readmode = true
            binding.saveMemo.setImageResource(com.example.ssgmemo.R.drawable.read)
            binding.writeContent.isFocusableInTouchMode = false
            binding.writeContent.isFocusable = false
            softkeyboardHide()
            binding.fontBar.visibility = View.GONE
            binding.adView.visibility = View.VISIBLE
        }

        binding.writeContent.setOnBackPressListener(object : BackPressEditText.OnBackPressListener {
            override fun onBackPress() {
                if ( isKeyboardShown(binding.writeContent.rootView) ) {
                    readmode = true
                    binding.saveMemo.setImageResource(com.example.ssgmemo.R.drawable.read)
                    binding.writeContent.isFocusableInTouchMode = false
                    binding.writeContent.isFocusable = false
                    binding.fontBar.visibility = View.VISIBLE
                    binding.adView.visibility = View.GONE
                }
            }
        })

        binding.fontBar.visibility = View.GONE
        binding.adView.visibility = View.VISIBLE

        binding.writeLayout.viewTreeObserver.addOnGlobalLayoutListener {
            // 현재 레이아웃의 크기
            val rect = Rect()
            binding.writeLayout.getWindowVisibleDisplayFrame(rect)

            // 키보드의 높이
            val screenHeight = binding.writeLayout.rootView.height
            val keyboardHeight = screenHeight - rect.bottom

            // EditText의 크기 조정
            val layoutParams = content.layoutParams
            layoutParams.height = screenHeight - keyboardHeight - content.y.toInt() - 300
            content.layoutParams = layoutParams

            if (layoutParams.height == screenHeight) {
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
                Log.d("test다", "마지막줄")
            } else if ( cursorLineIndex > 8 && !(content.scrollY >= scrollY && content.scrollY <= scrollY + 608)) {
                val scrollAmount = content.layout.getLineBottom(cursorLineIndex - 1) - content.height + 110
                content.scrollTo(0, scrollY)
                Log.d("test다", "중간줄")
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

        binding.bold.setOnClickListener {
            if (isSelected) {
                // 드래그한 텍스트가 있을 때 bold체로 변경
                val start = content.selectionStart
                val end = content.selectionEnd
                val ssb = SpannableStringBuilder(content.text)
                ssb.setSpan(StyleSpan(Typeface.BOLD), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                content.text = ssb
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
        val clipboardManager = getSystemService(AppCompatActivity.CLIPBOARD_SERVICE) as ClipboardManager
        val clipData = ClipData.newPlainText("label", text)
        clipboardManager.setPrimaryClip(clipData)
    }

}