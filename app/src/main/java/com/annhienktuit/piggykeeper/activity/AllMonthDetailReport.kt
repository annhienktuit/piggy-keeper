package com.annhienktuit.piggykeeper.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.annhienktuit.piggykeeper.R
import com.annhienktuit.piggykeeper.`object`.DetailTransaction
import com.annhienktuit.piggykeeper.utils.Extensions
import com.annhienktuit.piggykeeper.utils.FirebaseUtils
import com.anychart.APIlib
import com.anychart.AnyChart
import com.anychart.chart.common.dataentry.DataEntry
import com.anychart.chart.common.dataentry.ValueDataEntry
import com.anychart.charts.Pie
import com.anychart.enums.Align
import com.anychart.enums.LegendLayout
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.activity_all_month_detail_report.*

class AllMonthDetailReport : AppCompatActivity() {
    val user: FirebaseUser? = FirebaseUtils.firebaseAuth.currentUser
    var ref = FirebaseDatabase
        .getInstance("https://my-wallet-80ed7-default-rtdb.asia-southeast1.firebasedatabase.app/")
        .getReference("datas")

    //Variables for current income reporting
    private var listCurrentIncome = mutableListOf<DetailTransaction>()
    private var amountCurrentIncome = 0L
    private var amountCurrentDebt = 0L
    private var listCurrentIncomeData = mutableListOf<DataEntry>()
    //-----------------------------------
    //Variables for current expense reporting
    private var listCurrentExpense = mutableListOf<DetailTransaction>()
    private var amountCurrentExpense = 0L
    private var amountCurrentLoan = 0L
    private var listCurrentExpenseData = mutableListOf<DataEntry>()

    private lateinit var month: String
    private lateinit var year: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_all_month_detail_report)

        if (supportActionBar != null) {
            supportActionBar?.hide();
        }

        btnArrowBack.setOnClickListener {
            finish()
        }

        //get month and year from previous activity list item
        month = intent.getStringExtra("month").toString()
        year = intent.getStringExtra("year").toString()
        //set header UI
        yearTitle.text = year
        monthTitle.text = month
        //---------------------------------------------------

        setCurrentExpensePieChartData()
        setCurrentIncomePieChartData()
    }

    fun setCurrentIncomePieChartData() {
        //reference of Income
        val refIncome = ref.child(user?.uid.toString()).child("transactions").orderByChild("inorout").equalTo("true")

        refIncome.addValueEventListener(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                amountCurrentIncome = 0L
                amountCurrentDebt = 0L

                listCurrentIncome.removeAll(listCurrentIncome)
                listCurrentIncomeData.removeAll(listCurrentIncomeData)

                for(childBranch in snapshot.children){
                    if(childBranch.key.toString() != "total") {
                        listCurrentIncome.add(
                            DetailTransaction(
                                childBranch.child("category").value.toString(),
                                childBranch.child("money").value.toString(),
                                childBranch.child("currentMonth").value.toString(),
                                childBranch.child("currentYear").value.toString()
                            )
                        )
                    }
                }

                listCurrentIncome = handleListForChart(listCurrentIncome, month.toInt())

                for(item in listCurrentIncome){
                    if(item.category == "Debt"){
                        amountCurrentDebt += item.moneyAmount.toLong()
                    }
                    listCurrentIncomeData.add(ValueDataEntry(item.category, item.moneyAmount.toLong()))
                    amountCurrentIncome += item.moneyAmount.toLong()
                }

                income.text = amountCurrentIncome.toString()
                debt.text = amountCurrentDebt.toString()
                balance.text = Extensions.changeToMoney((income.text.toString().toLong() - expense.text.toString().toLong()).toString())

                pieChartIncome.setProgressBar(progressIncomeBar)
                APIlib.getInstance().setActiveAnyChartView(pieChartIncome)
                var pie: Pie = AnyChart.pie()


                pie.data(listCurrentIncomeData)

                pie.title("Current Month Income")

                pie.labels().position("outside")

                pie.legend()
                    .position("center-bottom")
                    .itemsLayout(LegendLayout.HORIZONTAL)
                    .align(Align.CENTER)

                pieChartIncome.setChart(pie)
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })

    }

    fun setCurrentExpensePieChartData() {
        //reference of expense
        val refExpense = ref.child(user?.uid.toString()).child("transactions").orderByChild("inorout").equalTo("false")

        refExpense.addValueEventListener(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                amountCurrentExpense = 0L
                amountCurrentLoan = 0L

                listCurrentExpense.removeAll(listCurrentExpense)
                listCurrentExpenseData.removeAll(listCurrentExpenseData)

                for(childBranch in snapshot.children){
                    if(childBranch.key.toString() != "total") {
                        listCurrentExpense.add(
                            DetailTransaction(
                                childBranch.child("category").value.toString(),
                                childBranch.child("money").value.toString(),
                                childBranch.child("currentMonth").value.toString(),
                                childBranch.child("currentYear").value.toString()
                            )
                        )
                    }
                }

                listCurrentExpense = handleListForChart(listCurrentExpense, month.toInt())

                for(item in listCurrentExpense){
                    if(item.category == "Loan"){
                        amountCurrentLoan += item.moneyAmount.toLong()
                    }
                    listCurrentExpenseData.add(ValueDataEntry(item.category, item.moneyAmount.toLong()))
                    amountCurrentExpense += item.moneyAmount.toLong()
                }

                expense.text = amountCurrentExpense.toString()
                loan.text = amountCurrentLoan.toString()
                balance.text = Extensions.changeToMoney((income.text.toString().toLong() - expense.text.toString().toLong()).toString())


                pieChartExpense.setProgressBar(progressExpenseBar)
                APIlib.getInstance().setActiveAnyChartView(pieChartExpense)
                var pie: Pie = AnyChart.pie()

                pie.data(listCurrentExpenseData)

                pie.title("Current Month Expense")

                pie.labels().position("outside")

                pie.legend()
                    .position("center-bottom")
                    .itemsLayout(LegendLayout.HORIZONTAL)
                    .align(Align.CENTER)

                pieChartExpense.setChart(pie)
            }
            override fun onCancelled(error: DatabaseError) {
            }
        })

    }

    private fun handleListForChart(list: MutableList<DetailTransaction>, month: Int) : MutableList<DetailTransaction>{

        var iForCurrentMonth = 0
        while(iForCurrentMonth < list.size){
            if(list[iForCurrentMonth].currentMonth != month.toString() || list[iForCurrentMonth].currentYear != year){
                list.remove(list[iForCurrentMonth])
                iForCurrentMonth--
            }
            iForCurrentMonth++
        }

        var i = 0;
        while(i < list.size){
            var j = i + 1
            while(j < list.size){
                if(list[j].category == list[i].category){
                    var moneyTemp: Long = list[i].moneyAmount.toLong()
                    moneyTemp += list[j].moneyAmount.toLong()
                    list[i].moneyAmount = moneyTemp.toString()
                    list.remove(list[j])
                    j--
                }
                j++
            }
            i++
        }
        return list
    }
}