package com.annhienktuit.piggykeeper.adapter

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import com.annhienktuit.piggykeeper.R
import com.annhienktuit.piggykeeper.`object`.DetailTransaction
import com.annhienktuit.piggykeeper.utils.Extensions

class AllMonthAdapter(private val context: Context, private val dataSource: MutableList<DetailTransaction>) : BaseAdapter() {
    private val inflater: LayoutInflater
            = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    override fun getCount(): Int {
        return dataSource.size
    }

    override fun getItem(position: Int): Any {
        return dataSource[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val rowView = inflater.inflate(R.layout.layout_all_month_list_item, parent, false)

        val year = rowView.findViewById<TextView>(R.id.tvYear)
        val month = rowView.findViewById<TextView>(R.id.tvMonth)
        val balance = rowView.findViewById<TextView>(R.id.tvBalance)
        val mood = rowView.findViewById<ImageView>(R.id.ivMood)
        val vndText = rowView.findViewById<TextView>(R.id.tvUSD)

        val trans = getItem(position) as DetailTransaction

        month.text = trans.currentMonth
        year.text = trans.currentYear

        balance.text = Extensions.changeToMoney(trans.moneyAmount)

        if(trans.moneyAmount.toLong() < 0){
                mood.setImageResource(R.drawable.ic_baseline_mood_bad_24)
                balance.setTextColor(Color.parseColor("#f44336"))
                vndText.setTextColor(Color.parseColor("#f44336"))
        }
        else{
                mood.setImageResource(R.drawable.ic_baseline_mood_24)
            }


        return rowView
    }

}