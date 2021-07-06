package com.annhienktuit.piggykeeper.utils

import android.app.Activity
import android.widget.Toast
import java.text.DecimalFormat
import java.text.NumberFormat

object Extensions {
    fun Activity.toast(msg: String){
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }
    fun changeToMoney(str: String?): String {
        var result = "0"
        try {
            val formatter: NumberFormat = DecimalFormat("#,###")
            if (str != null) {
                val myNumber = str.toDouble()
                if (myNumber < 0)
                    result = "-" + formatter.format(-myNumber)
                else
                    result = formatter.format(myNumber)
            }
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
        return result
    }

}