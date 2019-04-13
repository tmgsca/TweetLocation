package ca.example.tweetlocation.model

import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import ca.example.tweetlocation.data.TweetDetail
import ca.example.tweetlocation.data.TweetRepository


class TweetDetailViewModelFactory(private val tweetDetail: TweetDetail, private val repository: TweetRepository) : ViewModelProvider.NewInstanceFactory() {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return TweetDetailViewModel(tweetDetail, repository) as T
    }
}