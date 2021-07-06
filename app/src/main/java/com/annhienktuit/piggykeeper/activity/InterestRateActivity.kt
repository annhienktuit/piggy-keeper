package com.annhienktuit.piggykeeper.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.annhienktuit.piggykeeper.R
import com.annhienktuit.piggykeeper.dialog.DoneInterestDialog
import com.annhienktuit.piggykeeper.dialog.PeriodOfTimeDialog
import com.annhienktuit.piggykeeper.dialog.TypeOfInterestDialog
import com.annhienktuit.piggykeeper.dialog.TypeOfTimeDialog
import com.annhienktuit.piggykeeper.utils.InterestRate
//<<<<<<< HEAD:app/src/main/java/com/annhienktuit/piggykeeper/activity/InterestRateActivity.kt
//import com.annhienktuit.piggykeeper.*
//import com.annhienktuit.piggykeeper.dialog.DoneInterestDialog
//import com.annhienktuit.piggykeeper.dialog.PeriodOfTimeDialog
//import com.annhienktuit.piggykeeper.dialog.TypeOfInterestDialog
//import com.annhienktuit.piggykeeper.dialog.TypeOfTimeDialog
//=======
//import android.widget.Toast
//import com.annhienktuit.piggykeeper.utils.InterestRate
//>>>>>>> c5d049ed18003bfaab1bc661d1a68f81647c6e2b:app/src/main/java/com/annhienktuit/piggykeeper/InterestRateActivity.kt
import kotlinx.android.synthetic.main.activity_interest_rate.*

class InterestRateActivity : AppCompatActivity() {
    var rootMoney: Long = -1
    var interestRate: Double = -1.0
    var period: Int = -1
    var interestType: String = "null"
    var timeType: String = "null"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_interest_rate)

        if (supportActionBar != null) {
            supportActionBar?.hide();
        }

        btnArrowBack.setOnClickListener{
            finish()
        }

        TypeOfInterestLayout.setOnClickListener{
            var dialog = TypeOfInterestDialog()

            dialog.show(supportFragmentManager, "TypeOfInterestDialog")
        }

        periodOfTimeLayout.setOnClickListener{
            var dialog = PeriodOfTimeDialog()

            dialog.show(supportFragmentManager, "PeriodOfTimeDialog")
        }

        typeOfTimeLayout.setOnClickListener{
            var dialog = TypeOfTimeDialog()

            dialog.show(supportFragmentManager, "TypeOfTimeDialog")
        }

        doneBtn.setOnClickListener{
            var dialog = DoneInterestDialog()
            updateProperties()

            if(!checkProperties()){
                Toast.makeText(this, "Please fill all the fields", Toast.LENGTH_SHORT).show()
            }
            else {
                //pass data
                var bundle = Bundle()
                if(tvTypeOfInterest.text == "Compound interest"){
                    bundle.putDouble("compoundTotal", InterestRate.compoundInterest(rootMoney, interestRate, numberOfPeriods()))
                    bundle.putInt("numberOfPeriods", numberOfPeriods())
                    bundle.putLong("amountOfMoney", rootMoney)
                    bundle.putDouble("compoundProfit", InterestRate.compoundInterest(rootMoney, interestRate, numberOfPeriods()) - rootMoney * 1.0)
                    bundle.putString("type", "Compound")
                }
                else if(tvTypeOfInterest.text == "Simple interest"){
                    bundle.putDouble("simpleTotal", InterestRate.simpleInterest(rootMoney, interestRate, numberOfPeriods()))
                    bundle.putInt("numberOfPeriods", numberOfPeriods())
                    bundle.putLong("amountOfMoney", rootMoney)
                    bundle.putDouble("simpleProfit", InterestRate.simpleInterest(rootMoney, interestRate, numberOfPeriods()) - rootMoney * 1.0)
                    bundle.putString("type", "Simple")
                }
                dialog.arguments = bundle
                dialog.show(supportFragmentManager, "DoneDialog")
            }
        }
    }

    // cập nhật các fields trước khi tính toán
    private fun updateProperties(){
        if(!tfAmountOfMoney.text.isNullOrEmpty()){
            this.rootMoney = (tfAmountOfMoney.text.toString()).toLong()
        }

        if(!tfInterestRate.text.isNullOrEmpty()){
            this.interestRate = (tfInterestRate.text.toString()).toDouble() / 100
        }

        if(!invisiblePeriod.text.isNullOrEmpty()){
            this.period = (invisiblePeriod.text.toString()).toInt()
        }

        if(!tvTypeOfInterest.text.isNullOrEmpty()){
            this.interestType = tvTypeOfInterest.text.toString()
        }

        if(!tvTypeOfTime.text.isNullOrEmpty()){
            this.timeType = tvTypeOfTime.text.toString()
        }
    }

    // Kiểm tra người dùng đã nhập đầy đủ các fields hay chưa
    private fun checkProperties(): Boolean{
        if(rootMoney == -1L || interestRate == -1.0 || period == -1 || interestType == "null" || timeType == "null"){
            return false
        }
        return true
    }

    private fun numberOfPeriods(): Int{
        when (tvTypeOfTime.text) {
            "Weekly" -> {
                return period / 7
            }
            "Per 14 days" -> {
                return period / 14
            }
            "Per month" -> {
                return period / 30
            }
            "Per 2 months"->{
                return period / 60
            }
            "Per 3 months" -> {
                return period / 90
            }
            "Per 6 months" -> {
                return period / 180
            }
            "Per year" -> {
                return period / 365
            }
        }
        return 0
    }

}