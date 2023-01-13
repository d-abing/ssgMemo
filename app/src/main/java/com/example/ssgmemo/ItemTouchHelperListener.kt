package com.example.ssgmemo

interface ItemTouchHelperListener {
    fun onItemMove(from : Int,to:Int) : Boolean
    fun onItemSwipe(position:Int)
}