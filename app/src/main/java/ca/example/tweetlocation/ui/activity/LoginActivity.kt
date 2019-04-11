package ca.example.tweetlocation.ui.activity

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.util.Log
import ca.example.tweetlocation.R
import ca.example.tweetlocation.data.SessionUtils
import com.twitter.sdk.android.core.*

import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity() {
    companion object {
        const val TAG = "LoginActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        twitterLoginButton.callback = object : Callback<TwitterSession>() {
            override fun success(result: Result<TwitterSession>) {
                SessionUtils.twitterSession = result.data
                val self = this@LoginActivity
                val intent = Intent(self, MapsActivity::class.java)
                self.startActivity(intent)
                self.finish()
            }

            override fun failure(exception: TwitterException) {
                Log.e(TAG, exception.localizedMessage, exception)
                Snackbar.make(coordinatorLayout, getString(R.string.twitter_login_failed), 3).show()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        twitterLoginButton.onActivityResult(requestCode, resultCode, data)
    }
}
