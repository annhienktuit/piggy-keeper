package com.annhienktuit.piggykeeper.activity

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.LocationManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.Gravity
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.annhienktuit.piggykeeper.R
import com.annhienktuit.piggykeeper.utils.Extensions.toast
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.thecode.aestheticdialogs.*
import kotlinx.android.synthetic.main.activity_map.*
import java.io.IOException
import java.util.*


class MapActivity : AppCompatActivity() {
    lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    var latitude: Double = 0.0
    var longtitude: Double = 0.0
    var queryType: String = ""
    var isFindingBank: Boolean = false //false is atm, true is bank
    var magicError = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)
        if (getSupportActionBar() != null) {
            getSupportActionBar()?.hide();
        }
        if (isLocationEnabled(this) == false) {
//            val builder = AlertDialog.Builder(this)
//            builder.setTitle("No location services")
//            builder.setMessage("Make sure you have turned on and allowed location services")
//            builder.setIcon(R.drawable.ic_baseline_warning_24)
//            builder.setPositiveButton("Okay") { dialog, which ->
//            }
//            builder.show()
            AestheticDialog.Builder(this, DialogStyle.CONNECTIFY, DialogType.ERROR)
                .setTitle("No location services")
                .setMessage("Make sure you have turned on and allowed location services")
                .setCancelable(false)
                .setDarkMode(true)
                .setGravity(Gravity.CENTER)
                .setAnimation(DialogAnimation.SHRINK)
                .setOnClickListener(object : OnDialogClickListener {
                    override fun onClick(dialog: AestheticDialog.Builder) {
                        dialog.dismiss()
                        //actions...
                    }
                })
                .show()
        }
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        fetchLocation() //innitialize location
        btnfindATM.setOnClickListener {
            isFindingBank = false
            fetchLocationandOpenMap() //get location
        }
        btnfindBank.setOnClickListener {
            isFindingBank = true
            fetchLocationandOpenMap()
        }

        btn_back.setOnClickListener {
            finish()
        }

    }

    fun isLocationEnabled(context: Context): Boolean? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val lm = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            lm.isLocationEnabled
        } else {
            val mode: Int = Settings.Secure.getInt(
                context.getContentResolver(), Settings.Secure.LOCATION_MODE,
                Settings.Secure.LOCATION_MODE_OFF
            )
            mode != Settings.Secure.LOCATION_MODE_OFF
        }
    }

    private fun fetchLocationandOpenMap() {
        val task = fusedLocationProviderClient.lastLocation
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this, arrayOf(
                    android.Manifest.permission.ACCESS_FINE_LOCATION,
                    android.Manifest.permission.ACCESS_COARSE_LOCATION,
                    android.Manifest.permission.ACCESS_BACKGROUND_LOCATION
                ), 101
            )
            toast("Location not allowed by system!")
            return
        } else {

        }
        task.addOnSuccessListener {
            if (it != null) {
                textViewLocation.text = "Latitude: ${it.latitude}" + "\nLongtitude: ${it.longitude}"
                latitude = it.latitude
                longtitude = it.longitude
                Log.i("location: ","$latitude + $longtitude")
                convertLocation() //convert latitude to address
                openMap()
            } else {
                toast("Location not allowed!")
                Log.e("location: ", "Magic 2")
                magicError = true

            }
        }
        task.addOnFailureListener {
            toast("Location not allowed!")
            Log.e("location: ","Failed to process location")
            magicError = true
        }
    }

    fun convertLocation() {
        if(magicError) {
            latitude = 14.368876981493111
            longtitude = 108.00725808369043
        }
        val geocoder = Geocoder(this, Locale.ENGLISH)
        try {
            val addresses: List<Address>? = geocoder.getFromLocation(latitude, longtitude, 1)
            if (addresses != null) {
                val returnedAddress: Address = addresses[0]
                Log.i("location", addresses[0].toString())
                    val strReturnedAddress =
                        StringBuilder("Your address:${returnedAddress.featureName}, ${returnedAddress.thoroughfare}, ${returnedAddress.locality}, ${returnedAddress.countryName}")
                    for (i in 0 until returnedAddress.getMaxAddressLineIndex()) {
                        strReturnedAddress.append(returnedAddress.getAddressLine(i)).append("\n")
                    }
                    textViewLocation.setText(strReturnedAddress.toString())

            }
        } catch (e: IOException) {
            e.printStackTrace()
            textViewLocation.setText("")
        }
    }

    fun openMap() {
        //still okay but not optimize
//        val uri = String.format(Locale.ENGLISH, "geo:%f,%f", latitude, longtitude) //open google maps
//        val uri = "https://www.google.com/maps/search/atm/@$latitude,$longtitude"
//        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uri))
        if (!isFindingBank) queryType = "atm"
        else queryType = "bank"
        toast("Finding nearby $queryType")
        val mapIntent: Intent = Uri.parse(
            "geo:$latitude,$longtitude?q=$queryType ${textViewBankName.text.toString()}"
        ).let { location ->
            Intent(Intent.ACTION_VIEW, location)
        }
        startActivity(mapIntent)
    }

    fun fetchLocation() {
        val task = fusedLocationProviderClient.lastLocation
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this, arrayOf(
                    android.Manifest.permission.ACCESS_FINE_LOCATION,
                    android.Manifest.permission.ACCESS_COARSE_LOCATION,
                    android.Manifest.permission.ACCESS_BACKGROUND_LOCATION
                ), 101
            )
            return
        } else {
        }
        task.addOnSuccessListener {
            if (it != null) {
                latitude = it.latitude
                longtitude = it.longitude
                convertLocation() //covert latitude to address
            }
        }
        task.addOnFailureListener {
            Log.e("location: ","Failed to process location")
            magicError = true
            convertLocation() //convert latitude to address
        }
    }
}




