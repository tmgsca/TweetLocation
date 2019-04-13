package ca.example.tweetlocation.data

import android.os.Parcel
import android.os.Parcelable

data class TweetVideo(val url: String, val aspectRatio: Int, val duration: Long, val contentType: String) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readInt(),
        parcel.readLong(),
        parcel.readString()
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(url)
        parcel.writeLong(duration)
        parcel.writeString(contentType)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<TweetVideo> {
        override fun createFromParcel(parcel: Parcel): TweetVideo {
            return TweetVideo(parcel)
        }

        override fun newArray(size: Int): Array<TweetVideo?> {
            return arrayOfNulls(size)
        }
    }


}