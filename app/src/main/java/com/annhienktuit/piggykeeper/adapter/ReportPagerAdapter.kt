package com.annhienktuit.piggykeeper.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import com.annhienktuit.piggykeeper.fragments.CurrentMonthFragment
import com.annhienktuit.piggykeeper.fragments.PreviousMonthFragment

//This class manages 2 fragments previous month and current month in report fragment
class ReportPagerAdapter(fm: FragmentManager, behavior: Int) : FragmentStatePagerAdapter(fm, behavior){
    override fun getCount(): Int {
        return 2 //we have only two child fragments in report fragment
    }

    override fun getPageTitle(position: Int): CharSequence? {
        when(position){
            0 -> return "Previous Month"
            1 -> return "Current Month"
        }
        return super.getPageTitle(position)
    }

    override fun getItem(position: Int): Fragment {
        return when(position){
            0 -> PreviousMonthFragment()
            1 -> CurrentMonthFragment()
            else -> PreviousMonthFragment()
        }
    }

}