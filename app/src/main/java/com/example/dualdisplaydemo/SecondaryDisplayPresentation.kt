package com.example.dualdisplaydemo

import android.app.Presentation
import android.content.Context
import android.os.Bundle
import android.view.Display
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.painterResource
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.example.dualdisplaydemo.ui.theme.DualDisplayDemoTheme

class SecondaryDisplayPresentation(
    context: Context,
    display: Display,
) : Presentation(context, display), LifecycleOwner, SavedStateRegistryOwner {

    private val lifecycleRegistry = LifecycleRegistry(this)
    private val savedStateRegistryController = SavedStateRegistryController.create(this)

    override val lifecycle: Lifecycle get() = lifecycleRegistry
    override val savedStateRegistry: SavedStateRegistry get() = savedStateRegistryController.savedStateRegistry

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        savedStateRegistryController.performRestore(savedInstanceState)
        val composeView = ComposeView(context).apply {
            setViewTreeLifecycleOwner(this@SecondaryDisplayPresentation)
            setViewTreeSavedStateRegistryOwner(this@SecondaryDisplayPresentation)
            setContent {
                DualDisplayDemoTheme {
                    SecondaryDisplayContent()
                }
            }
        }
        setContentView(composeView)
        lifecycleRegistry.currentState = Lifecycle.State.CREATED
    }

    override fun onStart() {
        super.onStart()
        lifecycleRegistry.currentState = Lifecycle.State.STARTED
    }

    override fun onStop() {
        lifecycleRegistry.currentState = Lifecycle.State.CREATED
        super.onStop()
    }

    override fun dismiss() {
        lifecycleRegistry.currentState = Lifecycle.State.DESTROYED
        super.dismiss()
    }

    override fun onSaveInstanceState(): Bundle {
        val bundle = super.onSaveInstanceState()
        savedStateRegistryController.performSave(bundle)
        return bundle
    }
}

@Composable
fun SecondaryDisplayContent() {
    // Display an image on the back display
    Image(
        painter = painterResource(id = R.drawable.back_display), // Replace with your image resource
        contentDescription = "Secondary Display Image",
        modifier = Modifier.fillMaxSize()
    )

    // Display a video on the back display (uncomment to use)
//     VideoPlayer(uri = "file:///android_asset/TrailMarker_00001.mp4") // Replace with your video path
}