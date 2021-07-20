package com.annhienktuit.piggykeeper.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
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


class CurrentMonthFragment : Fragment() {

    private val user: FirebaseUser? = FirebaseUtils.firebaseAuth.currentUser
    var ref = FirebaseDatabase
        .getInstance("https://my-wallet-80ed7-default-rtdb.asia-southeast1.firebasedatabase.app/")
        .getReference("datas")
    lateinit var pieIncomeChart: AnyChartView
    lateinit var pieExpenseChart: AnyChartView
    lateinit var currentBalance: TextView
    lateinit var currentIncome: TextView
    lateinit var currentExpense: TextView
    lateinit var currentDebt: TextView
    lateinit var currentLoan: TextView
    lateinit var progressIncomeBar: ProgressBar
    lateinit var progressExpenseBar: ProgressBar
    //---------------------------------------------
    var amountCurrentExpense: Long = 0
    var amountCurrentIncome: Long = 0
    var amountCurrentDebt: Long = 0
    var amountCurrentLoan: Long = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val rootView: View = inflater.inflate(R.layout.fragment_current_month, container, false)
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
        currentBalance = rootView.findViewById(R.id.balance)
        currentExpense = rootView.findViewById(R.id.expense)
        currentIncome = rootView.findViewById(R.id.income)
        currentDebt = rootView.findViewById(R.id.debt)
        currentLoan = rootView.findViewById(R.id.loan)
        pieIncomeChart = rootView.findViewById(R.id.pieChartIncome)
        pieExpenseChart = rootView.findViewById(R.id.pieChartExpense)
    }

    private fun setIncomePieChartData() {
        val data = (activity as MainActivity)

        var listPieChartData = data.getCurrentIncomeData()
        amountCurrentDebt = data.getCurrentDebt()
        amountCurrentIncome = data.getCurrentIncome()

        currentIncome.text = changeToMoney(amountCurrentIncome.toString())
        currentDebt.text = changeToMoney(amountCurrentDebt.toString())
        currentBalance.text = changeToMoney((amountCurrentIncome - amountCurrentExpense).toString())
        pieIncomeChart.setProgressBar(progressIncomeBar)
        APIlib.getInstance().setActiveAnyChartView(pieIncomeChart)
        var pie: Pie = AnyChart.pie()
        pie.data(listPieChartData)

        pie.title("Current Month Income")

        pie.labels().position("outside")

        pie.legend()
            .position("center-bottom")
            .itemsLayout(LegendLayout.HORIZONTAL)
            .align(Align.CENTER)
                pieIncomeChart.setChart(pie)
    }


    private fun setExpensePieChartData() {
        val data = (activity as MainActivity)

        var listPieChartData = data.getCurrentExpenseData()
        amountCurrentLoan = data.getCurrentLoan()
        amountCurrentExpense = data.getCurrentExpense()

        currentExpense.text = changeToMoney(amountCurrentExpense.toString())
        currentLoan.text = changeToMoney(amountCurrentLoan.toString())
        currentBalance.text = changeToMoney((amountCurrentIncome - amountCurrentExpense).toString())
        pieExpenseChart.setProgressBar(progressExpenseBar)
        APIlib.getInstance().setActiveAnyChartView(pieExpenseChart)
        var pie: Pie = AnyChart.pie()


        pie.data(listPieChartData)

        pie.title("Current Month Expense")

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