package com.example.ssgmemo

interface CallbackListener {
    fun callback(cidx: Long)
    fun callmsg()
    fun fragmentOpen(item:String)
    fun addCtgr(ctgrName:String)
}