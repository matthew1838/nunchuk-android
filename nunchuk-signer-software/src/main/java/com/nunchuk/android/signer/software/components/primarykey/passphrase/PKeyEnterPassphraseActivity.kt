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

package com.nunchuk.android.signer.software.components.primarykey.passphrase

import android.content.Context
import android.os.Bundle
import androidx.activity.viewModels
import com.nunchuk.android.core.base.BaseActivity
import com.nunchuk.android.signer.software.R
import com.nunchuk.android.signer.software.databinding.ActivityPkeyEnterPassphraseBinding
import com.nunchuk.android.utils.viewModelProviderFactoryOf
import com.nunchuk.android.widget.NCToastMessage
import com.nunchuk.android.widget.util.addTextChangedCallback
import com.nunchuk.android.widget.util.setLightStatusBar
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class PKeyEnterPassphraseActivity : BaseActivity<ActivityPkeyEnterPassphraseBinding>() {

    @Inject
    internal lateinit var vmFactory: PKeyEnterPassphraseViewModel.Factory

    private val args: PKeyEnterPassphraseArgs by lazy {
        PKeyEnterPassphraseArgs.deserializeFrom(
            intent
        )
    }
    private val viewModel: PKeyEnterPassphraseViewModel by viewModels {
        viewModelProviderFactoryOf {
            vmFactory.create(args)
        }
    }

    override fun initializeBinding() = ActivityPkeyEnterPassphraseBinding.inflate(layoutInflater)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setLightStatusBar()
        setupViews()
        observeEvent()
    }

    private fun setupViews() {
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
        binding.passphrase.makeMaskedInput()
        binding.passphrase.addTextChangedCallback(viewModel::updatePassphrase)
        binding.btnContinue.setOnClickListener {
            viewModel.checkPassphrase()
        }
    }

    private fun observeEvent() {
        viewModel.event.observe(this, ::handleEvent)
    }

    private fun handleEvent(event: PKeyEnterPassphraseEvent) {
        when (event) {
            is PKeyEnterPassphraseEvent.LoadingEvent -> showOrHideLoading(event.loading)
            is PKeyEnterPassphraseEvent.CheckPassphraseSuccess -> {
                navigator.openAddSoftwareSignerNameScreen(
                    this,
                    args.mnemonic,
                    args.primaryKeyFlow,
                    username = event.result.username,
                    passphrase = binding.passphrase.getEditText(),
                    address = event.result.address
                )
            }
            is PKeyEnterPassphraseEvent.CheckPassphraseError -> {
                NCToastMessage(this).showError(message = getString(R.string.nc_primary_key_check_passphrase_not_found))
            }
        }
    }

    companion object {
        fun start(
            activityContext: Context,
            primaryKeyFlow: Int,
            mnemonic: String,
        ) {
            activityContext.startActivity(
                PKeyEnterPassphraseArgs(
                    mnemonic = mnemonic,
                    primaryKeyFlow = primaryKeyFlow,
                ).buildIntent(
                    activityContext
                )
            )
        }
    }
}