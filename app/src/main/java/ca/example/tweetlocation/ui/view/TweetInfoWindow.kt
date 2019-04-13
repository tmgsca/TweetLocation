package ca.example.tweetlocation.ui.view

import android.app.Activity
import android.content.Context
import android.view.View
import ca.example.tweetlocation.R
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.Marker
import kotlinx.android.synthetic.main.view_map_tweet_info_window.view.*

class TweetInfoWindow(private val context: Context) : GoogleMap.InfoWindowAdapter {

    override fun getInfoContents(marker: Marker?): View {
        val view = (context as Activity).layoutInflater.inflate(R.layout.view_map_tweet_info_window, null)
        view.userScreenNameTextView.text = marker?.title
        view.content.text = marker?.snippet
        return view
    }

    override fun getInfoWindow(marker: Marker?): View? {
        return null
    }
}