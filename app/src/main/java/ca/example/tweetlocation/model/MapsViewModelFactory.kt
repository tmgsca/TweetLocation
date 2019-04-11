package ca.example.tweetlocation.model

import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import ca.example.tweetlocation.data.TweetRepository

class MapsViewModelFactory(private val repository: TweetRepository) : ViewModelProvider.NewInstanceFactory() {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return MapsViewModel(repository) as T
    }
}