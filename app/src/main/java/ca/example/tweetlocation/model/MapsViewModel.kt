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
    var videoDialogUrl: String? = null
    var imageDialogUrl: String? = null

    private val isShowingImageDialog: MutableLiveData<Boolean> by lazy {
        MutableLiveData<Boolean>().also {
            it.value = false
        }
    }

    private val isShowingVideoDialog: MutableLiveData<Boolean> by lazy {
        MutableLiveData<Boolean>().also {
            it.value = false
        }
    }

    private val loading: MutableLiveData<Boolean> by lazy {
        MutableLiveData<Boolean>().also {
            it.value = false
        }
    }

    private val searchTweets: MutableLiveData<List<Tweet>> by lazy {
        MutableLiveData<List<Tweet>>()
    }

    private val mapTweets: MutableLiveData<List<Tweet>> by lazy {
        MutableLiveData<List<Tweet>>()
    }

    fun queryTweets(query: String) {
        loading.value = true
        repository.getTweets(query) {
            searchTweets.value = it
            loading.value = false
        }
    }

    fun queryTweets(query: String, location: Location) {
        val geocode = Geocode(location.latitude, location.longitude, DISTANCE, Geocode.Distance.KILOMETERS)
        loading.value = true
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
            loading.value = false
        }
    }

    fun clearSearch() {
        searchTweets.value = emptyList()
    }

    fun getLoading() = loading as LiveData<Boolean>

    fun getSearchTweets() = searchTweets as LiveData<List<Tweet>>

    fun getMapTweets() = mapTweets as LiveData<List<Tweet>>

    fun showVideoDialog(url: String) {
        videoDialogUrl = url
        isShowingVideoDialog.value = true
    }

    fun showImageDialog(url: String) {
        imageDialogUrl = url
        isShowingImageDialog.value = true
    }

    fun closeVideoDialog() {
        isShowingVideoDialog.value = false
        videoDialogUrl = null
    }

    fun closeImageDialog() {
        isShowingImageDialog.value = false
        imageDialogUrl = null
    }

    fun isShowingImageDialog() = isShowingImageDialog as LiveData<Boolean>

    fun isShowingVideoDialog() = isShowingVideoDialog as LiveData<Boolean>
}