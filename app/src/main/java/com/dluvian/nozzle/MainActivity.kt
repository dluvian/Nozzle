package com.dluvian.nozzle

import android.os.Build.VERSION.SDK_INT
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import coil.Coil
import coil.ImageLoader
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import com.dluvian.nozzle.ui.app.NozzleApp
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private lateinit var appContainer: AppContainer
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        appContainer = AppContainer(applicationContext)

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

    override fun onStop() {
        super.onStop()
        lifecycleScope.launch {
            appContainer.databaseSweeper.sweep()
        }
    }
}
