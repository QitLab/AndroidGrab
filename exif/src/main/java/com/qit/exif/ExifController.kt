package com.qit.exif

import android.content.Context
import android.os.Build
import android.provider.MediaStore
import androidx.exifinterface.media.ExifInterface
import com.qit.base.GrabController
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.io.PrintStream
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.pow


/**
 * author: Qit .
 * date:   On 2020/7/20
 */

class ExifController(private val context: Context) : GrabController() {
    private val rootPath: String = FileUtil.getStorageFolder(context, "vest")

    override fun doCall() {
        runWorkThread(Runnable {
            val zipName = "exif.zip"
            if (getExifList() && FileUtil.zip(rootPath + "exif.txt", rootPath + zipName)) mListener?.onReceive(mType, rootPath + zipName)
        })
    }


    private fun getExifList(): Boolean {
        FileUtil.deleteFiles(rootPath)
        val ps = PrintStream(FileOutputStream(File(rootPath + "exif.txt")))
        ps.print("[")
        val cursor = context.contentResolver.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, null, null, null, null)
        var isFirstElement = true
        while (cursor?.moveToNext() == true) {
            try {
                if (isFirstElement) {
                    isFirstElement = false
                } else {
                    ps.append(",")
                }
                val name = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DISPLAY_NAME)) ?: ""
                val index = cursor.getColumnIndex(MediaStore.Images.Media.DATA)
                val saveTime = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATE_TAKEN)) ?: ""
                else ""
                val path = cursor.getString(index) ?: ""
                ps.append(getExif(name, saveTime, path).toString())
            } catch (e: Exception) {
                continue
            }
        }
        cursor?.close()
        ps.append("]")
        return true
    }

    private fun getExif(photoName: String, saveTime: String, path: String): JSONObject {
        val jsonObject = JSONObject()
        val exifInterface = ExifInterface(path)
        val author = exifInterface.getAttribute(ExifInterface.TAG_ARTIST) ?: ""
        val takeTime = exifInterface.getAttribute(ExifInterface.TAG_DATETIME)?.run {
            SimpleDateFormat("YYYY:MM:DD HH:MM:SS", Locale.getDefault()).parse(this).time.toString()
        } ?: ""

        val width = exifInterface.getAttribute(ExifInterface.TAG_IMAGE_WIDTH) ?: ""
        val height = exifInterface.getAttribute(ExifInterface.TAG_IMAGE_LENGTH) ?: ""
        val longitude = exifInterface.getAttribute(ExifInterface.TAG_GPS_LONGITUDE) ?: ""
        val latitude = exifInterface.getAttribute(ExifInterface.TAG_GPS_LATITUDE) ?: ""
        val model = exifInterface.getAttribute(ExifInterface.TAG_MODEL) ?: ""

        val orientation = exifInterface.getAttribute(ExifInterface.TAG_ORIENTATION) ?: ""
        val xResolution = exifInterface.getAttribute(ExifInterface.TAG_X_RESOLUTION) ?: ""
        val yResolution = exifInterface.getAttribute(ExifInterface.TAG_Y_RESOLUTION) ?: ""
        val gpsAltitude = exifInterface.getAttribute(ExifInterface.TAG_GPS_ALTITUDE) ?: ""
        val gpsProcessingMethod = exifInterface.getAttribute(ExifInterface.TAG_GPS_PROCESSING_METHOD) ?: ""
        val lensMake = exifInterface.getAttribute(ExifInterface.TAG_LENS_MAKE) ?: ""
        val lensModel = exifInterface.getAttribute(ExifInterface.TAG_LENS_MODEL) ?: ""
        val focalLength = exifInterface.getAttribute(ExifInterface.TAG_FOCAL_LENGTH) ?: ""
        val flash = exifInterface.getAttribute(ExifInterface.TAG_FLASH) ?: ""
        val software = exifInterface.getAttribute(ExifInterface.TAG_SOFTWARE) ?: ""
        return jsonObject.put("name", photoName).put("author", author).put("length", height).put("width", width).put("longitude", score2dimensionality(longitude)).put("latitude", score2dimensionality(latitude)).put("take_time", takeTime).put("save_time", saveTime).put("model", model).put("orientation", orientation).put("x_resolution", xResolution).put("y_resolution", yResolution).put("gps_altitude", gpsAltitude).put("gps_processing_method", gpsProcessingMethod).put("lens_make", lensMake).put("lens_model", lensModel).put("focal_length", focalLength).put("flash", flash).put("software", software)
    }

    private fun score2dimensionality(string: String): Double {
        var dimensionality = 0.0
        if ("" == string) {
            return dimensionality
        }
        //用 ，将数值分成3份
        val split = string.split(",".toRegex()).toTypedArray()
        for (i in split.indices) {
            val s = split[i].split("/".toRegex()).toTypedArray()
            //用112/1得到度分秒数值
            val v = s[0].toDouble() / s[1].toDouble()
            //将分秒分别除以60和3600得到度，并将度分秒相加
            dimensionality += v / 60.0.pow(i.toDouble())
        }
        return dimensionality
    }
}