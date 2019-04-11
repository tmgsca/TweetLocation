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
import ca.example.tweetlocation.R
import ca.example.tweetlocation.model.MapsViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.twitter.sdk.android.core.models.Tweet
import kotlinx.android.synthetic.main.activity_maps.*
import java.util.*

class MapsActivity : AppCompatActivity(), LocationListener {

    companion object {
        private const val TAG = "MapsActivity"
        private const val ACCESS_FINE_LOCATION = 0
        private const val LOCATION_REQUEST_MIN_TIME = 10000L
        private const val LOCATION_REQUEST_MIN_DISTANCE = 100f
    }

    private lateinit var googleMap: GoogleMap
    private lateinit var locationManager: LocationManager
    private lateinit var viewModel: MapsViewModel

    // Lifecycle methods

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(MapsViewModel::class.java)
        setContentView(R.layout.activity_maps)
        (map as SupportMapFragment).getMapAsync {
            googleMap = it
            setupLocationListener()
            val sydney = LatLng(-34.0, 151.0)
            googleMap.moveCamera(CameraUpdateFactory.newLatLng(sydney))
        }
        setupObservers()
    }

    override fun onStart() {
        super.onStart()
        setupLocationListener()
    }

    // PermissionsResult listener

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, LOCATION_REQUEST_MIN_TIME, LOCATION_REQUEST_MIN_DISTANCE, this)
        }
    }

    // LocationListener methods

    override fun onLocationChanged(location: Location?) {
        Log.d(TAG, "onLocationChanged: $location")
        location?.let {
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
        viewModel.tweets.observe(this, Observer<Queue<Tweet>> { tweets ->
            tweets?.let { addMarkers(it) }
        })
    }

    private fun setupLocationListener() {
        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                //TODO: Show permission request dialog
            } else {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    ACCESS_FINE_LOCATION
                )
            }
        } else {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, LOCATION_REQUEST_MIN_TIME, LOCATION_REQUEST_MIN_DISTANCE, this)
        }
    }

    private fun addMarkers(tweets: Collection<Tweet>) {
        googleMap.clear()
        tweets
            .filter { it.coordinates != null }
            .map { MarkerOptions().position(LatLng(it.coordinates.latitude, it.coordinates.longitude)).title(it.user.name) }
            .forEach { googleMap.addMarker(it) }
    }
}
