package com.dluvian.nozzle

import android.os.Build.VERSION.SDK_INT
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import coil.Coil
import coil.ImageLoader
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import com.dluvian.nozzle.ui.app.NozzleApp

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val appContainer = AppContainer(applicationContext)

        // Allow GIFs
        val imageLoader = ImageLoader.Builder(applicationContext)
            .components {
                if (SDK_INT >= 28) {
                    add(ImageDecoderDecoder.Factory())
                } else {
                    add(GifDecoder.Factory())
                }
            }.build()
        Coil.setImageLoader(imageLoader)

        // Shrink composable when keyboard opens
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)

        setContent {
            NozzleApp(appContainer = appContainer)
        }
    }

}
