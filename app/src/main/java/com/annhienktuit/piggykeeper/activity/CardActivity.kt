package com.annhienktuit.piggykeeper.activity

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.annhienktuit.piggykeeper.R
import com.annhienktuit.piggykeeper.`object`.Card
import com.annhienktuit.piggykeeper.utils.Extensions.toast
import com.annhienktuit.piggykeeper.utils.FirebaseUtils
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_card.*


class CardActivity : AppCompatActivity() {
    val user: FirebaseUser? = FirebaseUtils.firebaseAuth.currentUser
    var ref = FirebaseDatabase
        .getInstance("https://my-wallet-80ed7-default-rtdb.asia-southeast1.firebasedatabase.app/")
        .getReference("datas").child(user?.uid.toString()).child("cards")
    var card: Card? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        var pos = intent.getIntExtra("position", 1)
        ref.keepSynced(true)
        getDatabase(ref.child("card" + (pos)),object : OnGetDataListener{
            override fun onSuccess(dataSnapshot: DataSnapshot) {
                card = dataSnapshot.getValue(Card::class.java)
                setData(card)
            }

            override fun onStart() {
                Log.d("khaidf", "Start getting card database")
            }

            override fun onFailure() {
                Log.d("khaidf", "Failed to get card database")
            }
        })
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_card)
        btn_back_card.setOnClickListener {
            finish()
        }

        btnCopyName.setOnClickListener {
            val clipboard: ClipboardManager =
                getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("Copied", card!!.namePerson.toString())
            toast("Copied to clipboard")
            clipboard.setPrimaryClip(clip)
        }
        btnCopyAccountNumber.setOnClickListener {
            val clipboard: ClipboardManager =
                getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("Copied", card?.accountNumber.toString())
            toast("Copied to clipboard")
            clipboard.setPrimaryClip(clip)
        }
        btnCopyCardNumber.setOnClickListener {
            val clipboard: ClipboardManager =
                getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("Copied", card?.cardNumber.toString())
            toast("Copied to clipboard")
            clipboard.setPrimaryClip(clip)
        }
        btnCopyAll.setOnClickListener {
//            val clipboard: ClipboardManager =
//                getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
//            val fullInformation = ClipData.newPlainText("Copied","Name: " + card!!.namePerson.toString() + "\n"
//                    + "Account number: " + card?.accountNumber.toString() + "\n"
//                    + "Cardnumber: " + card?.cardNumber.toString() )
//            toast("Copied to clipboard")
//            clipboard.setPrimaryClip(fullInformation)
            val sharingIntent = Intent(Intent.ACTION_SEND)
            sharingIntent.type = "text/plain"
            val shareBody = "This is my card information \n " +
                    "Name: ${card!!.namePerson}\n" +
                    "Account number: ${card!!.accountNumber}\n" +
                    "Cardnumber: + ${card!!.cardNumber}"
            sharingIntent.putExtra(Intent.EXTRA_SUBJECT, "Piggy Keeper")
            sharingIntent.putExtra(Intent.EXTRA_TEXT, shareBody)
            startActivity(Intent.createChooser(sharingIntent, "Share via"))
        }
    }
    fun setData(card: Card?) {
        var numberCardFormated = formatCard(card?.cardNumber.toString())
        numberCard.text = numberCardFormated
        dateValid.text = card?.expiredDate.toString()
        nameCard.text = card?.namePerson.toString()
        bankingName.text = card?.bankName.toString()
        NameContent.text = card?.namePerson.toString()
        NoAccount.text = card?.accountNumber.toString()
        NoCard.text = card?.cardNumber.toString()
    }
    interface OnGetDataListener {
        fun onSuccess(dataSnapshot: DataSnapshot)
        fun onStart()
        fun onFailure()
    }
    fun getDatabase(ref: DatabaseReference?, listener: OnGetDataListener?) {
        listener?.onStart()
        ref?.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                listener?.onSuccess(snapshot)
            }
            override fun onCancelled(error: DatabaseError) {
                Log.d("khaidf", "get database error")
                listener?.onFailure()
            }
        })
    }
    fun formatCard(cardNumber: String?): String? {
        if (cardNumber == null) return null
        val delimiter = ' '
        return cardNumber.replace(".{4}(?!$)".toRegex(), "$0$delimiter")
    }
}