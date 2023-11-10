package com.nunchuk.android.model.payment

import androidx.annotation.Keep

@Keep
enum class PaymentFrequency {
    DAILY,
    WEEKLY,
    MONTHLY,
    THREE_MONTHLY,
    SIX_MONTHLY,
    YEARLY,
}