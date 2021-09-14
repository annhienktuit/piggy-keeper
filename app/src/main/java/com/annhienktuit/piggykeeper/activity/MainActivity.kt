package com.annhienktuit.piggykeeper.activity

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.commit
import com.annhienktuit.piggykeeper.R
import com.annhienktuit.piggykeeper.`object`.*
import com.annhienktuit.piggykeeper.adapter.CardAdapter
import com.annhienktuit.piggykeeper.adapter.LimitationAdapter
import com.annhienktuit.piggykeeper.adapter.RecentTransactionAdapter
import com.annhienktuit.piggykeeper.adapter.SavingAdapter
import com.annhienktuit.piggykeeper.fragments.HomeFragment
import com.annhienktuit.piggykeeper.fragments.PlanningFragment
import com.annhienktuit.piggykeeper.fragments.ReportFragment
import com.annhienktuit.piggykeeper.fragments.UserFragment
import com.annhienktuit.piggykeeper.utils.FirebaseUtils
import com.anychart.chart.common.dataentry.DataEntry
import com.anychart.chart.common.dataentry.ValueDataEntry
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.thecode.aestheticdialogs.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.dialog_add_transaction.view.*
import kotlinx.android.synthetic.main.dialog_done_interest_rate.*
import kotlinx.android.synthetic.main.fragment_current_month.*
import kotlinx.android.synthetic.main.fragment_home.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class MainActivity : AppCompatActivity() {
    //----------------------------------------------------------
    val user: FirebaseUser? = FirebaseUtils.firebaseAuth.currentUser
    var ref = FirebaseDatabase
        .getInstance("https://my-wallet-80ed7-default-rtdb.asia-southeast1.firebasedatabase.app/")
        .getReference("datas")
    //-------------------------------------------------------
    private var savingList = ArrayList<Saving>()
    private var transactionList = ArrayList<RecentTransaction>()
    private var cardList = ArrayList<Card>()
    private var limitList = ArrayList<Limitation>()
    private var checked: Boolean = true
    //--------------------------------------------------
    private val savingAdapter = SavingAdapter(savingList)
    private val limitationAdapter = LimitationAdapter(limitList)
    private val cardAdapter = CardAdapter(cardList)
    private val transactionAdapter = RecentTransactionAdapter(transactionList)
    //-------------------------------------------------------
    private var name: String? = null
    private var income: String? = "0"
    private var expense: String? = "0"
    private var balance: String? = "0"
    //-----------------------------------
    private var indexTransaction: Int = 0
    private var indexSaving: Int = 0
    private var indexCard: Int = 0
    private var indexLimitation: Int = 0
    //-----------------------------------
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
    //-----------------------------------
    //Variables for Previous income reporting
    private var listPreviousIncome = mutableListOf<DetailTransaction>()
    private var amountPreviousIncome = 0L
    private var amountPreviousDebt = 0L
    private var listPreviousIncomeData = mutableListOf<DataEntry>()
    //-----------------------------------
    //Variables for Previous expense reporting
    private var listPreviousExpense = mutableListOf<DetailTransaction>()
    private var amountPreviousExpense = 0L
    private var amountPreviousLoan = 0L
    private var listPreviousExpenseData = mutableListOf<DataEntry>()
    //-----------------------------------
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        //overridePendingTransition(R.anim.from_middle, R.anim.to_middle) //transistion
        if(!isNetworkAvailable()){
            AestheticDialog.Builder(this, DialogStyle.CONNECTIFY, DialogType.ERROR)
                .setTitle("No Internet Connection")
                .setMessage("Dont't worry, your data will be upload when connection available")
                .setCancelable(false)
                .setDarkMode(true)
                .setGravity(Gravity.TOP)
                .setAnimation(DialogAnimation.SHRINK)
                .setOnClickListener(object : OnDialogClickListener {
                    override fun onClick(dialog: AestheticDialog.Builder) {
                        dialog.dismiss()
                    }
                })
                .show()
        }
        val pref = getSharedPreferences("Notification", MODE_PRIVATE)
        pref.edit().putBoolean("status", true).apply()
        var notificationStatus:Boolean = pref.getBoolean("status",true)
        Log.i("notiInitilize",notificationStatus.toString())
        //ALARM
            var alarmManager: AlarmManager? =
                getSystemService(Context.ALARM_SERVICE) as AlarmManager

            var notificationIntent = Intent(this, AlarmReceiver::class.java)
            val broadcast = PendingIntent.getBroadcast(
                this,
                100,
                notificationIntent,
                PendingIntent.FLAG_UPDATE_CURRENT
            )
            val cal = Calendar.getInstance()
            cal.timeInMillis = System.currentTimeMillis()
            cal[Calendar.HOUR_OF_DAY] = 21 // thời gian gửi noti
            cal[Calendar.MINUTE] = 0
            cal[Calendar.SECOND] = 0
            alarmManager!!.setRepeating(
                AlarmManager.RTC_WAKEUP,
                cal.timeInMillis,
                AlarmManager.INTERVAL_DAY,
                broadcast
            )
        //the others
        ref.keepSynced(true)
        if(user == null || !user!!.isEmailVerified && !user!!.email.equals("demose114@gmail.com"))
            startActivity(Intent(this@MainActivity, LoginActivity::class.java))
        else
        getDatabase(ref, object : OnGetDataListener {
            override fun onSuccess(dataSnapshot: DataSnapshot) {
                setUpDatabase(dataSnapshot)
                setCurrentIncomePieChartData()
                setCurrentExpensePieChartData()
                setPreviousIncomePieChartData()
                setPreviousExpensePieChartData()
                if (checked) {
                    setUI()
                }
                fab.setOnClickListener {
                    eventOnClickAddButton()
                }
            }
            override fun onStart() {

            }
            override fun onFailure() {
            }
        })

    }

    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE)
        return if (connectivityManager is ConnectivityManager) {
            val networkInfo: NetworkInfo? = connectivityManager.activeNetworkInfo
            networkInfo?.isConnected ?: false
        } else false
    }

    private fun eventOnClickAddButton() {
        val builder = AlertDialog.Builder(this)
        val viewInflater = LayoutInflater.from(this).inflate(R.layout.dialog_add_transaction, null, false)
        val textCategory = viewInflater.findViewById<AutoCompleteTextView>(R.id.textCategory)
        val editName = viewInflater.findViewById<TextInputEditText>(R.id.inputDetailTrans)
        val editMoney = viewInflater.findViewById<TextInputEditText>(R.id.inputMoneyTrans)
        //---------------------------------------------------------------
        val tlDetailTransaction = viewInflater.findViewById<TextInputLayout>(R.id.textloutDetailTransaction)
        val tlMoney = viewInflater.findViewById<TextInputLayout>(R.id.textloutMoneyTransaction)
        val tlCategory = viewInflater.findViewById<TextInputLayout>(R.id.category)
        //---------------------------------------------------------------
        val itemsIncome = resources.getStringArray(R.array.categoriesIncome)
        val itemsExpense = resources.getStringArray(R.array.categoriesExpense)
        val date = Calendar.getInstance()
        val dayFormatter = SimpleDateFormat("dd/MM/yyyy")
        val timeFormatter = SimpleDateFormat("HH:mm")
        var inorout = "true"
        val day = dayFormatter.format(date.time)
        val time = timeFormatter.format(date.time)
        val categoryAdapter2 = ArrayAdapter(applicationContext, R.layout.layout_category, itemsExpense)
        val categoryAdapter1 = ArrayAdapter(applicationContext, R.layout.layout_category, itemsIncome)
        viewInflater.toggleMoneyButton.check(R.id.toggleIncome)
        textCategory.setAdapter(categoryAdapter1)
        viewInflater.toggleMoneyButton.addOnButtonCheckedListener { group, checkedId, isChecked ->
            if (isChecked) {
                if (checkedId == R.id.toggleIncome) {
                    textCategory.setText("")
                    inorout = "true"
                    textCategory.setAdapter(categoryAdapter1)
                }
                if (checkedId == R.id.toggleExpense) {
                    inorout = "false"
                    textCategory.setText("")
                    textCategory.setAdapter(categoryAdapter2)
                }
            }
        }

        val refTrans = ref.child(user?.uid.toString()).child("transactions").child("transaction" + (indexTransaction + 1))
        val ref1 = ref.child(user?.uid.toString())
        textCategory.dropDownHeight = 500
        builder.setView(viewInflater)
        builder.setPositiveButton("OK"
        ) { dialog, which -> }
        builder.setNegativeButton("Cancel"
        ) { dialog, which -> dialog.dismiss() }
        val dialog = builder.create()
        dialog.show()
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
            if (textCategory.text.toString() != "" && editName.text.toString() != "" && editMoney.text.toString() != "") {
                val name = editName.text.toString()
                val money = editMoney.text.toString()
                ref1.child("transactions").child("total").setValue(indexTransaction + 1)
                refTrans.child("day").setValue(day)
                refTrans.child("money").setValue(money)
                refTrans.child("name").setValue(name)
                refTrans.child("time").setValue(time)
                refTrans.child("inorout").setValue(inorout)
                refTrans.child("index").setValue(indexTransaction + 1)
                refTrans.child("category").setValue(textCategory.text.toString())
                refTrans.child("currentMonth").setValue((date.get(Calendar.MONTH) + 1).toString())
                refTrans.child("currentYear").setValue(date.get(Calendar.YEAR).toString())
                if (inorout == "true") {
                    ref1.child("income").setValue((income?.toLong()?.plus(money.toLong())).toString())
                    ref1.child("balance").setValue((balance?.toLong()?.plus(money.toLong())).toString())
                }
                else {
                    ref1.child("expense").setValue((expense?.toLong()?.plus(money.toLong())).toString())
                    ref1.child("balance").setValue((balance?.toLong()?.minus(money.toLong())).toString())
                    val tmp = checkIfHasLimit(textCategory.text.toString())
                    if (tmp != 0) {
                        val curruntLimit = limitList[tmp - 1].current!!.toInt()
                        ref1.child("limits").child("limit" + tmp).child("currentLimit").setValue((curruntLimit + money.toInt()).toString())
                    }
                }
                dialog.dismiss()
            } else {
                if (editName.text.toString() == "") {
                    tlDetailTransaction.error = "Please enter name of transaction"
                } else {
                    tlDetailTransaction.error = null
                }
                if (editMoney.text.toString() == "") {
                    tlMoney.error = "Please enter money of transaction"
                } else {
                    tlMoney.error = null
                }
                if (textCategory.text.toString() == "") {
                    tlCategory.error = "Please choose field of category"
                } else {
                    tlCategory.error = null
                }
            }
        }

    }
    fun checkIfHasLimit(str: String): Int {
        for (data in limitList) {
            if (data.limitedGroup == str) return data.index!!.toInt()
        }
        return 0
    }

    override fun onResume() {
        super.onResume()
        checked = true
        if (user != null) {
            getDatabase(ref, object : OnGetDataListener {
                override fun onSuccess(dataSnapshot: DataSnapshot) {
                    setUpDatabase(dataSnapshot)
                    setCurrentIncomePieChartData()
                    setCurrentExpensePieChartData()
                    setPreviousIncomePieChartData()
                    setPreviousExpensePieChartData()
                    if (checked) {
                        setUI()
                    }
                    fab.setOnClickListener {
                        eventOnClickAddButton()
                    }
                }
                override fun onStart() {

                }
                override fun onFailure() {
                }
            })
        }
    }

    override fun onPause() {
        super.onPause()
        checked = false
    }

    private fun setUI() {
        //hide action bar
        if (supportActionBar != null) {
            supportActionBar!!.hide();
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val window: Window = window
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.statusBarColor = Color.parseColor("#FFFFFF")
        }
        //modify the display of bottom navigation view
        bottomNavigationView.background = null
        bottomNavigationView.menu.getItem(2).isEnabled = false
        //----------------------------------------------
        val fragment = when (bottomNavigationView.selectedItemId) {
            R.id.navReport -> ReportFragment()
            R.id.navPlanning -> PlanningFragment()
            R.id.navUser -> UserFragment()
            else -> {
                HomeFragment()
            }
        }
        val transaction = supportFragmentManager.beginTransaction()
        transaction.setCustomAnimations(
             R.anim.slide_in,
            R.anim.fade_out,
             R.anim.fade_in,
             R.anim.slide_out
        )
        transaction.replace(R.id.container_fragment, fragment)
        transaction.commit()

        bottomNavigationView.setOnItemSelectedListener(mOnBottomNavigationView)
    }

    private val mOnBottomNavigationView = BottomNavigationView.OnNavigationItemSelectedListener {
        when (it.itemId) {
            R.id.navHome -> {
                val transaction = supportFragmentManager.beginTransaction()
                transaction.replace(R.id.container_fragment, HomeFragment())
                transaction.commit()
                return@OnNavigationItemSelectedListener true
            }
            R.id.navReport -> {
                val transaction = supportFragmentManager.beginTransaction()
                transaction.replace(R.id.container_fragment, ReportFragment())
                transaction.commit()
                return@OnNavigationItemSelectedListener true
            }
            R.id.navPlanning -> {
                val transaction = supportFragmentManager.beginTransaction()
                transaction.replace(R.id.container_fragment, PlanningFragment())
                transaction.commit()
                return@OnNavigationItemSelectedListener true
            }
            R.id.navUser -> {
                val transaction = supportFragmentManager.beginTransaction()
                transaction.replace(R.id.container_fragment, UserFragment())
                transaction.commit()
                return@OnNavigationItemSelectedListener true
            }
        }
        false
    }

    fun setUpDatabase(data: DataSnapshot?) {
        val db = ref.child(user?.uid.toString())
        val savingDb = db.child("savings")
        val transactionDb = db.child("transactions")
        val cardDb = db.child("cards")
        val limitDb = db.child("limits")
        name = data?.child(user?.uid.toString())!!.child("name").value.toString()
        income = data.child(user?.uid.toString()).child("income").value.toString()
        expense = data.child(user?.uid.toString()).child("expense").value.toString()
        balance = data.child(user?.uid.toString()).child("balance").value.toString()
        getDatabase(savingDb, object : OnGetDataListener {
            override fun onSuccess(dataSnapshot: DataSnapshot) {
                savingList.clear()
                indexSaving = dataSnapshot.child("total").value.toString().toInt()
                for (data in dataSnapshot.children) {
                    if (data.key.toString() != "total") {
                        val index = data.child("index").value.toString()
                        val current = data.child("current").value.toString()
                        val price = data.child("price").value.toString()
                        val product = data.child("product").value.toString()
                        savingList.add(Saving(index.toIntOrNull(), current, null, price, product))
                    }
                }
                savingAdapter.notifyDataSetChanged()
            }
            override fun onStart() {
            }
            override fun onFailure() {
            }
        })

        getDatabase(transactionDb.orderByChild("index"), object : OnGetDataListener {
            override fun onSuccess(dataSnapshot: DataSnapshot) {
                transactionList.clear()
                indexTransaction = dataSnapshot.child("total").value.toString().toInt()
                for (data in dataSnapshot.children) {
                    if (data.key.toString() != "total") {
                        var day = data.child("day").value.toString()
                        val index = data.child("index").value.toString()
                        val inorout = data.child("inorout").value.toString()
                        val category = data.child("category").value.toString()
                        val money = data.child("money").value.toString()
                        val name = data.child("name").value.toString()
                        val time = data.child("time").value.toString()
                        transactionList.add(RecentTransaction(index.toIntOrNull(), day, inorout, money, name, time, category))
                    }
                }
                transactionAdapter.notifyDataSetChanged()
                transactionList.reverse()
            }
            override fun onStart() {
            }
            override fun onFailure() {
            }
        })
        getDatabase(cardDb, object : OnGetDataListener {
            override fun onSuccess(dataSnapshot: DataSnapshot) {
                cardList.clear()
                indexCard = dataSnapshot.child("total").value.toString().toInt()
                for (data in dataSnapshot.children) {
                    if (data.key.toString() != "total") {
                        val index = data.child("index").value.toString()
                        val accNum = data.child("accountNumber").value.toString()
                        val bankName = data.child("bankName").value.toString()
                        val cardNum = data.child("cardNumber").value.toString()
                        val date = data.child("expiredDate").value.toString()
                        val name = data.child("name").value.toString()
                        val namePerson = data.child("namePerson").value.toString()
                        cardList.add(Card(accNum, bankName, cardNum, date, index.toIntOrNull(), name, namePerson))
                    }
                }
                cardAdapter.notifyDataSetChanged()
            }
            override fun onStart() {
            }
            override fun onFailure() {
            }
        })
        getDatabase(limitDb.orderByChild("index"), object : OnGetDataListener {
            override fun onSuccess(dataSnapshot: DataSnapshot) {
                limitList.clear()
                indexLimitation = dataSnapshot.child("total").value.toString().toInt()
                for (data in dataSnapshot.children) {
                    if (data.key.toString() != "total") {
                        val index = data.child("index").value.toString()
                        val nameLimit = data.child("nameLimit").value.toString()
                        val costLimit = data.child("costLimit").value.toString()
                        val currentLimit = data.child("currentLimit").value.toString()
                        limitList.add(Limitation(index.toIntOrNull(), currentLimit, costLimit, nameLimit))
                    }
                }
                limitationAdapter.notifyDataSetChanged()
            }
            override fun onStart() {
            }
            override fun onFailure() {
            }
        })
    }
    interface OnGetDataListener {
        fun onSuccess(dataSnapshot: DataSnapshot)
        fun onStart()
        fun onFailure()
    }
    fun getDatabase(ref: DatabaseReference, listener: OnGetDataListener?) {
        listener?.onStart()
        ref.addValueEventListener(object : ValueEventListener{
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
        ref.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                listener?.onSuccess(snapshot)
            }
            override fun onCancelled(error: DatabaseError) {
                listener?.onFailure()
            }
        })
    }

    fun setCurrentIncomePieChartData() {
        //reference of Income
        val refIncome = ref.child(user?.uid.toString()).child("transactions").orderByChild("inorout").equalTo("true")

        //get current month
        var now: Calendar = Calendar.getInstance()
        var currentMonth = now.get(Calendar.MONTH) + 1

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

                listCurrentIncome = handleListForChart(listCurrentIncome, currentMonth)

                for(item in listCurrentIncome){
                    if(item.category == "Debt"){
                        amountCurrentDebt += item.moneyAmount.toLong()
                    }
                    listCurrentIncomeData.add(ValueDataEntry(item.category, item.moneyAmount.toLong()))
                    amountCurrentIncome += item.moneyAmount.toLong()
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })

    }

    fun setCurrentExpensePieChartData() {
        //reference of expense
        val refExpense = ref.child(user?.uid.toString()).child("transactions").orderByChild("inorout").equalTo("false")

        //get current month
        var now: Calendar = Calendar.getInstance()
        var currentMonth = now.get(Calendar.MONTH) + 1

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

                listCurrentExpense = handleListForChart(listCurrentExpense, currentMonth)

                for(item in listCurrentExpense){
                    if(item.category == "Loan"){
                        amountCurrentLoan += item.moneyAmount.toLong()
                    }
                    listCurrentExpenseData.add(ValueDataEntry(item.category, item.moneyAmount.toLong()))
                    amountCurrentExpense += item.moneyAmount.toLong()
                }
            }
            override fun onCancelled(error: DatabaseError) {
            }
        })

    }

    fun setPreviousIncomePieChartData() {
        //reference of Income
        val refIncome = ref.child(user?.uid.toString()).child("transactions").orderByChild("inorout").equalTo("true")

        //get previous month
        var now: Calendar = Calendar.getInstance()
        var previousMonth = now.get(Calendar.MONTH)

        if(previousMonth == 0){
            previousMonth = 12
        }

        refIncome.addValueEventListener(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                amountPreviousIncome = 0L
                amountPreviousDebt = 0L

                listPreviousIncome.removeAll(listPreviousIncome)
                listPreviousIncomeData.removeAll(listPreviousIncomeData)
                for(childBranch in snapshot.children){
                    if(childBranch.key.toString() != "total") {
                        listPreviousIncome.add(
                            DetailTransaction(
                                childBranch.child("category").value.toString(),
                                childBranch.child("money").value.toString(),
                                childBranch.child("currentMonth").value.toString(),
                                childBranch.child("currentYear").value.toString()
                            )
                        )
                    }
                }

                listPreviousIncome = handleListForChart(listPreviousIncome, previousMonth)

                for(item in listPreviousIncome){
                    if(item.category == "Debt"){
                        amountPreviousDebt += item.moneyAmount.toLong()
                    }
                    listPreviousIncomeData.add(ValueDataEntry(item.category, item.moneyAmount.toLong()))
                    amountPreviousIncome += item.moneyAmount.toLong()
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })

    }

    fun setPreviousExpensePieChartData() {
        //reference of expense
        val refExpense = ref.child(user?.uid.toString()).child("transactions").orderByChild("inorout").equalTo("false")

        //get previous month
        var now: Calendar = Calendar.getInstance()
        var previousMonth = now.get(Calendar.MONTH)

        if(previousMonth == 0){
            previousMonth = 12
        }

        refExpense.addValueEventListener(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                amountPreviousExpense = 0L
                amountPreviousLoan = 0L

                listPreviousExpenseData.removeAll(listPreviousExpenseData)
                listPreviousExpense.removeAll(listPreviousExpense)
                for(childBranch in snapshot.children){
                    if(childBranch.key.toString() != "total") {
                        listPreviousExpense.add(
                            DetailTransaction(
                                childBranch.child("category").value.toString(),
                                childBranch.child("money").value.toString(),
                                childBranch.child("currentMonth").value.toString(),
                                childBranch.child("currentYear").value.toString()
                            )
                        )
                    }
                }

                listPreviousExpense = handleListForChart(listPreviousExpense, previousMonth)

                for(item in listPreviousExpense){
                    if(item.category == "Loan"){
                        amountPreviousLoan += item.moneyAmount.toLong()
                    }
                    listPreviousExpenseData.add(ValueDataEntry(item.category, item.moneyAmount.toLong()))
                    amountPreviousExpense += item.moneyAmount.toLong()
                }
            }
            override fun onCancelled(error: DatabaseError) {
            }
        })

    }


    private fun handleListForChart(list: MutableList<DetailTransaction>, month: Int) : MutableList<DetailTransaction>{
        var now: Calendar = Calendar.getInstance()
        var currentYear = now.get(Calendar.YEAR)

        var iForCurrentMonth = 0
        while(iForCurrentMonth < list.size){
            if(list[iForCurrentMonth].currentMonth != month.toString() || list[iForCurrentMonth].currentYear != currentYear.toString()){
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

    fun getCurrentIncome(): Long{
        return amountCurrentIncome
    }

    fun getCurrentDebt() : Long{
        return amountCurrentDebt
    }

    fun getCurrentIncomeList() : MutableList<DetailTransaction>{
        return listCurrentIncome
    }

    fun getCurrentIncomeData() : MutableList<DataEntry>{
        return listCurrentIncomeData
    }

    fun getCurrentExpense(): Long{
        return amountCurrentExpense
    }

    fun getCurrentLoan() : Long{
        return amountCurrentLoan
    }

    fun getCurrentExpenseList() : MutableList<DetailTransaction>{
        return listCurrentExpense
    }

    fun getCurrentExpenseData() : MutableList<DataEntry>{
        return listCurrentExpenseData
    }

    //--------------------------
    fun getPreviousIncome(): Long{
        return amountPreviousIncome
    }

    fun getPreviousDebt() : Long{
        return amountPreviousDebt
    }

    fun getPreviousIncomeList() : MutableList<DetailTransaction>{
        return listPreviousIncome
    }

    fun getPreviousIncomeData() : MutableList<DataEntry>{
        return listPreviousIncomeData
    }

    fun getPreviousExpense(): Long{
        return amountPreviousExpense
    }

    fun getPreviousLoan() : Long{
        return amountPreviousLoan
    }

    fun getPreviousExpenseList() : MutableList<DetailTransaction>{
        return listPreviousExpense
    }

    fun getPreviousExpenseData() : MutableList<DataEntry>{
        return listPreviousExpenseData
    }

    fun getName(): String? {
        return name
    }
    fun getIncome(): String? {
        return income
    }
    fun getExpense(): String? {
        return expense
    }
    fun getBalance(): String? {
        return balance
    }
    fun getIndexTransaction(): Int {
        return indexTransaction
    }
    fun getIndexSaving(): Int {
        return indexSaving
    }
    fun getIndexCard(): Int {
        return indexCard
    }
    fun getIndexLimitation(): Int {
        return indexLimitation
    }
    fun getTransactionAdapter(): RecentTransactionAdapter {
        return transactionAdapter
    }
    fun getCardAdapter(): CardAdapter {
        return cardAdapter
    }
    fun getLimitationAdapter(): LimitationAdapter {
        return limitationAdapter
    }
    fun getSavingAdapter(): SavingAdapter {
        return savingAdapter
    }

}