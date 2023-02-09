package com.example.ssgmemo

import androidx.recyclerview.widget.RecyclerView

interface ItemTouchHelperListener {
    fun onItemMove(from : Int,to:Int) : Boolean
    fun onItemSwipe(position:Int)
}