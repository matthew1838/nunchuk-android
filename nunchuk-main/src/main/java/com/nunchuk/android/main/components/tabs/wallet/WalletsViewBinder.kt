/**************************************************************************
 * This file is part of the Nunchuk software (https://nunchuk.io/)        *							          *
 * Copyright (C) 2022 Nunchuk								              *
 *                                                                        *
 * This program is free software; you can redistribute it and/or          *
 * modify it under the terms of the GNU General Public License            *
 * as published by the Free Software Foundation; either version 3         *
 * of the License, or (at your option) any later version.                 *
 *                                                                        *
 * This program is distributed in the hope that it will be useful,        *
 * but WITHOUT ANY WARRANTY; without even the implied warranty of         *
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the          *
 * GNU General Public License for more details.                           *
 *                                                                        *
 * You should have received a copy of the GNU General Public License      *
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.  *
 *                                                                        *
 **************************************************************************/

package com.nunchuk.android.main.components.tabs.wallet

import android.view.ViewGroup
import androidx.core.view.get
import androidx.core.view.isVisible
import com.nunchuk.android.core.databinding.ItemWalletBinding
import com.nunchuk.android.core.util.getBTCAmount
import com.nunchuk.android.core.util.getUSDAmount
import com.nunchuk.android.model.WalletExtended
import com.nunchuk.android.share.wallet.bindWalletConfiguration
import com.nunchuk.android.widget.util.AbsViewBinder

internal class WalletsViewBinder(
    container: ViewGroup,
    wallets: List<WalletExtended>,
    val callback: (String) -> Unit = {}
) : AbsViewBinder<WalletExtended, ItemWalletBinding>(container, wallets) {

    override fun initializeBinding() = ItemWalletBinding.inflate(inflater, container, false)

    override fun bindItem(position: Int, model: WalletExtended) {
        val wallet = model.wallet
        val balance = "(${wallet.getUSDAmount()})"
        val binding = ItemWalletBinding.bind(container[position])
        binding.walletName.text = wallet.name
        binding.btc.text = wallet.getBTCAmount()
        binding.balance.text = balance
        binding.shareIcon.isVisible = model.isShared
        binding.config.bindWalletConfiguration(wallet)
        binding.root.setOnClickListener { callback(wallet.id) }
    }
}