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

package com.nunchuk.android.messages.util

import org.matrix.android.sdk.api.session.events.model.EventType
import org.matrix.android.sdk.api.session.events.model.isTextMessage
import org.matrix.android.sdk.api.session.room.timeline.TimelineEvent

// Naming follow Matrix's convention
const val STATE_NUNCHUK_WALLET = "io.nunchuk.wallet"
const val STATE_NUNCHUK_TRANSACTION = "io.nunchuk.transaction"
const val STATE_NUNCHUK_ERROR = "io.nunchuk.error"
const val STATE_NUNCHUK_SYNC = "io.nunchuk.sync"
const val STATE_NUNCHUK_CONTACT_REQUEST = "io.nunchuk.custom.contact_request"
const val STATE_NUNCHUK_CONTACT_INVITATION_ACCEPTED = "io.nunchuk.custom.invitation_accepted"
const val STATE_NUNCHUK_CONTACT_REQUEST_ACCEPTED = "io.nunchuk.custom.contact_request_accepted"
const val STATE_NUNCHUK_CONTACT_WITHDRAW_INVITATION = "io.nunchuk.custom.withdraw_invitation"
const val STATE_ROOM_SERVER_NOTICE = "m.server_notice"
const val STATE_ENCRYPTED_MESSAGE = "*Encrypted*"

fun TimelineEvent.isDisplayable() = isMessageEvent() || isEncryptedEvent() || isNotificationEvent() || isNunchukEvent()

fun TimelineEvent.isNotificationEvent() = isRoomMemberEvent() || isRoomCreateEvent() || isRoomNameEvent()

fun TimelineEvent.isRoomCreateEvent() = root.getClearType() == EventType.STATE_ROOM_CREATE

fun TimelineEvent.isRoomMemberEvent() = root.getClearType() == EventType.STATE_ROOM_MEMBER

fun TimelineEvent.isEncryptedEvent() = root.getClearType() == EventType.ENCRYPTED

fun TimelineEvent.isRoomNameEvent() = root.getClearType() == EventType.STATE_ROOM_NAME

fun TimelineEvent.isMessageEvent() = root.isTextMessage()

fun TimelineEvent.isNunchukEvent() = isNunchukWalletEvent() || isNunchukTransactionEvent() || isNunchukErrorEvent()

fun TimelineEvent.isNunchukWalletEvent() = root.getClearType() == STATE_NUNCHUK_WALLET

fun TimelineEvent.isNunchukTransactionEvent() = root.getClearType() == STATE_NUNCHUK_TRANSACTION

fun TimelineEvent.isNunchukConsumeSyncEvent() = root.getClearType() == STATE_NUNCHUK_SYNC

fun TimelineEvent.isNunchukErrorEvent() = root.getClearType() == STATE_NUNCHUK_ERROR

fun TimelineEvent.isContactUpdateEvent() = isContactRequestEvent() || isContactWithdrawInvitationEvent() || isContactRequestAcceptedEvent() || isContactInvitationAcceptedEvent()

fun TimelineEvent.isContactRequestEvent() = getMsgType() == STATE_NUNCHUK_CONTACT_REQUEST

fun TimelineEvent.isContactWithdrawInvitationEvent() = getMsgType() == STATE_NUNCHUK_CONTACT_WITHDRAW_INVITATION

fun TimelineEvent.isContactRequestAcceptedEvent() = getMsgType() == STATE_NUNCHUK_CONTACT_REQUEST_ACCEPTED

fun TimelineEvent.isContactInvitationAcceptedEvent() = getMsgType() == STATE_NUNCHUK_CONTACT_INVITATION_ACCEPTED

private fun TimelineEvent.getMsgType() = root.getClearContent()?.get("msgtype")
