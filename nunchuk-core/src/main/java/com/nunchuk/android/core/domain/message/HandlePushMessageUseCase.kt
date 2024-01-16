package com.nunchuk.android.core.domain.message

import com.nunchuk.android.core.push.PushEvent
import com.nunchuk.android.core.push.PushEventManager
import com.nunchuk.android.domain.di.IoDispatcher
import com.nunchuk.android.messages.util.getGroupId
import com.nunchuk.android.messages.util.getLastMessageContentSafe
import com.nunchuk.android.messages.util.getTransactionId
import com.nunchuk.android.messages.util.getWalletId
import com.nunchuk.android.messages.util.isAddKeyCompleted
import com.nunchuk.android.messages.util.isDraftWalletResetEvent
import com.nunchuk.android.messages.util.isGroupEmergencyLockdownStarted
import com.nunchuk.android.messages.util.isGroupMembershipRequestCreatedEvent
import com.nunchuk.android.messages.util.isGroupNameChanged
import com.nunchuk.android.messages.util.isGroupWalletCreatedEvent
import com.nunchuk.android.messages.util.isGroupWalletPrimaryOwnerUpdated
import com.nunchuk.android.messages.util.isKeyNameChanged
import com.nunchuk.android.messages.util.isServerTransactionEvent
import com.nunchuk.android.messages.util.isTransactionCancelled
import com.nunchuk.android.messages.util.isTransactionHandleErrorMessageEvent
import com.nunchuk.android.messages.util.isWalletCreated
import com.nunchuk.android.usecase.IsHandledEventUseCase
import com.nunchuk.android.usecase.SaveHandledEventUseCase
import com.nunchuk.android.usecase.UseCase
import com.nunchuk.android.usecase.byzantine.SyncGroupWalletUseCase
import com.nunchuk.android.usecase.wallet.GetServerWalletUseCase
import kotlinx.coroutines.CoroutineDispatcher
import org.matrix.android.sdk.api.session.room.timeline.TimelineEvent
import javax.inject.Inject

class HandlePushMessageUseCase @Inject constructor(
    @IoDispatcher dispatcher: CoroutineDispatcher,
    private val pushEventManager: PushEventManager,
    private val isHandledEventUseCase: IsHandledEventUseCase,
    private val saveHandledEventUseCase: SaveHandledEventUseCase,
    private val syncGroupWalletUseCase: SyncGroupWalletUseCase,
    private val getServerWalletUseCase: GetServerWalletUseCase,
) : UseCase<TimelineEvent, Unit>(dispatcher) {
    override suspend fun execute(parameters: TimelineEvent) {
        when {
            parameters.isTransactionHandleErrorMessageEvent() || parameters.isServerTransactionEvent() -> {
                val result = isHandledEventUseCase.invoke(parameters.eventId)
                if (result.getOrDefault(false).not()) {
                    saveHandledEventUseCase.invoke(parameters.eventId)
                    if (parameters.isTransactionHandleErrorMessageEvent()) {
                        pushEventManager.push(
                            PushEvent.MessageEvent(
                                parameters.getLastMessageContentSafe().orEmpty()
                            )
                        )
                    }
                    pushEventManager.push(
                        PushEvent.ServerTransactionEvent(
                            parameters.getWalletId().orEmpty(),
                            parameters.getTransactionId().orEmpty()
                        )
                    )
                }
            }
            parameters.isAddKeyCompleted() -> {
                val result = isHandledEventUseCase.invoke(parameters.eventId)
                if (result.getOrDefault(false).not()) {
                    saveHandledEventUseCase.invoke(parameters.eventId)
                    pushEventManager.push(PushEvent.AddDesktopKeyCompleted)
                }
            }
            parameters.isWalletCreated() -> {
                val result = isHandledEventUseCase.invoke(parameters.eventId)
                if (result.getOrDefault(false).not()) {
                    saveHandledEventUseCase.invoke(parameters.eventId)
                    pushEventManager.push(PushEvent.WalletCreate(parameters.getWalletId().orEmpty()))
                }
            }
            parameters.isTransactionCancelled() -> {
                val result = isHandledEventUseCase.invoke(parameters.eventId)
                if (result.getOrDefault(false).not()) {
                    saveHandledEventUseCase.invoke(parameters.eventId)
                    pushEventManager.push(
                        PushEvent.TransactionCancelled(
                            parameters.getWalletId().orEmpty(), parameters.getTransactionId().orEmpty()
                        )
                    )
                }
            }
            parameters.isDraftWalletResetEvent() -> {
                val result = isHandledEventUseCase.invoke(parameters.eventId)
                if (result.getOrDefault(false).not()) {
                    saveHandledEventUseCase.invoke(parameters.eventId)
                    pushEventManager.push(
                        PushEvent.DraftResetWallet(
                            parameters.getWalletId().orEmpty()
                        )
                    )
                }
            }
            parameters.isGroupMembershipRequestCreatedEvent() -> {
                val result = isHandledEventUseCase.invoke(parameters.eventId)
                if (result.getOrDefault(false).not()) {
                    saveHandledEventUseCase.invoke(parameters.eventId)
                    pushEventManager.push(
                        PushEvent.GroupMembershipRequestCreated(
                            parameters.getGroupId().orEmpty()
                        )
                    )
                }
            }
            parameters.isGroupWalletCreatedEvent() -> {
                val result = isHandledEventUseCase.invoke(parameters.eventId)
                if (result.getOrDefault(false).not()) {
                    saveHandledEventUseCase.invoke(parameters.eventId)
                    pushEventManager.push(
                        PushEvent.GroupWalletCreated(
                            parameters.getWalletId().orEmpty()
                        )
                    )
                }
            }
            parameters.isGroupEmergencyLockdownStarted() -> {
                val result = isHandledEventUseCase.invoke(parameters.eventId)
                if (result.getOrDefault(false).not()) {
                    saveHandledEventUseCase.invoke(parameters.eventId)
                    pushEventManager.push(
                        PushEvent.GroupEmergencyLockdownStarted(
                            parameters.getWalletId().orEmpty()
                        )
                    )
                }
            }
            parameters.isGroupNameChanged() || parameters.isKeyNameChanged() -> {
                val result = isHandledEventUseCase.invoke(parameters.eventId)
                if (result.getOrDefault(false).not()) {
                    saveHandledEventUseCase.invoke(parameters.eventId)
                    val groupId = parameters.getGroupId().orEmpty()
                    val walletId = parameters.getWalletId().orEmpty()
                    if (groupId.isNotEmpty()) {
                        syncGroupWalletUseCase(groupId)
                        pushEventManager.push(PushEvent.WalletChanged(walletId))
                    } else {
                        getServerWalletUseCase(walletId)
                        pushEventManager.push(PushEvent.WalletChanged(walletId))
                    }
                }
            }
            parameters.isGroupWalletPrimaryOwnerUpdated()) {
                val result = isHandledEventUseCase.invoke(parameters.eventId)
                if (result.getOrDefault(false).not()) {
                    saveHandledEventUseCase.invoke(parameters.eventId)
                    pushEventManager.push(
                        PushEvent.PrimaryOwnerUpdated(
                            parameters.getWalletId().orEmpty()
                        )
                    )
                }
            }
        }
    }
}