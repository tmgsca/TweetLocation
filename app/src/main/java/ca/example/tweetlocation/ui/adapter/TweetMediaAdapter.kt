package ca.example.tweetlocation.ui.adapter

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import ca.example.tweetlocation.R
import ca.example.tweetlocation.data.TweetMedium
import ca.example.tweetlocation.ui.view.TweetMediaListItemViewHolder

class TweetMediaAdapter(val onClick: (TweetMedium) -> Unit) : RecyclerView.Adapter<TweetMediaListItemViewHolder>() {

    var items: List<TweetMedium> = emptyList()

    fun loadItems(newItems: List<TweetMedium>) {
        items = newItems
    }

    override fun getItemCount(): Int = items.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TweetMediaListItemViewHolder =
        TweetMediaListItemViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.item_tweet_medium, parent, false)
        )


    override fun onBindViewHolder(holder: TweetMediaListItemViewHolder, position: Int) {
        holder.medium = items[position]
        holder.view.setOnClickListener { onClick(items[position]) }
    }
}