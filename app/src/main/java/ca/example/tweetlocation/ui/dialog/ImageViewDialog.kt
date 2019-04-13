package ca.example.tweetlocation.ui.dialog

import android.app.Dialog
import android.content.Context
import android.view.Window
import android.view.WindowManager
import ca.example.tweetlocation.R
import ca.example.tweetlocation.util.VolleyUtils
import kotlinx.android.synthetic.main.dialog_image_viewer.*

class ImageViewDialog(context: Context, url: String, onDismissListener: () -> Unit) : Dialog(context) {

    init {
        setOnDismissListener { onDismissListener() }
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.dialog_image_viewer)
        val layoutParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
        layoutParams.copyFrom(window?.attributes)
        imageView.setImageUrl(url, VolleyUtils.imageLoader)
    }
}