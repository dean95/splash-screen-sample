package com.example.splashscreensample

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.os.Bundle
import android.os.Process
import android.view.View
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.animation.doOnEnd
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.splashscreen.SplashScreenViewProvider
import androidx.interpolator.view.animation.FastOutLinearInInterpolator
import androidx.lifecycle.lifecycleScope
import com.example.splashscreensample.ui.theme.SplashScreenSampleTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private var appReady = false
    private var contentVisible by mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        /**
         * This is our launcher activity so we need to call installSplashScreen here to tell the Activity
         * to handle the transition between splash theme and an app theme.
         */
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)

        /**
         * The content view needs to be set before calling setOnExitAnimationListener to ensure that the
         * SplashScreenView is attached to the right view root.
         */
        setContent {
            SplashScreenSampleTheme {
                AnimatedVisibility(
                    visible = contentVisible,
                    enter = fadeIn(
                        animationSpec = tween(durationMillis = 500, easing = LinearEasing)
                    )
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.LightGray),
                        contentAlignment = Alignment.Center

                    ) {
                        Greeting("Android")
                    }
                }
            }
        }

        /**
         * We can optionally keep the splash screen visible until the condition isn't met anymore.
         */
        splashScreen.setKeepOnScreenCondition {
            appReady.not()
        }

        /**
         * This code is here only for demo purposes. It creates artificial delay to keep splash screen longer visible.
         */
        lifecycleScope.launch {
            delay(1000)
            appReady = true
        }

        /**
         * We're setting an optional OnExitAnimationListener on the SplashScreen. This tells the system that the
         * application will handle the exit animation. The listener will be called once the app is ready.
         */
        splashScreen.setOnExitAnimationListener { splashScreenViewProvider ->
            onSplashScreenExit(splashScreenViewProvider)
        }
    }

    /**
     * Handles the transition from the splash screen to the application.
     */
    private fun onSplashScreenExit(splashScreenViewProvider: SplashScreenViewProvider) {
        // Get references to the splash screen view and icon view
        val splashScreenView = splashScreenViewProvider.view
        val iconView = splashScreenViewProvider.iconView
        val accelerateInterpolator = FastOutLinearInInterpolator()

        // Change the alpha of the splash screen view
        val alpha = ObjectAnimator.ofFloat(splashScreenView, View.ALPHA, 1f, 0f)
        alpha.duration = 500L
        alpha.interpolator = accelerateInterpolator

        // Translate the icon up
        val translationY = ObjectAnimator.ofFloat(
            iconView,
            View.TRANSLATION_Y,
            iconView.translationY,
            -splashScreenView.height.toFloat()
        )
        translationY.duration = 500L
        translationY.interpolator = accelerateInterpolator

        // Play all of the animation together
        val animatorSet = AnimatorSet()
        animatorSet.playTogether(translationY, alpha)

        // Once the application is finished, we remove the splash screen from our view hierarchy.
        animatorSet.doOnEnd {
            splashScreenViewProvider.remove()
            contentVisible = true
        }

        animatorSet.start()
    }

    override fun onBackPressed() {
        /**
         * For demo purposes, every time the user presses the back button we kill the apps process. This ensures that
         * next time the app is launched, it will be a cold start and the splash screen will be visible.
         * DON'T do this in real apps.
         */
        Process.killProcess(Process.myPid())
    }
}

@Composable
fun Greeting(name: String) {
    Text(text = "Hello $name!")
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    SplashScreenSampleTheme {
        Greeting("Android")
    }
}
