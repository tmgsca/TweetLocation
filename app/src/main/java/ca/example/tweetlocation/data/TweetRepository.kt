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

    fun unfavoriteTweet(id: Long, responseCallback: (Tweet) -> Unit) {
        TwitterCore.getInstance().getApiClient(SessionUtils.twitterSession).favoriteService.destroy(id, false)
            .enqueue(object : Callback<Tweet> {
                override fun onFailure(call: Call<Tweet>?, t: Throwable?) {
                    //TODO: Handle error
                }

                override fun onResponse(call: Call<Tweet>?, response: Response<Tweet>) {
                    if (response.isSuccessful) {
                        responseCallback(response.body())
                    }
                }
            })
    }

    fun getTweet(id: Long, responseCallback: (Tweet) -> Unit) {
        TwitterCore.getInstance().getApiClient(SessionUtils.twitterSession).statusesService.show(id, null, null, true)
            .enqueue(object : Callback<Tweet> {
                override fun onFailure(call: Call<Tweet>?, t: Throwable?) {
                    //TODO: Handle error
                }

                override fun onResponse(call: Call<Tweet>?, response: Response<Tweet>) {
                    if (response.isSuccessful) {
                        responseCallback(response.body())
                    }
                }
            })
    }

    fun favoriteTweet(id: Long, responseCallback: (Tweet) -> Unit) {
        TwitterCore.getInstance().getApiClient(SessionUtils.twitterSession).favoriteService.create(id, false)
            .enqueue(object : Callback<Tweet> {
                override fun onFailure(call: Call<Tweet>?, t: Throwable?) {
                    //TODO: Handle error
                }

                override fun onResponse(call: Call<Tweet>?, response: Response<Tweet>) {
                    if (response.isSuccessful) {
                        responseCallback(response.body())
                    }
                }
            })
    }

    fun retweet(id: Long, responseCallback: (Tweet) -> Unit) {
        TwitterCore.getInstance().getApiClient(SessionUtils.twitterSession).statusesService.retweet(id, true)
            .enqueue(object : Callback<Tweet> {
                override fun onFailure(call: Call<Tweet>?, t: Throwable?) {
                    //TODO: Handle error
                }

                override fun onResponse(call: Call<Tweet>?, response: Response<Tweet>) {
                    if (response.isSuccessful) {
                        responseCallback(response.body())
                    }
                }
            })
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

            override fun onResponse(call: Call<Search>?, response: Response<Search>) {
                if (response.isSuccessful) {
                    responseCallback(response.body().tweets)
                }
            }
        })
    }

    fun getTweets(query: String, responseCallback: (List<Tweet>) -> Unit) {
        getTweets(query, null, responseCallback)
    }

}