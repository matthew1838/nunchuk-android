package com.nunchuk.android.wallet.util

import android.content.Context
import com.nunchuk.android.type.AddressType
import com.nunchuk.android.type.AddressType.*
import com.nunchuk.android.type.WalletType
import com.nunchuk.android.type.WalletType.ESCROW
import com.nunchuk.android.type.WalletType.MULTI_SIG
import com.nunchuk.android.wallet.core.R

fun WalletType.toReadableString(context: Context) = when (this) {
    ESCROW -> context.getString(R.string.nc_wallet_escrow_wallet)
    MULTI_SIG -> context.getString(R.string.nc_wallet_standard_wallet)
    else -> throw UnsupportedWalletTypeException(name)
}

fun AddressType.toReadableString(context: Context) = when (this) {
    NATIVE_SEGWIT -> context.getString(R.string.nc_wallet_native_segwit_wallet)
    NESTED_SEGWIT -> context.getString(R.string.nc_wallet_nested_segwit_wallet)
    LEGACY -> context.getString(R.string.nc_wallet_legacy_wallet)
    else -> throw UnsupportedAddressTypeException(name)
}

internal class UnsupportedWalletTypeException(message: String) : Exception(message)

internal class UnsupportedAddressTypeException(message: String) : Exception(message)

fun String.isWalletExisted() = this.lowercase().startsWith(WALLET_EXISTED)

internal const val WALLET_EXISTED = "wallet existed"

