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

package com.nunchuk.android.contact.usecase

import com.nunchuk.android.model.UserResponse
import com.nunchuk.android.repository.ContactsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

interface AutoCompleteSearchUseCase {
    fun execute(keyword: String): Flow<List<UserResponse>>
}

internal class AutoCompleteSearchUseCaseImpl @Inject constructor(
    private val contactsRepository: ContactsRepository
) : AutoCompleteSearchUseCase {

    override fun execute(keyword: String) = contactsRepository.autoCompleteSearch(keyword)

}