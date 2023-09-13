package com.nunchuk.android.model.transaction

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class AlertPayload(
    @SerializedName("master_name")
    val masterName: String,
    @SerializedName("pending_keys_count")
    val pendingKeysCount: Int,
    @SerializedName("dummy_transaction_id")
    val dummyTransactionId: String,
    @SerializedName("xfps")
    val xfps: List<String>,
    @SerializedName("claim_key")
    val claimKey: Boolean
): Parcelable