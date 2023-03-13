package com.example.ssgmemo.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.annotation.LayoutRes
import androidx.core.content.ContextCompat
import com.example.ssgmemo.R
import com.example.ssgmemo.SpinnerModel
import com.example.ssgmemo.databinding.ItemSpinner2Binding
import com.example.ssgmemo.databinding.ItemSpinnerBinding


class SpinnerAdapter(
    context: Context,
    @LayoutRes private val resId: Int,
    private val values: MutableList<SpinnerModel>
) : ArrayAdapter<SpinnerModel>(context, resId, values) {

    override fun getCount() = values.size


    override fun getItem(position: Int) = values[position]

    @SuppressLint("ViewHolder")
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val binding = if ( resId == R.layout.item_spinner ) { ItemSpinnerBinding.inflate(LayoutInflater.from(parent.context), parent, false) }
        else { ItemSpinner2Binding.inflate(LayoutInflater.from(parent.context), parent, false) }
        val model = values[position]
        try {
            if ( resId == R.layout.item_spinner ) {
                binding as ItemSpinnerBinding
                binding.imgSpinner.setImageResource(model.image)
                binding.txtName.text = model.name
            } else {
                binding as ItemSpinner2Binding
                binding.imgSpinner2.setImageResource(model.image)
                binding.txtName2.text = model.name

            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return binding.root
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        val binding = ItemSpinnerBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        val model = values[position]
        try {
            binding.imgSpinner.setImageResource(model.image)
            binding.txtName.text = model.name

        } catch (e: Exception) {
            e.printStackTrace()
        }
        return binding.root
    }

}