package com.annhienktuit.piggykeeper.activity


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.annhienktuit.piggykeeper.R
import com.annhienktuit.piggykeeper.`object`.*
import com.annhienktuit.piggykeeper.adapter.SavingDetailAdapter
import com.annhienktuit.piggykeeper.utils.Extensions.changeToMoney
import com.annhienktuit.piggykeeper.utils.FirebaseUtils
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_saving.*
import kotlinx.android.synthetic.main.dialog_add_saving_detail.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class SavingActivity : AppCompatActivity() {
    var savingDetailList = ArrayList<SavingDetail>()
    val savingDetailAdapter = SavingDetailAdapter(savingDetailList)
    val user: FirebaseUser? = FirebaseUtils.firebaseAuth.currentUser
    var ref = FirebaseDatabase
        .getInstance("https://my-wallet-80ed7-default-rtdb.asia-southeast1.firebasedatabase.app/")
        .getReference("datas").child(user?.uid.toString()).child("savings")
    val ref1 = FirebaseDatabase
        .getInstance("https://my-wallet-80ed7-default-rtdb.asia-southeast1.firebasedatabase.app/")
        .getReference("datas").child(user?.uid.toString())
    var refTrans = ref1.child("transactions")
    //-----------------------------------------------
    var saving: Saving? = null
    var totalDetail: Int = 0

    //---------------------------------------------
    var pos: Int = 0
    var current: String? = null
    var total: String? = null
    var nameProduct: String? = null

    //---------------------------------------------
    var expense: String? = null
    var balance: String? = null
    var totalTrans: Int = 0
    var left: Long = 0
    //---------------------------------------------
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_saving)
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN
        ref.keepSynced(true)
        pos = intent.getIntExtra("position", 0)
        //Get total expense
        getDatabase(ref1, object : OnGetDataListener {
            override fun onSuccess(dataSnapshot: DataSnapshot) {
                expense = dataSnapshot.child("expense").value.toString()
                balance = dataSnapshot.child("balance").value.toString()
            }

            override fun onStart() {
            }

            override fun onFailure() {
            }
        })
        //Get total transaction
        getDatabase(ref1.child("transactions"), object : OnGetDataListener {
            override fun onSuccess(dataSnapshot: DataSnapshot) {
                totalTrans = dataSnapshot.child("total").value.toString().toInt()
                refTrans = ref1.child("transactions").child("transaction" + (totalTrans + 1))
            }

            override fun onStart() {
            }

            override fun onFailure() {
            }
        })
        //Get detail transaction
        getDatabase(ref.child("saving" + pos).child("details").orderByChild("index"),
            object : OnGetDataListener {
                override fun onSuccess(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.hasChild("total"))
                        totalDetail = dataSnapshot.child("total").value.toString().toIntOrNull()!!
                    savingDetailList.clear()
                    for (data in dataSnapshot.children) {
                        if (data.key.toString() != "total") {
                            var tmp1 = data.child("cost").value.toString()
                            var tmp2 = data.child("day").value.toString()
                            var tmp3 = data.child("time").value.toString()
                            var tmp4 = data.child("transName").value.toString()
                            var tmp5 = data.child("index").value.toString()
                            savingDetailList.add(SavingDetail(tmp1, tmp2, tmp3, tmp4, tmp5.toIntOrNull()))
                        }
                    }
                    savingDetailList.reverse()
                }

                override fun onStart() {

                }

                override fun onFailure() {

                }
            })
        getDatabase(ref.child("saving$pos"), object : OnGetDataListener {
            override fun onSuccess(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    val index = dataSnapshot.child("index").value.toString()
                    val tmp1 = dataSnapshot.child("current").value.toString()
                    val tmp2 = dataSnapshot.child("price").value.toString()
                    val tmp3 = dataSnapshot.child("product").value.toString()
                    current = tmp1
                    total = tmp2
                    nameProduct = tmp3
                    left = tmp2.toLong() - tmp1.toLong()
                    saving = Saving(index.toIntOrNull(), tmp1, savingDetailList, tmp2, tmp3)
                    setData(saving)
                }
            }

            override fun onStart() {

            }

            override fun onFailure() {

            }
        })
        btn_back_saving.setOnClickListener {
            finish()
        }
        floatingAdd.setOnClickListener {
            eventOnClickAddButton()
        }

    }

    private fun eventOnClickAddButton() {
        val builder = AlertDialog.Builder(this)
        val viewInflater =
            LayoutInflater.from(this).inflate(R.layout.dialog_add_saving_detail, null, false)
        val editName = viewInflater.findViewById<EditText>(R.id.editNameSaving)
        val editMoney = viewInflater.findViewById<EditText>(R.id.editMoneySaving)
        builder.setView(viewInflater)
        builder.setPositiveButton(
            "OK"
        ) { dialog, which ->
            val name = editName.text.toString()
            var money = editMoney.text.toString()
            if (money.toLong() > left) money = left.toString()
            var date = Calendar.getInstance()
            var dayFormatter = SimpleDateFormat("dd/MM/yyyy")
            var timeFormatter = SimpleDateFormat("HH:mm")
            var day = dayFormatter.format(date.time)
            var time = timeFormatter.format(date.time)
            val ref3 = ref.child("saving" + pos)
            val ref2 =
                ref.child("saving" + pos).child("details").child("detail" + (totalDetail + 1))
            //Set data in main activity recent transaction
            ref1.child("transactions").child("total").setValue(totalTrans + 1)
            ref1.child("expense").setValue((expense?.toLong()?.plus(money.toLong())).toString())
            ref1.child("balance").setValue((balance?.toLong()?.minus(money.toLong())).toString())
            refTrans.child("day").setValue(day)
            refTrans.child("money").setValue(money)
            refTrans.child("name").setValue("Saving from " + nameProduct)
            refTrans.child("time").setValue(time)
            refTrans.child("inorout").setValue("false")
            refTrans.child("index").setValue(totalTrans + 1)
            refTrans.child("category").setValue("Saving")
            refTrans.child("currentMonth").setValue((date.get(Calendar.MONTH) + 1).toString())
            refTrans.child("currentYear").setValue(date.get(Calendar.YEAR).toString())
            //Set data in saving transaction
            ref3.child("details").child("total").setValue(totalDetail + 1)
            ref2.child("index").setValue(totalDetail + 1)
            ref2.child("cost").setValue(money)
            ref2.child("day").setValue(day)
            ref2.child("time").setValue(time)
            ref2.child("transName").setValue(name)
            ref3.child("current")
                .setValue((saving!!.currentSaving!!.toLong() + money.toLong()).toString())
            dialog.dismiss()
        }
        builder.setNegativeButton(
            "Cancel"
        ) { dialog, which -> dialog.dismiss() }
        builder.create()
        builder.show()
    }

    private fun setData(saving: Saving?) {
        recyclerTransactionSaving.adapter = SavingDetailAdapter(savingDetailList)
        setSwipeToDelete(recyclerTransactionSaving)
        recyclerTransactionSaving.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        recyclerTransactionSaving.setHasFixedSize(true)
        nameOfSavingProduct.text = saving?.nameOfProduct.toString()
        if (total != null && current != null) {
            totalSaving.text = "of " + changeToMoney(total) + " USD"
            currentSaving.text = changeToMoney(current) + " "
        }
        val tmp1 = current?.toLong()
        val tmp2 = total?.toLong()
        if (tmp1 == tmp2) {
            notifyCompleted.text = "You have enough money to buy this product"
            floatingAdd.isEnabled = false
        } else {
            notifyCompleted.text =
                "You need to save " + changeToMoney((tmp2!! - tmp1!!).toString()) + " USD"
            floatingAdd.isEnabled = true
        }
        val per = tmp1!! * 100 / tmp2!!
        percentage.text = "$per%"
        progressSavings.progress = per.toInt()
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
                val dialog = AlertDialog.Builder(this@SavingActivity)
                dialog.setTitle("Confirm")
                dialog.setIcon(R.drawable.ic_baseline_warning_24)
                dialog.setMessage("Do you want to delete this transaction?")
                dialog.setPositiveButton("OK") { dialog, which ->
                    savingDetailAdapter.deleteItem(position, balance.toString(), expense.toString(), pos,
                        current.toString()
                    )
                }
                dialog.setNegativeButton("Cancel") { dialog, which ->
                    dialog.dismiss()
                    rv.adapter!!.notifyDataSetChanged()
                }
                dialog.show()
            }
        }
        val itemTouchHelper = ItemTouchHelper(simpleItemTouchCallback)
        itemTouchHelper.attachToRecyclerView(rv)
    }
}
