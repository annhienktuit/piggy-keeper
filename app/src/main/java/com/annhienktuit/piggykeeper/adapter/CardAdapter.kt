package com.annhienktuit.piggykeeper.adapter

import android.app.AlertDialog
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.annhienktuit.piggykeeper.activity.CardActivity
import com.annhienktuit.piggykeeper.R
import com.annhienktuit.piggykeeper.`object`.Card
import com.annhienktuit.piggykeeper.utils.FirebaseUtils
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.layout_card_planning.view.*

class CardAdapter(private val cardList: List<Card>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    val user: FirebaseUser? = FirebaseUtils.firebaseAuth.currentUser
    var ref = FirebaseDatabase.getInstance("https://my-wallet-80ed7-default-rtdb.asia-southeast1.firebasedatabase.app/")
        .getReference("datas").child(user?.uid.toString())
    class CardViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val card: TextView = itemView.cardName
        val person: TextView = itemView.personName
        val bank: TextView = itemView.bankName
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.layout_card_planning, parent, false)
        return CardViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        try {
            val currentItem = cardList[position]
            var holder1 = holder as CardViewHolder
            holder1.card.text = currentItem.nameCard
            holder1.person.text = currentItem.namePerson
            holder1.bank.text = currentItem.bankName
            holder1.itemView.setOnClickListener {
                val intent = Intent(holder.itemView.context, CardActivity::class.java)
                intent.putExtra("position", currentItem.index)
                holder1.itemView.context.startActivity(intent)
            }
            holder.itemView.setOnLongClickListener {
                val builder = AlertDialog.Builder(holder.itemView.context)
                builder.setMessage("Do you want to delete this card?")
                builder.setPositiveButton("Yes")
                { dialog, which ->
                    ref.child("cards").child("card" + currentItem.index).removeValue()
                }
                builder.setNegativeButton("No")
                { dialog, which ->
                    dialog.dismiss()
                }
                builder.create()
                builder.show()
                false
            }
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
    }

    override fun getItemCount() = cardList.size

    fun getFirstCard(): Int {
        if (cardList.isNotEmpty()) return cardList[0].index!!
        return 0
    }
    fun deleteItem(pos: Int) {
        val currentItem = cardList[pos]
        ref.child("cards").child("card" + currentItem.index).removeValue()
    }
}