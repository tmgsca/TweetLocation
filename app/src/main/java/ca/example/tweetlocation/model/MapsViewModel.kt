package ca.example.tweetlocation.model

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import android.location.Location
import ca.example.tweetlocation.data.SessionUtils
import com.twitter.sdk.android.core.TwitterCore
import com.twitter.sdk.android.core.models.Search
import com.twitter.sdk.android.core.models.Tweet
import com.twitter.sdk.android.core.services.params.Geocode
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*

class MapsViewModel : ViewModel() {

    companion object {
        private const val DISTANCE = 100
    }

    val tweets: MutableLiveData<Queue<Tweet>> by lazy {
        MutableLiveData<Queue<Tweet>>()
    }

    fun queryTweets(query: String, location: Location) {

        TwitterCore.getInstance().getApiClient(SessionUtils.twitterSession).searchService.tweets(
            query,
            Geocode(location.latitude, location.longitude, DISTANCE, Geocode.Distance.KILOMETERS),
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
                response?.body()?.tweets?.let { results ->
                    val filteredResults = LinkedList<Tweet>(results.filter { r -> r.coordinates != null }.sortedBy { r -> r.createdAt })
                    tweets.value?.let { currentTweets ->
                        if (filteredResults.count() + currentTweets.count() > 100) {
                            val difference = Math.abs(filteredResults.count() - currentTweets.count())
                            for (i in 0..difference) {
                                currentTweets.poll()
                            }
                        }
                        currentTweets.addAll(LinkedList<Tweet>(filteredResults))
                        tweets.value = currentTweets
                    } ?: run {
                        tweets.value = LinkedList<Tweet>(filteredResults)
                    }
                }
            }
        })
    }
}