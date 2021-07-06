package com.annhienktuit.piggykeeper.`object`

data class RecentTransaction(
    val index: Int? = null,
    val dayOfTrans: String? = null,
    val inOrOut: String? = null,
    val moneyOfTrans: String? = null,
    val nameOfTrans: String? = null,
    val timeOfTrans: String? = null,
    val category: String? = null
)