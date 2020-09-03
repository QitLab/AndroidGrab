package com.qit.device

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.BatteryManager
import android.os.Handler
import com.qit.base.GrabController
import org.json.JSONArray
import org.json.JSONObject
import java.util.*

class DeviceController(private val context: Context) : GrabController(), SensorEventListener {

    private var sensorManager: SensorManager? = null
    private var lightNotifyCount = 0
    private var gyroScopeNotifyCount = 0
    private val sensorValues = JSONArray()


    private var mBatteryBroadcastReceiver: BatteryBroadcastReceiver? = null
    private val params = HashMap<String, Any>()

    init {
        sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    }

    override fun doCall() {
        runWorkThread(Runnable {
            registerBatteryReceiver()
        })
    }

    private fun registerBatteryReceiver() {
        if (mBatteryBroadcastReceiver == null) {
            mBatteryBroadcastReceiver = BatteryBroadcastReceiver()
            val filter = IntentFilter()
            filter.addAction(Intent.ACTION_BATTERY_CHANGED)
            context.registerReceiver(mBatteryBroadcastReceiver, filter)
        }
    }

    /**
     * 电池电量监听，上传指纹信息时使用
     */
    internal inner class BatteryBroadcastReceiver : BroadcastReceiver() {

        override fun onReceive(context: Context, intent: Intent) {
            val level = intent.getIntExtra("level", 0)
            val batteryStatus = intent.getIntExtra("status", BatteryManager.BATTERY_STATUS_UNKNOWN)
            val chargeStatus = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, BatteryManager.BATTERY_PLUGGED_USB)
            initDeviceInfoList(batteryStatus, level, chargeStatus)
            unRegisterBatteryBroadcastReceiver()
        }

    }

    private fun unRegisterBatteryBroadcastReceiver() {
        if (mBatteryBroadcastReceiver != null) {
            context.unregisterReceiver(mBatteryBroadcastReceiver)
            mBatteryBroadcastReceiver = null
        }
    }

    /**
     * 收集设备信息
     *
     * @param batteryStatus 电池状态
     * public static final int BATTERY_STATUS_CHARGING = 2;
     * public static final int BATTERY_STATUS_DISCHARGING = 3;
     * public static final int BATTERY_STATUS_FULL = 5;
     * public static final int BATTERY_STATUS_NOT_CHARGING = 4;
     * public static final int BATTERY_STATUS_UNKNOWN = 1;
     * @param level         电量
     * @param chargeStatus
    public static final int BATTERY_PLUGGED_AC = OsProtoEnums.BATTERY_PLUGGED_AC; // = 1
    public static final int BATTERY_PLUGGED_USB = OsProtoEnums.BATTERY_PLUGGED_USB; // = 2
    public static final int BATTERY_PLUGGED_WIRELESS = OsProtoEnums.BATTERY_PLUGGED_WIRELESS; // = 4
     */
    @Synchronized
    fun initDeviceInfoList(batteryStatus: Int, level: Int, chargeStatus: Int) {
        try {
            initStaticDeviceInfo(params)
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            params["batteryPercentage"] = (level.toDouble() / 100).toString()
            params["batteryStatus"] = batteryStatus.toString()
            params["chargeType"] = chargeStatus.toString()
            registerSensor()
        }
    }

    /**
     * 初始化静态设备变量信息
     *
     * @param params
     */
    private fun initStaticDeviceInfo(params: MutableMap<String, Any>) {
        val heightPixels = DeviceUtils.getDisplayMetrics(context).heightPixels
        val widthPixels = DeviceUtils.getDisplayMetrics(context).widthPixels
        params["screenResolution"] = "$widthPixels * $heightPixels"
        params["totalDiskSize"] = DeviceUtils.getSDAllSize()
        params["availableDiskSize"] = DeviceUtils.getSDFreeSize()
//        params["macAddress"] = DeviceUtils.getMACAdresse(context)
        params["carrierName"] = DeviceUtils.getNetWorkOperatorName(context)
        params["networkOperator"] = DeviceUtils.getNetWorkOperator(context)
        params["networkType"] = DeviceUtils.getNetworkType(context)
        params["isRooted"] = DeviceUtils.isRoot()
        params["screenBrightness"] = DeviceUtils.getScreenBrightness(context)
        params["elapsedRealtime"] = DeviceUtils.getElapsedRealtime()
        params["uptimeMillis"] = DeviceUtils.getUpdateMills()
//        params["cupInfo"] = DeviceUtils.getCpuInfo()
        params["modelName"] = DeviceUtils.getModelName()
        params["handSetMakers"] = DeviceUtils.getProductName()
        params["manufacturerName"] = DeviceUtils.getManufacturerName()
        params["memorySize"] = DeviceUtils.getTotalMem().toString()
        params["availableMemory"] = DeviceUtils.getFreeMem(context)
        params["systemVersion"] = DeviceUtils.getSysVersion()
//        params["carrierIpAddress"] = DeviceUtils.getIpAddress(context)
//        params["wifiName"] = DeviceUtils.getWifiName(context)
        params["isUsingProxyPort"] = DeviceUtils.isUsingProxyPort()
        params["isUsingVPN"] = DeviceUtils.isUsingVPN()
        params["brand"] = DeviceUtils.getBrand()
        params["board"] = DeviceUtils.getBoard()
        params["serial"] = DeviceUtils.getSerial()
        params["display"] = DeviceUtils.getDiaplay()
        params["id"] = DeviceUtils.getId()
//        params["wifiList"] = DeviceUtils.getWifiList(context)
        params["gatewayIP"] = DeviceUtils.getGateWayIp(context)
        params["language"] = DeviceUtils.getDefaultLanguage()
        params["displayLanguage"] = DeviceUtils.getDefaultDisplayLanguage()
        params["country"] = DeviceUtils.getDefaultCountry()
        params["displayCountry"] = DeviceUtils.getDefaultDisplayCountry()
        params["phoneType"] = DeviceUtils.getPhoneType(context)
        params["isUSBDebug"] = DeviceUtils.isOpenUSBDebug(context)
        params["currentSystemTime"] = (System.currentTimeMillis() / 1000)
        params["audio_internal"] = SDCardUtils.getAudioInternalCount(context)
        params["video_internal"] = SDCardUtils.getVideoInternalCount(context)
        params["images_internal"] = SDCardUtils.getImagesInternalCount(context)
        params["audio_internal"] = SDCardUtils.getAudioInternalCount(context)
        params["sensorList"] = DeviceUtils.getSensorList(context)
        params["android_id"] = ""
//        params["imei"] = DeviceUtils.getIMEI(context)
//        params["imsi"] = DeviceUtils.getIMSI(context)
//        params["phoneNumber"] = DeviceUtils.getPhoneNumber(context)
//        params["simSerialNumber"] = DeviceUtils.getSimSerialNumber(context)
//        params["audio_external"] = SDCardUtils.getAudioExternalCount(context)
//        params["images_external"] = SDCardUtils.getImagesExternalCount(context)
//        params["video_external"] = SDCardUtils.getVideoExternalCount(context)
    }

    private fun registerSensor() {
        val sensorLight = sensorManager!!.getDefaultSensor(Sensor.TYPE_LIGHT)
        sensorManager!!.registerListener(this, sensorLight, SensorManager.SENSOR_DELAY_NORMAL)
        val sensorGryoscope = sensorManager!!.getDefaultSensor(Sensor.TYPE_GYROSCOPE)
        sensorManager!!.registerListener(this, sensorGryoscope, SensorManager.SENSOR_DELAY_NORMAL)
        Handler().postDelayed({
            params["sensorsValue"] = sensorValues.toString()
            unRegisterSensor()
            mListener?.onReceive(mType,JSONObject(params as Map<*, *>).toString())
        }, 5000)
    }

    private fun unRegisterSensor() {
        if (sensorManager != null) {
            sensorManager!!.unregisterListener(this)
            lightNotifyCount = 0
            gyroScopeNotifyCount = 0
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
    }

    override fun onSensorChanged(sensorEvent: SensorEvent?) {
        try {
            val jsonObject = JSONObject()
            jsonObject.put("x", sensorEvent!!.values[0].toDouble())
            jsonObject.put("y", sensorEvent.values[1].toDouble())
            jsonObject.put("z", sensorEvent.values[2].toDouble())
            if (sensorEvent.sensor.getType() == Sensor.TYPE_LIGHT && lightNotifyCount == 0) {
                lightNotifyCount++
                val lightJsonObject = JSONObject()
                lightJsonObject.put("light", jsonObject.toString())
                sensorValues.put(lightJsonObject)
            } else if (sensorEvent.sensor.getType() == Sensor.TYPE_GYROSCOPE && gyroScopeNotifyCount == 0) {
                gyroScopeNotifyCount++
                val gyroscopeJsonObject = JSONObject()
                gyroscopeJsonObject.put("gyroscope", jsonObject.toString())
                sensorValues.put(gyroscopeJsonObject)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

}