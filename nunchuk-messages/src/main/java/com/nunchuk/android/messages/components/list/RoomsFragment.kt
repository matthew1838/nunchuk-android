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

package com.nunchuk.android.messages.components.list

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView.VERTICAL
import com.nunchuk.android.core.account.AccountManager
import com.nunchuk.android.core.base.BaseFragment
import com.nunchuk.android.core.util.hideLoading
import com.nunchuk.android.core.util.showLoading
import com.nunchuk.android.messages.R
import com.nunchuk.android.messages.components.list.RoomsEvent.LoadingEvent
import com.nunchuk.android.messages.databinding.FragmentMessagesBinding
import com.nunchuk.android.model.RoomWallet
import com.nunchuk.android.widget.NCWarningDialog
import dagger.hilt.android.AndroidEntryPoint
import org.matrix.android.sdk.api.session.room.model.RoomSummary
import javax.inject.Inject

@AndroidEntryPoint
class RoomsFragment : BaseFragment<FragmentMessagesBinding>() {

    private val viewModel: RoomsViewModel by activityViewModels()

    @Inject
    lateinit var accountManager: AccountManager

    @Inject
    lateinit var roomShareViewPool: RoomShareViewPool

    private lateinit var adapter: RoomAdapter

    private var emptyStateView: View? = null

    override fun initializeBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentMessagesBinding.inflate(inflater, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViews()
        observeEvent()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        emptyStateView = null
    }

    private fun setupViews() {
        adapter = RoomAdapter(accountManager.getAccount().name, ::openRoomDetailScreen, ::handleRemoveRoom)
        binding.recyclerView.setRecycledViewPool(roomShareViewPool.recycledViewPool)
        binding.recyclerView.setHasFixedSize(true)
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext(), VERTICAL, false)
        binding.recyclerView.adapter = adapter
        binding.fab.setOnClickListener {
            navigator.openCreateRoomScreen(requireActivity().supportFragmentManager)
        }
        setEmptyState()
        emptyStateView?.findViewById<View>(R.id.btnAddContacts)?.setOnClickListener {
            navigator.openAddContactsScreen(childFragmentManager, viewModel::listenRoomSummaries)
        }
        emptyStateView?.isVisible = false
    }

    private fun setEmptyState() {
        emptyStateView = binding.viewStubEmptyState.inflate()
        emptyStateView?.findViewById<TextView>(R.id.tvEmptyStateDes)?.text = getString(R.string.nc_message_empty_messages)
        emptyStateView?.findViewById<ImageView>(R.id.ivContactAdd)?.setImageResource(R.drawable.ic_messages_new)
    }

    private fun openRoomDetailScreen(summary: RoomSummary) {
        navigator.openRoomDetailActivity(requireContext(), summary.roomId)
    }

    private fun observeEvent() {
        viewModel.state.observe(viewLifecycleOwner, ::handleState)
        viewModel.event.observe(viewLifecycleOwner, ::handleEvent)
    }

    private fun handleState(state: RoomsState) {
        adapter.roomWallets.apply {
            clear()
            addAll(state.roomWallets.map(RoomWallet::roomId))
        }
        adapter.submitList(state.rooms.filter(RoomSummary::shouldShow))
        emptyStateView?.isVisible = state.rooms.isEmpty()

        hideLoading()
    }

    private fun handleEvent(event: RoomsEvent) {
        when (event) {
            is LoadingEvent -> {
                if (event.loading) {
                    if (viewModel.getVisibleRooms().isNotEmpty()) {
                        showLoading()
                    } else {
                        binding.skeletonContainer.root.isVisible = true
                    }
                } else {
                    binding.skeletonContainer.root.isVisible = false
                    hideLoading()
                }
            }
        }
    }

    private fun handleRemoveRoom(roomSummary: RoomSummary, hasSharedWallet: Boolean) {
        if (hasSharedWallet) {
            NCWarningDialog(requireActivity())
                .showDialog(
                    message = getString(R.string.nc_warning_delete_shared_wallet),
                    onYesClick = {
                        viewModel.removeRoom(roomSummary)
                        deleteRoom(roomSummary)
                    },
                    onNoClick = {
                        val position = viewModel.getVisibleRooms().indexOfFirst { it.roomId == roomSummary.roomId }
                        if (position in 0 until adapter.itemCount) {
                            adapter.notifyItemChanged(position)
                        }
                    }
                )
        } else {
            viewModel.removeRoom(roomSummary)
            deleteRoom(roomSummary)
        }
    }

    private fun deleteRoom(roomSummary: RoomSummary) {
        val newList = viewModel.getVisibleRooms().toMutableList().apply {
            remove(roomSummary)
        }
        adapter.submitList(newList)
    }

    companion object {
        fun newInstance() = RoomsFragment()
    }

}