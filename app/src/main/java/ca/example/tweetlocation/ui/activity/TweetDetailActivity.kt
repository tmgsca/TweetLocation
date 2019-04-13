package ca.example.tweetlocation.ui.activity

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import ca.example.tweetlocation.R
import ca.example.tweetlocation.data.SessionUtils
import ca.example.tweetlocation.data.TweetDetail
import ca.example.tweetlocation.data.TweetRepository
import ca.example.tweetlocation.model.TweetDetailViewModel
import ca.example.tweetlocation.model.TweetDetailViewModelFactory
import ca.example.tweetlocation.util.VolleyUtils
import kotlinx.android.synthetic.main.activity_tweet_detail.*


class TweetDetailActivity : AppCompatActivity() {

    private lateinit var viewModel: TweetDetailViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupViewModel()
        setContentView(R.layout.activity_tweet_detail)
        setupObservers()
        setupViewData()
    }

    private fun setupViewData() {
        username.text = viewModel.userScreenName
        userFullName.text = viewModel.userFullName
        text.text = viewModel.text
        coordinates.text = viewModel.coordinates
        photo.setImageUrl(viewModel.userPhotoUrl, VolleyUtils.imageLoader)
        date.text = viewModel.createdAt
    }

    private fun setupObservers() {
        viewModel.getRetweetCount().observe(this, Observer<Int> {
            retweetCount.text = "$it"
        })

        viewModel.getFavoriteCount().observe(this, Observer<Int> {
            favoriteCount.text = "$it"
        })

        viewModel.getRetweeted().observe(this, Observer<Boolean> {
            it?.let { retweeted ->
                retweetButton.isEnabled = retweeted
            } ?: run { retweetButton.isEnabled = false }
        })
    }

    private fun setupViewModel() {
        SessionUtils.twitterSession?.let {
            val repository = TweetRepository(it)
            val tweetDetail = intent.getParcelableExtra<TweetDetail>("tweetDetail")
            val factory = TweetDetailViewModelFactory(tweetDetail, repository)
            viewModel = ViewModelProviders.of(this, factory).get(TweetDetailViewModel::class.java)
        }
    }
}
