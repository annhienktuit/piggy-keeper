package com.annhienktuit.piggykeeper.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.AdapterView.OnItemClickListener
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.get
import com.annhienktuit.piggykeeper.R
import com.annhienktuit.piggykeeper.`object`.DetailTransaction
import com.annhienktuit.piggykeeper.adapter.AllMonthAdapter
import com.annhienktuit.piggykeeper.utils.FirebaseUtils
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.activity_all_month_report.*
import kotlinx.android.synthetic.main.layout_all_month_list_item.view.*


class AllMonthReport : AppCompatActivity() {
    val user: FirebaseUser? = FirebaseUtils.firebaseAuth.currentUser
    var ref = FirebaseDatabase
        .getInstance("https://my-wallet-80ed7-default-rtdb.asia-southeast1.firebasedatabase.app/")
        .getReference("datas")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_all_month_report)

        if (supportActionBar != null) {
            supportActionBar!!.hide();
        }

        btnArrowBack.setOnClickListener{
            finish()
        }

        setListView()
    }

    private fun setListView() {
        val refTrans = ref.child(user?.uid.toString()).child("transactions")

        refTrans.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                var monthlyTransaction = mutableListOf<DetailTransaction>()

                monthlyTransaction.removeAll(monthlyTransaction)
                    for(childBranch in snapshot.children) {
                        if(childBranch.key.toString() != "total"){
                            monthlyTransaction.add(
                                DetailTransaction(
                                    childBranch.child("category").value.toString(),
                                    childBranch.child("money").value.toString(),
                                    childBranch.child("currentMonth").value.toString(),
                                    childBranch.child("currentYear").value.toString(),
                                    childBranch.child("inorout").value.toString()
                                )
                            )
                        }

                    }



                Log.d("monthlyTrans", monthlyTransaction.toString())

                    monthlyTransaction = handleMonthlyTransaction(monthlyTransaction)
                    setReportAdapter(monthlyTransaction)


            }

            override fun onCancelled(error: DatabaseError) {

            }

        })
    }

    private fun handleMonthlyTransaction(listMonthlyTrans: MutableList<DetailTransaction>): MutableList<DetailTransaction> {
        var k = 0;
        // set negative value for expense transaction
        while(k < listMonthlyTrans.size){
            if(listMonthlyTrans[k].inOrOut == "false"){
                listMonthlyTrans[k].moneyAmount = (-listMonthlyTrans[k].moneyAmount.toLong()).toString()
            }
            k++
        }

        //group by month and year
        var i = 0;
        while(i < listMonthlyTrans.size){
            var j = i + 1
            while(j < listMonthlyTrans.size){
                if(listMonthlyTrans[j].currentMonth == listMonthlyTrans[i].currentMonth && listMonthlyTrans[j].currentYear == listMonthlyTrans[i].currentYear){
                    if(listMonthlyTrans[j].inOrOut != "null"){
                        var moneyTemp: Long = listMonthlyTrans[i].moneyAmount.toLong()
                        moneyTemp += listMonthlyTrans[j].moneyAmount.toLong()
                        listMonthlyTrans[i].moneyAmount = moneyTemp.toString()
                        listMonthlyTrans.remove(listMonthlyTrans[j])
                        j--
                        }
                    }
                j++
                }
            i++
            }

        //sort by month and year descending
        listMonthlyTrans.reverse()

        return listMonthlyTrans
    }

    private fun setReportAdapter(list: MutableList<DetailTransaction>){
        val adapter = AllMonthAdapter(this, list)
        listReport.adapter = adapter

        listReport.onItemClickListener = OnItemClickListener { parent, view, position, id ->
            val intent = Intent(this, AllMonthDetailReport::class.java)

            intent.putExtra("month", parent[position].tvMonth.text)
            intent.putExtra("year", parent[position].tvYear.text)

            startActivity(intent)
        }
    }
}