package com.annhienktuit.piggykeeper.activity

import android.app.DatePickerDialog
import android.app.DatePickerDialog.OnDateSetListener
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.annhienktuit.piggykeeper.R
import com.annhienktuit.piggykeeper.utils.FirebaseUtils
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_setting.*


class SettingActivity : AppCompatActivity() {
    val user: FirebaseUser? = FirebaseUtils.firebaseAuth.currentUser
    val ref = FirebaseDatabase
        .getInstance("https://my-wallet-80ed7-default-rtdb.asia-southeast1.firebasedatabase.app/")
        .getReference("datas").child(user!!.uid)
    //-------------------------------------------------------
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setting)
        btn_back_setting.setOnClickListener {
            finish()
        }
        //Get database
        getDatabase(ref, object : OnGetDataListener {
            override fun onSuccess(dataSnapshot: DataSnapshot) {
                if (!dataSnapshot.hasChild("name")) settingName.setText("Empty")
                else settingName.setText(dataSnapshot.child("name").value.toString())
                if (!dataSnapshot.hasChild("phone")) settingPhone.text = "Empty"
                else settingPhone.text = dataSnapshot.child("phone").value.toString()
                if (!dataSnapshot.hasChild("gender")) settingGender.text = "Empty"
                else settingGender.text = dataSnapshot.child("gender").value.toString()
                if (!dataSnapshot.hasChild("dob")) settingDOB.text = "Empty"
                else settingDOB.text = dataSnapshot.child("dob").value.toString()
                if (!dataSnapshot.hasChild("occupation")) settingOccupation.text = "Empty"
                else settingOccupation.text = dataSnapshot.child("occupation").value.toString()
            }
            override fun onStart() {
            }
            override fun onFailure() {
            }
        })
        //Edit button
        settingEditName.setOnClickListener {
            editName()
        }
        settingEditPhone.setOnClickListener {
            editPhone()
        }
        settingEditGender.setOnClickListener {
            editGender()
        }
        settingEditOccupation.setOnClickListener {
            editOccupation()
        }
        settingEditDOB.setOnClickListener {
            val dateSetListener =
                OnDateSetListener { it, year, monthOfYear, dayOfMonth ->
                    val yyyy = year.toString()
                    var MM = ""
                    var dd = ""
                    if (monthOfYear + 1 < 10) MM = "0" + (monthOfYear + 1)
                    else MM = (monthOfYear + 1).toString()
                    if (dayOfMonth < 10) dd = "0" + dayOfMonth
                    else dd = dayOfMonth.toString()
                    ref.child("dob").setValue(dd + "/" + MM + "/" + yyyy)
                }

            val datePickerDialog = DatePickerDialog(
                this, android.R.style.Theme_Holo_Light_Dialog_NoActionBar,
                dateSetListener, 2021, 0, 1
            )
            datePickerDialog.show()
        }

    }

    private fun editOccupation() {
        val view = LayoutInflater.from(this@SettingActivity).inflate(R.layout.dialog_setting_occupation, null)
        val builder = AlertDialog.Builder(this@SettingActivity)
        val tlOccupation = view.findViewById<TextInputLayout>(R.id.textloutOccupationSetting)
        val occupation = view.findViewById<TextInputEditText>(R.id.textOccupationSetting)
        builder.setView(view)
        builder.setPositiveButton("OK") {dialog, which ->}
        builder.setNegativeButton("Cancel") { dialog, which ->
            dialog.dismiss()
        }
        val dialog = builder.create()
        dialog.show()
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
            val tmp1 = occupation.text.toString()
            if (tmp1 != "") {
                ref.child("occupation").setValue(tmp1)
                dialog.dismiss()
            } else {
                if (tmp1 == "") {
                    tlOccupation.error = "Please enter your occupation"
                }
                else tlOccupation.error = null
            }
        }
    }

    private fun editGender() {
        val view = LayoutInflater.from(this@SettingActivity).inflate(R.layout.dialog_setting_gender, null)
        val builder = AlertDialog.Builder(this@SettingActivity)
        val tlGender = view.findViewById<AutoCompleteTextView>(R.id.textGenderSetting)
        val itemsGender = listOf("Male", "Female", "Other")
        val genderAdapter = ArrayAdapter(this@SettingActivity, R.layout.layout_category, itemsGender)
        tlGender.setAdapter(genderAdapter)
        builder.setView(view)
        builder.setPositiveButton("OK") { dialog, which -> }
        builder.setNegativeButton("Cancel") { dialog, which ->
            dialog.dismiss()
        }
        val dialog = builder.create()
        dialog.show()
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
            val tmp1 = tlGender.text.toString()
            if (tmp1 != "") {
                ref.child("gender").setValue(tmp1)
                dialog.dismiss()
            } else {
                if (tmp1 == "") {
                    tlGender.error = "Please choose one field"
                }
                else tlGender.error = null
            }
        }
    }

    private fun editPhone() {
        val view = LayoutInflater.from(this@SettingActivity).inflate(R.layout.dialog_setting_phone, null)
        val builder = AlertDialog.Builder(this@SettingActivity)
        val tlPhone = view.findViewById<TextInputLayout>(R.id.textloutPhoneSetting)
        val phone = view.findViewById<TextInputEditText>(R.id.textPhoneSetting)
        builder.setView(view)
        builder.setPositiveButton("OK") {dialog, which ->}
        builder.setNegativeButton("Cancel") { dialog, which ->
            dialog.dismiss()
        }
        val dialog = builder.create()
        dialog.show()
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
            val tmp1 = phone.text.toString()
            if (tmp1 != "") {
                ref.child("phone").setValue(tmp1)
                dialog.dismiss()
            } else {
                if (tmp1 == "") {
                    tlPhone.error = "Please enter your name"
                }
                else tlPhone.error = null
            }
        }
    }

    private fun editName() {
        val view = LayoutInflater.from(this@SettingActivity).inflate(R.layout.dialog_setting_name, null)
        val builder = AlertDialog.Builder(this@SettingActivity)
        val tlName = view.findViewById<TextInputLayout>(R.id.textloutNameSetting)
        val name = view.findViewById<TextInputEditText>(R.id.textNameSetting)
        builder.setView(view)
        builder.setPositiveButton("OK") {dialog, which ->}
        builder.setNegativeButton("Cancel") { dialog, which ->
            dialog.dismiss()
        }
        val dialog = builder.create()
        dialog.show()
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
            val tmp1 = name.text.toString()
            if (tmp1 != "") {
                ref.child("name").setValue(tmp1)
                dialog.dismiss()
            } else {
                if (tmp1 == "") {
                    tlName.error = "Please enter your name"
                }
                else tlName.error = null
            }
        }
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
}