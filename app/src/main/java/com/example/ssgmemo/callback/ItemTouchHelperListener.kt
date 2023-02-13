package com.example.ssgmemo.callback

import androidx.recyclerview.widget.RecyclerView

interface ItemTouchHelperListener {
    fun onItemMove(from : Int,to:Int) : Boolean
}