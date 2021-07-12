package com.nunchuk.android.transaction.receive.address.unused

sealed class UnusedAddressEvent {
    data class GenerateAddressErrorEvent(val message: String) : UnusedAddressEvent()
}

data class UnusedAddressState(val addresses: List<String> = emptyList())