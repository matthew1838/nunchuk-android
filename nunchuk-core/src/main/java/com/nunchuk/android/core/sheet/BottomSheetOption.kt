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

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import com.nunchuk.android.core.base.BaseBottomSheet
import com.nunchuk.android.core.databinding.FragmentSheetOptionBinding

class BottomSheetOption : BaseBottomSheet<FragmentSheetOptionBinding>() {
    private lateinit var listener: BottomSheetOptionListener

    override fun onAttach(context: Context) {
        super.onAttach(context)
        listener = if (context is BottomSheetOptionListener) {
            context
        } else if (parentFragment is BottomSheetOptionListener) {
            parentFragment as BottomSheetOptionListener
        } else {
            throw IllegalArgumentException("Activity or parent fragment should implement BottomSheetOptionListener")
        }
    }

    override fun initializeBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentSheetOptionBinding {
        return FragmentSheetOptionBinding.inflate(inflater, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val title = arguments?.getString(EXTRA_TITLE).orEmpty()
        binding.title.text = title
        binding.title.isVisible = title.isNotEmpty()
        val options = arguments?.getParcelableArrayList<SheetOption>(EXTRA_OPTIONS).orEmpty()
        binding.recyclerView.adapter = SheetOptionAdapter(options) {
            listener.onOptionClicked(it)
            dismissAllowingStateLoss()
        }
    }

    companion object {
        private const val EXTRA_TITLE = "extra_title"
        private const val EXTRA_OPTIONS = "extra_options"

        fun newInstance(options: List<SheetOption>, title: String? = null, ): BottomSheetOption {
            return BottomSheetOption().apply {
                arguments = Bundle().apply {
                    putString(EXTRA_TITLE, title)
                    putParcelableArrayList(EXTRA_OPTIONS, ArrayList(options))
                }
            }
        }
    }
}

interface BottomSheetOptionListener {
    fun onOptionClicked(option: SheetOption)
}