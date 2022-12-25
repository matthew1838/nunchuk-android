package com.nunchuk.android.repository

import com.nunchuk.android.model.MemberSubscription
import com.nunchuk.android.model.MembershipPlan
import com.nunchuk.android.model.MembershipStepInfo
import kotlinx.coroutines.flow.Flow

interface MembershipRepository {
    fun getSteps(plan: MembershipPlan): Flow<List<MembershipStepInfo>>
    suspend fun saveStepInfo(info: MembershipStepInfo)
    suspend fun deleteStepBySignerId(masterSignerId: String)
    suspend fun getSubscription() : MemberSubscription
    suspend fun restart(plan: MembershipPlan)
    fun getLocalCurrentPlan(): Flow<MembershipPlan>
    fun isRegisterColdcard(): Flow<Boolean>
    fun isRegisterAirgap(): Flow<Boolean>
    suspend fun setRegisterColdcard(value: Boolean)
    suspend fun setRegisterAirgap(value: Boolean)
}