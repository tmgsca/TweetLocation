package ca.example.tweetlocation.ui.dialog

import android.app.Dialog
import android.content.Context
import android.net.Uri
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import android.widget.MediaController
import ca.example.tweetlocation.R
import kotlinx.android.synthetic.main.dialog_video_player.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread

class VideoViewDialog(context: Context, url: String, onDismissListener: () -> Unit) : Dialog(context) {

    private val mediaController: MediaController = MediaController(context)

    init {
        setOnDismissListener { onDismissListener() }
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.dialog_video_player)
        val layoutParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
        layoutParams.copyFrom(window?.attributes)
        window?.attributes = layoutParams
        doAsync {
            videoView.setVideoURI(Uri.parse(url))
            uiThread {
                mediaController.setAnchorView(videoView)
                (mediaController.parent as ViewGroup).removeView(mediaController)
                mediaControllerLayout.addView(mediaController)
                mediaController.visibility = View.VISIBLE
                mediaController.setMediaPlayer(videoView)
                videoView.start()
            }
        }
        mediaController.show(0)
        videoView.setOnPreparedListener {
            videoPlayerProgressBar.visibility = View.GONE
        }
    }

    override fun show() {
        super.show()
        videoPlayerProgressBar.visibility = View.VISIBLE
    }
}