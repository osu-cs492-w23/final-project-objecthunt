package com.example.data

import android.os.Parcel
import android.os.Parcelable
import java.io.Serializable

data class ItemToFind(
    val name: String,
    val latitude: Long,
    val longtitude: Long
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readLong(),
        parcel.readLong()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(name)
        parcel.writeLong(latitude)
        parcel.writeLong(longtitude)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<ItemToFind> {
        override fun createFromParcel(parcel: Parcel): ItemToFind {
            return ItemToFind(parcel)
        }

        override fun newArray(size: Int): Array<ItemToFind?> {
            return arrayOfNulls(size)
        }
    }
}