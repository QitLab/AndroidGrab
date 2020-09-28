package com.qit.applist

import android.content.Context
import android.content.pm.ApplicationInfo
import android.text.TextUtils
import com.qit.base.GrabController
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

/**
 * author: Qit .
 * date:   On 2020-09-02
 * 获取App列表
 */
class AppListController(private val context: Context) : GrabController() {

    override fun doCall() {
        runWorkThread(Runnable {
            val allAppNames = getAllAppNames()
            if (!TextUtils.isEmpty(allAppNames)) run {
                try {
                    val jsonObject = JSONObject()
                    jsonObject.put("installed_apps", allAppNames)
                    jsonObject.put("installed_apps_version", "1")
                    mListener?.onReceive(mType, jsonObject.toString())
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

        })
    }

    /**
     * 获取手机上安装的app
     */
    private fun getAllAppNames(): String {
        val jsonArray = JSONArray()
        val packages = context.packageManager.getInstalledPackages(0)

        for (packageInfo in packages) {
            // Only display the non-system app info
            if (packageInfo.applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM == 0) {
                try {
                    val jsonObject = JSONObject()
                    jsonObject.put("app_name",
                        packageInfo.applicationInfo.loadLabel(context.packageManager).toString())
                    jsonObject.put("package_name", packageInfo.packageName)
                    jsonObject.put("version_code", packageInfo.versionCode)
                    jsonObject.put("version_name", packageInfo.versionName)
                    jsonObject.put("first_install_time", packageInfo.firstInstallTime)
                    jsonObject.put("last_update_time", packageInfo.lastUpdateTime)
                    jsonArray.put(jsonObject)
                } catch (e: JSONException) {
                    e.printStackTrace()
                }

            }
        }
        return jsonArray.toString()
    }

}