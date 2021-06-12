package com.annhienktuit.mywallet.fragments

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import com.annhienktuit.mywallet.*
import com.annhienktuit.mywallet.`object`.DetailTransaction
import com.annhienktuit.mywallet.activity.*
import com.annhienktuit.mywallet.utils.FirebaseUtils
import com.annhienktuit.mywallet.utils.FirebaseUtils.firebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.io.File
import java.io.FileOutputStream
import java.lang.Exception
import java.lang.StringBuilder

class UserFragment : Fragment() {

    val user: FirebaseUser? = FirebaseUtils.firebaseAuth.currentUser
    var ref = FirebaseDatabase
        .getInstance("https://my-wallet-80ed7-default-rtdb.asia-southeast1.firebasedatabase.app/")
        .getReference("datas")

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
                Toast.makeText(activity, "There is no card in system", Toast.LENGTH_SHORT).show()
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
            Toast.makeText(activity, "Signed Out", Toast.LENGTH_LONG).show()
            activity?.let {
                val intent = Intent(it, LoginActivity::class.java)
                it.startActivity(intent)
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

        val btnExport = view.findViewById<Button>(R.id.btnExport)
        btnExport.setOnClickListener {
            export()
        }

        return view
    }

    fun export(){
        val refTrans = ref.child(user?.uid.toString()).child("transactions")

        refTrans.addValueEventListener(object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                var data: StringBuilder = StringBuilder()
                data.clear()

                //write data separated by colon
                data.append("Date,Time,InOrOut,Category,Amount Of Money")
                for(childBranch in snapshot.children){
                    data.append("\n" +
                            "${childBranch.child("day").value.toString()}," +
                            "${childBranch.child("time").value.toString()}," +
                            "${childBranch.child("inorout").value.toString()}," +
                            "${childBranch.child("category").value.toString()}," +
                            "${childBranch.child("money").value.toString()}")
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
                        "com.annhienktuit.mywallet.fileprovider",
                    fileLocation)

                    var fileIntent: Intent = Intent(Intent.ACTION_SEND)
                    fileIntent.type = "text/csv"
                    fileIntent.putExtra(Intent.EXTRA_SUBJECT, "Data")
                    fileIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    fileIntent.putExtra(Intent.EXTRA_STREAM, path)

                    startActivity(Intent.createChooser(fileIntent, "Send Mail"))
                }
                catch (e: Exception){
                    Toast.makeText(activity, "$e", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }

        })
    }
}