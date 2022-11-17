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

package com.nunchuk.android.core.qr

import android.content.Context
import android.graphics.Bitmap
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.nunchuk.android.core.databinding.ActivityDynamicQrBinding
import com.nunchuk.android.widget.util.setLightStatusBar

class DynamicQRCodeActivity : AppCompatActivity() {

    private val args: DynamicQRCodeArgs by lazy { DynamicQRCodeArgs.deserializeFrom(intent) }

    private lateinit var bitmaps: List<Bitmap>

    private lateinit var binding: ActivityDynamicQrBinding

    private var index = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setLightStatusBar()

        binding = ActivityDynamicQrBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupViews()
    }

    private val updateTextTask = object : Runnable {
        override fun run() {
            handler.postDelayed(this, INTERVAL)
            bindQrCodes()
        }
    }

    private fun setupViews() {
        bitmaps = args.values.mapNotNull(String::convertToQRCode)
        binding.btnClose.setOnClickListener { finish() }
        handler.post(updateTextTask)

        binding.toolbar.setNavigationOnClickListener { finish() }
    }

    private fun bindQrCodes() {
        calculateIndex()
        binding.qrCode.setImageBitmap(bitmaps[index])
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(updateTextTask)
    }

    private fun calculateIndex() {
        index++
        if (index >= bitmaps.size) {
            index = 0
        }
    }

    companion object {
        const val INTERVAL = 400L

        private var handler = Handler(Looper.getMainLooper())

        fun start(activityContext: Context, values: List<String>) {
            activityContext.startActivity(DynamicQRCodeArgs(values).buildIntent(activityContext))
        }
    }

}

