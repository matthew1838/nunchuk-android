package com.nunchuk.android.main.membership.byzantine.recurringpayment

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.core.view.WindowCompat
import androidx.navigation.fragment.NavHostFragment
import com.nunchuk.android.core.base.BaseActivity
import com.nunchuk.android.main.R
import com.nunchuk.android.widget.databinding.ActivityNavigationBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class RecurringPaymentActivity : BaseActivity<ActivityNavigationBinding>() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, false)

        val navHostFragment =
            (supportFragmentManager.findFragmentById(R.id.nav_host) as NavHostFragment)
        navHostFragment.navController.setGraph(
            R.navigation.recurring_payment_navigation,
            intent.extras
        )

    }

    override fun initializeBinding(): ActivityNavigationBinding {
        return ActivityNavigationBinding.inflate(layoutInflater)
    }

    companion object {
        private const val GROUP_ID = "group_id"
        private const val WALLET_ID = "wallet_id"

        fun navigate(
            activity: Context,
            groupId: String,
            walletId: String?,
        ) {
            val intent = buildIntent(activity, groupId, walletId)
            activity.startActivity(intent)
        }

        fun buildIntent(
            activity: Context,
            groupId: String,
            walletId: String?,
        ): Intent {
            return Intent(activity, RecurringPaymentActivity::class.java)
                .putExtra(GROUP_ID, groupId)
                .putExtra(WALLET_ID, walletId)
        }
    }
}