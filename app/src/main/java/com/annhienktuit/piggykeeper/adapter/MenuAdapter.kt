package com.annhienktuit.piggykeeper.adapter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.annhienktuit.piggykeeper.R
import com.annhienktuit.piggykeeper.`object`.Menu
import kotlinx.android.synthetic.main.layout_menu_item.view.*

class MenuAdapter(private val menuList: List<Menu>) : RecyclerView.Adapter<MenuAdapter.MenuViewHolder>() {

    class MenuViewHolder(listItemView: View) : RecyclerView.ViewHolder(listItemView) {
        val titleTextView:TextView = listItemView.tvMenu
        val imageMenu: ImageView = listItemView.imgMenu
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MenuViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.layout_menu_item, parent, false)
        return MenuViewHolder(itemView)

    }

    override fun onBindViewHolder(holder: MenuViewHolder, position: Int) {
        val currentItem = menuList[position]
        var menuHolder = holder as MenuViewHolder
        menuHolder.titleTextView.text = currentItem.title
        menuHolder.imageMenu.setImageResource(R.drawable.ic_baseline_settings_24)
    }

    override fun getItemCount(): Int {
        return 0
    }
}