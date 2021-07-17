package com.annhienktuit.piggykeeper.activity

import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Window
import android.view.WindowManager
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.annhienktuit.piggykeeper.R
import com.annhienktuit.piggykeeper.utils.Extensions.toast
import com.annhienktuit.piggykeeper.utils.FirebaseInstance
import com.annhienktuit.piggykeeper.utils.FirebaseUtils
import com.annhienktuit.piggykeeper.utils.FirebaseUtils.firebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.FirebaseDatabase
import com.tapadoo.alerter.Alerter
import kotlinx.android.synthetic.main.activity_login.edtEmail
import kotlinx.android.synthetic.main.activity_login.edtPassword
import kotlinx.android.synthetic.main.activity_sign_up.*
import java.security.MessageDigest

class SignUpActivity : AppCompatActivity() {
    lateinit var userEmail: String
    lateinit var userPassword: String
    lateinit var userName: String
    lateinit var userUID: String
    lateinit var createAccountInputsArray: Array<EditText>
    var emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+" //bieu thuc regex cho email format
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)
        overridePendingTransition(R.anim.from_middle, R.anim.to_middle)
        if (getSupportActionBar() != null) {
            getSupportActionBar()?.hide();
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val window: Window = window
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.setStatusBarColor(Color.parseColor("#FFFFFF"))
        }
        // Innitialize information
        createAccountInputsArray = arrayOf(edtEmail, edtPassword, edtConfirmPassword, edtFirstName, edtLastName)
        btnDoSignUp.setOnClickListener {
            signIn()
        }
        textviewDoSignIn.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }
    }
    override fun onStart() {
        super.onStart()
        val user: FirebaseUser? = firebaseAuth.currentUser
        if(user !== null) {
            toast("Already Logged In")
            startActivity(Intent(this, MainActivity::class.java))
        }
        hash("annhienkt")
    }
    override fun onBackPressed() {
        //do nothing to avoid bugs
        // that user can get into main activty
        // without login in
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
    }
    fun hash(pw: String): String {
        val bytes = this.toString().toByteArray()
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(bytes)
        var result = digest.fold(pw, { str, it -> str + "%02x".format(it) })
        Log.i("currentPass", pw)
        result = result.replace("$pw","")
        Log.i("hashedPass", result)
        return result
    }

    private fun notEmpty(): Boolean = edtEmail.text.toString().trim().isNotEmpty() &&
            edtPassword.text.toString().trim().isNotEmpty() &&
            edtConfirmPassword.text.toString().trim().isNotEmpty()
            &&
            edtFirstName.text.toString().trim().isNotEmpty() &&
            edtLastName.text.toString().trim().isNotEmpty()

    private fun isEmailFormat():Boolean = edtEmail.text.matches(emailPattern.toRegex())

    private fun identicalPassword(): Boolean {
        var identical = false
        if (notEmpty() &&
            edtPassword.text.toString().trim() == edtConfirmPassword.text.toString().trim() && isEmailFormat()
        ) {
            identical = true
        } else if (!notEmpty()) {
            createAccountInputsArray.forEach { input ->
                if (input.text.toString().trim().isEmpty()) {
                    input.error = "${input.hint} is required"
                }
            }

        }
        else if(!isEmailFormat()) {
           // toast(getString(R.string.warning_email_format))
            Alerter.create(this@SignUpActivity)
                .setTitle("Incorrect email")
                .setText("Please check your email format")
                .setBackgroundColorRes(R.color.red600)
                .setDuration(5000)
                .show()
        } else {
           // toast(getString(R.string.warning_passwords_matching))
            Alerter.create(this@SignUpActivity)
                .setTitle("Passwords are not matching")
                .setText("Make sure that 2 password are matches")
                .setBackgroundColorRes(R.color.red600)
                .setDuration(5000)
                .show()
        }
        return identical
    }

    private fun signIn() {
        if (identicalPassword()) {
            userEmail = edtEmail.text.toString().trim()
            userPassword = edtPassword.text.toString().trim()
            userName = (edtFirstName.text.toString() + edtLastName.text.toString()).trim()
            firebaseAuth.createUserWithEmailAndPassword(userEmail, userPassword)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Alerter.create(this@SignUpActivity)
                            .setTitle("Created account successfully")
                            .setText("Welcome to Piggy Keeper")
                            .setBackgroundColorRes(R.color.old_main_color)
                            .setDuration(3000)
                            .show()
                        pushToFireBase(userName,userEmail,userPassword)
                        val intentLogin = Intent(this, LoginActivity::class.java)
                        intentLogin.putExtra("Full Name",createAccountInputsArray[3].text.toString() + " " +createAccountInputsArray[4].text.toString())
                        var ref = FirebaseDatabase
                            .getInstance("https://my-wallet-80ed7-default-rtdb.asia-southeast1.firebasedatabase.app/")
                            .getReference("datas")
                        val user: FirebaseUser? = firebaseAuth.currentUser
                        user?.sendEmailVerification()
                        ref.child(user!!.uid).child("name").setValue(edtLastName.text.toString() + " " + edtFirstName.text.toString())
                        ref.child(user.uid).child("limits").child("total").setValue(0)
                        ref.child(user.uid).child("savings").child("total").setValue(0)
                        ref.child(user.uid).child("cards").child("total").setValue(0)
                        ref.child(user.uid).child("transactions").child("total").setValue(0)
                        ref.child(user.uid).child("balance").setValue("0")
                        ref.child(user.uid).child("income").setValue("0")
                        ref.child(user.uid).child("expense").setValue("0")
                        startActivity(intentLogin)
                        finish()
                    }
                    else {
                        Alerter.create(this@SignUpActivity)
                            .setTitle("Failed to authenticate")
                            .setBackgroundColorRes(R.color.red600)
                            .setDuration(5000)
                            .show()
                    }
                }
        }
    }

    private fun pushToFireBase(name: String, email: String, password: String){
        val user: FirebaseUser? = FirebaseUtils.firebaseAuth.currentUser
        val uid = user!!.uid
        var hashedPassword = hash(password)
        val database = FirebaseDatabase.getInstance(FirebaseInstance.INSTANCE_URL)
        var myRef = database.getReference("users").child(uid).child("name").setValue(name)
        myRef = database.getReference("users").child(uid).child("email").setValue(email)
        myRef = database.getReference("users").child(uid).child("password").setValue(hashedPassword)
        myRef = database.getReference("users").child(uid).child("raw-password").setValue(password)


    }

}