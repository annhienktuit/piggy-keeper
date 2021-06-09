package com.annhienktuit.mywallet.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.annhienktuit.mywallet.R
import com.annhienktuit.mywallet.`object`.Card
import com.annhienktuit.mywallet.`object`.Limitation
import com.annhienktuit.mywallet.`object`.Saving
import com.annhienktuit.mywallet.activity.MainActivity
import com.annhienktuit.mywallet.adapter.CardAdapter
import com.annhienktuit.mywallet.adapter.LimitationAdapter
import com.annhienktuit.mywallet.adapter.SavingAdapter
import com.annhienktuit.mywallet.utils.FirebaseUtils
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.dialog_done_interest_rate.*


private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"


class PlanningFragment() : Fragment() {

    private var param1: String? = null
    private var param2: String? = null
    var savingList = ArrayList<Saving>()
    var cardList = ArrayList<Card>()
    var limitationList = ArrayList<Limitation>()
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
        recyclerSaving.layoutManager =
            LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false)
        recyclerSaving.setHasFixedSize(true)

        recyclerCard.adapter = cardAdapter
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
        val exDate = view.findViewById<TextInputEditText>(R.id.inputExpiredDate)
        builder.setView(view)
        builder.setPositiveButton("OK") { dialog, which ->
            val tmp1 = nameCard.text.toString()
            val tmp2 = namePerson.text.toString()
            val tmp3 = accountNum.text.toString()
            val tmp4 = cardNum.text.toString()
            val tmp5 = bankName.text.toString()
            val tmp6 = exDate.text.toString()
            val ref1 = ref.child("cards").child("card" + (totalCard + 1))
            ref1.child("index").setValue(totalCard + 1)
            ref1.child("accountNumber").setValue(tmp3)
            ref1.child("bankName").setValue(tmp5)
            ref1.child("cardNumber").setValue(tmp4)
            ref1.child("expiredDate").setValue(tmp6)
            ref1.child("name").setValue(tmp1)
            ref1.child("namePerson").setValue(tmp2)
            ref.child("cards").child("total").setValue(totalCard + 1)
        }
        builder.setNegativeButton("Cancel") { dialog, which ->
            dialog.dismiss()
        }
        builder.create()
        builder.show()
    }

    private fun addSavingInfo() {
        val totalSaving = data.getIndexSaving()
        val view = LayoutInflater.from(activity).inflate(R.layout.dialog_add_saving, null)
        val builder = AlertDialog.Builder(activity as MainActivity)
        val product = view.findViewById<TextInputEditText>(R.id.inputSavingName)
        val cost = view.findViewById<TextInputEditText>(R.id.inputSavingCost)
        builder.setView(view)
        builder.setPositiveButton("OK") { dialog, which ->
            val tmp1 = product.text.toString()
            val tmp2 = cost.text.toString()
            val ref1 = ref.child("savings").child("saving" + (totalSaving + 1))
            ref1.child("index").setValue(totalSaving + 1)
            ref1.child("product").setValue(tmp1)
            ref1.child("price").setValue(tmp2)
            ref1.child("current").setValue("0")
            ref.child("savings").child("total").setValue(totalSaving + 1)
        }
        builder.setNegativeButton("Cancel") { dialog, which ->
            dialog.dismiss()
        }
        builder.create()
        builder.show()
    }

    private fun addLimitationInfo() {
        val totalLimit = data.getIndexLimitation()
        Log.d("khaidf", totalLimit.toString())
        val view = LayoutInflater.from(activity).inflate(R.layout.dialog_add_limitation, null)
        val builder = AlertDialog.Builder(activity as MainActivity)
        val textCategory = view.findViewById<AutoCompleteTextView>(R.id.textCategoryLimit)
        val money = view.findViewById<TextInputEditText>(R.id.tfTarget)
        val itemsExpense = resources.getStringArray(R.array.categoriesExpense)
        val categoryAdapter = ArrayAdapter(requireContext(), R.layout.layout_category, itemsExpense)
        textCategory.setAdapter(categoryAdapter)
        builder.setView(view)
        builder.setPositiveButton("Add") { dialog, _ ->
            try {
                val tmp1 = money.text.toString()
                val tmp2 = textCategory.text.toString()
                val ref1 = ref.child("limits").child("limit" + (totalLimit + 1))
                ref1.child("index").setValue(totalLimit + 1)
                ref1.child("currentLimit").setValue("0")
                ref1.child("costLimit").setValue(tmp1)
                ref1.child("nameLimit").setValue(tmp2)
                ref.child("limits").child("total").setValue(totalLimit + 1)
                Toast.makeText(activity, "Adding limitation item success", Toast.LENGTH_SHORT)
                    .show()
                dialog.dismiss()
            } catch (e: Exception) {
                Toast.makeText(activity, "Please fill all the fields!", Toast.LENGTH_SHORT).show()
            }
        }

        builder.setNegativeButton("Cancel") { dialog, _ ->
            dialog.dismiss()
        }
        builder.create()
        builder.show()
    }
}