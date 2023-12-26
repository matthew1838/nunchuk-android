package com.nunchuk.android.core.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ClaimInheritanceTxParam(
    val masterSignerIds: List<String> = arrayListOf(),
    val magicalPhrase: String = "",
    val derivationPaths: List<String> = arrayListOf(),
) : Parcelable {
    companion object {
        fun empty() = ClaimInheritanceTxParam()
    }
}

fun ClaimInheritanceTxParam?.isInheritanceClaimFlow() : Boolean {
    if (this == null) return false
    return masterSignerIds.isNotEmpty() && magicalPhrase.isNotEmpty() && derivationPaths.isNotEmpty()
}