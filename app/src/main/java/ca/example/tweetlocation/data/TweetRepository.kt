package ca.example.tweetlocation.data

import com.twitter.sdk.android.core.TwitterCore
import com.twitter.sdk.android.core.TwitterSession
import com.twitter.sdk.android.core.models.Search
import com.twitter.sdk.android.core.models.Tweet
import com.twitter.sdk.android.core.services.params.Geocode
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class TweetRepository(private val session: TwitterSession) {

    fun addTweetToFavorites(tweet: Tweet) {
        //TODO: Implement
    }


    fun retweet(tweet: Tweet) {
        //TODO: Implement
    }

    fun getTweets(query: String, geocode: Geocode?, responseCallback: (List<Tweet>) -> Unit) {
        TwitterCore.getInstance().getApiClient(SessionUtils.twitterSession).searchService.tweets(
            query,
            geocode,
            null,
            null,
            "recent",
            100,
            null,
            null,
            null,
            null
        ).enqueue(object : Callback<Search> {
            override fun onFailure(call: Call<Search>?, t: Throwable?) {
                //TODO: Handle error
            }

            override fun onResponse(call: Call<Search>?, response: Response<Search>?) {
                val tweets = response?.body()?.tweets?.let { it } ?: emptyList<Tweet>()
                responseCallback(tweets)
            }
        })
    }

    fun getTweets(query: String, responseCallback: (List<Tweet>) -> Unit) {
        getTweets(query, null, responseCallback)
    }

}