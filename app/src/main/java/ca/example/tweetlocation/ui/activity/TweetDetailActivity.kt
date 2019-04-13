package ca.example.tweetlocation.ui.activity

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.GridLayoutManager
import android.view.View
import ca.example.tweetlocation.R
import ca.example.tweetlocation.data.SessionUtils
import ca.example.tweetlocation.data.TweetMedium
import ca.example.tweetlocation.data.TweetRepository
import ca.example.tweetlocation.model.TweetDetailViewModel
import ca.example.tweetlocation.model.TweetDetailViewModelFactory
import ca.example.tweetlocation.ui.adapter.TweetMediaAdapter
import ca.example.tweetlocation.ui.dialog.ImageViewDialog
import ca.example.tweetlocation.ui.dialog.VideoViewDialog
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
        setupRecyclerView()
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
            userScreenNameTextView.text = getString(R.string.user_screen_name, it)
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
                retweetButton.alpha = if (retweeted) 0.5F else 1F
            } ?: run {
                retweetButton.isEnabled = false
                retweetButton.alpha = 0.5F
            }
        })

        viewModel.isShowingImageDialog().observe(this, Observer<Boolean> { isShowingImageDialog ->
            isShowingImageDialog?.let {
                if (it) viewModel.imageDialogUrl?.let { url -> showImageDialog(url) }
            }
        })
        viewModel.isShowingVideoDialog().observe(this, Observer<Boolean> { isShowingVideoDialog ->
            isShowingVideoDialog?.let {
                if (it) viewModel.videoDialogUrl?.let { url -> showVideoDialog(url) }
            }
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

    private fun setupRecyclerView() {
        val adapter = TweetMediaAdapter {
            if (it.type == "photo") viewModel.showImageDialog(it.url)
            else it.video?.url?.let { url ->
                viewModel.showVideoDialog(url)
            }
        }
        mediaRecyclerView.layoutManager = GridLayoutManager(this, 6).apply {
            orientation = GridLayoutManager.VERTICAL
        }
        mediaRecyclerView.adapter = adapter

        viewModel.getMedia().observe(this, Observer<List<TweetMedium>> {
            it?.let { media -> adapter.loadItems(media) }
            adapter.notifyDataSetChanged()
        })
    }

    private fun showImageDialog(url: String) {
        ImageViewDialog(this, url) {
            viewModel.closeImageDialog()
        }.show()
    }

    private fun showVideoDialog(url: String) {
        VideoViewDialog(this, url) {
            viewModel.closeVideoDialog()
        }.show()
    }
}
