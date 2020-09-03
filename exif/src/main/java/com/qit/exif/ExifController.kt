package com.qit.exif

import android.content.Context
import android.provider.MediaStore
import androidx.exifinterface.media.ExifInterface
import com.qit.base.GrabController
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.pow


/**
 * author: Qit .
 * date:   On 2020/7/20
 */

class ExifController(private val context: Context) : GrabController() {

    override fun doCall() {
        runWorkThread(Runnable {
            mListener?.onReceive(mType,getExifList())
        })
    }


    private fun getExifList(): String {
        val cursor = context.contentResolver.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, null,
                null, null, null)
        val jsonArray = JSONArray()
        while (cursor?.moveToNext()!!) {
            val name = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DISPLAY_NAME))
            val index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
            val dateIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_TAKEN)
            val saveTime = cursor.getString(dateIndex)
            val path = cursor.getString(index)
            jsonArray.put(getExif(name, saveTime, path))
        }
        cursor.close()
        return jsonArray.toString()
    }

    private fun getExif(photoName: String, saveTime: String, path: String): JSONObject {
        val jsonObject = JSONObject()
        val exifInterface = ExifInterface(path)
        val author = exifInterface.getAttribute(ExifInterface.TAG_ARTIST)
        val takeTime = exifInterface.getAttribute(ExifInterface.TAG_DATETIME)?.run {
            SimpleDateFormat("YYYY:MM:DD HH:MM:SS", Locale.getDefault()).parse(this).time.toString()
        } ?: ""

        val width = exifInterface.getAttribute(ExifInterface.TAG_IMAGE_WIDTH)
        val height = exifInterface.getAttribute(ExifInterface.TAG_IMAGE_LENGTH)
        val longitude = exifInterface.getAttribute(ExifInterface.TAG_GPS_LONGITUDE)
        val latitude = exifInterface.getAttribute(ExifInterface.TAG_GPS_LATITUDE)
        val model = exifInterface.getAttribute(ExifInterface.TAG_MODEL)
        jsonObject.put("name", photoName)
        jsonObject.put("author", author)
        jsonObject.put("length", height)
        jsonObject.put("width", width)
        jsonObject.put("longitude", score2dimensionality(longitude))
        jsonObject.put("latitude", score2dimensionality(latitude))
        jsonObject.put("take_time", takeTime)
        jsonObject.put("save_time", saveTime)
        jsonObject.put("model", model)
        return jsonObject
    }

    private fun score2dimensionality(string: String?): Double {
        var dimensionality = 0.0
        if (null == string) {
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