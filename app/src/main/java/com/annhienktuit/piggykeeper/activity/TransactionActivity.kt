package com.annhienktuit.piggykeeper.activity

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.DatePicker
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.annhienktuit.piggykeeper.R
import com.annhienktuit.piggykeeper.`object`.RecentTransaction
import com.annhienktuit.piggykeeper.adapter.RecentTransactionAdapter
import com.annhienktuit.piggykeeper.utils.FirebaseUtils
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_transaction.*
import java.text.SimpleDateFormat
import java.util.*

class TransactionActivity : AppCompatActivity() {

    val user: FirebaseUser? = FirebaseUtils.firebaseAuth.currentUser
    var ref = FirebaseDatabase
        .getInstance("https://my-wallet-80ed7-default-rtdb.asia-southeast1.firebasedatabase.app/")
        .getReference("datas").child(user?.uid.toString())

    val transactionList = ArrayList<RecentTransaction>()
    lateinit var balance: String
    lateinit var income: String
    lateinit var expense: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_transaction)
        setData()
        val format = SimpleDateFormat("dd/MM/yyyy")
        val currentDay = format.format(Date().time)
        btn_pick_day_transaction.text = currentDay
        getDatabase(ref, object : OnGetDataListener {
            override fun onSuccess(dataSnapshot: DataSnapshot) {
                balance = dataSnapshot.child("balance").value.toString()
                income = dataSnapshot.child("income").value.toString()
                expense = dataSnapshot.child("expense").value.toString()
            }
            override fun onStart() {
            }
            override fun onFailure() {
            }
        })
        getDatabase(ref.child("transactions").orderByChild("day")
            .equalTo(btn_pick_day_transaction.text.toString()), object : OnGetDataListener {
            override fun onSuccess(dataSnapshot: DataSnapshot) {
                transactionList.clear()
                for (data in dataSnapshot.children) {
                    val day = data.child("day").value.toString()
                    val index = data.child("index").value.toString()
                    val category = data.child("category").value.toString()
                    var inorout = data.child("inorout").value.toString()
                    var money = data.child("money").value.toString()
                    var name = data.child("name").value.toString()
                    var time = data.child("time").value.toString()
                    transactionList.add(RecentTransaction( index.toIntOrNull(), day, inorout, money, name, time, category))
                }
                transactionList.reverse()
                setData()
            }
            override fun onStart() {
            }
            override fun onFailure() {
            }
        })

        btn_pick_day_transaction.setOnClickListener {
            val calendar = Calendar.getInstance()
            val yearr = calendar.get(Calendar.YEAR)
            val monthh = calendar.get(Calendar.MONTH)
            val dayy = calendar.get(Calendar.DAY_OF_MONTH)
            transactionList.clear()
            val datePicker = DatePickerDialog(this, object : DatePickerDialog.OnDateSetListener {
                override fun onDateSet(view: DatePicker?, year: Int, month: Int, dayOfMonth: Int) {
                    val date = dayOfMonth.toString() + "/" + (month + 1).toString() + "/" + year.toString()
                    val formatter = SimpleDateFormat("dd/MM/yyyy")
                    val tmp1 = formatter.parse(date)
                    val tmp2 = formatter.format(tmp1)
                    btn_pick_day_transaction.text = tmp2.toString()
                    getDatabase(ref.child("transactions").orderByChild("day")
                        .equalTo(btn_pick_day_transaction.text.toString()), object : OnGetDataListener {
                        override fun onSuccess(dataSnapshot: DataSnapshot) {
                            transactionList.clear()
                            for (data in dataSnapshot.children) {
                                val day = data.child("day").value.toString()
                                val index = data.child("index").value.toString()
                                var inorout = data.child("inorout").value.toString()
                                val category = data.child("category").value.toString()
                                var money = data.child("money").value.toString()
                                var name = data.child("name").value.toString()
                                var time = data.child("time").value.toString()
                                transactionList.add(RecentTransaction(index.toIntOrNull(), day, inorout, money, name, time, category))
                            }
                            transactionList.reverse()
                            setData()
                        }
                        override fun onStart() {
                        }
                        override fun onFailure() {
                        }
                    })
                }
            }, yearr, monthh, dayy)
            datePicker.show()
        }
        btn_back_transaction.setOnClickListener {
            onBackPressed()
        }
    }
    private fun setData() {
        recyclerDetailTransaction.removeAllViews()
        recyclerDetailTransaction.adapter = RecentTransactionAdapter(transactionList)
        recyclerDetailTransaction.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        recyclerDetailTransaction.setHasFixedSize(true)
        setSwipeToDelete(recyclerDetailTransaction, RecentTransactionAdapter(transactionList))
    }

    interface OnGetDataListener {
        fun onSuccess(dataSnapshot: DataSnapshot)
        fun onStart()
        fun onFailure()
    }
    fun getDatabase(ref: DatabaseReference, listener: OnGetDataListener?) {
        listener?.onStart()
        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                listener?.onSuccess(snapshot)
            }
            override fun onCancelled(error: DatabaseError) {
                listener?.onFailure()
            }
        })
    }
    fun getDatabase(ref: Query, listener: OnGetDataListener?) {
        listener?.onStart()
        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                listener?.onSuccess(snapshot)
            }
            override fun onCancelled(error: DatabaseError) {
                listener?.onFailure()
            }
        })
    }

    private fun setSwipeToDelete(rv: RecyclerView, adapter: RecentTransactionAdapter) {
        val simpleItemTouchCallback: ItemTouchHelper.SimpleCallback = object :
            ItemTouchHelper.SimpleCallback(30, ItemTouchHelper.LEFT) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, swipeDir: Int) {
                //Remove swiped item from list and notify the RecyclerView
                val position = viewHolder.adapterPosition
                if (!adapter.isSavingTran(position)) {
                    val dialog = AlertDialog.Builder(this@TransactionActivity)
                    dialog.setTitle("Confirm")
                    dialog.setIcon(R.drawable.ic_baseline_warning_24)
                    dialog.setMessage("Do you want to delete this transaction?")
                    dialog.setPositiveButton("OK") { dialog, which ->
                        adapter.deleteItem(position, balance, income, expense)
                    }
                    dialog.setNegativeButton("Cancel") { dialog, which ->
                        dialog.dismiss()
                        rv.adapter!!.notifyDataSetChanged()
                    }
                    dialog.show()
                } else {
                    val dialog = AlertDialog.Builder(this@TransactionActivity)
                    dialog.setMessage("Please go to saving to delete this transaction!")
                    dialog.setPositiveButton("OK") { dialog, which ->
                        dialog.dismiss()
                        rv.adapter!!.notifyDataSetChanged()
                    }
                    dialog.show()
                }
            }
        }
        val itemTouchHelper = ItemTouchHelper(simpleItemTouchCallback)
        itemTouchHelper.attachToRecyclerView(rv)
    }
}