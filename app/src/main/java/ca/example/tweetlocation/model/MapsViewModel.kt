package ca.example.tweetlocation.model

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import android.location.Location
import ca.example.tweetlocation.data.TweetRepository
import com.twitter.sdk.android.core.models.Tweet
import com.twitter.sdk.android.core.services.params.Geocode

class MapsViewModel(private val repository: TweetRepository) : ViewModel() {

    companion object {
        private const val DISTANCE = 10
    }

    var latitude: Double? = null
    var longitude: Double? = null

    private val searchTweets: MutableLiveData<List<Tweet>> by lazy {
        MutableLiveData<List<Tweet>>()
    }

    private val mapTweets: MutableLiveData<List<Tweet>> by lazy {
        MutableLiveData<List<Tweet>>()
    }

    fun queryTweets(query: String) {
        repository.getTweets(query) {
            searchTweets.value = it
        }
    }

    fun queryTweets(query: String, location: Location) {
        val geocode = Geocode(location.latitude, location.longitude, DISTANCE, Geocode.Distance.KILOMETERS)
        repository.getTweets(query, geocode) { results ->
            val filteredResults = results.filter { r -> r.coordinates != null }
            mapTweets.value?.let { currentTweets ->
                val oldTweets = ArrayList(currentTweets)
                oldTweets.addAll(filteredResults.filter { !oldTweets.map { tweet -> tweet.id }.contains(it.id) })
                oldTweets.sortBy { it.createdAt }
                if (oldTweets.count() > 100) {
                    mapTweets.value = oldTweets.drop(oldTweets.count() - 100)
                } else {
                    mapTweets.value = oldTweets
                }
            } ?: run {
                mapTweets.value = ArrayList(filteredResults).sortedBy { it.createdAt }
            }
        }
    }

    fun getSearchTweets() = searchTweets as LiveData<List<Tweet>>

    fun getMapTweets() = mapTweets as LiveData<List<Tweet>>
}