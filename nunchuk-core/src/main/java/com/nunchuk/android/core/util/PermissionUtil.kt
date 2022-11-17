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

import android.Manifest.permission.CAMERA
import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.app.Activity
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityCompat.checkSelfPermission
import com.nunchuk.android.utils.CrashlyticsReporter

const val CAMERA_PERMISSION_REQUEST_CODE = 0x1024
private const val READ_STORAGE_PERMISSION_REQUEST_CODE = 0x2048

fun Activity.isPermissionGranted(permission: String) =
    checkSelfPermission(this, permission) == PERMISSION_GRANTED

fun Activity.isPermissionGranted(permissions: Array<String>): Boolean =
    permissions.all { checkSelfPermission(this, it) == PERMISSION_GRANTED }

fun Activity.checkReadExternalPermission(): Boolean {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
        return false
    }
    val isGranted = isPermissionGranted(READ_EXTERNAL_STORAGE)
    if (!isGranted) {
        requestReadExternalPermission()
    }
    return isGranted
}

fun Activity.requestReadExternalPermission() = try {
    ActivityCompat.requestPermissions(
        this, arrayOf(READ_EXTERNAL_STORAGE), READ_STORAGE_PERMISSION_REQUEST_CODE
    )
} catch (e: Exception) {
    CrashlyticsReporter.recordException(e)
}

// TODO eliminate duplicated
fun Activity.checkCameraPermission(): Boolean {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
        return false
    }
    val isGranted = isPermissionGranted(CAMERA)
    if (!isGranted) {
        requestCameraPermission()
    }
    return isGranted
}

fun Activity.requestCameraPermission() = try {
    ActivityCompat.requestPermissions(
        this, arrayOf(CAMERA), CAMERA_PERMISSION_REQUEST_CODE
    )
} catch (e: Exception) {
    CrashlyticsReporter.recordException(e)
}
