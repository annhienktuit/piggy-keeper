package com.annhienktuit.piggykeeper.fragments

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.transition.TransitionInflater
import com.annhienktuit.piggykeeper.R
import com.annhienktuit.piggykeeper.activity.MainActivity
import com.annhienktuit.piggykeeper.activity.TransactionActivity
import com.annhienktuit.piggykeeper.adapter.RecentTransactionAdapter
import com.annhienktuit.piggykeeper.utils.Extensions.changeToMoney
import com.maxkeppeler.sheets.info.InfoSheet


private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"
class HomeFragment : Fragment() {
    lateinit var data: MainActivity
    lateinit var name: String
    lateinit var balance: String
    lateinit var income: String
    lateinit var expense: String


    lateinit var transactionAdapter: RecentTransactionAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val inflater = TransitionInflater.from(requireContext())
        enterTransition = inflater.inflateTransition(R.transition.slide_right)
    }

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_home, container, false)
        setData(view)
        val pref = this.requireActivity()
            .getSharedPreferences("pref", Context.MODE_PRIVATE)
        Log.i("NotiFrag: ", pref.getString("status","").toString())

        val btnSeeAllTransaction = view.findViewById<Button>(R.id.btnSeeAll)
        btnSeeAllTransaction.setOnClickListener {
            val intent = Intent(activity, TransactionActivity::class.java)
            startActivity(intent)
        }
        return view
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            HomeFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
    fun setData(view: View) {
        var recyclerTransaction = view.findViewById(R.id.recyclerTransaction) as RecyclerView
        var emptyView = view.findViewById(R.id.emptyImage) as ImageView
        var emptyText = view.findViewById(R.id.emptyText) as TextView
        val handler = Handler()
        data = (activity as MainActivity)
        transactionAdapter = data.getTransactionAdapter()
        name = data.getName().toString()
        balance = data.getBalance().toString()
        income = data.getIncome().toString()
        expense = data.getExpense().toString()
        setSwipeToDelete(recyclerTransaction)
        recyclerTransaction.adapter = transactionAdapter
        handler.postDelayed(Runnable {
            if(transactionAdapter.itemCount == 0) {
                recyclerTransaction.visibility = View.INVISIBLE
                emptyView.visibility = View.VISIBLE
                emptyText.visibility = View.VISIBLE
            }
            else {
                recyclerTransaction.visibility = View.VISIBLE
                emptyView.visibility = View.INVISIBLE
                emptyText.visibility = View.INVISIBLE
            }

        }, 150)
        recyclerTransaction.layoutManager = LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false)
        recyclerTransaction.setHasFixedSize(true)
        var txtName = view.findViewById<TextView>(R.id.txtName)
        var txtBalance = view.findViewById<TextView>(R.id.textBalance)
        var txtIncome = view.findViewById<TextView>(R.id.textIncome)
        var txtExpense = view.findViewById<TextView>(R.id.textExpense)
        txtName.text = name
        txtBalance.text = changeToMoney(balance) + " USD"
        txtIncome.text = changeToMoney(income)
        txtExpense.text = changeToMoney(expense)
    }
    private fun setSwipeToDelete(rv: RecyclerView) {
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
                if (!transactionAdapter.isSavingTran(position)) {
//                    val dialog = AlertDialog.Builder(context)
//                    dialog.setTitle("Confirm")
//                    dialog.setIcon(R.drawable.ic_baseline_warning_24)
//                    dialog.setMessage("Do you want to delete this transaction?")
//                    dialog.setPositiveButton("OK") { dialog, which ->
//                        transactionAdapter.deleteItem(position, balance, income, expense)
//                    }
//                    dialog.setNegativeButton("Cancel") { dialog, which ->
//                        dialog.dismiss()
//                        rv.adapter!!.notifyDataSetChanged()
//                    }
//                    dialog.show()
                    context?.let {
                        InfoSheet().show(it) {
                            title("Do you want to delete this transaction?")
                            content("You can not undo this action.")
                            onNegative("Cancel") {
                                rv.adapter!!.notifyDataSetChanged()
                            }
                            onPositive("OK") {
                                transactionAdapter.deleteItem(position, balance, income, expense)
                            }
                        }
                    }
                } else {
                    val dialog = AlertDialog.Builder(context)
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

