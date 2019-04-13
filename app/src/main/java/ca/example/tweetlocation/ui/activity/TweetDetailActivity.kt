package ca.example.tweetlocation.ui.activity

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import ca.example.tweetlocation.R
import ca.example.tweetlocation.data.SessionUtils
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
        setupActionListeners()
    }

    private fun setupObservers() {

        viewModel.isLoading().observe(this, Observer<Boolean> {
            it?.let { isLoading ->
                loadingView.visibility = if (isLoading) View.VISIBLE else View.GONE
            } ?: run {
                loadingView.visibility = View.GONE
            }
        })

        viewModel.getUserFullName().observe(this, Observer<String> {
            userNameTextView.text = it
        })

        viewModel.getUserScreenName().observe(this, Observer<String> {
            userScreenNameTextView.text = it
        })

        viewModel.getText().observe(this, Observer<String> {
            textTextView.text = it
        })

        viewModel.getCoordinates().observe(this, Observer<String> {
            coordinatesTextView.text = it
        })

        viewModel.getUserPhotoUrl().observe(this, Observer<String> {
            userPhotoImageView.setImageUrl(it, VolleyUtils.imageLoader)
        })

        viewModel.getCreatedAt().observe(this, Observer<String> {
            createdAtTextView.text = it
        })

        viewModel.getRetweetCount().observe(this, Observer<Int> {
            retweetCountTextView.text = "$it"
        })

        viewModel.getFavoriteCount().observe(this, Observer<Int> {
            favoriteCountTextView.text = "$it"
        })

        viewModel.isFavorite().observe(this, Observer<Boolean> {
            it?.let { favorite ->
                favoriteButton.isSelected = favorite
            } ?: run { favoriteButton.isSelected = false }
        })

        viewModel.isRetweeted().observe(this, Observer<Boolean> {
            it?.let { retweeted ->
                retweetButton.isEnabled = !retweeted
            } ?: run { retweetButton.isEnabled = false }
        })
    }

    private fun setupActionListeners() {
        retweetButton.setOnClickListener {
            viewModel.retweet()
        }
        favoriteButton.setOnClickListener {
            viewModel.isFavorite().value?.let { isFavorite ->
                if (isFavorite) viewModel.unfavorite() else viewModel.favorite()
            }
        }
    }

    private fun setupViewModel() {
        SessionUtils.twitterSession?.let {
            if (intent.hasExtra("tweetId")) {
                val repository = TweetRepository(it)
                val tweetId = intent.getLongExtra("tweetId", -1)
                val factory = TweetDetailViewModelFactory(tweetId, repository)
                viewModel = ViewModelProviders.of(this, factory).get(TweetDetailViewModel::class.java)
            }
        }
    }
}
