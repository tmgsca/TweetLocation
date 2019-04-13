package ca.example.tweetlocation.ui.adapter

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import ca.example.tweetlocation.R
import ca.example.tweetlocation.ui.view.TweetListItemViewHolder
import com.twitter.sdk.android.core.models.Tweet

class TweetSearchAdapter(val onClick: (Tweet) -> Unit) : RecyclerView.Adapter<TweetListItemViewHolder>() {

    var items: List<Tweet> = emptyList()

    fun loadItems(newItems: List<Tweet>) {
        items = newItems
    }

    override fun getItemCount(): Int = items.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TweetListItemViewHolder =
        TweetListItemViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.item_tweet_search_result, parent, false)
        )


    override fun onBindViewHolder(holder: TweetListItemViewHolder, position: Int) {
        holder.tweet = items[position]
        holder.view.setOnClickListener { onClick(items[position]) }
    }
}