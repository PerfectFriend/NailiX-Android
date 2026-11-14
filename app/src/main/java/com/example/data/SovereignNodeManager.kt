package com.example.data

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

data class NodeStatus(
    val onionAddress: String = "nailhub7xqv5p29dkmw3o4l.onion",
    val localAddress: String = "192.168.1.145:3000",
    val isConnected: Boolean = true,
    val isTorRoutingEnabled: Boolean = true,
    val latencyMs: Long = 42,
    val cpuLoadPercent: Int = 18,
    val npuStatus: String = "AMD Ryzen AI NPU: Активен (RDNA 3.5 / XDNA 2)",
    val ramUsage: String = "8.4 / 32 GB",
    val uptimeHours: Int = 168
)

data class SimplexQueue(
    val queueAddress: String = "smp://simplex.nailhub.local/v1/queue_master_v3",
    val fingerprint: String = "4F8E:91BA:220C:DD81:9AA4:31EF",
    val unreadCount: Int = 3,
    val totalMessages: Int = 47,
    val isEncrypted: Boolean = true
)

data class SimplexMessage(
    val id: String = UUID.randomUUID().toString().take(8),
    val timestamp: Long = System.currentTimeMillis(),
    val senderFingerprint: String,
    val type: String, // BOOKING_REQUEST, SCHEDULE_SYNC, STATUS_ACK, PING
    val payloadEncrypted: String,
    val decryptedSummary: String
)

data class P2PSyncState(
    val lastSyncTimestamp: Long = System.currentTimeMillis(),
    val stateHash: String = "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855",
    val syncedRecordsCount: Int = 124,
    val pendingConflicts: Int = 0,
    val isSyncActive: Boolean = false
)

data class NodeRoiCalculation(
    val hostedMastersCount: Int = 15,
    val monthlyPricePerMaster: Double = 5.0,
    val monthlyAiGenerations: Int = 400,
    val pricePerAiGeneration: Double = 0.05,
    val hardwareCost: Double = 800.0
) {
    val monthlyHostingRevenue: Double get() = hostedMastersCount * monthlyPricePerMaster
    val monthlyAiRevenue: Double get() = monthlyAiGenerations * pricePerAiGeneration
    val totalMonthlyRevenue: Double get() = monthlyHostingRevenue + monthlyAiRevenue
    val paybackPeriodMonths: Double get() = if (totalMonthlyRevenue > 0) hardwareCost / totalMonthlyRevenue else 99.0
    val roi12MonthsPercent: Double get() = if (hardwareCost > 0) ((totalMonthlyRevenue * 12 - hardwareCost) / hardwareCost) * 100.0 else 0.0
}

object SovereignNodeManager {

    private val _nodeStatus = MutableStateFlow(NodeStatus())
    val nodeStatus: StateFlow<NodeStatus> = _nodeStatus.asStateFlow()

    private val _simplexQueue = MutableStateFlow(SimplexQueue())
    val simplexQueue: StateFlow<SimplexQueue> = _simplexQueue.asStateFlow()

    private val _simplexMessages = MutableStateFlow(
        listOf(
            SimplexMessage(
                id = "msg_01",
                timestamp = System.currentTimeMillis() - 1000 * 60 * 18,
                senderFingerprint = "7A11:BB89:33EF:9901",
                type = "BOOKING_REQUEST",
                payloadEncrypted = "c21wX2VuY3J5cHRlZF9kYXRhX2hhc2hfY2xpZW50XzEwMQ==",
                decryptedSummary = "Новая запись: Маникюр + SPA (24.10 14:00) от Клиент #7A11"
            ),
            SimplexMessage(
                id = "msg_02",
                timestamp = System.currentTimeMillis() - 1000 * 60 * 45,
                senderFingerprint = "C044:E21A:119B:884A",
                type = "SCHEDULE_SYNC",
                payloadEncrypted = "c21wX3N5bmNfaGFzaF92ZWN0b3JjbG9ja185OTQ=",
                decryptedSummary = "CRDT Синхронизация расписания: 2 новых слота от Мастера #C044"
            ),
            SimplexMessage(
                id = "msg_03",
                timestamp = System.currentTimeMillis() - 1000 * 60 * 120,
                senderFingerprint = "NODE:BEELINK-SER9",
                type = "STATUS_ACK",
                payloadEncrypted = "YmVlbGlua19zZXI5X25wdV9va19oZWFsdGg=",
                decryptedSummary = "Здоровье ноды: NPU / GPU в норме, температура 44°C, 0 ошибок"
            )
        )
    )
    val simplexMessages: StateFlow<List<SimplexMessage>> = _simplexMessages.asStateFlow()

    private val _p2pSyncState = MutableStateFlow(P2PSyncState())
    val p2pSyncState: StateFlow<P2PSyncState> = _p2pSyncState.asStateFlow()

    private val _roiCalc = MutableStateFlow(NodeRoiCalculation())
    val roiCalc: StateFlow<NodeRoiCalculation> = _roiCalc.asStateFlow()

    fun updateNodeAddress(onion: String, local: String) {
        _nodeStatus.value = _nodeStatus.value.copy(
            onionAddress = onion.trim(),
            localAddress = local.trim()
        )
    }

    fun toggleTorRouting(enabled: Boolean) {
        _nodeStatus.value = _nodeStatus.value.copy(
            isTorRoutingEnabled = enabled,
            latencyMs = if (enabled) (35..65).random().toLong() else (5..15).random().toLong()
        )
    }

    suspend fun pingNode(): Boolean {
        // Simulates ping and health stats renewal
        kotlinx.coroutines.delay(600)
        _nodeStatus.value = _nodeStatus.value.copy(
            isConnected = true,
            latencyMs = if (_nodeStatus.value.isTorRoutingEnabled) (38..58).random().toLong() else (8..18).random().toLong(),
            cpuLoadPercent = (12..28).random(),
            uptimeHours = _nodeStatus.value.uptimeHours + 1
        )
        return true
    }

    private val _isNetworkOnline = MutableStateFlow(true)
    val isNetworkOnline: StateFlow<Boolean> = _isNetworkOnline.asStateFlow()

    private val _pendingOfflineQueue = MutableStateFlow<List<SimplexMessage>>(emptyList())
    val pendingOfflineQueue: StateFlow<List<SimplexMessage>> = _pendingOfflineQueue.asStateFlow()

    fun updateNetworkConnectivity(online: Boolean) {
        val wasOffline = !_isNetworkOnline.value
        _isNetworkOnline.value = online
        if (wasOffline && online && _pendingOfflineQueue.value.isNotEmpty()) {
            flushPendingOfflineQueue()
        }
    }

    fun flushPendingOfflineQueue() {
        val queue = _pendingOfflineQueue.value
        if (queue.isNotEmpty()) {
            _simplexMessages.value = queue + _simplexMessages.value
            _simplexQueue.value = _simplexQueue.value.copy(
                totalMessages = _simplexQueue.value.totalMessages + queue.size,
                unreadCount = _simplexQueue.value.unreadCount + queue.size
            )
            _pendingOfflineQueue.value = emptyList()
        }
    }

    fun sendSimplexBooking(bookingId: Int, serviceName: String, clientName: String, date: String, time: String) {
        val newMsg = SimplexMessage(
            id = "bk_${System.currentTimeMillis().toString().takeLast(5)}",
            timestamp = System.currentTimeMillis(),
            senderFingerprint = "CLI:" + UUID.randomUUID().toString().take(8).uppercase(),
            type = "BOOKING_REQUEST",
            payloadEncrypted = "smp_enc_" + UUID.randomUUID().toString(),
            decryptedSummary = "Приватная запись SimpleX: $serviceName ($date $time) для $clientName"
        )
        if (!_isNetworkOnline.value) {
            _pendingOfflineQueue.value = _pendingOfflineQueue.value + newMsg
        } else {
            _simplexMessages.value = listOf(newMsg) + _simplexMessages.value
            _simplexQueue.value = _simplexQueue.value.copy(
                totalMessages = _simplexQueue.value.totalMessages + 1,
                unreadCount = _simplexQueue.value.unreadCount + 1
            )
        }
    }

    suspend fun triggerP2PSync(): P2PSyncState {
        _p2pSyncState.value = _p2pSyncState.value.copy(isSyncActive = true)
        kotlinx.coroutines.delay(800)
        val now = System.currentTimeMillis()
        val md = MessageDigest.getInstance("SHA-256")
        val hashBytes = md.digest("nailhub_p2p_state_$now".toByteArray())
        val newHash = hashBytes.joinToString("") { "%02x".format(it) }

        val updated = P2PSyncState(
            lastSyncTimestamp = now,
            stateHash = newHash,
            syncedRecordsCount = _p2pSyncState.value.syncedRecordsCount + (1..3).random(),
            pendingConflicts = 0,
            isSyncActive = false
        )
        _p2pSyncState.value = updated
        return updated
    }

    fun updateRoiParams(masters: Int, fee: Double, aiGens: Int) {
        _roiCalc.value = _roiCalc.value.copy(
            hostedMastersCount = masters,
            monthlyPricePerMaster = fee,
            monthlyAiGenerations = aiGens
        )
    }

    fun exportEncryptedBackupJson(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val dateStr = sdf.format(Date())
        return """
            {
              "protocol": "NailiX-P2P-v1.0-RC1",
              "node_onion": "${_nodeStatus.value.onionAddress}",
              "simplex_queue": "${_simplexQueue.value.queueAddress}",
              "export_timestamp": "$dateStr",
              "state_hash": "${_p2pSyncState.value.stateHash}",
              "cipher": "AES-256-GCM / Curve25519",
              "payload_status": "SECURE_OFFLINE_VERIFIED"
            }
        """.trimIndent()
    }
}
