package com.example.firstaidfront.models

import android.os.Parcel
import android.os.Parcelable

data class CourseItem(
    val id: Int,
    val name: String,
    val description: String,
    val urlImage: String? = null
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(id)
        parcel.writeString(name)
        parcel.writeString(description)
        parcel.writeString(urlImage)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<CourseItem> {
        override fun createFromParcel(parcel: Parcel): CourseItem {
            return CourseItem(parcel)
        }

        override fun newArray(size: Int): Array<CourseItem?> {
            return arrayOfNulls(size)
        }
    }
}