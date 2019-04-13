package ca.example.tweetlocation.model

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MediatorLiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import ca.example.tweetlocation.data.TweetDetail
import ca.example.tweetlocation.data.TweetRepository

class TweetDetailViewModel(private val tweetDetail: TweetDetail, private val repository: TweetRepository) : ViewModel() {

    private val id = tweetDetail.id
    val userFullName = tweetDetail.userFullName
    val text = tweetDetail.text
    val userScreenName = tweetDetail.userScreenName
    val userPhotoUrl = tweetDetail.userPhotoUrl
    val coordinates = "${tweetDetail.latitude} | ${tweetDetail.longitude}"
    val createdAt = tweetDetail.createdAt
    val media = tweetDetail.media

    private val retweeted: MutableLiveData<Boolean> by lazy {
        MutableLiveData<Boolean>().apply { value = tweetDetail.retweeted }
    }

    fun getRetweeted(): LiveData<Boolean> = retweeted

    private val favorited: MutableLiveData<Boolean> by lazy {
        MutableLiveData<Boolean>().apply { value = tweetDetail.favorited }
    }

    fun getFavorited(): LiveData<Boolean> = favorited

    private val favoriteCount: MutableLiveData<Int> by lazy {
        MutableLiveData<Int>().apply { value = tweetDetail.favoriteCount }
    }

    fun getFavoriteCount(): LiveData<Int> = favoriteCount

    private val retweetCount: MutableLiveData<Int> by lazy {
        MutableLiveData<Int>().apply { value = tweetDetail.retweetCount }
    }

    fun getRetweetCount(): LiveData<Int> = retweetCount

    fun retweet() {
        repository.retweet(id) {
            retweetCount.value = it.retweetCount
        }
    }

    fun favorite() {
        repository.favoriteTweet(id) {
            favoriteCount.value = it.favoriteCount
        }
    }

    fun unfavorite() {
        repository.unfavoriteTweet(id) {
            favoriteCount.value = it.favoriteCount
        }
    }
}