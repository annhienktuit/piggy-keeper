package com.annhienktuit.piggykeeper.utils

import android.app.Application
import com.google.firebase.database.FirebaseDatabase

class FirebaseOffline : Application() {
    override fun onCreate() {
        super.onCreate()
        FirebaseDatabase.getInstance("https://my-wallet-80ed7-default-rtdb.asia-southeast1.firebasedatabase.app").setPersistenceEnabled(true)
    }
}