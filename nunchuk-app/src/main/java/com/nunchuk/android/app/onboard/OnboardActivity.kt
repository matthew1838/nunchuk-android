package com.nunchuk.android.app.onboard

import android.os.Bundle
import androidx.compose.ui.platform.ComposeView
import androidx.core.view.WindowCompat
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.nunchuk.android.app.onboard.intro.onboardIntro
import com.nunchuk.android.app.onboard.intro.onboardIntroRoute
import com.nunchuk.android.compose.NunchukTheme
import com.nunchuk.android.core.base.BaseComposeActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class OnboardActivity : BaseComposeActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContentView(
            ComposeView(this).apply {
                setContent {
                    val navController = rememberNavController()
                    NunchukTheme {
                        NavHost(
                            navController = navController,
                            startDestination = onboardIntroRoute
                        ) {
                            onboardIntro()
                        }
                    }
                }
            }
        )
    }
}