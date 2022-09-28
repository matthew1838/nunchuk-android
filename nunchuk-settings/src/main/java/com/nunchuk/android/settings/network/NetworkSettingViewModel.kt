package com.nunchuk.android.settings.network

import androidx.lifecycle.viewModelScope
import com.nunchuk.android.arch.vm.NunchukViewModel
import com.nunchuk.android.core.account.AccountManager
import com.nunchuk.android.core.domain.GetAppSettingUseCase
import com.nunchuk.android.core.domain.InitAppSettingsUseCase
import com.nunchuk.android.core.domain.UpdateAppSettingUseCase
import com.nunchuk.android.core.matrix.SessionHolder
import com.nunchuk.android.core.profile.UserProfileRepository
import com.nunchuk.android.domain.di.IoDispatcher
import com.nunchuk.android.model.AppSettings
import com.nunchuk.android.utils.onException
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject

@HiltViewModel
internal class NetworkSettingViewModel @Inject constructor(
    private val initAppSettingsUseCase: InitAppSettingsUseCase,
    private val updateAppSettingUseCase: UpdateAppSettingUseCase,
    private val getAppSettingUseCase: GetAppSettingUseCase,
    private val accountManager: AccountManager,
    private val repository: UserProfileRepository,
    private val sessionHolder: SessionHolder,
    private val appScope: CoroutineScope,
    @IoDispatcher private val dispatcher: CoroutineDispatcher
) : NunchukViewModel<NetworkSettingState, NetworkSettingEvent>() {

    override val initialState = NetworkSettingState()

    val currentAppSettings: AppSettings?
        get() = state.value?.appSetting

    var initAppSettings: AppSettings? = null

    fun updateCurrentState(appSettings: AppSettings) {
        updateState {
            copy(appSetting = appSettings)
        }
    }

    fun fireResetTextHostServerEvent(appSettings: AppSettings) {
        event(
            NetworkSettingEvent.ResetTextHostServerEvent(appSettings)
        )
    }

    fun getAppSettings() {
        viewModelScope.launch {
            getAppSettingUseCase.execute()
                .flowOn(Dispatchers.IO)
                .onException { }
                .flowOn(Dispatchers.Main)
                .collect {
                    initAppSettings = it
                    updateState {
                        copy(appSetting = it)
                    }
                    event(
                        NetworkSettingEvent.ResetTextHostServerEvent(it)
                    )
                }
        }
    }

    fun updateAppSettings(appSettings: AppSettings) {
        viewModelScope.launch {
            updateAppSettingUseCase.execute(appSettings)
                .flowOn(Dispatchers.IO)
                .onException { }
                .flowOn(Dispatchers.Main)
                .collect {
                    initAppSettings = it
                    updateState {
                        copy(appSetting = it)
                    }
                    event(NetworkSettingEvent.UpdateSettingSuccessEvent(it))
                }
        }
    }

    fun resetToDefaultAppSetting() {
        viewModelScope.launch {
            initAppSettingsUseCase.execute()
                .flowOn(Dispatchers.IO)
                .onException { }
                .flowOn(Dispatchers.Main)
                .collect {
                    initAppSettings = it
                    updateState {
                        copy(appSetting = it)
                    }
                    event(NetworkSettingEvent.UpdateSettingSuccessEvent(it))
                }
        }
    }

    fun signOut() {
        viewModelScope.launch {
            repository.signOut()
                .flowOn(Dispatchers.IO)
                .onException { }
                .collect {}
        }
        appScope.launch {
            event(NetworkSettingEvent.LoadingEvent(true))
            withContext(dispatcher) {
                sessionHolder.clearActiveSession()
                accountManager.signOut()
            }
            event(NetworkSettingEvent.SignOutSuccessEvent)
        }
    }
}