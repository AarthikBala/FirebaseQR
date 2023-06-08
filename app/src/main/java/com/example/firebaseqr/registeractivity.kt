package com.example.firebaseqr

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class registeractivity : AppCompatActivity() {
    private lateinit var fire : FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registeractivity);
        fire = Firebase.auth
        //intent to qractivity
        var qractivity: Intent = Intent(this,qractivity::class.java);
        //intent to mainactivity (login activity)
        var mainActivity:Intent = Intent(this,MainActivity::class.java);
        //email address edit text view
        var email : EditText = findViewById(R.id.remailedit);
        //password edit text view
        var password: EditText = findViewById(R.id.rpasswordedit);
        //initialize snackbar
        var snackbar: Snackbar = Snackbar.make(this,email,"Please,enter valid email and password",
            Snackbar.LENGTH_LONG);
        //login textview
        var buttontologin : TextView = findViewById(R.id.buttonlogin);
        //signup button
        var signupbutton : Button = findViewById(R.id.signupbutton);
        //signup progress bar
        var signupprogress : ProgressBar = findViewById(R.id.progresssignup);
        buttontologin.setOnClickListener {
            //starting login activity(mainactivity)
            startActivity(mainActivity);
            //clear from backstack
            finish();
        }
        signupbutton.setOnClickListener {
            signupprogress.visibility = View.VISIBLE;
            //check that the email and password are not empty
            if(!(TextUtils.isEmpty(email.text)) && !(TextUtils.isEmpty(password.text))){
                //complete the login using firebase auth
                fire.createUserWithEmailAndPassword(email.text.toString(),password.text.toString())
                    .addOnCompleteListener {
                        if(it.isSuccessful){
                            startActivity(qractivity);
                            signupprogress.visibility=View.GONE;
                            finish();
                        }else{
                            it.addOnFailureListener {
                                snackbar.setText("${it.message.toString()}").show();
                            }
                        }
                    }
            }else{
                //show snack bar
                snackbar.show();
            }
            signupprogress.visibility=View.GONE;
        }
    }
}