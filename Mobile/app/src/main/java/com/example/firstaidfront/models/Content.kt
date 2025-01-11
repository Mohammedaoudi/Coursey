package com.example.firstaidfront.models

import android.os.Parcel
import android.os.Parcelable

data class Content(
    val id: Int,
    val title: String,
    val description: String,
    val url: String? = null,
    val orderIndex: Int
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readInt()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(id)
        parcel.writeString(title)
        parcel.writeString(description)
        parcel.writeString(url)
        parcel.writeInt(orderIndex)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<Content> {
        override fun createFromParcel(parcel: Parcel): Content = Content(parcel)
        override fun newArray(size: Int): Array<Content?> = arrayOfNulls(size)
    }
}
