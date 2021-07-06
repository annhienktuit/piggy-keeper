package com.annhienktuit.piggykeeper.dialog

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.annhienktuit.piggykeeper.R
import com.annhienktuit.piggykeeper.activity.InterestRateActivity
import kotlinx.android.synthetic.main.activity_interest_rate.*
import java.text.SimpleDateFormat
import java.util.*

//<<<<<<< HEAD:app/src/main/java/com/annhienktuit/piggykeeper/dialog/PeriodOfTimeDialog.kt
//import com.annhienktuit.piggykeeper.R
//=======
//import kotlinx.android.synthetic.main.activity_interest_rate.*
//import java.text.SimpleDateFormat
//import java.util.*
//>>>>>>> c5d049ed18003bfaab1bc661d1a68f81647c6e2b:app/src/main/java/com/annhienktuit/piggykeeper/PeriodOfTimeDialog.kt

class PeriodOfTimeDialog: DialogFragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView: View = inflater.inflate(R.layout.dialog_period_of_time, container, false)

        val beginningLayout: LinearLayout = rootView.findViewById(R.id.beginningDateLayout) as LinearLayout
        val endingLayout: LinearLayout = rootView.findViewById(R.id.endingDateLayout) as LinearLayout
        val tvBegin: TextView = rootView.findViewById(R.id.tvBeginningDate) as TextView
        val tvEnd: TextView = rootView.findViewById(R.id.tvEndingDate) as TextView
        val submitBtn: Button = rootView.findViewById(R.id.submitBtn) as Button

        beginningLayout.setOnClickListener{
            clickDatePicker(tvBegin)
        }

        endingLayout.setOnClickListener{
            clickDatePicker(tvEnd)
        }

        submitBtn.setOnClickListener{
            setText(tvBegin, tvEnd)
            dismiss()
        }

        return rootView
    }

    private fun clickDatePicker(dateDisplay: TextView){
        val myCalendar = Calendar.getInstance() //Create Calendar Instance
        val year = myCalendar.get(Calendar.YEAR)
        val month = myCalendar.get(Calendar.MONTH)
        val day = myCalendar.get(Calendar.DAY_OF_MONTH)

        val dpd = DatePickerDialog(requireActivity(), DatePickerDialog.OnDateSetListener { view, selectedYear, selectedMonth, selectedDayOfMonth -> // this is the chosen time when hit OK
            // the below code will run when set the date picker
            val selectedDate = "$selectedDayOfMonth/${selectedMonth + 1}/$selectedYear"

            dateDisplay.text = selectedDate

        }, year, month, day) //this is current year, month, day and show when open the dialog


        dpd.show()
    }

    // set text for tvPeriodOfTime in interest rate activity
    private fun setText(begin: TextView, end: TextView){
        if(begin.text.isNullOrEmpty() || end.text.isNullOrEmpty()){
            Toast.makeText(activity, "Please select both beginning date and ending date", Toast.LENGTH_SHORT).show()
        }
        else{
            var days: Long = calculateDays(begin, end)

            if(days < 0){
                Toast.makeText(activity, "Ending date must be greater than beginning date", Toast.LENGTH_SHORT).show()
            }
            else{
                    if(days <= 1){
                        (activity as InterestRateActivity).tvPeriodOfTime.text = "${begin.text.toString()} - ${end.text.toString()} ($days day)"
                    }
                    else {
                        (activity as InterestRateActivity).tvPeriodOfTime.text = "${begin.text.toString()} - ${end.text.toString()} ($days days)"
                    }

                (activity as InterestRateActivity).invisiblePeriod.text = days.toString()
            }

        }
    }
}

    private fun calculateDays(begin: TextView, end: TextView) : Long{

        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH)

        var simpleBeginDate = sdf.parse(begin.text.toString())
        var simpleEndDate = sdf.parse(end.text.toString())

        var beginDateInDays = simpleBeginDate.time / (60000 * 24 * 60) // return beginning date in days since january 1 1970
        var endDateInDays = simpleEndDate.time / (60000 * 24 * 60) // return ending date in days since january 1 1970

        return endDateInDays - beginDateInDays
    }