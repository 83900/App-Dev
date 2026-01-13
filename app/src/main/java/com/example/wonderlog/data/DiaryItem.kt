package com.example.wonderlog.data

import android.os.Parcel
import android.os.Parcelable

/**
 * 日记项数据模型
 */
data class DiaryItem(
    val id: String,
    val travelId: String,
    val title: String,
    val content: String,
    val location: String,
    val date: String,
    val images: List<String> = emptyList()
) : Parcelable {
    
    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.createStringArrayList() ?: emptyList()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(travelId)
        parcel.writeString(title)
        parcel.writeString(content)
        parcel.writeString(location)
        parcel.writeString(date)
        parcel.writeStringList(images)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<DiaryItem> {
        override fun createFromParcel(parcel: Parcel): DiaryItem {
            return DiaryItem(parcel)
        }

        override fun newArray(size: Int): Array<DiaryItem?> {
            return arrayOfNulls(size)
        }
    }
}
