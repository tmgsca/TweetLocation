package ca.example.tweetlocation.data

import android.os.Parcel
import android.os.Parcelable

data class TweetMedium(val type: String, val url: String) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString()
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(type)
        parcel.writeString(url)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<TweetMedium> {
        override fun createFromParcel(parcel: Parcel): TweetMedium {
            return TweetMedium(parcel)
        }

        override fun newArray(size: Int): Array<TweetMedium?> {
            return arrayOfNulls(size)
        }
    }

}