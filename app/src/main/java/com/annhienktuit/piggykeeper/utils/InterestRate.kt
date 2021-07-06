package com.annhienktuit.piggykeeper.utils

import kotlin.math.pow
import kotlin.math.round

//https://timo.vn/blogs/tai-khoan-tiet-kiem/cach-tinh-lai-suat-ngan-hang-lai-suat-tiet-kiem-nhanh-chong-4/
//https://nhadautumeeyland.com/cong-thuc-tinh-lai-suat-don-va-lai-suat-kep/
object InterestRate {
    //Lãi suất kép
    fun compoundInterest(PV: Long, interest: Double, period: Int): Double {
        return round(PV*((1+interest).pow(period))) //FV = PV x (1 + i)^n
    }
    //Lãi suất đơn
    fun simpleInterest(PV: Long, interest: Double, period: Int): Double{
        return round(PV*(1 + (interest * period))) //FV = PV x (1 + i * n)
    }
}