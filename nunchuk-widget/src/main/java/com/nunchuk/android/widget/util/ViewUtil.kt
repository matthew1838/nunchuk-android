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

package com.nunchuk.android.widget.util

import android.content.DialogInterface
import android.text.InputFilter
import android.text.InputType
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.annotation.LayoutRes
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.R
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.nunchuk.android.widget.NCEditTextView
import kotlinx.coroutines.*


fun NCEditTextView.heightExtended(dimensionPixelSize: Int) {
    getEditTextView().heightExtended(dimensionPixelSize)
}

fun NCEditTextView.setMaxLength(maxLength: Int) {
    getEditTextView().filters = arrayOf(InputFilter.LengthFilter(maxLength))
}

fun EditText.setMaxLength(maxLength: Int) {
    filters = arrayOf(InputFilter.LengthFilter(maxLength))
}

fun EditText.heightExtended(dimensionPixelSize: Int) {
    isSingleLine = false
    inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_MULTI_LINE
    layoutParams.height = dimensionPixelSize
}

fun NCEditTextView.addTextChangedCallback(callback: (String) -> Unit) {
    getEditTextView().addTextChangedCallback(callback)
}

fun EditText.addTextChangedCallback(callback: (String) -> Unit) {
    addTextChangedListener(object : TextWatcherAdapter() {
        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
            callback("$s")
        }
    })
}

fun ViewGroup.inflate(@LayoutRes resourceId: Int): View {
    return LayoutInflater
        .from(context)
        .inflate(resourceId, this, false)
}

fun DialogInterface.expandDialog() {
    val bottomSheetDialog = this as BottomSheetDialog
    val designBottomSheet: View? = bottomSheetDialog.findViewById(R.id.design_bottom_sheet)
    designBottomSheet?.let {
        BottomSheetBehavior.from(it).state = BottomSheetBehavior.STATE_EXPANDED
    }
}

fun BottomSheetDialogFragment.addStateChangedCallback(
    onExpanded: () -> Unit = {},
    onCollapsed: () -> Unit = {},
    onHidden: () -> Unit = {}
) {
    (dialog?.findViewById(R.id.design_bottom_sheet) as ViewGroup?)?.let {
        val bottomSheetBehavior = BottomSheetBehavior.from(it)
        bottomSheetBehavior.peekHeight = BottomSheetBehavior.PEEK_HEIGHT_AUTO
        bottomSheetBehavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(view: View, i: Int) {
                if (BottomSheetBehavior.STATE_EXPANDED == i) {
                    onExpanded()
                }
                if (BottomSheetBehavior.STATE_COLLAPSED == i) {
                    onCollapsed()
                }
                if (BottomSheetBehavior.STATE_HIDDEN == i) {
                    dismissAllowingStateLoss()
                    onHidden()
                }
            }

            override fun onSlide(view: View, v: Float) {}
        })
    }

}

fun EditText.setOnEnterOrSpaceListener(callback: () -> Unit) {
    setOnKeyListener(object : View.OnKeyListener {
        override fun onKey(v: View, keyCode: Int, event: KeyEvent): Boolean {
            // only work with hardware keyboard
            if (event.action == KeyEvent.ACTION_DOWN
                && (keyCode == KeyEvent.KEYCODE_ENTER || keyCode == KeyEvent.KEYCODE_SPACE)
            ) {
                callback()
                return true
            }
            return false
        }
    })
}

fun EditText.setOnEnterListener(callback: () -> Unit) {
    setOnKeyListener(object : View.OnKeyListener {
        override fun onKey(v: View, keyCode: Int, event: KeyEvent): Boolean {
            if (event.action == KeyEvent.ACTION_DOWN && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                callback()
                return true
            }
            return false
        }
    })
}

fun RecyclerView.smoothScrollToLastItem() {
    adapter?.itemCount?.let {
        if (it > 0) {
            smoothScrollToPosition(it - 1)
        }
    }
}

const val DELAY = 100L


@Suppress("unused")
fun RecyclerView.isLastItemVisible(): Boolean {
    val adapter = adapter ?: return false
    if (adapter.itemCount != 0) {
        val linearLayoutManager = layoutManager as LinearLayoutManager
        val lastVisibleItemPosition = linearLayoutManager.findLastCompletelyVisibleItemPosition()
        if (lastVisibleItemPosition != RecyclerView.NO_POSITION && lastVisibleItemPosition == adapter.itemCount - 1) return true
    }
    return false
}

fun RecyclerView.isFirstItemVisible(): Boolean {
    val adapter = adapter ?: return false
    if (adapter.itemCount != 0) {
        val linearLayoutManager = layoutManager as LinearLayoutManager
        val visibleItemPosition = linearLayoutManager.findFirstVisibleItemPosition()
        if (visibleItemPosition != RecyclerView.NO_POSITION && visibleItemPosition == 0) return true
    }
    return false
}

fun View.setOnDebounceClickListener(interval: Long = 500L, coroutineScope: CoroutineScope = MainScope(), clickListener: (View) -> Unit) {
    debounceClick(interval, coroutineScope, clickListener)
}

internal fun View.debounceClick(interval: Long = 500L, coroutineScope: CoroutineScope = MainScope(), clicked: (View) -> Unit) {
    setOnClickListener(debounce(interval, coroutineScope) { clicked(it) })
}

private fun <T> debounce(interval: Long = 500L, coroutineScope: CoroutineScope = MainScope(), func: (T) -> Unit): (T) -> Unit {
    var job: Job? = null
    return { param: T ->
        if (job?.isCompleted != false) {
            job = coroutineScope.launch {
                func(param)
                delay(interval)
            }
        }
    }
}

fun NCEditTextView.preventWhitespaceInput() {
    getEditTextView().filters = arrayOf(InputFilter { source, _, _, _, _, _ ->
        source.toString().filterNot { it.isWhitespace() }
    })
}





