package com.qit.location

import android.annotation.SuppressLint
import android.content.Context
import android.location.*
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.qit.base.GrabController
import com.qit.base.LocationType
import java.io.IOException
import java.util.*


/**
 * author: Qit .
 * date:   On 2020/7/20
 */

class LocationController(private val context: Context, private val location: String) :
    GrabController() {
    override fun doCall() {
        runWorkThread(Runnable {
            mListener?.onReceive(mType, location)
        })
    }

    fun requestLocation(): LocationWrapper? {
        val locationWrapper = LocationWrapper.getInstance(context)
        locationWrapper.requestLocation()
        return locationWrapper
    }

    @SuppressLint("MissingPermission")
    class LocationWrapper private constructor(private val context: Context) : LocationListener,
        LocationCallback() {
        companion object {
            @Volatile
            private var instance: LocationWrapper? = null
            fun getInstance(context: Context) = instance ?: synchronized(this) {
                instance ?: LocationWrapper(context).also { instance = it }
            }
        }

        fun requestLocation() {
            if (isRunning) return
            isRunning = true
            requestLocationByGoogleHigh()
        }

        private val locationProviderClient =
            LocationServices.getFusedLocationProviderClient(context)
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
                //                RupeePlusDataManager().checkUploadLocation(type,
                //                    location.latitude,
                //                    location.longitude,
                //                    province,
                //                    city,
                //                    street,
                //                    addressText)
                Log.d("Qit",
                    location.latitude.toString() + "---" + location.longitude.toString() + "---" + province + "---" + city + "---" + street + "---" + addressText)
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


    }
}