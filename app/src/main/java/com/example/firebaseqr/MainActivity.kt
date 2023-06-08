package com.example.firebaseqr

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.auth.ktx.auth
import com.google.firebase.auth.ktx.userProfileChangeRequest
import com.google.firebase.ktx.Firebase

class MainActivity : AppCompatActivity() {
    private lateinit var fire:FirebaseAuth;
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main);
        //firebase auth instance
        fire = Firebase.auth;
        //qractivity intent
        var qractivity: Intent = Intent(this,qractivity::class.java);
        //check currently signed in or not
        if(fire.currentUser !=null){
            //if user is already signed in move to qractivity
            startActivity(qractivity);
            //remove this activity from backstack
            finish();
        }
        //email address edit text view
        var email : EditText = findViewById(R.id.emailedit);
        var snackbar:Snackbar = Snackbar.make(this,email,"Please,enter valid email and password",Snackbar.LENGTH_LONG);
        //password edit text view
        var password: EditText = findViewById(R.id.passwordedit);
        //intent to register activity
        var registeractivity:Intent = Intent(this,registeractivity::class.java);
        //sign-up text in view
        var buttontoregister:TextView = findViewById(R.id.buttonregister);
        //login button
        var loginbutton : Button = findViewById(R.id.loginbutton);
        //login progress bar
        var loginprogress : ProgressBar = findViewById(R.id.progresslogin);
        loginbutton.setOnClickListener {
            //make progress bar visible
            loginprogress.visibility = View.VISIBLE;
            //check that the email and password are not empty
            if(!(TextUtils.isEmpty(email.text)) && !(TextUtils.isEmpty(password.text))){
                //complete the login using firebase auth
                fire.signInWithEmailAndPassword(email.text.toString(),password.text.toString())
                    .addOnCompleteListener {
                        if(it.isSuccessful){
                            startActivity(qractivity);
                            loginprogress.visibility=View.GONE;
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
            loginprogress.visibility=View.GONE;
        }
        buttontoregister.setOnClickListener {
        //starting register activity
            startActivity(registeractivity);
            //clear from backstack
            finish();
        }
    }
}