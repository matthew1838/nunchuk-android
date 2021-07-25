package com.nunchuk.android.main.components.tabs.wallet

import android.view.ViewGroup
import androidx.core.view.get
import com.nunchuk.android.core.signer.SignerModel
import com.nunchuk.android.main.R
import com.nunchuk.android.signer.databinding.ItemSignerBinding
import com.nunchuk.android.widget.util.AbsViewBinder

internal class SignersViewBinder(
    container: ViewGroup,
    signers: List<SignerModel>,
    val callback: (SignerModel) -> Unit = {}
) : AbsViewBinder<SignerModel, ItemSignerBinding>(container, signers) {

    override fun initializeBinding() = ItemSignerBinding.inflate(inflater, container, false)

    override fun bindItem(position: Int, model: SignerModel) {
        val binding = ItemSignerBinding.bind(container[position])
        val xfpValue = "XFP: ${model.fingerPrint}"
        val signerType = if (model.software) context.getString(R.string.nc_signer_type_software) else context.getString(R.string.nc_signer_type_air_gapped)

        binding.signerName.text = model.name
        binding.xpf.text = xfpValue
        binding.signerType.text = signerType

        binding.root.setOnClickListener { callback(model) }
    }

}