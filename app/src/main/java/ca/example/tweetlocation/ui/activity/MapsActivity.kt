package ca.example.tweetlocation.ui.activity

import android.Manifest
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.Toast
import ca.example.tweetlocation.R
import ca.example.tweetlocation.data.SessionUtils
import ca.example.tweetlocation.data.TweetRepository
import ca.example.tweetlocation.model.MapsViewModel
import ca.example.tweetlocation.model.MapsViewModelFactory
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.twitter.sdk.android.core.models.Tweet
import kotlinx.android.synthetic.main.activity_maps.*

class MapsActivity : AppCompatActivity(), LocationListener {

    companion object {
        private const val TAG = "MapsActivity"
        private const val ACCESS_FINE_LOCATION = 0
        private const val LOCATION_REQUEST_MIN_TIME = 1000L
        private const val LOCATION_REQUEST_MIN_DISTANCE = 100f
    }

    private var googleMap: GoogleMap? = null
    private lateinit var locationManager: LocationManager
    private lateinit var viewModel: MapsViewModel
    private var location: Location? = null

    // Lifecycle methods

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupViewModel()
        setContentView(R.layout.activity_maps)
        (map as SupportMapFragment).getMapAsync {
            googleMap = it
            moveCameraToCurrentLocation()
            setupObservers()
        }
    }

    private fun moveCameraToCurrentLocation() {
        googleMap?.let { m ->
            location?.let { l ->
                val position = LatLng(l.latitude, l.longitude)
                m.moveCamera(CameraUpdateFactory.newLatLng(position))
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        locationManager.removeUpdates(this)
    }

    override fun onStart() {
        super.onStart()
        setupLocationListener()
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
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, LOCATION_REQUEST_MIN_TIME, LOCATION_REQUEST_MIN_DISTANCE, this)
                }
            } else if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
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
        this.location = location
        moveCameraToCurrentLocation()
        this.location?.let {
            viewModel.queryTweets("", it)
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
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, LOCATION_REQUEST_MIN_TIME, LOCATION_REQUEST_MIN_DISTANCE, this)
        }
    }

    private fun addMarkers(tweets: Collection<Tweet>) {
        googleMap?.clear()
        tweets
            .filter { it.coordinates != null }
            .map { MarkerOptions().position(LatLng(it.coordinates.latitude, it.coordinates.longitude)).title(it.user.name) }
            .forEach { googleMap?.addMarker(it) }
    }

    private fun setupViewModel() {
        SessionUtils.twitterSession?.let {
            val repository = TweetRepository(it)
            val factory = MapsViewModelFactory(repository)
            viewModel = ViewModelProviders.of(this, factory).get(MapsViewModel::class.java)
        }
    }

}
