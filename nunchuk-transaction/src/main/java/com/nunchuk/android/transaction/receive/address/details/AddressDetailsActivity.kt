package com.nunchuk.android.transaction.receive.address.details

import android.content.Context
import android.os.Bundle
import com.nunchuk.android.core.base.BaseActivity
import com.nunchuk.android.qr.convertToQRCode
import com.nunchuk.android.transaction.databinding.ActivityAddressDetailsBinding
import com.nunchuk.android.widget.util.setLightStatusBar

class AddressDetailsActivity : BaseActivity() {

    private val args: AddressDetailsArgs by lazy { AddressDetailsArgs.deserializeFrom(intent) }

    private lateinit var binding: ActivityAddressDetailsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setLightStatusBar()

        binding = ActivityAddressDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupViews()
    }

    private fun setupViews() {
        binding.qrCode.setImageBitmap(args.address.convertToQRCode())
        binding.address.text = args.address
        binding.balance.text = args.balance
        binding.toolbar.setNavigationOnClickListener { finish() }
        binding.btnClose.setOnClickListener { finish() }
    }

    companion object {
        fun start(activityContext: Context, address: String, balance: String) {
            val intent = AddressDetailsArgs(address = address, balance = balance).buildIntent(activityContext)
            activityContext.startActivity(intent)
        }
    }

}