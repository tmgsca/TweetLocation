package ca.example.tweetlocation.ui.view

import android.support.v7.widget.RecyclerView
import android.view.View
import ca.example.tweetlocation.data.TweetMedium
import ca.example.tweetlocation.util.VolleyUtils
import kotlinx.android.synthetic.main.item_tweet_medium.view.*

class TweetMediaListItemViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
    var medium: TweetMedium? = null
        set(value) {
            field = value
            view.mediumThumbnailImageView.setImageUrl(medium?.url, VolleyUtils.imageLoader)
            view.playOverlay.visibility = if (medium?.type == "video") View.VISIBLE else View.GONE
        }
}