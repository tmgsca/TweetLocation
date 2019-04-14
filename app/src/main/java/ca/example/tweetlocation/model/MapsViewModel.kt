package ca.example.tweetlocation.model

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import ca.example.tweetlocation.data.TweetRepository
import com.twitter.sdk.android.core.models.Tweet
import com.twitter.sdk.android.core.services.params.Geocode
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import java.util.stream.Collectors

class MapsViewModel(private val repository: TweetRepository) : ViewModel() {

    companion object {
        private const val DISTANCE = 5
        private const val MAX_MARKERS = 100
    }

    // Properties

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

    // Queries

    fun queryTweets(query: String) {
        loading.value = true
        repository.getTweets(query) {
            searchTweets.value = it
            loading.value = false
        }
    }

    fun queryGeocodedTweets(query: String) {
        latitude?.let { lat ->
            longitude?.let { lng ->
                val geocode = Geocode(lat, lng, DISTANCE, Geocode.Distance.KILOMETERS)
                repository.getTweets(query, geocode) { results ->
                    doAsync {
                        val filteredResults = results.parallelStream().filter { r -> r.coordinates != null }
                        mapTweets.value?.let { currentTweets ->
                            val oldTweets = ArrayList(currentTweets)
                            val oldTweetsSet = oldTweets.map { it.id }.toSet()
                            oldTweets.addAll(filteredResults.filter { it.id !in oldTweetsSet }.collect(Collectors.toList()))
                            oldTweets.sortBy { it.createdAt }
                            uiThread {
                                if (oldTweets.count() > MAX_MARKERS) {
                                    mapTweets.value = oldTweets.drop(oldTweets.count() - MAX_MARKERS)
                                } else {
                                    mapTweets.value = oldTweets
                                }
                            }
                        } ?: run {
                            uiThread {
                                mapTweets.value = filteredResults.collect(Collectors.toList()).sortedBy { it.createdAt }
                            }
                        }
                    }
                }
            }
        }
    }

    // UI Actions

    fun clearSearch() {
        searchTweets.value = emptyList()
    }

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

    // Accessors

    fun getLoading() = loading as LiveData<Boolean>
    fun getSearchTweets() = searchTweets as LiveData<List<Tweet>>
    fun getMapTweets() = mapTweets as LiveData<List<Tweet>>
    fun isShowingImageDialog() = isShowingImageDialog as LiveData<Boolean>
    fun isShowingVideoDialog() = isShowingVideoDialog as LiveData<Boolean>
}