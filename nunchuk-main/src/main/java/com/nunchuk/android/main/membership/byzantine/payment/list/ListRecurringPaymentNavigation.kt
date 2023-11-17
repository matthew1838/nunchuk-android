package com.nunchuk.android.main.membership.byzantine.payment.list

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument

const val recurringPaymentRoute = "recurring_payment/{groupId}/{walletId}"

fun NavGraphBuilder.recurringPaymentsList(
    onOpenAddRecurringPayment: () -> Unit,
    groupId: String,
    walletId: String,
) {
    composable(
        route = recurringPaymentRoute,
        arguments = listOf(
            navArgument("groupId") {
                type = NavType.StringType
                defaultValue = groupId
            },
            navArgument("walletId") {
                type = NavType.StringType
                defaultValue = walletId
            },
        )
    ) {
        ListRecurringPaymentRoute(
            onOpenAddRecurringPayment = onOpenAddRecurringPayment,
        )
    }
}

fun NavController.navigateToListRecurringPayment(
    navOptions: NavOptions? = null,
    groupId: String,
    walletId: String,
) {
    navigate("recurring_payment/${groupId}/${walletId}", navOptions)
}
