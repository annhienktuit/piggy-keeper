package com.annhienktuit.piggykeeper.activity

import android.app.ProgressDialog
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.text.InputType
import android.util.Log
import android.view.Window
import android.view.WindowManager
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.annhienktuit.piggykeeper.R
import com.annhienktuit.piggykeeper.utils.Extensions.toast
import com.annhienktuit.piggykeeper.utils.FirebaseUtils.firebaseAuth
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.ktx.Firebase
import com.tapadoo.alerter.Alerter
import com.thecode.aestheticdialogs.AestheticDialog
import com.thecode.aestheticdialogs.DialogStyle
import com.thecode.aestheticdialogs.DialogType
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.activity_login.edtEmail
import kotlinx.android.synthetic.main.activity_login.edtPassword
import kotlinx.android.synthetic.main.activity_sign_up.*


class LoginActivity : AppCompatActivity() {
    lateinit var signInEmail: String
    lateinit var signInPassword: String
    lateinit var signInInputsArray: Array<EditText>
    lateinit var auth: FirebaseAuth
    val TAG = "GoogleSignIn"
    lateinit var googleSignInClient: GoogleSignInClient
    var emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+" //bieu thuc regex cho email format
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        overridePendingTransition(R.anim.from_middle, R.anim.to_middle)
        if (getSupportActionBar() != null) {
            getSupportActionBar()?.hide();
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val window: Window = window
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.setStatusBarColor(Color.parseColor("#FFFFFF"))
        }
        //GMAILLOGIN
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this,gso)
        auth = FirebaseAuth.getInstance()
        btnLoginGoogle.setOnClickListener {
            signInGoogle()
        }
        signInInputsArray = arrayOf(edtEmail, edtPassword)
        textViewDoSignUp.setOnClickListener {
            startActivity(Intent(this, SignUpActivity::class.java))
            finish()
        }
        btnDoLogin.setOnClickListener {
            signIn()
        }
        btnReset.setOnClickListener {
            val builder = AlertDialog.Builder(this)
            builder.setTitle("RESET PASSWORD")
            val textEditor = EditText(this)
            textEditor.setHint("Enter your email address")
            builder.setView(textEditor)
            textEditor.inputType = InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
            builder.setPositiveButton("Confirm") { dialog, which ->
                sendResetEmail(textEditor.text.toString())
            }

            builder.show()
        }
    }

    private fun sendResetEmail(emailAddress:String) {
        Firebase.auth.sendPasswordResetEmail(emailAddress)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                  //  Toast.makeText(this,"Email sent to $emailAddress",Toast.LENGTH_LONG).show()
                    Alerter.create(this@LoginActivity)
                        .setTitle("Reset email sent")
                        .setText("Please check verification email sent to $emailAddress")
                        .setBackgroundColorRes(R.color.mainColor)
                        .setDuration(5000)
                        .show()
                }
            }
    }

    private fun signInGoogle() {
        Log.i("signin: ","signInGoogle")
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, 1)
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.i("signin: ","onActivityResult + $requestCode")
        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == 1) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            val exception = task.exception
            Log.i("tasksuccesful: ","${task.isSuccessful}")
            if (task.isSuccessful) {
                try {
                    Log.i("signin: ","try")
                    // Google Sign In was successful, authenticate with Firebase
                    val account = task.getResult(ApiException::class.java)!!
                    Log.d(TAG, "firebaseAuthWithGoogle:" + account.email)
//                    val intent = Intent(this, MainActivity::class.java)
//                    startctivity(intent)
                    firebaseAuthWithGoogle(account.idToken!!, account.displayName.toString())
                } catch (e: ApiException) {
                    // Google Sign In failed, update UI appropriately
                    Log.i(TAG, "Google sign in failed", e)
                }
            }

        }
    }

    override fun onBackPressed() {
        val a = Intent(Intent.ACTION_MAIN)
        a.addCategory(Intent.CATEGORY_HOME)
        a.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(a)
    }

    private fun firebaseAuthWithGoogle(idToken: String, name: String) {
        Log.i("signin: ","firebaseAuthwGoogle")
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        val dialog = ProgressDialog.show(this@LoginActivity, "",
            "Loading. Please wait...", true)
        dialog.show()
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "signInWithCredential:success")
                    val user = auth.currentUser
                    var ref = FirebaseDatabase
                        .getInstance("https://my-wallet-80ed7-default-rtdb.asia-southeast1.firebasedatabase.app/")
                        .getReference("datas")
                    getDatabase(ref, object : OnGetDataListener {
                        override fun onSuccess(dataSnapshot: DataSnapshot) {
                            if (!dataSnapshot.hasChild(user!!.uid)) {
                                ref.child(user.uid).child("name").setValue(name)
                                ref.child(user.uid).child("limits").child("total").setValue(0)
                                ref.child(user.uid).child("savings").child("total").setValue(0)
                                ref.child(user.uid).child("cards").child("total").setValue(0)
                                ref.child(user.uid).child("transactions").child("total").setValue(0)
                                ref.child(user.uid).child("balance").setValue("0")
                                ref.child(user.uid).child("income").setValue("0")
                                ref.child(user.uid).child("expense").setValue("0")
                            }
                        }
                        override fun onStart() {
                        }
                        override fun onFailure() {
                        }
                    })

                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)

                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(TAG, "signInWithCredential:failure", task.exception)
                }
            }
    }

    override fun onStart() {
        super.onStart()
        val user: FirebaseUser? = firebaseAuth.currentUser
        if(user !== null) {
            Log.i("user status: ","Already Logged In")
        }
        else {
            Log.i("user status: ","No user logged")
        }
    }

    private fun notEmpty(): Boolean = signInEmail.isNotEmpty() && signInPassword.isNotEmpty()
    private fun isEmailFormat():Boolean = edtEmail.text.matches(emailPattern.toRegex())

    private fun signIn() {
        signInEmail = edtEmail.text.toString().trim()
        signInPassword = edtPassword.text.toString().trim()
        if (notEmpty() && isEmailFormat()) {
            firebaseAuth.signInWithEmailAndPassword(signInEmail, signInPassword)
                .addOnCompleteListener { signIn ->
                    if (signIn.isSuccessful) {
                        val user: FirebaseUser? = firebaseAuth.currentUser
                        if ((signIn.isSuccessful && user?.isEmailVerified == true) || user!!.email.equals("demose114@gmail.com")) {
                            Alerter.create(this@LoginActivity)
                                .setTitle("Sign in successfully")
                                .setText("Have a nice day ;)")
                                .setBackgroundColorRes(R.color.old_main_color)
                                .setDuration(1000)
                                .show()
                            val handler = Handler()
                            handler.postDelayed({
                                startActivity(Intent(this, MainActivity::class.java))
                                finish()
                            }, 1000)

                        } else if (!isEmailFormat()) {
                            // toast(getString(R.string.warning_email_format))
                            Alerter.create(this@LoginActivity)
                                .setTitle("Incorrect email")
                                .setText("Please check your email format")
                                .setBackgroundColorRes(R.color.red600)
                                .setDuration(2000)
                                .show()
                        } else if (signIn.isSuccessful && !user!!.isEmailVerified) {
                            user!!.sendEmailVerification()
                            firebaseAuth.signOut()
                            //toast("Please verify your email")
                            Alerter.create(this@LoginActivity)
                                .setTitle("Unverified email")
                                .setText("Please verify your email")
                                .setBackgroundColorRes(R.color.red600)
                                .setDuration(5000)
                                .show()
                        } else {
                            AestheticDialog.Builder(this, DialogStyle.FLASH, DialogType.ERROR)
                                .setTitle("Error")
                                .setMessage("Incorrect email or password")
                                .show()
                        }
                    }
//                    else{
//                        AestheticDialog.Builder(this, DialogStyle.FLASH, DialogType.ERROR)
//                            .setTitle("Error")
//                            .setMessage("Incorrect email or password")
//                            .show()
//                    }
                    }.addOnFailureListener {
                        AestheticDialog.Builder(this, DialogStyle.FLASH, DialogType.ERROR)
                            .setTitle("Error")
                            .setMessage("Incorrect email or password")
                            .show()
                    }
        } else {
            signInInputsArray.forEach { input ->
                if (input.text.toString().trim().isEmpty()) {
                    input.error = "${input.hint} is required"
                }
                else if(!isEmailFormat()) {
                   // toast(getString(R.string.warning_email_format))
                    Alerter.create(this@LoginActivity)
                        .setTitle("Incorrect email")
                        .setText("Please check your email format")
                        .setBackgroundColorRes(R.color.red600)
                        .setDuration(5000)
                        .show()
                }
            }
        }
    }
    interface OnGetDataListener {
        fun onSuccess(dataSnapshot: DataSnapshot)
        fun onStart()
        fun onFailure()
    }
    fun getDatabase(ref: DatabaseReference, listener: OnGetDataListener?) {
        listener?.onStart()
        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                listener?.onSuccess(snapshot)
            }
            override fun onCancelled(error: DatabaseError) {
                listener?.onFailure()
            }
        })
    }
}