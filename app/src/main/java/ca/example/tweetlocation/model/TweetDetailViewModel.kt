package ca.example.tweetlocation.model

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import ca.example.tweetlocation.data.TweetMedium
import ca.example.tweetlocation.data.TweetRepository
import ca.example.tweetlocation.data.TweetVideo
import com.twitter.sdk.android.core.models.Tweet

class TweetDetailViewModel(private val id: Long, private val repository: TweetRepository) :
    ViewModel() {

    init {
        repository.getTweet(id) {
            userFullname.value = it.user.name
            text.value = it.text
            userScreenName.value = it.user.screenName
            userPhotoUrl.value = it.user.profileImageUrl
            createdAt.value = it.createdAt
            retweeted.value = it.retweeted
            favorited.value = it.favorited
            favoriteCount.value = it.favoriteCount
            retweetCount.value = it.retweetCount
            coordinates.value = extractCoordinates(it)
            media.value = extractTweetMedia(it)
            loading.value = false
        }
    }

    // Properties

    private val loading: MutableLiveData<Boolean> by lazy {
        MutableLiveData<Boolean>().apply { value = true }
    }

    private val userFullname: MutableLiveData<String> by lazy {
        MutableLiveData<String>()
    }

    private val text: MutableLiveData<String> by lazy {
        MutableLiveData<String>()
    }

    private val userScreenName: MutableLiveData<String> by lazy {
        MutableLiveData<String>()
    }

    private val userPhotoUrl: MutableLiveData<String> by lazy {
        MutableLiveData<String>()
    }

    private val createdAt: MutableLiveData<String> by lazy {
        MutableLiveData<String>()
    }

    private val coordinates: MutableLiveData<String> by lazy {
        MutableLiveData<String>()
    }

    private val retweeted: MutableLiveData<Boolean> by lazy {
        MutableLiveData<Boolean>()
    }

    private val favorited: MutableLiveData<Boolean> by lazy {
        MutableLiveData<Boolean>()
    }

    private val favoriteCount: MutableLiveData<Int> by lazy {
        MutableLiveData<Int>()
    }

    private val retweetCount: MutableLiveData<Int> by lazy {
        MutableLiveData<Int>()
    }

    private val media: MutableLiveData<List<TweetMedium>> by lazy {
        MutableLiveData<List<TweetMedium>>()
    }

    // Accessors

    fun isLoading(): LiveData<Boolean> = loading
    fun getUserFullName(): LiveData<String> = userFullname
    fun getText(): LiveData<String> = text
    fun getUserScreenName(): LiveData<String> = userScreenName
    fun getUserPhotoUrl(): LiveData<String> = userPhotoUrl
    fun getCreatedAt(): LiveData<String> = createdAt
    fun getCoordinates(): LiveData<String> = coordinates
    fun isRetweeted(): LiveData<Boolean> = retweeted
    fun isFavorite(): LiveData<Boolean> = favorited
    fun getFavoriteCount(): LiveData<Int> = favoriteCount
    fun getRetweetCount(): LiveData<Int> = retweetCount
    fun getMedia(): LiveData<List<TweetMedium>> = media

    // Actions

    fun retweet() {
        repository.retweet(id) {
            retweetCount.value = it.retweetCount
            retweeted.value = it.retweeted
        }
    }

    fun favorite() {
        repository.favoriteTweet(id) {
            favoriteCount.value = it.favoriteCount
            favorited.value = it.favorited
        }
    }

    fun unfavorite() {
        repository.unfavoriteTweet(id) {
            favoriteCount.value = it.favoriteCount
            favorited.value = it.favorited
        }
    }

    // Helpers

    private fun extractCoordinates(tweet: Tweet): String {
        return if (tweet.coordinates?.latitude != null && tweet.coordinates?.longitude != null) {
            "${tweet.coordinates.latitude}, ${tweet.coordinates.longitude}"
        } else ""
    }

    private fun extractTweetMedia(tweet: Tweet): List<TweetMedium> {
        return tweet.extendedEntities.media.map { entity ->
            TweetMedium(
                entity.type,
                entity.mediaUrlHttps,
                if (entity.videoInfo != null) TweetVideo(
                    entity.videoInfo.variants.last().url,
                    entity.videoInfo.aspectRatio.first() / entity.videoInfo.aspectRatio.last()
                ) else null
            )
        }
    }
}