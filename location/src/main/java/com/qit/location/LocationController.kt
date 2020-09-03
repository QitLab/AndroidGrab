package com.qit.location

import android.content.Context
import com.qit.base.GrabController
import org.json.JSONObject


/**
 * author: Qit .
 * date:   On 2020/7/20
 */

class LocationController(private val context: Context) : GrabController() {
    override fun doCall() {
        runWorkThread(Runnable {
            requestLocation()
        })
    }

    private fun requestLocation(): LocationWrapper? {
        val locationWrapper = LocationWrapper.getInstance(context,
            object : LocationWrapper.OnLocationSuccessListener {
                override fun onSuccess(location: LocationWrapper.GrabLocation) {
                    val item = JSONObject()
                    item.put("location_type", location.locationType)
                    item.put("latitude", location.latitude)
                    item.put("longitude", location.longitude)
                    item.put("province", location.province)
                    item.put("city", location.city)
                    item.put("street", location.street)
                    item.put("address", location.addressText)
                    mListener?.onReceive(mType, item.toString())
                }
            })
        locationWrapper.requestLocation()
        return locationWrapper
    }
}