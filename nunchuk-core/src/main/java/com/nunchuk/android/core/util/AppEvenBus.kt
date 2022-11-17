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

package com.nunchuk.android.core.util

class AppEvenBus {

    private var listeners = HashSet<AppEventListener>()

    fun subscribe(listener: AppEventListener) {
        listeners.add(listener)
    }

    fun publish(session: AppEvent) {
        listeners.forEach {
            it(session)
        }
    }

    fun unsubscribe(listener: AppEventListener) {
        listeners.remove(listener)
    }

    companion object {
        val instance = InstanceHolder.instance
    }

    private object InstanceHolder {
        var instance = AppEvenBus()
    }
}

typealias AppEventListener = (AppEvent) -> Unit

sealed class AppEvent {
    object AppResumedEvent : AppEvent()
}
