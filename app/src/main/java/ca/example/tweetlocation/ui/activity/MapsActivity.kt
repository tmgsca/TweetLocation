package ca.example.tweetlocation.ui.activity

import android.Manifest
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.SearchView
import ca.example.tweetlocation.R
import ca.example.tweetlocation.data.SessionUtils
import ca.example.tweetlocation.data.TweetRepository
import ca.example.tweetlocation.model.MapsViewModel
import ca.example.tweetlocation.model.MapsViewModelFactory
import ca.example.tweetlocation.ui.adapter.TweetSearchAdapter
import ca.example.tweetlocation.ui.dialog.ImageViewDialog
import ca.example.tweetlocation.ui.dialog.VideoViewDialog
import ca.example.tweetlocation.ui.view.TweetInfoWindow
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.twitter.sdk.android.core.models.Tweet
import kotlinx.android.synthetic.main.activity_maps.*
import java.util.*
import kotlin.concurrent.fixedRateTimer

class MapsActivity : AppCompatActivity(), LocationListener, GoogleMap.OnInfoWindowClickListener {

    companion object {
        private const val TAG = "MapsActivity"
        private const val ACCESS_FINE_LOCATION = 0
        private const val LOCATION_REQUEST_MIN_TIME = 1000L
        private const val LOCATION_REQUEST_MIN_DISTANCE = 100f
    }

    private var userCircle: Circle? = null
    private var googleMap: GoogleMap? = null
    private var markers: MutableList<Marker> = mutableListOf()
    private var locationManager: LocationManager? = null
    private lateinit var viewModel: MapsViewModel
    private var tweetQueryTimer: Timer? = null

    // Lifecycle methods

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupViewModel()
        setContentView(R.layout.activity_maps)
        setupSearchView()
        supportActionBar?.hide()
        (map as SupportMapFragment).getMapAsync {
            googleMap = it
            googleMap?.setInfoWindowAdapter(TweetInfoWindow(this))
            googleMap?.setOnInfoWindowClickListener(this)
            // We won't request the permissions here because they're going to be requested elsewhere already.
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            )
                googleMap?.isMyLocationEnabled = true
            setupObservers()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        locationManager?.removeUpdates(this)
        tweetQueryTimer?.cancel()
        tweetQueryTimer = null
    }

    override fun onStop() {
        super.onStop()
        tweetQueryTimer?.cancel()
        tweetQueryTimer = null
    }

    override fun onStart() {
        super.onStart()
        setupLocationListener()
        setupQueryTimer()
    }

    private fun setupQueryTimer() {
        if (tweetQueryTimer == null) {
            tweetQueryTimer = fixedRateTimer("tweetQueryTimer", false, 0, 10000) {
                runOnUiThread {
                    viewModel.queryGeocodedTweets(" ")
                }
            }
        }
    }

    // Map InfoWindow click listener

    override fun onInfoWindowClick(marker: Marker?) {
        startTweetDetailActivity(marker?.tag as Tweet)
    }

    // PermissionsResult listener

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (grantResults.isNotEmpty()) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (ContextCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    googleMap?.isMyLocationEnabled = true
                    locationManager?.requestLocationUpdates(
                        LocationManager.GPS_PROVIDER,
                        LOCATION_REQUEST_MIN_TIME,
                        LOCATION_REQUEST_MIN_DISTANCE,
                        this
                    )
                }
            } else if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(
                        this,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    )
                ) {
                    ActivityCompat.requestPermissions(
                        this,
                        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                        ACCESS_FINE_LOCATION
                    )
                }
            }
        }
    }

    // LocationListener methods

    override fun onLocationChanged(location: Location?) {
        location?.let {
            if (viewModel.latitude == null && viewModel.longitude == null) {
                viewModel.latitude = it.latitude
                viewModel.longitude = it.longitude
                viewModel.queryGeocodedTweets(" ")
                moveCameraAndZoomToLocation()
            } else {
                viewModel.latitude = it.latitude
                viewModel.longitude = it.longitude
                moveCameraToLocation()
            }
            userCircle?.remove()
            userCircle = googleMap?.addCircle(
                CircleOptions()
                    .center(
                        LatLng(it.latitude, it.longitude)
                    )
                    .radius(5000.0)
                    .strokeColor(getColor(R.color.colorPrimary))
                    .fillColor(getColor(R.color.circleFill))
            )
        }
    }

    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
        Log.d(TAG, "onStatusChanged: $provider $status")
    }

    override fun onProviderEnabled(provider: String?) {
        Log.d(TAG, "onProviderEnabled: $provider")
    }

    override fun onProviderDisabled(provider: String?) {
        Log.d(TAG, "onProviderDisabled: $provider")
    }

    // View trigger

    private fun startTweetDetailActivity(tweet: Tweet) {
        val intent = Intent(this, TweetDetailActivity::class.java)
        intent.putExtra("tweetId", tweet.id)
        startActivity(intent)
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

    // Private methods

    private fun setupSearchView() {
        val adapter = TweetSearchAdapter(
            onTweetClick = { startTweetDetailActivity(it) },
            onMediaClick = {
                if (it.type == "video") it.video?.url?.let { url ->
                    viewModel.showVideoDialog(url)
                } else viewModel.showImageDialog(it.url)
            })
        searchView.imeOptions = searchView.imeOptions or EditorInfo.IME_FLAG_NO_EXTRACT_UI
        searchResultsRecyclerView.layoutManager = LinearLayoutManager(this).apply {
            orientation = LinearLayoutManager.VERTICAL
        }
        searchResultsRecyclerView.adapter = adapter
        viewModel.getSearchTweets().observe(this, Observer<List<Tweet>> {
            adapter.loadItems(it ?: emptyList())
            adapter.notifyDataSetChanged()
        })
        searchView.setOnQueryTextFocusChangeListener { _, hasFocus ->
            searchResultsRecyclerView.visibility = if (hasFocus) View.VISIBLE else View.GONE
            searchBackgroundView.visibility = if (hasFocus) View.VISIBLE else View.GONE
            if (!hasFocus) viewModel.clearSearch()
        }
        searchView.setOnCloseListener {
            viewModel.clearSearch()
            false
        }
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                query?.let {
                    viewModel.clearSearch()
                    viewModel.queryTweets(it)
                } ?: viewModel.clearSearch()
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return true
            }
        })
    }

    private fun moveCameraToLocation() {
        googleMap?.let { m ->
            viewModel.latitude?.let { latitude ->
                viewModel.longitude?.let { longitude ->
                    val position = LatLng(latitude, longitude)
                    m.moveCamera(CameraUpdateFactory.newLatLng(position))
                }
            }
        }
    }

    private fun moveCameraAndZoomToLocation() {
        googleMap?.let { m ->
            viewModel.latitude?.let { latitude ->
                viewModel.longitude?.let { longitude ->
                    val position = LatLng(latitude, longitude)
                    m.moveCamera(CameraUpdateFactory.newLatLngZoom(position, 10.0f))
                }
            }
        }
    }

    private fun setupObservers() {
        viewModel.getLoading().observe(this, Observer<Boolean> { loading ->
            loading?.let {
                progressBar.visibility = if (it) View.VISIBLE else View.GONE
            }
        })
        viewModel.getMapTweets().observe(this, Observer<List<Tweet>> { tweets ->
            tweets?.let {
                addMarkers(it)
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

    private fun setupLocationListener() {
        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                ACCESS_FINE_LOCATION
            )

        } else {
            googleMap?.isMyLocationEnabled = true
            locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                LOCATION_REQUEST_MIN_TIME,
                LOCATION_REQUEST_MIN_DISTANCE,
                this
            )
        }
    }

    private fun addMarkers(tweets: Collection<Tweet>) {

        // Convert to set to improve filter performance
        val tweetSet = tweets.toSet()
        val currentMarkedTweetsSet = (markers.map { marker -> marker.tag as Tweet }).toSet()

        // Remove deleted markers
        markers.filter { it.tag as Tweet !in tweetSet }.forEach { it.remove() }

        // Add tweets after subtracting from the current marker set
        tweets.parallelStream()
            .filter { it !in currentMarkedTweetsSet }
            .forEach { tweet ->
                val position = LatLng(tweet.coordinates.latitude, tweet.coordinates.longitude)
                val options = MarkerOptions().position(position)
                runOnUiThread {
                    googleMap?.let {
                        val marker = it.addMarker(options)
                        marker?.tag = tweet
                        markers.add(marker)
                    }
                }
            }
    }

    private fun setupViewModel() {
        SessionUtils.twitterSession?.let {
            val repository = TweetRepository(it)
            val factory = MapsViewModelFactory(repository)
            viewModel = ViewModelProviders.of(this, factory).get(MapsViewModel::class.java)
        }
    }
}
