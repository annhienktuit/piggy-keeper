package com.annhienktuit.piggykeeper.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.transition.TransitionInflater
import com.annhienktuit.piggykeeper.R
import com.annhienktuit.piggykeeper.activity.MainActivity
import com.annhienktuit.piggykeeper.adapter.CardAdapter
import com.annhienktuit.piggykeeper.adapter.LimitationAdapter
import com.annhienktuit.piggykeeper.adapter.SavingAdapter
import com.annhienktuit.piggykeeper.utils.FirebaseUtils
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.FirebaseDatabase
import java.util.*
import kotlin.collections.ArrayList


private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"


class PlanningFragment() : Fragment() {

    private var param1: String? = null
    private var param2: String? = null
    lateinit var data: MainActivity
    //-----------------------------------------------
    val user: FirebaseUser? = FirebaseUtils.firebaseAuth.currentUser
    var ref = FirebaseDatabase
        .getInstance("https://my-wallet-80ed7-default-rtdb.asia-southeast1.firebasedatabase.app/")
        .getReference("datas").child(user?.uid.toString())
    //--------------------------------------------------
    lateinit var recyclerSaving: RecyclerView
    lateinit var recyclerCard: RecyclerView
    lateinit var recyclerLimitation: RecyclerView

    lateinit var limitationAdapter: LimitationAdapter
    lateinit var savingAdapter: SavingAdapter
    lateinit var cardAdapter: CardAdapter

    lateinit var btnAddLimitation: MaterialButton
    lateinit var btnAddSaving: MaterialButton
    lateinit var btnAddCard: MaterialButton
    //------------------------------------
    var totalTrans: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val inflater = TransitionInflater.from(requireContext())
        enterTransition = inflater.inflateTransition(R.transition.slide_right)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
        //open add limitation dialog
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        var view = inflater.inflate(R.layout.fragment_planning, container, false)
        data = activity as MainActivity
        totalTrans = data.getIndexTransaction()
        setData(view)

        //open add saving dialog
        btnAddSaving.setOnClickListener {
            addSavingInfo()
        }

        //open add limitation dialog
        btnAddLimitation.setOnClickListener {
            addLimitationInfo()
        }

        //open add card dialog
        btnAddCard.setOnClickListener {
            addCardInfo()
        }

        return view
    }

    companion object {

        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            PlanningFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

    private fun setData(view: View) {
        //------------------------
        savingAdapter = data.getSavingAdapter()
        cardAdapter = data.getCardAdapter()
        limitationAdapter = data.getLimitationAdapter()
        //--------------------------------
        recyclerSaving = view.findViewById(R.id.recyclerSavings)
        recyclerCard = view.findViewById(R.id.recyclerCards)
        recyclerLimitation = view.findViewById(R.id.recyclerLimitation)
        //------------------------------------------
        btnAddLimitation = view.findViewById(R.id.btnAddLimitation)
        btnAddSaving = view.findViewById(R.id.btnAddSaving)
        btnAddCard = view.findViewById(R.id.btnAddCard)
        //------------------------------------------

        //------------------------------------------
        recyclerSaving.adapter = savingAdapter
        setSwipeToDeleteSaving(recyclerSaving)
        recyclerSaving.layoutManager =
            LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false)
        recyclerSaving.setHasFixedSize(true)

        recyclerCard.adapter = cardAdapter
        setSwipeToDeleteCard(recyclerCard)
        recyclerCard.layoutManager =
            LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false)
        recyclerCard.setHasFixedSize(true)

        recyclerLimitation.adapter = limitationAdapter
        recyclerLimitation.layoutManager =
            LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false)
        recyclerLimitation.setHasFixedSize(true)

    }
    private fun addCardInfo() {
        val totalCard = data.getIndexCard()
        val view = LayoutInflater.from(activity).inflate(R.layout.dialog_add_card, null)
        val builder = AlertDialog.Builder(activity as MainActivity)
        val nameCard = view.findViewById<TextInputEditText>(R.id.inputNameCard)
        val namePerson = view.findViewById<TextInputEditText>(R.id.inputCardholderName)
        val accountNum = view.findViewById<TextInputEditText>(R.id.inputAccountNumber)
        val cardNum = view.findViewById<TextInputEditText>(R.id.inputCardNumber)
        val bankName = view.findViewById<TextInputEditText>(R.id.inputBankName)
        //------------------------------------------------------------------
        val tlDescrip = view.findViewById<TextInputLayout>(R.id.textloutDescripCard)
        val tlCardHolder = view.findViewById<TextInputLayout>(R.id.textloutCardHolder)
        val tlAccountNum = view.findViewById<TextInputLayout>(R.id.textloutAccountNum)
        val tlMonth = view.findViewById<AutoCompleteTextView>(R.id.chooseMonth)
        val tlYear = view.findViewById<AutoCompleteTextView>(R.id.chooseYear)
        val tlBankName = view.findViewById<TextInputLayout>(R.id.textloutBankName)
        val tlCardNum = view.findViewById<TextInputLayout>(R.id.textloudCardNumber)
        //---------------------------------------------------------------------
        val itemsMonth = listOf("01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11", "12")
        val itemsYear = ArrayList<String>()
        val date = Calendar.getInstance()
        val start = date.get(Calendar.YEAR) -5
        val end = date.get(Calendar.YEAR) + 10
        for (i in start..end) {
            itemsYear.add(i.toString())
        }
        val monthAdapter = ArrayAdapter(data, R.layout.layout_category, itemsMonth)
        val yearAdapter = ArrayAdapter(data, R.layout.layout_category, itemsYear)
        tlMonth.setAdapter(monthAdapter)
        tlYear.setAdapter(yearAdapter)
        //-----------------------------------------------------------------------
        builder.setView(view)
        builder.setPositiveButton("OK") { dialog, which -> }
        builder.setNegativeButton("Cancel") { dialog, which ->
            dialog.dismiss()
        }
        val dialog = builder.create()
        dialog.show()
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
            val tmp1 = nameCard.text.toString()
            val tmp2 = namePerson.text.toString()
            val tmp3 = accountNum.text.toString()
            val tmp4 = cardNum.text.toString()
            val tmp5 = bankName.text.toString()
            val tmp6 = tlMonth.text.toString()
            val tmp7 = tlYear.text.toString()
            val ref1 = ref.child("cards").child("card" + (totalCard + 1))
            //---------------------------------------------------------------
            if (tmp1 != "" && tmp2 != "" && tmp3 != "" && tmp4.length == 16 && tmp5 != "" && tmp6 != "") {
                val tmp8 = tlMonth.text.toString() + "/" + tlYear.text.toString().substring(2, 4)
                ref1.child("index").setValue(totalCard + 1)
                ref1.child("accountNumber").setValue(tmp3)
                ref1.child("bankName").setValue(tmp5)
                ref1.child("cardNumber").setValue(tmp4)
                ref1.child("expiredDate").setValue(tmp8)
                ref1.child("name").setValue(tmp1)
                ref1.child("namePerson").setValue(tmp2)
                ref.child("cards").child("total").setValue(totalCard + 1)
                dialog.dismiss()
            } else {
                if (tmp1 == "") {
                    tlDescrip.error = "Please fill in the blank"
                }
                else tlDescrip.error = null
                if (tmp2 == "") {
                    tlCardHolder.error = "Please input card holder name"
                }
                else tlCardHolder.error = null
                if (tmp3 == "") {
                    tlAccountNum.error = "Please input account number"
                }
                else tlAccountNum.error = null
                if (tmp4.length != 16) {
                    tlCardNum.error = "Please input valid card number"
                }
                else tlCardNum.error = null
                if (tmp5 == "") {
                    tlBankName.error = "Please input name of bank"
                }
                else tlBankName.error = null
                if (tmp6 == "") {
                    tlMonth.error = "Please choose expired month"
                }
                else tlMonth.error = null
                if (tmp7 == "") {
                    tlYear.error = "Please choose expired year"
                }
                else tlYear.error = null
            }
        }
    }

    private fun addSavingInfo() {
        val totalSaving = data.getIndexSaving()
        val view = LayoutInflater.from(activity).inflate(R.layout.dialog_add_saving, null)
        val builder = AlertDialog.Builder(activity as MainActivity)
        val product = view.findViewById<TextInputEditText>(R.id.inputSavingName)
        val cost = view.findViewById<TextInputEditText>(R.id.inputSavingCost)
        val tlProduct = view.findViewById<TextInputLayout>(R.id.textloutNameSaving)
        val tlCost = view.findViewById<TextInputLayout>(R.id.textloutCostSaving)
        builder.setView(view)
        builder.setPositiveButton("OK") {dialog, which ->}
        builder.setNegativeButton("Cancel") { dialog, which ->
            dialog.dismiss()
        }
        val dialog = builder.create()
        dialog.show()
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
            val tmp1 = product.text.toString()
            val tmp2 = cost.text.toString()
            if (tmp1 != "" && tmp2 != "") {
                val ref1 = ref.child("savings").child("saving" + (totalSaving + 1))
                ref1.child("index").setValue(totalSaving + 1)
                ref1.child("product").setValue(tmp1)
                ref1.child("price").setValue(tmp2)
                ref1.child("current").setValue("0")
                ref.child("savings").child("total").setValue(totalSaving + 1)
                dialog.dismiss()
            } else {
                if (tmp1 == "") {
                    tlProduct.error = "Please input name of product"
                }
                else tlProduct.error = null
                if (tmp2 == "") {
                    tlCost.error = "Please input cost of saving"
                }
                else tlCost.error = null
            }
        }
    }

    private fun addLimitationInfo() {
        val totalLimit = data.getIndexLimitation()
        val view = LayoutInflater.from(activity).inflate(R.layout.dialog_add_limitation, null)
        val builder = AlertDialog.Builder(activity as MainActivity)
        val tlCategoryLimit = view.findViewById<AutoCompleteTextView>(R.id.textCategoryLimit)
        val tlTarget = view.findViewById<TextInputLayout>(R.id.textloutTarget)
        val target = view.findViewById<TextInputEditText>(R.id.target)
        val ref1 = ref.child("limits").child("limit" + (totalLimit + 1))
        val itemsExpense = resources.getStringArray(R.array.categoriesExpense)
        val categoryAdapter = ArrayAdapter(data, R.layout.layout_category, itemsExpense)
        tlCategoryLimit.setAdapter(categoryAdapter)
        builder.setView(view)
        builder.setPositiveButton("Add") { dialog, _ -> }
        builder.setNegativeButton("Cancel") { dialog, _ ->
            dialog.dismiss()
        }
        val dialog = builder.create()
        dialog.show()
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
            val tmp1 = tlCategoryLimit.text.toString()
            val tmp2 = target.text.toString()
            if (tmp1 != "" && tmp2 != "") {
                if (data.checkIfHasLimit(tmp1) == 0) {
                    ref1.child("index").setValue(totalLimit + 1)
                    ref1.child("currentLimit").setValue("0")
                    ref1.child("costLimit").setValue(tmp2)
                    ref1.child("nameLimit").setValue(tmp1)
                    ref.child("limits").child("total").setValue(totalLimit + 1)
                    dialog.dismiss()
                }
                else {
                    Toast.makeText(activity, "This limitation is existed. Please select another limitation", Toast.LENGTH_SHORT)
                        .show()
                }
            } else {
                if (tmp1 == "") {
                    tlCategoryLimit.error = "Please choose a limitation"
                }
                else tlCategoryLimit.error = null
                if (tmp2 == "") {
                    tlTarget.error = "Please input target limit"
                }
                else tlTarget.error = null
            }
        }
    }
    private fun setSwipeToDeleteSaving(rv: RecyclerView) {
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
                val dialog = AlertDialog.Builder(context!!)
                dialog.setTitle("Confirm")
                dialog.setIcon(R.drawable.ic_baseline_warning_24)
                dialog.setMessage("Do you want to delete this saving?")
                dialog.setPositiveButton("OK") { dialog, which ->
                    savingAdapter.deleteItemSaving(position, totalTrans)
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
    private fun setSwipeToDeleteCard(rv: RecyclerView) {
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
                val dialog = AlertDialog.Builder(context!!)
                dialog.setTitle("Confirm")
                dialog.setIcon(R.drawable.ic_baseline_warning_24)
                dialog.setMessage("Do you want to delete this card?")
                dialog.setPositiveButton("OK") { dialog, which ->
                    cardAdapter.deleteItem(position)
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