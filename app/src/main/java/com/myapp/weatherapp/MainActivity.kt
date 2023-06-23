package com.myapp.weatherapp

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.api.gax.core.BackgroundResource
import com.myapp.weatherapp.APIresponse.*
import com.myapp.weatherapp.databinding.ActivityMainBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.*

class MainActivity : AppCompatActivity() {



    private lateinit var mainBinding: ActivityMainBinding
    private lateinit var mFusedLocationClient: FusedLocationProviderClient
    private val permissionId = 2
    private var latitude_longitude = ""
    private val key = "3342e659ecbd461b85f102432232006"
    private val ayuda = "current.json?"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mainBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(mainBinding.root)
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        mainBinding.cvFeelslike.visibility = View.GONE
        mainBinding.cvHumidity.visibility = View.GONE
        mainBinding.cvWindDir.visibility = View.GONE
        mainBinding.cvWindKph.visibility = View.GONE

        mainBinding.btnLocation.setOnClickListener {
            getLocation()

        }
    }

    @SuppressLint("MissingPermission", "SetTextI18n")
    private fun getLocation() {
        if (checkPermissions()) {
            if (isLocationEnabled()) {
                mFusedLocationClient.lastLocation.addOnCompleteListener(this) { task ->
                    val location: Location? = task.result
                    if (location != null) {
                        val geocoder = Geocoder(this, Locale.getDefault())
                        val list: List<Address> =
                            geocoder.getFromLocation(location.latitude, location.longitude, 1) as List<Address>
                        weatherFromLatitudeLongitude("${list[0].latitude},${list[0].longitude}",key)
                    }
                }

            }
            else {
                Toast.makeText(this, "Please turn on location", Toast.LENGTH_LONG).show()
                val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                startActivity(intent)
            }
        } else {
            requestPermissions()
        }
    }

    private fun isLocationEnabled(): Boolean {
        val locationManager: LocationManager =
            getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER
        )
    }

    private fun checkPermissions(): Boolean {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            return true
        }
        return false
    }

    private fun requestPermissions() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
            ),
            permissionId
        )
    }

    @SuppressLint("MissingSuperCall")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        if (requestCode == permissionId) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                getLocation()
            }
        }
    }

    private fun getRetrofit():Retrofit{
        return Retrofit.Builder()
            .baseUrl("https://api.weatherapi.com/v1/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    private fun weatherFromLatitudeLongitude(wfll:String, key:String){
        CoroutineScope(Dispatchers.IO).launch {
            val call = getRetrofit().create(WeatherApi::class.java).getCurrentWeather("$wfll","$key")
            Log.e("EEEEEEERRROOOOORRR", call.toString())
            runOnUiThread{
                mainBinding.tvCity.text = call.location.name
                mainBinding.tvCondition.text = call.current.condition.text
                mainBinding.tvTemp.text = call.current.temp_c.toString()+"ºC"
                mainBinding.tvWinKph.text = call.current.wind_kph.toString()
                mainBinding.tvWindDir.text = call.current.wind_dir
                mainBinding.tvHumidity.text = call.current.humidity.toString()+"%"
                mainBinding.tvFeelslike.text = call.current.feelslike_c.toString()+"ºC"

                mainBinding.cvFeelslike.visibility = View.VISIBLE
                mainBinding.cvHumidity.visibility = View.VISIBLE
                mainBinding.cvWindDir.visibility = View.VISIBLE
                mainBinding.cvWindKph.visibility = View.VISIBLE


                val conditionMap = mapOf(
                    "Sunny" to R.drawable.soleado,
                    "Overcast" to R.drawable.nublado,
                    "Rainy" to R.drawable.lluvioso
                )

                val conditionText = call.current.condition.text

                for ((condition, resourceId) in conditionMap) {
                    if (conditionText.contains(condition, ignoreCase = true)) {
                        mainBinding.root.setBackgroundResource(resourceId)
                        break
                    }
                }
                when {
                    call.current.condition.text.contains("sunny", ignoreCase = true) -> {
                        mainBinding.root.setBackgroundResource(R.drawable.soleado)
                    }
                    call.current.condition.text.contains("cloudy", ignoreCase = true) -> {
                        mainBinding.root.setBackgroundResource(R.drawable.nublado)
                    }
                    call.current.condition.text.contains("rain", ignoreCase = true) -> {
                        mainBinding.root.setBackgroundResource(R.drawable.lluvioso)
                    }
                }

            }
        }
    }






}