package com.annhienktuit.piggykeeper.activity

import FlipPageViewTransformer
import android.content.Context
import android.os.Bundle
import android.os.PersistableBundle
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager.widget.ViewPager
import com.annhienktuit.piggykeeper.R
import com.annhienktuit.piggykeeper.`object`.CoderModel
import com.annhienktuit.piggykeeper.adapter.CoderAdapter
import com.wajahatkarim3.easyflipviewpager.CardFlipPageTransformer2
import kotlinx.android.synthetic.main.activity_about.*


private var mContext: Context? = null
class AboutActivity : AppCompatActivity() {
    private lateinit var coderModelList: ArrayList<CoderModel>
    private lateinit var coderAdapter: CoderAdapter
    private lateinit var actionBar: ActionBar
    val urlNhien = "https://www.facebook.com/annhienkt/"
    val urlHoan = "https://www.facebook.com/lekhaihoan.2306"
    val urlHien = "https://www.facebook.com/tran.thanhhien.967"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)
        mContext = this
        loadData()
        //actionBar = this.supportActionBar!!
        val cardFlipPageTransformer = CardFlipPageTransformer2()
        cardFlipPageTransformer.isScalable = false
        viewpager.addOnPageChangeListener(object: ViewPager.OnPageChangeListener{
            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {
                //actionBar.title = coderModelList[position].name
            }

            override fun onPageSelected(position: Int) {
            }

            override fun onPageScrollStateChanged(state: Int) {

            }
        })

        btnArrowBack.setOnClickListener {
            finish()
        }
    }

    override fun onSaveInstanceState(outState: Bundle, outPersistentState: PersistableBundle) {
//        super.onSaveInstanceState(outState, outPersistentState)
    }

    private fun loadData() {
        coderModelList = ArrayList()
        coderModelList.add(
            CoderModel("Nhien Nguyen",
            "Team Leader & Functional Developer",
            R.drawable.avt_nhien_deptrai,urlNhien))
        coderModelList.add(CoderModel("Hoan Le",
            "Database & Functional Developer",
            R.drawable.hoanle,urlHoan))
        coderModelList.add(CoderModel("Hien Tran",
            "UI Designer & Functional Developer",
            R.drawable.hien_bede,urlHien))
        coderAdapter = CoderAdapter(applicationContext , coderModelList)
        viewpager.adapter = coderAdapter
        viewpager.setPadding(100,0,100,0)
    }

}