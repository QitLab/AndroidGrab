package com.qit.location

import android.annotation.SuppressLint
import android.content.Context
import android.location.*
import android.os.Handler
import android.os.Looper
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.qit.base.LocationType
import java.io.IOException
import java.util.*

@SuppressLint("MissingPermission")
class LocationWrapper private constructor(private val context: Context,
                                          private val listener: OnLocationSuccessListener) :
    LocationListener, LocationCallback() {
    interface OnLocationSuccessListener {
        fun onSuccess(location: GrabLocation)
    }

    companion object {
        @Volatile
        private var instance: LocationWrapper? = null
        fun getInstance(context: Context, listener: OnLocationSuccessListener) =
            instance ?: synchronized(this) {
                instance ?: LocationWrapper(context, listener).also { instance = it }
            }
    }

    fun requestLocation() {
        if (isRunning) return
        isRunning = true
        requestLocationByGoogleHigh()
    }

    private val locationProviderClient = LocationServices.getFusedLocationProviderClient(context)
    private val mHandler: Handler
    private var isRunning = false
    private var locationManager =
        context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

    private fun requestLocationByGoogle() {
        locationProviderClient.lastLocation.addOnCompleteListener { task ->
            if (!task.isSuccessful || task.result == null) {
                requestLocationByNet()
            } else {
                handleGoogleSuccess(LocationType.Google, task.result!!)
            }
        }
    }


    private fun requestLocationByGoogleHigh() {
        locationProviderClient.requestLocationUpdates(LocationRequest.create().setInterval(5000)
            .setFastestInterval(3000).setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY),
            this,
            Looper.myLooper())
    }

    override fun onLocationResult(locationResult: LocationResult?) {
        mHandler.removeCallbacksAndMessages(null)
        locationProviderClient.removeLocationUpdates(this)
        if (locationResult == null || locationResult.lastLocation == null) {
            requestLocationByGoogle()
        } else {
            handleGoogleSuccess(LocationType.Google, locationResult.lastLocation)
        }
    }

    private fun handleGoogleSuccess(@LocationType type: Int, location: Location) {
        var province = ""
        var city = ""
        var street = ""
        var addressText = ""
        val addresses: List<Address>
        val geocoder = Geocoder(context, Locale.getDefault())
        try {
            addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
            if (addresses != null && !addresses.isEmpty()) {
                val address = addresses[0]
                province = if (address.adminArea == null) "" else address.adminArea
                city = if (address.locality == null) "" else address.locality
                street = if (address.thoroughfare == null) "" else address.thoroughfare
                addressText =
                    if (address.getAddressLine(0) == null) "" else address.getAddressLine(0)
            } else {
            }
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            isRunning = false
            listener.onSuccess(GrabLocation(type,
                location.latitude,
                location.longitude,
                province,
                city,
                street,
                addressText))
        }
    }

    @SuppressLint("MissingPermission")
    private fun requestLocationByNet() {
        locationManager.requestSingleUpdate(LocationManager.NETWORK_PROVIDER,
            this,
            Looper.myLooper())
    }

    override fun onLocationChanged(location: Location) {
        handleGoogleSuccess(LocationType.Network, location)
    }

    @SuppressLint("MissingPermission")
    fun destroy() {
        locationManager.removeUpdates(this)
    }

    init {
        if (Looper.getMainLooper().thread !== Thread.currentThread()) Looper.prepare()
        mHandler = Handler()
    }

    data class GrabLocation(val locationType: Int,
                            val latitude: Double,
                            val longitude: Double,
                            val province: String,
                            val city: String,
                            val street: String,
                            val addressText: String)
}