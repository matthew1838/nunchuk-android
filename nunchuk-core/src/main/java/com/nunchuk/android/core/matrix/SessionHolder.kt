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

package com.nunchuk.android.core.matrix

import androidx.lifecycle.ProcessLifecycleOwner
import androidx.lifecycle.asFlow
import com.nunchuk.android.core.util.isAtLeastStarted
import com.nunchuk.android.log.fileLog
import com.nunchuk.android.utils.CrashlyticsReporter
import org.matrix.android.sdk.api.session.Session
import org.matrix.android.sdk.api.session.getRoom
import org.matrix.android.sdk.api.session.room.model.Membership
import org.matrix.android.sdk.api.session.room.roomSummaryQueryParams
import java.util.concurrent.atomic.AtomicReference
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SessionHolder @Inject constructor(
    private val sessionListener: SessionListener
) {
    private var activeSessionReference: AtomicReference<Session?> = AtomicReference()

    private var currentRoomId: String? = null
    private var isLeaveRoom: Boolean = false

    // isOpen state is hidden inside matrix sdk, there is no way to know exactly variable value
    fun storeActiveSession(session: Session) {
        fileLog(message = "storeActiveSession of ${session.myUserId}")
        getSafeActiveSession()?.apply {
            removeListener(sessionListener)
        }
        session.apply {
            activeSessionReference.set(this)
            addListener(sessionListener)
            cryptoService().setWarnOnUnknownDevices(false)
            try {
                open()
                if (!syncService().hasAlreadySynced()) {
                    syncService().startSync(true)
                } else {
                    syncService().startSync(ProcessLifecycleOwner.get().isAtLeastStarted())
                }
                pushersService().refreshPushers()
            } catch (e: Exception) {
                CrashlyticsReporter.recordException(e)
            }
        }
    }

    fun clearActiveRoom() {
        currentRoomId = null
    }

    fun setActiveRoom(roomId: String, isLeaveRoom: Boolean) {
        this.currentRoomId = roomId
        this.isLeaveRoom = isLeaveRoom
    }

    fun clearActiveSession() {
        try {
            getSafeActiveSession()?.apply {
                removeListener(sessionListener)
                close()
            }
        } catch (e: Error) {
            CrashlyticsReporter.recordException(e)
        }
        activeSessionReference.set(null)
        currentRoomId = null
    }

    fun getSafeActiveSession(): Session? {
        return activeSessionReference.get()
    }

    fun hasActiveSession() = activeSessionReference.get() != null

    fun hasActiveRoom() = currentRoomId != null

    fun getActiveRoomId() = currentRoomId.orEmpty()

    fun getActiveRoomIdSafe() = currentRoomId.orEmpty()

    fun isLeaveRoom() : Boolean = isLeaveRoom

    fun getCurrentRoom() = activeSessionReference.get()?.getRoom(getActiveRoomId())
}

fun Session.roomSummariesFlow() = roomService().getRoomSummariesLive(roomSummaryQueryParams {
    memberships = Membership.activeMemberships()
}).asFlow()
