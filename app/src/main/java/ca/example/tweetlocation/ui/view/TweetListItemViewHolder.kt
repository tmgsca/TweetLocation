package ca.example.tweetlocation.ui.view

import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import ca.example.tweetlocation.data.TweetMedium
import ca.example.tweetlocation.data.TweetVideo
import ca.example.tweetlocation.ui.adapter.TweetMediaAdapter
import ca.example.tweetlocation.util.VolleyUtils
import com.twitter.sdk.android.core.models.Tweet
import kotlinx.android.synthetic.main.item_tweet_search_result.view.*

class TweetListItemViewHolder(val view: View) : RecyclerView.ViewHolder(view) {

    fun bind(tweet: Tweet, onClick: (TweetMedium) -> Unit) {
        this.tweet = tweet
        val adapter = TweetMediaAdapter(onClick = { onClick(it) })

        adapter.loadItems(tweet.extendedEntities?.media?.map {
            TweetMedium(
                it.type,
                it.mediaUrlHttps,
                if (it.videoInfo != null) TweetVideo(
                    it.videoInfo.variants.last().url,
                    it.videoInfo.aspectRatio.first() / it.videoInfo.aspectRatio.last()
                ) else null
            )
        }
            ?: emptyList())
        view.mediaRecyclerView.layoutManager = GridLayoutManager(view.context, 3).apply {
            orientation = LinearLayoutManager.VERTICAL
        }
        view.mediaRecyclerView.adapter = adapter
    }

    private var tweet: Tweet? = null
        set(value) {
            field = value
            view.photoImageView.setImageUrl(tweet?.user?.profileImageUrl, VolleyUtils.imageLoader)
            view.tweetTextView.text = tweet?.text
            view.userNameTextView.text = tweet?.user?.name
            view.userScreenNameTextView.text = "@${tweet?.user?.screenName}"
            view.dateTextView.text = tweet?.createdAt
        }
}