package com.annhienktuit.piggykeeper.fragments

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.transition.TransitionInflater
import com.annhienktuit.piggykeeper.R
import com.annhienktuit.piggykeeper.activity.*
import com.annhienktuit.piggykeeper.utils.FirebaseUtils
import com.annhienktuit.piggykeeper.utils.FirebaseUtils.firebaseAuth
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*


class UserFragment : Fragment() {

    val user: FirebaseUser? = FirebaseUtils.firebaseAuth.currentUser
    var ref = FirebaseDatabase
        .getInstance("https://my-wallet-80ed7-default-rtdb.asia-southeast1.firebasedatabase.app/")
        .getReference("datas")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val inflater = TransitionInflater.from(requireContext())
        enterTransition = inflater.inflateTransition(R.transition.slide_right)
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_user, container, false)
        val name = view?.findViewById<TextView>(R.id.nameOfUser)
        val data = activity as MainActivity
        name?.text = data.getName()
        val btnCardManager = view?.findViewById<Button>(R.id.btnmanagecard)
        btnCardManager?.setOnClickListener {
            val totalCard = data.getCardAdapter().itemCount
            if (totalCard == 0) {
                val builder = AlertDialog.Builder(activity as MainActivity)
                builder.setTitle("No Available Card")
                builder.setMessage("You haven't added any credit card to your account")
                builder.setIcon(R.drawable.ic_baseline_credit_card_24)
                builder.setPositiveButton("Okay") { dialog, which ->

                }
                builder.show()
            } else {
                activity?.let {
                    val intent = Intent(it, CardActivity::class.java)
                    intent.putExtra("position", data.getCardAdapter().getFirstCard())
                    it.startActivity(intent)
                }
            }
        }
        val btnSignOut = view?.findViewById<Button>(R.id.btnLogOut)
        btnSignOut?.setOnClickListener {
            firebaseAuth.signOut()
            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build()
            val mGoogleSignInClient = GoogleSignIn.getClient(it.context, gso)
            mGoogleSignInClient.signOut()
            Toast.makeText(activity, "Signed Out", Toast.LENGTH_LONG).show()
            activity?.let {
                val intent = Intent(it, LoginActivity::class.java)
                it.startActivity(intent)
                it.finish()
            }

        }
        val btnMap = view?.findViewById<Button>(R.id.btnMap)
        btnMap?.setOnClickListener {
            activity?.let {
                Log.i("status", "ok")
                val intent = Intent(it, MapActivity::class.java)
                it.startActivity(intent)
            }
        }
        val btnCurrencies = view?.findViewById<Button>(R.id.btnCurrenciesExchange)
        btnCurrencies?.setOnClickListener {
            activity?.let {
                val intent = Intent(it, MoneyExchangeActivity::class.java)
                it.startActivity(intent)
            }
        }

        val btnInterest = view?.findViewById<Button>(R.id.btnInterestRate)
        btnInterest?.setOnClickListener {
            activity?.let {
                val intent = Intent(it, InterestRateActivity::class.java)
                it.startActivity(intent)
            }
        }
        val btnAbout = view?.findViewById<Button>(R.id.btnAboutUs)
        btnAbout?.setOnClickListener {
            activity?.let {
                val intent = Intent(it, AboutActivity::class.java)
                it.startActivity(intent)
            }
        }
        val btnSetting = view?.findViewById<Button>(R.id.btnSettings)
        btnSetting?.setOnClickListener {
            activity?.let {
                val intent = Intent(it, SettingActivity::class.java)
                it.startActivity(intent)
            }
        }
        val btnExport = view.findViewById<Button>(R.id.btnExport)
        btnExport.setOnClickListener {
            export()
        }
        val btnUserName = view?.findViewById<TextView>(R.id.nameOfUser)
        btnUserName?.setOnClickListener {
            activity?.let {
                val intent = Intent(it, UserActivity::class.java)
                it.startActivity(intent)
            }
        }
        val avatar = view?.findViewById<ImageView>(R.id.avatar)
        avatar?.setOnClickListener {
            activity?.let {
                val intent = Intent(it, UserActivity::class.java)
                it.startActivity(intent)
            }
        }

        return view
    }

    fun export(){
        val refTrans = ref.child(user?.uid.toString()).child("transactions")
        val sdf = SimpleDateFormat("dd/MM/yyyy")
        val currentDate = sdf.format(Date())
        var default_name:String = currentDate
        Log.i("time",default_name)
        refTrans.addValueEventListener(object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                var data: StringBuilder = StringBuilder()
                data.clear()

                //write data separated by colon
                data.append("Date,Time,Name of Transaction, Type of Transaction(true is income),Category,Amount Of Money")
                for(childBranch in snapshot.children){
                    if(childBranch.key.toString() != "total"){
                        data.append("\n" +
                                "${childBranch.child("day").value.toString()}," +
                                "${childBranch.child("time").value.toString()}," +
                                "${childBranch.child("name").value.toString()}," +
                                "${childBranch.child("inorout").value.toString()}," +
                                "${childBranch.child("category").value.toString()}," +
                                childBranch.child("money").value.toString()
                        )
                    }
                }

                try{
                    //saving the file into device
                    var out: FileOutputStream = context!!.openFileOutput("data.csv", Context.MODE_PRIVATE)
                    out.write((data.toString()).toByteArray())
                    out.close()

                    //exporting
                    var context: Context = activity!!.applicationContext
                    var fileLocation: File = File(context.filesDir, "data.csv")

                    var path: Uri = FileProvider.getUriForFile(
                        context,
                        "com.annhienktuit.piggykeeper.fileprovider",
                    fileLocation)

                    var fileIntent: Intent = Intent(Intent.ACTION_SEND)
                    fileIntent.type = "text/csv"
                    fileIntent.putExtra(Intent.EXTRA_SUBJECT, "$default_name")
                    fileIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    fileIntent.putExtra(Intent.EXTRA_STREAM, path)

                    startActivity(Intent.createChooser(fileIntent, "Send Mail"))
                }
                catch (e: Exception){

                }
            }

            override fun onCancelled(error: DatabaseError) {
            }

        })
    }
}