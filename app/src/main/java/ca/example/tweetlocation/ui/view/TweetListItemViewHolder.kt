package ca.example.tweetlocation.ui.view

import android.support.v7.widget.RecyclerView
import android.view.View
import ca.example.tweetlocation.util.VolleyUtils
import com.twitter.sdk.android.core.models.Tweet
import kotlinx.android.synthetic.main.item_tweet_search_result.view.*

class TweetListItemViewHolder(val view: View) : RecyclerView.ViewHolder(view) {

    var tweet: Tweet? = null
        set(value) {
            field = value
            view.photoImageView.setImageUrl(tweet?.user?.profileImageUrl, VolleyUtils.imageLoader)
            view.tweetTextView.text = tweet?.text
            view.userNameTextView.text = tweet?.user?.name
            view.userScreenNameTextView.text = tweet?.user?.screenName
        }
}