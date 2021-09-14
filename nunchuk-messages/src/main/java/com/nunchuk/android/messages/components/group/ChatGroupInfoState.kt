package com.nunchuk.android.messages.components.group

import com.nunchuk.android.model.RoomWallet
import org.matrix.android.sdk.api.session.room.model.RoomMemberSummary
import org.matrix.android.sdk.api.session.room.model.RoomSummary

data class ChatGroupInfoState(
    val summary: RoomSummary? = null,
    val roomMembers: List<RoomMemberSummary> = emptyList(),
    val roomWallet: RoomWallet? = null
)

sealed class ChatGroupInfoEvent {
    object RoomNotFoundEvent : ChatGroupInfoEvent()
    data class UpdateRoomNameError(val message: String) : ChatGroupInfoEvent()
    data class UpdateRoomNameSuccess(val name: String) : ChatGroupInfoEvent()
    object LeaveRoomSuccess : ChatGroupInfoEvent()
    data class LeaveRoomError(val message: String) : ChatGroupInfoEvent()
}