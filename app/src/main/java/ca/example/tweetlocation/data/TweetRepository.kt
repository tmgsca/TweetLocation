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

    fun unfavoriteTweet(id: Long, onSuccess: (Tweet) -> Unit, onFailure: () -> Unit) {
        TwitterCore.getInstance().getApiClient(SessionUtils.twitterSession).favoriteService.destroy(id, false)
            .enqueue(object : Callback<Tweet> {
                override fun onFailure(call: Call<Tweet>?, t: Throwable?) {
                    onFailure()
                }

                override fun onResponse(call: Call<Tweet>?, response: Response<Tweet>) {
                    if (response.isSuccessful) {
                        onSuccess(response.body())
                    } else {
                        onFailure()
                    }
                }
            })
    }

    fun getTweet(id: Long, onSuccess: (Tweet) -> Unit, onFailure: () -> Unit) {
        TwitterCore.getInstance().getApiClient(SessionUtils.twitterSession).statusesService.show(id, null, null, true)
            .enqueue(object : Callback<Tweet> {
                override fun onFailure(call: Call<Tweet>?, t: Throwable?) {
                    onFailure()
                }

                override fun onResponse(call: Call<Tweet>?, response: Response<Tweet>) {
                    if (response.isSuccessful) {
                        onSuccess(response.body())
                    } else {
                        onFailure()
                    }
                }
            })
    }

    fun favoriteTweet(id: Long, onSuccess: (Tweet) -> Unit, onFailure: () -> Unit) {
        TwitterCore.getInstance().getApiClient(SessionUtils.twitterSession).favoriteService.create(id, false)
            .enqueue(object : Callback<Tweet> {
                override fun onFailure(call: Call<Tweet>?, t: Throwable?) {
                    onFailure()
                }

                override fun onResponse(call: Call<Tweet>?, response: Response<Tweet>) {
                    if (response.isSuccessful) {
                        onSuccess(response.body())
                    } else {
                        onFailure()
                    }
                }
            })
    }

    fun retweet(id: Long, onSuccess: (Tweet) -> Unit, onFailure: () -> Unit) {
        TwitterCore.getInstance().getApiClient(SessionUtils.twitterSession).statusesService.retweet(id, true)
            .enqueue(object : Callback<Tweet> {
                override fun onFailure(call: Call<Tweet>?, t: Throwable?) {
                    onFailure()
                }

                override fun onResponse(call: Call<Tweet>?, response: Response<Tweet>) {
                    if (response.isSuccessful) {
                        onSuccess(response.body())
                    } else {
                        onFailure()
                    }
                }
            })
    }

    fun getTweets(query: String, geocode: Geocode?, onSuccess: (List<Tweet>) -> Unit, onFailure: () -> Unit) {
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
                onFailure()
            }

            override fun onResponse(call: Call<Search>?, response: Response<Search>) {
                if (response.isSuccessful) {
                    onSuccess(response.body().tweets)
                } else {
                    onFailure()
                }
            }
        })
    }

    fun getTweets(query: String, onSuccess: (List<Tweet>) -> Unit, onFailure: () -> Unit) {
        getTweets(query, null, onSuccess, onFailure)
    }

}