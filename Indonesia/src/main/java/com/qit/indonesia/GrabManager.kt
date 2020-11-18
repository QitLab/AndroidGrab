package com.qit.indonesia

import android.Manifest
import android.content.Context
import com.qit.applist.AppListController
import com.qit.base.GrabController
import com.qit.base.GrabType.Companion.TYPE_APP_LIST
import com.qit.base.GrabType.Companion.TYPE_DEVICE_FINGER
import com.qit.base.GrabType.Companion.TYPE_UPLOCAD_LOCATION
import com.qit.contact.ContactController
import com.qit.device.DeviceController
import com.qit.exif.ExifController
import com.qit.location.LocationController
import com.qit.sms.SmsController
import pub.devrel.easypermissions.EasyPermissions


class GrabManager(private val context: Context, private val listener: GrabController.GrabListener) {
    private fun <T : GrabController> getData(controller: T, type: Int) {
        controller.registerListener(type, listener)
    }

    fun getAllGrab(appListCondition: () -> Boolean = { true },
                   deviceCondition: () -> Boolean = { true },
                   locationCondition: () -> Boolean = { true }) {
        getAppList(appListCondition)
        getDevice(deviceCondition)
        getLocation(locationCondition)
    }

    fun getAppList(appListCondition: () -> Boolean = { true }) {
        if (appListCondition()) getData(AppListController(context), TYPE_APP_LIST)
    }

    fun getDevice(deviceCondition: () -> Boolean = { true }) {
        if (deviceCondition()) getData(DeviceController(context), TYPE_DEVICE_FINGER)
    }

    fun getLocation(locationCondition: () -> Boolean = { true }) {
        if (EasyPermissions.hasPermissions(context,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION) && locationCondition()) getData(
            LocationController(context),
            TYPE_UPLOCAD_LOCATION)
    }

}