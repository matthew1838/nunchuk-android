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

package com.nunchuk.android.messages.components.group.members

import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.nunchuk.android.core.base.BaseViewHolder
import com.nunchuk.android.core.util.shorten
import com.nunchuk.android.messages.R
import com.nunchuk.android.messages.databinding.ItemSuggestContactBinding
import com.nunchuk.android.widget.util.inflate
import org.matrix.android.sdk.api.session.room.model.RoomMemberSummary

internal class GroupMembersAdapter : RecyclerView.Adapter<ContactViewHolder>() {

    internal var items: List<RoomMemberSummary> = ArrayList()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ContactViewHolder(
        parent.inflate(R.layout.item_suggest_contact)
    )

    override fun onBindViewHolder(holder: ContactViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount() = items.size

}

internal class ContactViewHolder(
    itemView: View
) : BaseViewHolder<RoomMemberSummary>(itemView) {

    private val binding = ItemSuggestContactBinding.bind(itemView)

    override fun bind(data: RoomMemberSummary) {
        binding.avatar.text = data.displayName?.shorten().orEmpty()
        binding.name.text = data.displayName.orEmpty()
        binding.email.text = data.userId
        binding.email.isVisible = data.userId.isNotEmpty()
    }

}
