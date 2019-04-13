package ca.example.tweetlocation.ui.view

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.view.View
import ca.example.tweetlocation.R
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.Marker
import com.twitter.sdk.android.core.models.Tweet
import kotlinx.android.synthetic.main.view_map_tweet_info_window.view.*

class TweetInfoWindow(private val context: Context) : GoogleMap.InfoWindowAdapter {

    @SuppressLint("InflateParams")
    override fun getInfoContents(marker: Marker): View {
        val view = (context as Activity).layoutInflater.inflate(R.layout.view_map_tweet_info_window, null)
        val tweet = marker.tag as Tweet
        view.userNameTextView.text = tweet.user.name
        view.userScreenNameTextView.text = tweet.user.screenName
        view.textTextView.text = tweet.text
        view.createdAtTextView.text = tweet.createdAt
        return view
    }

    override fun getInfoWindow(marker: Marker?): View? {
        return null
    }
}