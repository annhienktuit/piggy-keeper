package com.annhienktuit.piggykeeper.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import com.annhienktuit.piggykeeper.R
import com.annhienktuit.piggykeeper.activity.MainActivity
import com.annhienktuit.piggykeeper.utils.FirebaseUtils
import com.annhienktuit.piggykeeper.utils.License
import com.anychart.APIlib
import com.anychart.AnyChart
import com.anychart.AnyChartView
import com.anychart.charts.Pie
import com.anychart.enums.Align
import com.anychart.enums.LegendLayout
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.FirebaseDatabase
import java.text.DecimalFormat
import java.text.NumberFormat

class PreviousMonthFragment : Fragment() {

    private val user: FirebaseUser? = FirebaseUtils.firebaseAuth.currentUser
    var ref = FirebaseDatabase
        .getInstance("https://my-wallet-80ed7-default-rtdb.asia-southeast1.firebasedatabase.app/")
        .getReference("datas")
    lateinit var pieIncomeChart: AnyChartView
    lateinit var pieExpenseChart: AnyChartView
    lateinit var previousBalance: TextView
    lateinit var previousIncome: TextView
    lateinit var previousExpense: TextView
    lateinit var previousDebt: TextView
    lateinit var previousLoan: TextView
    lateinit var progressIncomeBar: ProgressBar
    lateinit var progressExpenseBar: ProgressBar
    //--------------------------------------------
    var amountPreviousLoan: Long = 0
    var amountPreviousExpense: Long = 0
    var amountPreviousDebt: Long = 0
    var amountPreviousIncome: Long = 0
    //---------------------------------------------

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?,
    ): View? {
        // Inflate the layout for this fragment
        val rootView: View = inflater.inflate(R.layout.fragment_previous_month, container, false)
        setData(rootView)
        pieIncomeChart.setLicenceKey(License.anyChartLicense)
        pieExpenseChart.setLicenceKey(License.anyChartLicense)
        setIncomePieChartData()
        setExpensePieChartData()

        return rootView
    }

    private fun setData(rootView: View){
        progressIncomeBar = rootView.findViewById(R.id.progressIncomeBar)
        progressExpenseBar = rootView.findViewById(R.id.progressExpenseBar)
        previousBalance = rootView.findViewById(R.id.balance)
        previousExpense = rootView.findViewById(R.id.expense)
        previousIncome = rootView.findViewById(R.id.income)
        previousDebt = rootView.findViewById(R.id.debt)
        previousLoan = rootView.findViewById(R.id.loan)
        pieIncomeChart = rootView.findViewById(R.id.pieChartIncome)
        pieExpenseChart = rootView.findViewById(R.id.pieChartExpense)
    }

    private fun setIncomePieChartData() {
        val data = (activity as MainActivity)

        var listPieChartData = data.getPreviousIncomeData()
        amountPreviousDebt = data.getPreviousDebt()
        amountPreviousIncome = data.getPreviousIncome()

        previousIncome.text = changeToMoney(amountPreviousIncome.toString())
        previousDebt.text = changeToMoney(amountPreviousDebt.toString())
        previousBalance.text = changeToMoney((amountPreviousIncome - amountPreviousExpense).toString())
        pieIncomeChart.setProgressBar(progressIncomeBar)
        APIlib.getInstance().setActiveAnyChartView(pieIncomeChart)
        var pie: Pie = AnyChart.pie()

        pie.data(listPieChartData)

        pie.title("Previous Month Income")

        pie.labels().position("outside")

        pie.legend()
            .position("center-bottom")
            .itemsLayout(LegendLayout.HORIZONTAL)
            .align(Align.CENTER)

        pieIncomeChart.setChart(pie)
    }

    private fun setExpensePieChartData() {
        val data = (activity as MainActivity)

        var listPieChartData = data.getPreviousExpenseData()
        amountPreviousLoan = data.getPreviousLoan()
        amountPreviousExpense = data.getPreviousExpense()

        previousExpense.text = changeToMoney(amountPreviousExpense.toString())
        previousLoan.text = changeToMoney(amountPreviousLoan.toString())
        previousBalance.text = changeToMoney((amountPreviousIncome - amountPreviousExpense).toString())
        pieExpenseChart.setProgressBar(progressExpenseBar)
        APIlib.getInstance().setActiveAnyChartView(pieExpenseChart)
        var pie: Pie = AnyChart.pie()

        pie.data(listPieChartData)

        pie.title("Previous Month Expense")

        pie.labels().position("outside")

        pie.legend()
            .position("center-bottom")
            .itemsLayout(LegendLayout.HORIZONTAL)
            .align(Align.CENTER)

        pieExpenseChart.setChart(pie)
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