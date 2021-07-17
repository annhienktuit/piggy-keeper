package com.annhienktuit.piggykeeper.activity

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isEmpty
import androidx.core.view.isNotEmpty
import com.annhienktuit.piggykeeper.R
import com.annhienktuit.piggykeeper.utils.FirebaseUtils
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.ktx.Firebase
import com.thecode.aestheticdialogs.AestheticDialog
import com.thecode.aestheticdialogs.DialogStyle
import com.thecode.aestheticdialogs.DialogType
import kotlinx.android.synthetic.main.activity_change_password.*


class ChangePasswordActivity : AppCompatActivity() {
    val user: FirebaseUser? = FirebaseUtils.firebaseAuth.currentUser
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_change_password)
        btnDoChangePassword.setOnClickListener {
            if(inputConfirm.text?.isEmpty() == true) confirmPass.error = "Please fill in all fields"
            else if(inputNew.text?.isEmpty() == true) newPass.error = "Please fill in new password"
            else if(inputCurrent.text?.isEmpty() == true) currentPass.error = "Please fill in current password"
            else {
                val credential = EmailAuthProvider
                    .getCredential(user?.email.toString(), inputCurrent.text.toString())
                if (user != null) {
                    user.reauthenticate(credential)
                        .addOnCompleteListener {
                            if(isNotEmpty() && isCorrect()){
                                changePassword(inputNew.text.toString())
                            }
                            else if(isNotEmpty() && !isCorrect()) {
                                AestheticDialog.Builder(this, DialogStyle.FLASH, DialogType.ERROR)
                                    .setTitle("Error")
                                    .setMessage("Please make sure two passwords are identical")
                                    .show()
                            }
                        }
                        .addOnFailureListener {
                            AestheticDialog.Builder(this, DialogStyle.FLASH, DialogType.ERROR)
                                .setTitle("Error")
                                .setMessage("Please check again your current password")
                                .show()
                        }
                }
            }
        }
    }

    private fun changePassword(newPassword:String) {
        user!!.updatePassword(newPassword)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d("password: ", "User password updated.")
                    AestheticDialog.Builder(this, DialogStyle.FLASH, DialogType.SUCCESS)
                        .setTitle("Success")
                        .setMessage("Password changed successfully")
                        .show()
                }
            }.addOnFailureListener {
                AestheticDialog.Builder(this, DialogStyle.FLASH, DialogType.ERROR)
                    .setTitle("Error")
                    .setMessage("There was an error on changing your password")
                    .show()
            }
    }

    private fun isCorrect(): Boolean {
        return inputNew.text.toString() == inputConfirm.text.toString()
    }

    private fun isNotEmpty(): Boolean {
        if(confirmPass.isNotEmpty() && newPass.isNotEmpty() && currentPass.isNotEmpty()){
            return true
        }
        return false
    }
}