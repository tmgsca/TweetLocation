package ca.example.tweetlocation.data
import com.twitter.sdk.android.core.TwitterSession

// This singleton is responsible for keeping the TwitterSession in memory in order to keep it simple. In a real scenario
// it should be kept in a database or shared preferences.
object SessionUtils {
    var twitterSession: TwitterSession? = null
}