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

package com.nunchuk.android.signer.nfc.recover

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.navigation.fragment.findNavController
import com.nunchuk.android.core.base.BaseFragment
import com.nunchuk.android.signer.R
import com.nunchuk.android.signer.databinding.FragmentNfcKeyRecoverInfoBinding
import com.nunchuk.android.signer.nfc.decryption.NfcDecryptionKeyFragment

class RecoverNfcKeyGuideFragment : BaseFragment<FragmentNfcKeyRecoverInfoBinding>() {
    private val openDocument = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri ?: return@registerForActivityResult
        findNavController().navigate(R.id.nfcDecryptionKeyFragment, NfcDecryptionKeyFragment.buildArguments(uri))
    }

    override fun initializeBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentNfcKeyRecoverInfoBinding {
        return FragmentNfcKeyRecoverInfoBinding.inflate(inflater, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.btnContinue.setOnClickListener {
            openDocument.launch("application/*")
        }
        binding.toolbar.setNavigationOnClickListener {
            activity?.onBackPressed()
        }
    }
}