package com.annhienktuit.piggykeeper.adapter

import android.app.AlertDialog
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.annhienktuit.piggykeeper.R
import com.annhienktuit.piggykeeper.`object`.Limitation
import com.annhienktuit.piggykeeper.utils.FirebaseUtils
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.FirebaseDatabase
import com.sasank.roundedhorizontalprogress.RoundedHorizontalProgressBar
import kotlinx.android.synthetic.main.layout_limitation_item.view.*
import java.text.DecimalFormat
import java.text.NumberFormat

class LimitationAdapter(private val limitationList: List<Limitation>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    val user: FirebaseUser? = FirebaseUtils.firebaseAuth.currentUser
    var ref = FirebaseDatabase
        .getInstance("https://my-wallet-80ed7-default-rtdb.asia-southeast1.firebasedatabase.app/")
        .getReference("datas").child(user?.uid.toString())
    class LimitationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nameLimit: TextView = itemView.tvTitle
        val total: TextView = itemView.tvTarget
        val current: TextView = itemView.tvSubTitle
        val progress: RoundedHorizontalProgressBar = itemView.progressBarLimit
        val mMenus: ImageView = itemView.ivMore
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if(viewType == 0) {
            val itemView = LayoutInflater.from(parent.context)
                .inflate(R.layout.layout_limitation_item, parent, false)
            return LimitationViewHolder(itemView)
        }
        else{
            val itemView = LayoutInflater.from(parent.context)
                .inflate(R.layout.layout_limitation_over_item, parent, false)
            return LimitationViewHolder(itemView)
        }
    }

    override fun getItemViewType(position: Int): Int {
        if (limitationList.get(position).current!! < limitationList.get(position).target!!)
            return 1
        else
            return 0
    }


    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        fun popupMenus(v: View, adapterPosition: Int) {
            val pos = limitationList[adapterPosition]
            val popupMenus = PopupMenu(holder.itemView.context, v)
            popupMenus.inflate(R.menu.show_menu_limitation)
            popupMenus.setOnMenuItemClickListener {
                when(it.itemId){
                    R.id.editText->{
                        val view = LayoutInflater.from(holder.itemView.context).inflate(R.layout.dialog_edit_limitation,null)
                        val nameEdit = view.findViewById<TextView>(R.id.editNameLimitation)
                        val moneyEdit = view.findViewById<TextInputEditText>(R.id.editMoneyLimitation)
                        nameEdit.text = pos.limitedGroup.toString()
                        moneyEdit.setText(pos.target.toString())
                        AlertDialog.Builder(holder.itemView.context)
                            .setView(view)
                            .setPositiveButton("OK") {
                                    dialog,_->
                                ref.child("limits").child("limit" + pos.index).child("costLimit")
                                    .setValue(moneyEdit.text.toString())
                                dialog.dismiss()
                            }
                            .setNegativeButton("Cancel") {
                                    dialog,_->
                                dialog.dismiss()
                            }
                            .create()
                            .show()

                        true
                    }
                    R.id.delete->{
                        /**set delete*/
                        AlertDialog.Builder(holder.itemView.context)
                            .setTitle("Delete")
                            .setIcon(R.drawable.ic_baseline_warning_24)
                            .setMessage("Are you sure delete this Information")
                            .setPositiveButton("Yes"){
                                    dialog,_->
                                ref.child("limits").child("limit" + pos.index).removeValue()
                                notifyDataSetChanged()
                                dialog.dismiss()
                            }
                            .setNegativeButton("No"){
                                    dialog,_->
                                dialog.dismiss()
                            }
                            .create()
                            .show()

                        true
                    }
                    else-> true
                }

            }
            popupMenus.show()
            val popup = PopupMenu::class.java.getDeclaredField("mPopup")
            popup.isAccessible = true
            val menu = popup.get(popupMenus)
            menu.javaClass.getDeclaredMethod("setForceShowIcon",Boolean::class.java)
                .invoke(menu,true)
        }
        val currentItem = limitationList[position]
        val holder1 = holder as LimitationViewHolder
        holder1.nameLimit.text = currentItem.limitedGroup
        if (currentItem.target != null)
            holder1.total.text = changeToMoney(currentItem.target)
        else
            holder1.total.text = currentItem.target
        try {
            val tmp1 = currentItem.current?.toLongOrNull()
            val tmp2 = currentItem.target?.toLongOrNull()
            var result = 0
            var result2 = 0
            var result3 = 0
            if (tmp1 != null && tmp2 != null) {
                result = ((tmp1 * 100) / tmp2).toInt()
                result2 = (tmp2 - tmp1).toInt()
                result3 = (tmp1 - tmp2).toInt()
                if (tmp1 <= tmp2) {
                    holder1.current.text = (changeToMoney(result2.toString()) + " USD left")
                    holder1.progress.progress = result
                } else {
                    holder1.current.text = ("Over " + changeToMoney(result3.toString()) + " USD")
                    holder1.progress.progress = 100
                }
            }
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
        holder1.mMenus.setOnClickListener {
            popupMenus(it, position)
        }
    }

    override fun getItemCount() = limitationList.size

    private fun changeToMoney(str: String?): String? {
        val formatter: NumberFormat = DecimalFormat("#,###")
        if (str != null) {
            try {
                val myNumber = str.toDouble()
                return if (myNumber < 0)
                    formatter.format(-myNumber)
                else
                    formatter.format(myNumber)
            }
            catch (e:NumberFormatException){
                Log.e("numberformat: ", e.toString() )
            }

        }
        return null
    }
}



