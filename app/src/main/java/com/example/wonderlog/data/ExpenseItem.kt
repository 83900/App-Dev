package com.example.wonderlog.data

import android.os.Parcel
import android.os.Parcelable

/**
 * 费用项数据模型
 */
data class ExpenseItem(
    val id: String,
    val travelId: String,
    val title: String,
    val amount: String,
    val category: String,
    val date: String,
    val iconResId: Int
) : Parcelable {
    
    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readInt()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(travelId)
        parcel.writeString(title)
        parcel.writeString(amount)
        parcel.writeString(category)
        parcel.writeString(date)
        parcel.writeInt(iconResId)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<ExpenseItem> {
        override fun createFromParcel(parcel: Parcel): ExpenseItem {
            return ExpenseItem(parcel)
        }

        override fun newArray(size: Int): Array<ExpenseItem?> {
            return arrayOfNulls(size)
        }
    }
}
