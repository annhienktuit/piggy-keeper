package com.annhienktuit.piggykeeper.utils

object DemandDeposit {
    fun calculateInterest(amount: Double, rate: Double, period: Int): Double{
        return amount*rate*(period.toDouble()/360) //Công thức tính lãi suất tiết kiệm không kỳ hạn
    }
}