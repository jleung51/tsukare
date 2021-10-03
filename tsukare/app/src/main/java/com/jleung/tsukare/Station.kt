package com.jleung.tsukare

import android.os.Parcel
import android.os.Parcelable

data class Station(val name: String, val lat: Double, val lon: Double): Parcelable {

    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readDouble(),
        parcel.readDouble()
    )

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel?, p1: Int) {
        dest?.writeString(name)
        dest?.writeDouble(lat)
        dest?.writeDouble(lon)
    }

    companion object CREATOR : Parcelable.Creator<Station> {
        override fun createFromParcel(parcel: Parcel): Station {
            return Station(parcel)
        }

        override fun newArray(size: Int): Array<Station?> {
            return arrayOfNulls(size)
        }
    }
}