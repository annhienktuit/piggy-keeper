package com.annhienktuit.piggykeeper.activity

import android.R.id.message
import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.annhienktuit.piggykeeper.R
import com.annhienktuit.piggykeeper.utils.FirebaseUtils
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_user.*


class UserActivity : AppCompatActivity() {
    val user: FirebaseUser? = FirebaseUtils.firebaseAuth.currentUser
    val ref = FirebaseDatabase
        .getInstance("https://my-wallet-80ed7-default-rtdb.asia-southeast1.firebasedatabase.app/")
        .getReference("datas").child(user!!.uid)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user)
        //Get database
        getDatabase(ref, object : OnGetDataListener {
            @SuppressLint("SetTextI18n")
            override fun onSuccess(dataSnapshot: DataSnapshot) {
                if (!dataSnapshot.hasChild("name")) btnName.setText("Not set yet")
                else btnName.setText(dataSnapshot.child("name").value.toString())
                if (!dataSnapshot.hasChild("phone")) btnPhone.text = "Not set yet"
                else btnPhone.text = dataSnapshot.child("phone").value.toString()
                if (!dataSnapshot.hasChild("gender")) btnGender.text = "Not set yet"
                else btnGender.text = dataSnapshot.child("gender").value.toString()
                if (!dataSnapshot.hasChild("dob")) btnDOB.text = "Not set yet"
                else btnDOB.text = dataSnapshot.child("dob").value.toString()
                if (!dataSnapshot.hasChild("occupation")) btnOccupation.text = "Not set yet"
                else btnOccupation.text = dataSnapshot.child("occupation").value.toString()
            }
            override fun onStart() {
            }
            override fun onFailure() {
            }
        })
        btnShare.setOnClickListener {
            val sharingIntent = Intent(Intent.ACTION_SEND)
            sharingIntent.type = "text/plain"
            val shareBody = "Love this money management app, try it now at: https://play.google.com/store/apps/details?id=com.annhienktuit.piggykeeper"
            sharingIntent.putExtra(Intent.EXTRA_SUBJECT, "Piggy Keeper")
            sharingIntent.putExtra(Intent.EXTRA_TEXT, shareBody)
            startActivity(Intent.createChooser(sharingIntent, "Share via"))
        }
        btnDevTeam.setOnClickListener {
            val intent = Intent(
                Intent.ACTION_SENDTO, Uri.fromParts(
                    "mailto", "annhien.se@gmail.com", null
                )
            )
            intent.putExtra(Intent.EXTRA_SUBJECT, "Report bug for Piggy Keeper")
            intent.putExtra(Intent.EXTRA_TEXT, message)
            startActivity(Intent.createChooser(intent, "Choose an Email client :"))
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