package com.example.ssgmemo

import android.content.Context
import android.util.AttributeSet
import android.view.KeyEvent


class BackPressEditText : androidx.appcompat.widget.AppCompatEditText {
    private var _listener: OnBackPressListener? = null
//    private var indexListener: OnGetIndexListener? = null

    constructor(context: Context) : super(context) {}
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {}
    constructor(context: Context, attrs: AttributeSet?, defStyle: Int) : super(
        context,
        attrs,
        defStyle
    )

    override fun onKeyPreIme(keyCode: Int, event: KeyEvent): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK && _listener != null) {
            _listener!!.onBackPress()
        }
        return super.onKeyPreIme(keyCode, event)
    }

//    override fun setOnClickListener(l: OnClickListener?) {
//        indexListener!!.getIndex()
//        super.setOnClickListener(l)
//    }

    fun setOnBackPressListener(`$listener`: OnBackPressListener?) {
        _listener = `$listener`
    }
//    fun setGetIndexListener(listener: () -> Unit){
//        indexListener = listener
//    }

    interface OnBackPressListener {
        fun onBackPress()
    }
//    interface OnGetIndexListener{
//        fun getIndex()
//    }
}
// 위 주석은 클릭 이벤트 발생시 엑션을 위함.