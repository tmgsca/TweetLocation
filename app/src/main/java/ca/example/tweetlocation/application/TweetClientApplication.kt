package ca.example.tweetlocation.application

import android.app.Application
import android.util.Log
import com.twitter.sdk.android.core.DefaultLogger
import com.twitter.sdk.android.core.Twitter
import com.twitter.sdk.android.core.TwitterAuthConfig
import com.twitter.sdk.android.core.TwitterConfig


class TweetClientApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        Twitter.initialize(this)
    }
}