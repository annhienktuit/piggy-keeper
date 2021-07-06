package com.annhienktuit.piggykeeper.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.viewpager.widget.PagerAdapter
import com.annhienktuit.piggykeeper.R
import com.annhienktuit.piggykeeper.`object`.CoderModel
import kotlinx.android.synthetic.main.card_about_item.view.*


class CoderAdapter(private val context: Context, private val coderList: ArrayList<CoderModel>) :PagerAdapter() {
    override fun getCount(): Int {
        return coderList.size
    }

    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view == `object`
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val view = LayoutInflater.from(context).inflate(R.layout.card_about_item, container, false)
        val model = coderList[position]
        val name = model.name
        val description = model.description
        val img = model.img
        val url = model.url
        view.bannerIv.setImageResource(img!!)
        view.tvDescription.text = description!!
        view.tvName.text = name!!
        container.addView(view,position)

        return view
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        container.removeView(`object` as View)
    }


}