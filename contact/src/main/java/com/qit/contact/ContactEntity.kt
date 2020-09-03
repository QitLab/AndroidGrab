package com.qit.contact

import android.os.Parcel
import android.os.Parcelable

class ContactEntity() : Parcelable {

    var id: Long = 0
    var name: String? = null
    var number: String? = null
    var isChecked: Boolean = false
    var type: String? = null
    var lastUpdateTime: Long = 0L
    var lastContactTime: Long = 0L
    var lastUsedTime: Long = 0L
    var contactTimes: String? = null
    var groups: ArrayList<String>? = null

    constructor(parcel: Parcel) : this() {
        id = parcel.readLong()
        name = parcel.readString()
        number = parcel.readString()
        isChecked = parcel.readByte() != 0.toByte()
        type = parcel.readString()
        lastUpdateTime = parcel.readLong()
        lastContactTime = parcel.readLong()
        lastUsedTime = parcel.readLong()
        contactTimes = parcel.readString()
        groups = parcel.createStringArrayList()
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeLong(id)
        parcel.writeString(name)
        parcel.writeString(number)
        parcel.writeByte(if (isChecked) 1 else 0)
        parcel.writeString(type)
        parcel.writeLong(lastUpdateTime)
        parcel.writeLong(lastContactTime)
        parcel.writeLong(lastUsedTime)
        parcel.writeString(contactTimes)
        parcel.writeStringList(groups)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<ContactEntity> {
        override fun createFromParcel(parcel: Parcel): ContactEntity {
            return ContactEntity(parcel)
        }

        override fun newArray(size: Int): Array<ContactEntity?> {
            return arrayOfNulls(size)
        }
    }


}