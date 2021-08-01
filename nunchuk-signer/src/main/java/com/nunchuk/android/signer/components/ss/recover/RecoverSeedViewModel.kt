package com.nunchuk.android.signer.components.ss.recover

import androidx.lifecycle.viewModelScope
import com.nunchuk.android.arch.vm.NunchukViewModel
import com.nunchuk.android.core.util.countWords
import com.nunchuk.android.core.util.lastWord
import com.nunchuk.android.core.util.replaceLastWord
import com.nunchuk.android.model.Result.Error
import com.nunchuk.android.model.Result.Success
import com.nunchuk.android.signer.components.ss.recover.RecoverSeedEvent.*
import com.nunchuk.android.usecase.CheckMnemonicUseCase
import com.nunchuk.android.usecase.GetBip39WordListUseCase
import kotlinx.coroutines.launch
import javax.inject.Inject

internal class RecoverSeedViewModel @Inject constructor(
    private val getBip39WordListUseCase: GetBip39WordListUseCase,
    private val checkMnemonicUseCase: CheckMnemonicUseCase
) : NunchukViewModel<RecoverSeedState, RecoverSeedEvent>() {

    private var bip39Words = ArrayList<String>()
    override val initialState = RecoverSeedState()

    init {
        viewModelScope.launch {
            val result = getBip39WordListUseCase.execute()
            if (result is Success) {
                bip39Words = ArrayList(result.data)
            }
        }
    }

    fun handleInputEvent(mnemonic: String) {
        if (mnemonic != getState().mnemonic) {
            updateState { copy(mnemonic = mnemonic) }
            val word = mnemonic.lastWord()
            if (word.isNotEmpty()) {
                filter(word)
            }
        }
        event(CanGoNextStepEvent(mnemonic.countWords() == NUM_WORDS))
    }

    private fun filter(word: String) {
        updateState { copy(suggestions = bip39Words.filter { it.startsWith(word) }.take(MAX_WORDS)) }
    }

    fun handleContinueEvent() {
        val mnemonic = getState().mnemonic
        if (mnemonic.isEmpty()) {
            event(MnemonicRequiredEvent)
        } else {
            checkMnemonic(mnemonic)
        }
    }

    fun handleSelectWord(word: String) {
        updateState { copy(suggestions = ArrayList()) }
        val updatedMnemonic = getState().mnemonic.replaceLastWord(word)
        updateState { copy(mnemonic = updatedMnemonic) }
        event(UpdateMnemonicEvent(updatedMnemonic))
    }

    private fun checkMnemonic(mnemonic: String) {
        viewModelScope.launch {
            when (checkMnemonicUseCase.execute(mnemonic)) {
                is Error -> event(InvalidMnemonicEvent)
                is Success -> event(ValidMnemonicEvent(mnemonic))
            }
        }
    }

    companion object {
        private const val MAX_WORDS = 10
        private const val NUM_WORDS = 24
    }

}