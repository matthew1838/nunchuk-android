package com.nunchuk.android.core.repository

import com.google.gson.Gson
import com.nunchuk.android.core.account.AccountManager
import com.nunchuk.android.core.data.api.TRANSACTION_PAGE_COUNT
import com.nunchuk.android.core.data.model.byzantine.GroupResponse
import com.nunchuk.android.core.data.model.byzantine.KeyHealthStatusDto
import com.nunchuk.android.core.data.model.byzantine.WalletHealthStatusResponse
import com.nunchuk.android.core.data.model.byzantine.toModel
import com.nunchuk.android.core.manager.UserWalletApiManager
import com.nunchuk.android.core.mapper.toAlert
import com.nunchuk.android.core.mapper.toByzantineGroup
import com.nunchuk.android.core.mapper.toGroupEntity
import com.nunchuk.android.core.mapper.toKeyHealthStatus
import com.nunchuk.android.core.persistence.NcDataStore
import com.nunchuk.android.model.Alert
import com.nunchuk.android.model.ByzantineGroup
import com.nunchuk.android.model.GroupStatus
import com.nunchuk.android.model.byzantine.KeyHealthStatus
import com.nunchuk.android.persistence.dao.AlertDao
import com.nunchuk.android.persistence.dao.GroupDao
import com.nunchuk.android.persistence.dao.KeyHealthStatusDao
import com.nunchuk.android.persistence.entity.AlertEntity
import com.nunchuk.android.persistence.entity.KeyHealthStatusEntity
import com.nunchuk.android.type.Chain
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

internal class ByzantineSyncer @Inject constructor(
    private val alertDao: AlertDao,
    private val groupDao: GroupDao,
    private val keyHealthStatusDao: KeyHealthStatusDao,
    private val userWalletApiManager: UserWalletApiManager,
    private val accountManager: AccountManager,
    ncDataStore: NcDataStore,
    private val gson: Gson,
    applicationScope: CoroutineScope,
) {

    private val chatId by lazy { accountManager.getAccount().chatId }
    private val chain =
        ncDataStore.chain.stateIn(applicationScope, SharingStarted.Eagerly, Chain.MAIN)

    suspend fun syncAlerts(
        groupId: String
    ): List<Alert>? {
        val remoteList = arrayListOf<Alert>()
        var index = 0
        kotlin.runCatching {
        while (true) {
                val response = userWalletApiManager.groupWalletApi.getAlerts(groupId, offset = index)
                if (response.isSuccess.not()) return null
                val alertList = response.data.alerts.orEmpty().map { it.toAlert() }
                remoteList.addAll(alertList)
                if (response.data.alerts.orEmpty().size < TRANSACTION_PAGE_COUNT) break
                index += TRANSACTION_PAGE_COUNT
            }
        }.onFailure {
            return null
        }

        val newList = mutableListOf<AlertEntity>()
        val updateList = mutableListOf<AlertEntity>()

        val localMap = alertDao.getAlerts(groupId, chatId, chain.value).associateByTo(mutableMapOf()) { it.id }

        remoteList.forEach { remote ->
            val local = localMap[remote.id]
            if (local != null) {
                updateList += local.copy(
                    viewable = remote.viewable,
                    payload = gson.toJson(remote.payload),
                    body = remote.body,
                    createdTimeMillis = remote.createdTimeMillis,
                    status = remote.status,
                    title = remote.title,
                    type = remote.type.name
                )
                localMap.remove(remote.id)
            } else {
                newList += remote.toAlertEntity(groupId)
            }
        }

        val deleteList = localMap.values.toList()

        if (newList.isNotEmpty() || updateList.isNotEmpty() || deleteList.isNotEmpty()) {
            alertDao.updateData(newList, updateList, deleteList)
        }
        return (newList + updateList).map { it.toAlert() }
    }

    suspend fun syncGroups(): List<ByzantineGroup>? {
        kotlin.runCatching {
            val groups = userWalletApiManager.groupWalletApi.getGroups().data.groups.orEmpty()
            val groupLocals =
                groupDao.getGroups(accountManager.getAccount().chatId, chain.value).firstOrNull()
                    ?: emptyList()
            val allGroupIds = groupLocals.map { it.groupId }.toHashSet()
            val addGroupIds = HashSet<String>()
            val chatId = accountManager.getAccount().chatId
            groupDao.updateOrInsert(groups.filter {
                it.status != GroupStatus.DELETED.name && it.id.isNullOrEmpty().not()
            }.map { group ->
                addGroupIds.add(group.id.orEmpty())
                group.toGroupEntity(chatId, chain.value, groupDao)
            }.toList())
            allGroupIds.removeAll(addGroupIds)
            if (allGroupIds.isNotEmpty()) {
                groupDao.deleteGroups(allGroupIds.toList(), chatId = chatId)
            }
            return groups.map { it.toByzantineGroup() }
        }.onFailure { return null }
        return null
    }

    suspend fun syncGroup(groupId: String): ByzantineGroup? {
        kotlin.runCatching {
            val response = userWalletApiManager.groupWalletApi.getGroup(groupId)
            if (response.isSuccess.not()) return null
            val groupRemote = response.data.data ?: return null
            if (groupRemote.status == GroupStatus.DELETED.name) {
                groupDao.deleteGroups(listOf(groupId), chatId = chatId)
                return null
            }
            groupDao.updateOrInsert(groupRemote.toGroupEntity(chatId, chain.value, groupDao))
            return groupRemote.toByzantineGroup()
        }.onFailure { return null }
        return null
    }

    suspend fun syncKeyHealthStatus(groupId: String, walletId: String): List<KeyHealthStatus>? {
        kotlin.runCatching {
            val localMap = keyHealthStatusDao.getKeys(groupId, walletId, chatId, chain.value).associateByTo(mutableMapOf()) { it.xfp }
            val response = userWalletApiManager.groupWalletApi.getWalletHealthStatus(groupId, walletId)
            val remoteList = arrayListOf<KeyHealthStatusDto>()
            remoteList.addAll(response.data.statuses)
            val newList = mutableListOf<KeyHealthStatusEntity>()
            val updateList = mutableListOf<KeyHealthStatusEntity>()

            remoteList.forEach { remote ->
                val local = localMap[remote.xfp]
                if (local != null) {
                    updateList += local.copy(
                        canRequestHealthCheck = remote.canRequestHealthCheck,
                        lastHealthCheckTimeMillis = remote.lastHealthCheckTimeMillis ?: 0L,
                        xfp = remote.xfp,
                    )
                    localMap.remove(remote.xfp)
                } else {
                    newList += remote.toKeyHealthStatusEntity(groupId, walletId)
                }
            }

            val deleteList = localMap.values.toList()

            if (newList.isNotEmpty() || updateList.isNotEmpty() || deleteList.isNotEmpty()) {
                keyHealthStatusDao.updateData(newList, updateList, deleteList)
            }
            return (newList + updateList).map { it.toKeyHealthStatus() }
        }.onFailure { return null }
        return null
    }

    private fun Alert.toAlertEntity(
        groupId: String
    ): AlertEntity {
        return AlertEntity(
            id = id,
            viewable = viewable,
            body = body,
            createdTimeMillis = createdTimeMillis,
            status = status,
            title = title,
            chatId = chatId,
            type = type.name,
            chain = chain.value,
            payload = gson.toJson(payload),
            groupId = groupId
        )
    }

    private fun KeyHealthStatusDto.toKeyHealthStatusEntity(
        groupId: String,
        walletId: String
    ): KeyHealthStatusEntity {
        return KeyHealthStatusEntity(
            xfp = xfp,
            canRequestHealthCheck = canRequestHealthCheck,
            lastHealthCheckTimeMillis = lastHealthCheckTimeMillis ?: 0L,
            chatId = chatId,
            chain = chain.value,
            groupId = groupId,
            walletId = walletId
        )
    }
}