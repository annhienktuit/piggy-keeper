package com.annhienktuit.piggykeeper.activity

import android.R.id.message
import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.annhienktuit.piggykeeper.R
import com.annhienktuit.piggykeeper.utils.Extensions.toast
import com.annhienktuit.piggykeeper.utils.FirebaseUtils
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import io.card.payment.CardIOActivity
import io.card.payment.CreditCard
import kotlinx.android.synthetic.main.activity_user.*


class UserActivity : AppCompatActivity() {
    val MY_SCAN_REQUEST_CODE = 1
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
            val shareBody = "Love this money management app, try it now"
            sharingIntent.putExtra(Intent.EXTRA_SUBJECT, "Piggy Keeper")
            sharingIntent.putExtra(Intent.EXTRA_TEXT, shareBody)
            startActivity(Intent.createChooser(sharingIntent, "Share via"))
        }
        btnDevTeam.setOnClickListener {
            val intent = Intent(
                Intent.ACTION_SENDTO, Uri.fromParts(
                    "mailto", "annhien.se@email.com", null
                )
            )
            intent.putExtra(Intent.EXTRA_SUBJECT, "Report bug for Piggy Keeper")
            intent.putExtra(Intent.EXTRA_TEXT, message)
            startActivity(Intent.createChooser(intent, "Choose an Email client :"))
        }
        btnOccupation.setOnClickListener {
            val scanIntent = Intent(this, CardIOActivity::class.java)


            scanIntent.putExtra(CardIOActivity.EXTRA_REQUIRE_EXPIRY, false) // default: false

            scanIntent.putExtra(CardIOActivity.EXTRA_REQUIRE_CVV, false) // default: false

            scanIntent.putExtra(CardIOActivity.EXTRA_REQUIRE_POSTAL_CODE, false) // default: false

            startActivityForResult(scanIntent, MY_SCAN_REQUEST_CODE)
        }
    }
    interface OnGetDataListener {
        fun onSuccess(dataSnapshot: DataSnapshot)
        fun onStart()
        fun onFailure()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == MY_SCAN_REQUEST_CODE) {
            Log.i("request: ","$requestCode")
            var resultDisplayStr: String
            if (data != null && data.hasExtra(CardIOActivity.EXTRA_SCAN_RESULT)) {
                val scanResult: CreditCard? =
                    data.getParcelableExtra(CardIOActivity.EXTRA_SCAN_RESULT)

                // Never log a raw card number. Avoid displaying it, but if necessary use getFormattedCardNumber()
                resultDisplayStr = """
                Card Number: ${scanResult?.redactedCardNumber}
                """.trimIndent()
                // Do something with the raw number, e.g.:
                // myService.setCardNumber( scanResult.cardNumber );
                toast(scanResult!!.cardNumber)
            } else {
                resultDisplayStr = "Scan was canceled."
            }
            toast(resultDisplayStr)
            // do something with resultDisplayStr, maybe display it in a textView
            // resultTextView.setText(resultDisplayStr);
        }
        // else handle other activity results
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