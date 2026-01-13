package com.example.wonderlog.data

import android.os.Parcel
import android.os.Parcelable

/**
 * 旅行项数据模型
 */
data class TravelItem(
    val id: String,
    val userId: String,
    val title: String,
    val locations: List<String>,
    val date: String,
    val budget: String = ""
) : Parcelable {
    
    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.createStringArrayList() ?: emptyList(),
        parcel.readString() ?: "",
        parcel.readString() ?: ""
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(userId)
        parcel.writeString(title)
        parcel.writeStringList(locations)
        parcel.writeString(date)
        parcel.writeString(budget)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<TravelItem> {
        override fun createFromParcel(parcel: Parcel): TravelItem {
            return TravelItem(parcel)
        }

        override fun newArray(size: Int): Array<TravelItem?> {
            return arrayOfNulls(size)
        }
    }
}
