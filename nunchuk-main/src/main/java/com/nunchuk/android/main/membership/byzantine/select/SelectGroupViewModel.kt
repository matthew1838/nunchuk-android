package com.nunchuk.android.main.membership.byzantine.select

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nunchuk.android.core.domain.membership.SetLocalMembershipPlanFlowUseCase
import com.nunchuk.android.model.isPersonalPlan
import com.nunchuk.android.model.toMembershipPlan
import com.nunchuk.android.model.wallet.WalletOption
import com.nunchuk.android.usecase.membership.GetGroupAssistedWalletConfigUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SelectGroupViewModel @Inject constructor(
    private val getGroupAssistedWalletConfigUseCase: GetGroupAssistedWalletConfigUseCase,
    private val setLocalMembershipPlanFlowUseCase: SetLocalMembershipPlanFlowUseCase,
    private val applicationScope: CoroutineScope
) : ViewModel() {
    private val _state = MutableStateFlow(SelectGroupUiState())
    val state = _state.asStateFlow()

    private val _event = MutableSharedFlow<SelectGroupEvent>()
    val event = _event.asSharedFlow()

    init {
        viewModelScope.launch {
            _event.emit(SelectGroupEvent.Loading(true))
            getGroupAssistedWalletConfigUseCase(Unit)
                .onSuccess { config ->
                    _state.update {
                        it.copy(
                            walletsCount = config.walletsCount,
                            groupOptions = config.groupOptions,
                            personalOptions = config.personalOptions,
                            isLoaded = true
                        )
                    }
                }
            _event.emit(SelectGroupEvent.Loading(false))
        }
    }


    fun setLocalMembershipPlan(slug: String) {
        applicationScope.launch {
            val plan = slug.toMembershipPlan()
            if (plan.isPersonalPlan()) {
                setLocalMembershipPlanFlowUseCase(slug.toMembershipPlan())
            }
        }
    }

    fun checkGroupTypeAvailable(slug: String): Boolean = (state.value.walletsCount[slug] ?: 0) > 0
}

sealed class SelectGroupEvent {
    data class Loading(val isLoading: Boolean) : SelectGroupEvent()
}

data class SelectGroupUiState(
    val groupOptions: List<WalletOption> = emptyList(),
    val personalOptions: List<WalletOption> = emptyList(),
    val isLoaded: Boolean = false,
    val walletsCount: Map<String, Int> = emptyMap(),
)