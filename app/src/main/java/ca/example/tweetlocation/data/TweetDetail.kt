package ca.example.tweetlocation.data

import android.os.Parcel
import android.os.Parcelable

data class TweetDetail(
    val id: Long,
    val userFullName: String,
    val text: String,
    val userScreenName: String,
    val favoriteCount: Int,
    val retweetCount: Int,
    val userPhotoUrl: String,
    val latitude: Double?,
    val longitude: Double?,
    val favorited: Boolean,
    val retweeted: Boolean,
    val createdAt: String,
    val media: List<TweetMedium>

) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readLong(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readInt(),
        parcel.readInt(),
        parcel.readString(),
        parcel.readValue(Double::class.java.classLoader) as? Double,
        parcel.readValue(Double::class.java.classLoader) as? Double,
        parcel.readByte() != 0.toByte(),
        parcel.readByte() != 0.toByte(),
        parcel.readString(),
        parcel.createTypedArrayList(TweetMedium)
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeLong(id)
        parcel.writeString(userFullName)
        parcel.writeString(text)
        parcel.writeString(userScreenName)
        parcel.writeInt(favoriteCount)
        parcel.writeInt(retweetCount)
        parcel.writeString(userPhotoUrl)
        parcel.writeValue(latitude)
        parcel.writeValue(longitude)
        parcel.writeByte(if (favorited) 1 else 0)
        parcel.writeByte(if (retweeted) 1 else 0)
        parcel.writeString(createdAt)
        parcel.writeTypedList(media)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<TweetDetail> {
        override fun createFromParcel(parcel: Parcel): TweetDetail {
            return TweetDetail(parcel)
        }

        override fun newArray(size: Int): Array<TweetDetail?> {
            return arrayOfNulls(size)
        }
    }

}