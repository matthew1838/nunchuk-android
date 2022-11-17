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

package com.nunchuk.android.core.sheet

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.nunchuk.android.core.R
import com.nunchuk.android.core.databinding.ItemSheetOptionBinding
import com.nunchuk.android.widget.util.setOnDebounceClickListener

class SheetOptionAdapter(
    private val options: List<SheetOption>,
    private val onItemClicked: (option: SheetOption) -> Unit
) : RecyclerView.Adapter<SheetOptionHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SheetOptionHolder {
        return SheetOptionHolder(
            ItemSheetOptionBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        ).apply {
            itemView.setOnDebounceClickListener {
                onItemClicked(options[this.bindingAdapterPosition])
            }
        }
    }

    override fun onBindViewHolder(holder: SheetOptionHolder, position: Int) {
        holder.bind(options[position])
    }

    override fun getItemCount(): Int = options.size

}

class SheetOptionHolder(private val binding: ItemSheetOptionBinding) : RecyclerView.ViewHolder(binding.root) {
    fun bind(option: SheetOption) {
        if (option.resId > 0) {
            binding.tvLabel.setCompoundDrawablesRelativeWithIntrinsicBounds(option.resId, 0, 0, 0)
        }
        option.label?.let {
            binding.tvLabel.text = it
        } ?: run {
            binding.tvLabel.setText(option.stringId)
        }
        if (option.isDeleted) {
            binding.tvLabel.setTextColor(ContextCompat.getColor(binding.root.context, R.color.nc_orange_color))
        } else {
            binding.tvLabel.setTextColor(ContextCompat.getColor(binding.root.context, R.color.nc_black_color))
        }
    }
}