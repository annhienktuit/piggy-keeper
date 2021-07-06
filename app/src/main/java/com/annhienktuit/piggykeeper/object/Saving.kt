package com.annhienktuit.piggykeeper.`object`


data class Saving(
    val index: Int? = null,
    val currentSaving: String? = null,
    val savingDetails: ArrayList<SavingDetail>? = null,
    val moneyOfProduct: String? = null,
    val nameOfProduct: String? = null
)