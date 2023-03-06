package com.example.ssgmemo

import android.app.Activity
import android.util.Log
import android.widget.Toast
import com.example.ssgmemo.callback.CallbackListener


class BackKeyHandler(private val activity: Activity) {
    private var backKeyPressedTime: Long = 0
    private var toast: Toast? = null
    lateinit var callbackListener: CallbackListener

    fun onBackPressed() {
        if (System.currentTimeMillis() > backKeyPressedTime + 3000) {
            backKeyPressedTime = System.currentTimeMillis()
            showGuide()
            return
        }
        if (System.currentTimeMillis() <= backKeyPressedTime + 3000) {
            toast!!.cancel()
            activity.finish()
        }
    }

    private fun showGuide() {
        toast = Toast.makeText(activity, "저장되지 않은 메모는 사라집니다", Toast.LENGTH_SHORT)
        toast!!.show()
    }
}