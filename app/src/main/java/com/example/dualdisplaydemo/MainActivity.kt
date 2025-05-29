package com.example.dualdisplaydemo

import android.hardware.display.DisplayManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.OptIn
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.DefaultLoadControl
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import com.example.dualdisplaydemo.ui.theme.DualDisplayDemoTheme


class MainActivity : ComponentActivity() {
    private var secondaryPresentation: SecondaryDisplayPresentation? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val displayManager = getSystemService(DISPLAY_SERVICE) as DisplayManager
        val secondaryDisplay = displayManager.displays.find { it.displayId != android.view.Display.DEFAULT_DISPLAY }
        secondaryPresentation = secondaryDisplay?.let { SecondaryDisplayPresentation(this, it) }
        enableEdgeToEdge()
        setContent {
            DualDisplayDemoTheme {
                PrimaryDisplayContent()
            }
        }
        secondaryPresentation?.show()
    }
    override fun onDestroy() {
        super.onDestroy()
        secondaryPresentation?.dismiss()
    }
}

@Composable
fun PrimaryDisplayContent() {
    // Display an image on the front display
//    Image(
//        painter = painterResource(id = R.drawable.slam_dunk), // Replace with your image resource
//        contentDescription = "Primary Display Image",
//        modifier = Modifier.fillMaxSize()
//    )

    // Display a video on the front display (uncomment to use)
    VideoPlayer(uri = "file:///android_asset/TrailMarker_00001.mp4")  // Replace with your video path
}


@OptIn(UnstableApi::class)
@Composable
fun VideoPlayer(uri: String) {
    val context = LocalContext.current

    // 1) Create (and remember) a single ExoPlayer instance for the lifetime of this composable:
    val exoPlayer = remember {
        ExoPlayer.Builder(context)
            .setLoadControl(
                DefaultLoadControl.Builder()
                .setBufferDurationsMs(
                    /* minBufferMs = */ 15_000,
                    /* maxBufferMs = */ 50_000,
                    /* playbackBufferMs = */ 3_000,
                    /* backBufferMs = */ 1_000
                ).build()
            )
            .build()
            .apply {
                repeatMode = Player.REPEAT_MODE_ONE
            }
    }

    // 2) Whenever `uri` changes, submit it in a LaunchedEffect (so the player
    //    isn’t rebuilt, just reconfigured):
    LaunchedEffect(uri) {
        exoPlayer.setMediaItem(MediaItem.fromUri(uri))
        exoPlayer.prepare()
        exoPlayer.playWhenReady = true
    }

    // 3) Release when this composable leaves the composition:
    DisposableEffect(exoPlayer) {
        onDispose { exoPlayer.release() }
    }

    // 4) Also remember your PlayerView so it isn’t recreated on every recomposition:
    val playerView = remember {
        PlayerView(context).apply {
            useController = false      // optional: hide controls if you don’t need them
            resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
            player = exoPlayer
        }
    }

    AndroidView(
        factory = { playerView },
        modifier = Modifier.fillMaxSize()
    )
}