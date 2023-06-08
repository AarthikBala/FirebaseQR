package com.example.firebaseqr

import android.Manifest
import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.location.provider.ProviderProperties
import android.media.Image
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.animation.Interpolator
import android.view.animation.LinearInterpolator
import android.view.animation.TranslateAnimation
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.util.TimeUtils
import androidx.core.util.isNotEmpty
import androidx.core.util.size
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import com.google.android.gms.vision.CameraSource
import com.google.android.gms.vision.Detector
import com.google.android.gms.vision.barcode.Barcode
import com.google.android.gms.vision.barcode.BarcodeDetector
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcode.FORMAT_ALL_FORMATS
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


class qractivity : AppCompatActivity() {
    private lateinit var fire:FirebaseAuth
    private var cameracode : Int = 1001
    lateinit var barcode : BarcodeDetector
    lateinit var camera : CameraSource
    lateinit var database : DatabaseReference
    private fun barcodeconstructor(){
        barcode = BarcodeDetector.Builder(this)
            .setBarcodeFormats(FORMAT_ALL_FORMATS)
            .build();
        camera = CameraSource.Builder(this,barcode)
            .setAutoFocusEnabled(true)
            .build()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_qractivity);
        //initialize firebase auth
        var past : String = ""
        var messageview : TextView = findViewById(R.id.messages);
        var card : ConstraintLayout = findViewById(R.id.cardview);
        fire = Firebase.auth;
        database = Firebase.database.reference
        //login activity
        var mainActivity:Intent = Intent(this,MainActivity::class.java);
        //logout button in the view
        var logout = findViewById<Button>(R.id.logout);
        //code line in view
        fun askforpermission(){
            ActivityCompat.requestPermissions(this,arrayOf(Manifest.permission.CAMERA),cameracode);
        }
        var surface : SurfaceView = findViewById(R.id.surfaceview);
        if(ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED){
                barcodeconstructor();
        }else{
            askforpermission();
        }
        var codeline:View = findViewById(R.id.codeline);
        var transanimation = TranslateAnimation(0F,0F,-100F,1500F).apply{
            duration = 1000L
            interpolator = LinearInterpolator()
        }
        transanimation.repeatCount = Animation.INFINITE;
        transanimation.repeatMode = Animation.REVERSE;
        //animate codeline
        codeline.animation = transanimation.apply {
            start()
        }
        barcodeconstructor();
        //providing surface view holder to halder it lifecyclechange
        surface.holder.addCallback(object:SurfaceHolder.Callback{
            @SuppressLint("MissingPermission")
            override fun surfaceCreated(p0: SurfaceHolder) {
                //start the camera with given resource
                camera.start(p0);
            }

            @SuppressLint("MissingPermission")
            override fun surfaceChanged(p0: SurfaceHolder, p1: Int, p2: Int, p3: Int) {
                camera.start(p0)
            }

            override fun surfaceDestroyed(p0: SurfaceHolder) {
                //stop the came when surface view stops
                camera.stop();
            }
        })
        //set processor for barcode dectector which is like providing the functionallity
        barcode.setProcessor(object:Detector.Processor<Barcode>{
            override fun release() {
                //stop the camera when barcode stops working
                camera.stop()
            }

            override fun receiveDetections(p0: Detector.Detections<Barcode>?) {
                //execute the code when the decteced bar code is not emply and its not null
                if(p0?.detectedItems!!.isNotEmpty() && p0?.detectedItems != null){
                    //detect the first barcode
                    var current = p0!!.detectedItems.valueAt(0)
                    //find the format of the given barcode and format the string according to the type
                    var strr =when(current.format){
                            Barcode.WIFI -> "WIFI:${current.wifi.ssid}\n Password:${current.wifi.password}"
                            Barcode.EMAIL -> "Email : ${current.email.address} \n Subject: ${current.email.subject}"
                            Barcode.PHONE -> "Phone:${current.phone.number}"
                            Barcode.CALENDAR_EVENT -> "Calender:${current.calendarEvent.start}-${current.calendarEvent.end}"
                            Barcode.TEXT ->"Text: ${current.toString()}"
                            Barcode.URL -> "url:${current.url.url} \n title : ${current.url.title}"
                            Barcode.PRODUCT -> "Product: ${current.toString()}"
                            Barcode.GEO -> "Location: lat-${current.geoPoint.lat}\nlong:${current.geoPoint.lng}"
                            else -> current.displayValue
                        }
                    //action involving view should be run on ui thread
                    runOnUiThread {
                        //execute the code if the last url was not as now detected
                        if(past != strr){
                        card.visibility = View.INVISIBLE;
                            //set text to to messageview
                        messageview.setText(strr)
                        card.visibility = View.VISIBLE;
                            past = strr;
                            //store the data in our realtime database : https://console.firebase.google.com/u/0/project/newauth-c95ba/database/newauth-c95ba-default-rtdb/data/~2F
                            database.child("message").child(LocalDateTime.now().format(
                                DateTimeFormatter.ofPattern("yyMMddHHmmss"))).setValue(strr);
                        }else{
                            card.visibility = View.VISIBLE;
                        }
                    }
                }
            }
        })
        //initialize the snackbar
        var snack: Snackbar = Snackbar.make(logout,"you are sucessfully loged out",Snackbar.LENGTH_LONG);
        //logout onclick listener
        logout.setOnClickListener {
            //startActivity(Intent(this,testeracti::class.java));
            //signing out the current cridential
            fire.signOut();
            //showing the snack bar about signout
            snack.show();
            //show login activity
            startActivity(mainActivity);
            //clear this activity from backstack
            finish();
        }
    }
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if(requestCode == cameracode && grantResults[0] == PackageManager.PERMISSION_GRANTED){
            barcodeconstructor()
        }else{
            Toast.makeText(applicationContext,"error in request",Toast.LENGTH_SHORT).show();
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }
}