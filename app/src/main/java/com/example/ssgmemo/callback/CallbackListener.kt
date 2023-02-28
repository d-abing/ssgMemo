package com.example.ssgmemo.callback

import android.view.View
import com.example.ssgmemo.Ctgr

interface CallbackListener {
    fun callback(cidx: Long) {}
    fun callmsg() {}
    fun fragmentOpen(item:String, ctgridx: String?) {}
    fun fragmentOpen(memoCtgr: Int, memoidx: String){}
    fun fragmentOpen(memoCtgr:String,memoidx:String, isList:Boolean) {}
    fun addCtgr(ctgrName:String) {}
    fun deleteCtgr(ctgridx: String){}
    fun deleteMemo(memoidx: String){}
    fun openKeyBoard(view:View){}
    fun closeKeyBoard(){}
    fun deleteMemoList(){}
    fun deleteMemoFromCtgr(cidx: String) {}
    fun deleteCtgrList(){}
}