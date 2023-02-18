package com.example.ssgmemo.callback

import com.example.ssgmemo.Ctgr

interface CallbackListener {
    fun callback(cidx: Long) {}
    fun callmsg() {}
    fun fragmentOpen(item:String, ctgridx: String?) {}
    fun fragmentOpen(memoCtgr:Int,memoidx:String) {}
    fun addCtgr(ctgrName:String) {}
    fun deleteCtgr(ctgridx: String){}
    fun deleteMemo(memoidx: String){}
}