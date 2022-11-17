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

package com.nunchuk.android.messages.components.group.action

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.nunchuk.android.arch.args.FragmentArgs
import com.nunchuk.android.core.base.BaseBottomSheet
import com.nunchuk.android.core.util.hideLoading
import com.nunchuk.android.core.util.showLoading
import com.nunchuk.android.messages.R
import com.nunchuk.android.messages.components.create.ContactsAdapter
import com.nunchuk.android.messages.components.create.ReceiptsViewBinder
import com.nunchuk.android.messages.components.group.action.AddMembersEvent.*
import com.nunchuk.android.messages.databinding.BottomSheetAddMembersBinding
import com.nunchuk.android.model.Contact
import com.nunchuk.android.widget.NCToastMessage
import com.nunchuk.android.widget.util.addTextChangedCallback
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AddMembersBottomSheet : BaseBottomSheet<BottomSheetAddMembersBinding>() {

    private val viewModel: AddMembersViewModel by viewModels()

    private val args: AddMembersBottomSheetArgs by lazy { AddMembersBottomSheetArgs.deserializeFrom(arguments) }

    private lateinit var adapter: ContactsAdapter

    override fun initializeBinding(inflater: LayoutInflater, container: ViewGroup?): BottomSheetAddMembersBinding {
        return BottomSheetAddMembersBinding.inflate(inflater, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dialog?.setCanceledOnTouchOutside(false)
        setupViews()
        observeEvent()
        viewModel.initRoom(args.roomId)
    }

    private fun observeEvent() {
        viewModel.state.observe(this, ::handleState)
        viewModel.event.observe(this, ::handleEvent)
    }

    private fun handleState(state: AddMembersState) {
        bindReceiptList(state.receipts)
        bindContactList(state.suggestions)
    }

    private fun bindContactList(suggestions: List<Contact>) {
        adapter.items = suggestions
    }

    private fun bindReceiptList(receipts: List<Contact>) {
        val isEmpty = receipts.isEmpty()
        if (isEmpty) {
            binding.receipts.removeAllViews()
        } else {
            ReceiptsViewBinder(binding.receipts, receipts, viewModel::handleRemove).bindItems()
        }
        binding.doneBtn.isVisible = !isEmpty
        binding.receipts.isVisible = !isEmpty
    }

    private fun handleEvent(event: AddMembersEvent) {
        when (event) {
            NoContactsEvent -> showNoContactsError()
            is AddMembersSuccessEvent -> showMembersAdded()
            is AddMembersError -> showAddMembersError(event.message)
        }
    }

    private fun showMembersAdded() {
        NCToastMessage(requireActivity()).show(getString(R.string.nc_message_new_members_added))
        hideLoading()
        cleanUp()
    }

    private fun showAddMembersError(message: String) {
        hideLoading()
        NCToastMessage(requireActivity()).show(message)
    }

    private fun showNoContactsError() {
        NCToastMessage(requireActivity()).show(getString(R.string.nc_message_empty_contacts))
        cleanUp()
    }

    private fun setupViews() {
        adapter = ContactsAdapter {
            viewModel.handleSelectContact(it)
            binding.input.setText("")
        }

        binding.contactList.layoutManager = LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)
        binding.contactList.adapter = adapter

        binding.closeBtn.setOnClickListener {
            cleanUp()
        }
        binding.input.addTextChangedCallback(viewModel::handleInput)
        binding.doneBtn.setOnClickListener {
            showLoading()
            viewModel.handleDone()
        }
    }

    private fun cleanUp() {
        viewModel.cleanUp()
        binding.input.setText("")
        adapter.items = emptyList()
        binding.receipts.removeAllViews()
        dismiss()
    }

    companion object {

        private const val TAG = "AddMembersBottomSheet"

        private fun newInstance(roomId: String) = AddMembersBottomSheet().apply {
            arguments = AddMembersBottomSheetArgs(roomId).buildBundle()
        }

        fun show(fragmentManager: FragmentManager, roomId: String): AddMembersBottomSheet {
            return newInstance(roomId).apply { show(fragmentManager, TAG) }
        }
    }

}

data class AddMembersBottomSheetArgs(val roomId: String) : FragmentArgs {

    override fun buildBundle() = Bundle().apply {
        putString(EXTRA_ROOM_ID, roomId)
    }

    companion object {
        private const val EXTRA_ROOM_ID = "EXTRA_ROOM_ID"

        fun deserializeFrom(data: Bundle?) = AddMembersBottomSheetArgs(
            data?.getString(EXTRA_ROOM_ID).orEmpty()
        )
    }
}
