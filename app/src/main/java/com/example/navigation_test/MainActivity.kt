package com.example.navigation_test

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.net.ConnectivityManager
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.util.*


class MainActivity : AppCompatActivity() {
    var tvEnabledGPS: TextView? = null
    var tvStatusGPS: TextView? = null
    var tvLocationGPS: TextView? = null
    var tvEnabledNet: TextView? = null
    var tvStatusNet: TextView? = null
    var tvLocationNet: TextView? = null

    private lateinit var locationManager: LocationManager
    var sbGPS = StringBuilder()
    var sbNet = StringBuilder()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        tvEnabledGPS = findViewById(R.id.tvEnabledGPS);
        tvStatusGPS = findViewById(R.id.tvStatusGPS);
        tvLocationGPS = findViewById(R.id.tvLocationGPS);
        tvEnabledNet = findViewById(R.id.tvEnabledNet);
        tvStatusNet = findViewById(R.id.tvStatusNet);
        tvLocationNet = findViewById(R.id.tvLocationNet);
        locationManager = (getSystemService(LOCATION_SERVICE) as LocationManager?)!!;
        requestPermissions()
    }

    override fun onResume() {
        super.onResume()

        checkEnabled();
    }

    override fun onPause() {
        super.onPause()
        locationManager.removeUpdates(locationListener)
    }

    private val locationListener: LocationListener = object : LocationListener {

        override fun onProviderDisabled(provider: String) {
            checkEnabled()
        }

        override fun onProviderEnabled(provider: String) {
            checkEnabled()
            showLocation(locationManager.getLastKnownLocation(provider))
        }

        override fun onLocationChanged(location: Location) {
            showLocation(location)
        }

        override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {
            if (provider == LocationManager.GPS_PROVIDER) {
                tvStatusGPS!!.text = "Status: $status"
            } else if (provider == LocationManager.NETWORK_PROVIDER) {
                tvStatusNet!!.text = "Status: $status"
            }
        }
    }

    private fun showLocation(location: Location?) {
        if (location == null) return
        if (location.getProvider().equals(LocationManager.GPS_PROVIDER)) {
            tvLocationGPS?.setText(formatLocation(location))
        } else if (location.getProvider().equals(
                LocationManager.NETWORK_PROVIDER
            )
        ) {
            tvLocationNet?.setText(formatLocation(location))
        }
    }

    private fun formatLocation(location: Location?): String? {
        return if (location == null) "" else java.lang.String.format(
            "Coordinates: lat = %1$.4f, lon = %2$.4f, time = %3\$tF %3\$tT",
            location.getLatitude(), location.getLongitude(), Date(
                location.getTime()
            )
        )
    }

    private fun checkEnabled() {
        tvEnabledGPS!!.text = ("Enabled: "
            + locationManager
            .isProviderEnabled(LocationManager.GPS_PROVIDER))
        tvEnabledNet!!.text = ("Enabled: "
            + locationManager
            .isProviderEnabled(LocationManager.NETWORK_PROVIDER))
    }

    fun onClickLocationSettings(view: View?) {
        startActivity(
            Intent(
                Settings.ACTION_LOCATION_SOURCE_SETTINGS
            )
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        // if request is cancelled, the results array is empty
        if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

            getLocation()
        } else {
            Toast.makeText(
                this@MainActivity,
                "permission denied",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun getLocation() {
        locationManager.requestLocationUpdates(
            LocationManager.GPS_PROVIDER,
            1000L, 0.5F, locationListener
        )
        locationManager.requestLocationUpdates(
            LocationManager.EXTRA_GNSS_CAPABILITIES,
            2000L,
            0.5F,
            locationListener
        )
        locationManager.requestLocationUpdates(
            LocationManager.NETWORK_PROVIDER, 1000L, 0.5F,
            locationListener
        );
    }

    private fun requestPermissions() {
        val requestCode = 2
        val reqPermissions = arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
        val permissionCheckFineLocation =
            ContextCompat.checkSelfPermission(this@MainActivity, reqPermissions[0]) ==
                PackageManager.PERMISSION_GRANTED
        val permissionCheckCoarseLocation =
            ContextCompat.checkSelfPermission(this@MainActivity, reqPermissions[1]) ==
                PackageManager.PERMISSION_GRANTED
        if (!(permissionCheckFineLocation && permissionCheckCoarseLocation)) { // if permissions are not already granted, request permission from the user
            ActivityCompat.requestPermissions(this@MainActivity, reqPermissions, requestCode)
        } else {
            val message = "call getLocation"
            Toast.makeText(this@MainActivity, message, Toast.LENGTH_LONG).show()
            getLocation()
        }
    }

    private fun getWiFi(){
        val cm:ConnectivityManager = getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
        val info = cm.activeNetwork
    }
}