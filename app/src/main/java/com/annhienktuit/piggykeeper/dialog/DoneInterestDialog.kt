package com.annhienktuit.piggykeeper.dialog

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.annhienktuit.piggykeeper.R
import java.text.DecimalFormat

class DoneInterestDialog: DialogFragment() {
    private var numberOfPeriods: Int = 0
    private var amountOfMoney: Long = 0
    private var profitAmount: Double = 0.0
    private var total: Double = 0.0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView: View = inflater.inflate(R.layout.dialog_done_interest_rate, container, false)

        val closeBtn: Button = rootView.findViewById(R.id.closeBtn) as Button
        val tvNumberOfPeriod: TextView = rootView.findViewById(R.id.numberOfPeriods) as TextView
        val tvAmountOfMoney: TextView = rootView.findViewById(R.id.amountOfMoney) as TextView
        val tvProfitAmount: TextView = rootView.findViewById(R.id.profitAmount) as TextView
        val tvTotal: TextView = rootView.findViewById(R.id.total) as TextView

        //cập nhật các argument chuyền qua bằng bundle
        setArguments()

        //Đổi text view tương ứng theo giá trị vừa cập nhật
        tvNumberOfPeriod.text = numberOfPeriods.toString()
        tvAmountOfMoney.text = amountOfMoney.toString()
        tvProfitAmount.text = String.format("%.2f", profitAmount)
        tvTotal.text = String.format("%.2f", total)

        closeBtn.setOnClickListener{
            dismiss()
        }

        return rootView
    }

    private fun setArguments(){
        if(arguments?.getString("type") == "Compound"){
            numberOfPeriods = requireArguments().getInt("numberOfPeriods")
            amountOfMoney = requireArguments().getLong("amountOfMoney")
            profitAmount = requireArguments().getDouble("compoundProfit")
            total = roundTwoDecimals(requireArguments().getDouble("compoundTotal"))
        }
        else if(arguments?.getString("type") == "Simple"){
            numberOfPeriods = requireArguments().getInt("numberOfPeriods")
            amountOfMoney = requireArguments().getLong("amountOfMoney")
            profitAmount = requireArguments().getDouble("simpleProfit")
            total = roundTwoDecimals(requireArguments().getDouble("simpleTotal"))
            Log.i("total", "$total")
        }
    }
    fun roundTwoDecimals(d: Double): Double {
        val twoDForm = DecimalFormat("#.##")
        return java.lang.Double.valueOf(twoDForm.format(d))
    }
}