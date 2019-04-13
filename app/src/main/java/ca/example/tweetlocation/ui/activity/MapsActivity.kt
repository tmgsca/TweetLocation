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
import android.widget.SearchView
import android.widget.Toast
import ca.example.tweetlocation.R
import ca.example.tweetlocation.data.SessionUtils
import ca.example.tweetlocation.data.TweetDetail
import ca.example.tweetlocation.data.TweetMedium
import ca.example.tweetlocation.data.TweetRepository
import ca.example.tweetlocation.model.MapsViewModel
import ca.example.tweetlocation.model.MapsViewModelFactory
import ca.example.tweetlocation.ui.adapter.TweetSearchAdapter
import ca.example.tweetlocation.ui.view.TweetInfoWindow
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.twitter.sdk.android.core.models.Tweet
import kotlinx.android.synthetic.main.activity_maps.*

class MapsActivity : AppCompatActivity(), LocationListener, GoogleMap.OnInfoWindowClickListener {

    companion object {
        private const val TAG = "MapsActivity"
        private const val ACCESS_FINE_LOCATION = 0
        private const val LOCATION_REQUEST_MIN_TIME = 1000L
        private const val LOCATION_REQUEST_MIN_DISTANCE = 100f
    }

    private var googleMap: GoogleMap? = null
    private var locationManager: LocationManager? = null
    private lateinit var viewModel: MapsViewModel

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
            setupObservers()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        locationManager?.removeUpdates(this)
    }

    override fun onStart() {
        super.onStart()
        setupLocationListener()
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
                viewModel.queryTweets(" ", it)
                moveCameraToLocation()
            } else {
                viewModel.latitude = it.latitude
                viewModel.longitude = it.longitude
            }
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

    // Private methods

    private fun setupSearchView() {

        val adapter = TweetSearchAdapter {
            startTweetDetailActivity(it)
        }

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
                    viewModel.queryTweets(it)
                } ?: viewModel.clearSearch()
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return true
            }
        })
    }

    private fun startTweetDetailActivity(tweet: Tweet) {
        val detail = TweetDetail(
            tweet.id,
            tweet.user.name,
            tweet.text,
            tweet.user.screenName,
            tweet.favoriteCount,
            tweet.retweetCount,
            tweet.user.profileImageUrl,
            tweet.coordinates?.latitude,
            tweet.coordinates?.longitude,
            tweet.favorited,
            tweet.retweeted,
            tweet.createdAt,
            tweet.extendedEntities.media.map { TweetMedium(it.type, it.mediaUrlHttps) }
        )
        val intent = Intent(this, TweetDetailActivity::class.java)
        intent.putExtra("tweetDetail", detail)
        startActivity(intent)
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

    private fun setupObservers() {
        viewModel.getMapTweets().observe(this, Observer<List<Tweet>> { tweets ->
            tweets?.let {
                addMarkers(it)
                Toast.makeText(this, "Found ${it.size} tweets with geolocation enabled", Toast.LENGTH_SHORT).show()
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
            locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                LOCATION_REQUEST_MIN_TIME,
                LOCATION_REQUEST_MIN_DISTANCE,
                this
            )
        }
    }

    private fun addMarkers(tweets: Collection<Tweet>) {
        googleMap?.clear()
        tweets
            .filter { it.coordinates != null }
            .forEach {
                val options = MarkerOptions().position(LatLng(it.coordinates.latitude, it.coordinates.longitude))
                    .title("@${it.user.screenName}").snippet(it.text)
                val marker = googleMap?.addMarker(options)
                marker?.tag = it
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
