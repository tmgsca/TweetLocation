package ca.example.tweetlocation.util

import android.graphics.Bitmap
import android.support.v4.util.LruCache
import ca.example.tweetlocation.application.TweetClientApplication
import com.android.volley.RequestQueue
import com.android.volley.toolbox.ImageLoader
import com.android.volley.toolbox.Volley


object VolleyUtils {
    val requestQueue: RequestQueue
    val imageLoader: ImageLoader

    init {
        requestQueue = Volley.newRequestQueue(TweetClientApplication.appContext)
        imageLoader = ImageLoader(this.requestQueue, object : ImageLoader.ImageCache {
            private val mCache = LruCache<String, Bitmap>(10)
            override fun putBitmap(url: String, bitmap: Bitmap) {
                mCache.put(url, bitmap)
            }

            override fun getBitmap(url: String): Bitmap? {
                return mCache.get(url)
            }
        })
    }
}