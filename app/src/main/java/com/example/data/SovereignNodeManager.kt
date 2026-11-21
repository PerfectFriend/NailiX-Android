package com.example.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.InetSocketAddress
import java.net.Socket
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

data class NodeStatus(
    val onionAddress: String = "",
    val localAddress: String = "127.0.0.1:3000",
    val isConnected: Boolean = false,
    val isTorRoutingEnabled: Boolean = false,
    val latencyMs: Long = -1L,
    val lastCheckedAt: Long = 0L,
    val diagnosticMessage: String = "Проверка связи не выполнялась"
)

data class SimplexQueue(
    val queueAddress: String = "smp://не подключено",
    val fingerprint: String = "—",
    val totalMessages: Int = 0,
    val pendingOfflineCount: Int = 0
)

data class SimplexMessage(
    val id: String = UUID.randomUUID().toString().take(8),
    val timestamp: Long = System.currentTimeMillis(),
    val senderFingerprint: String,
    val type: String,
    val payloadSha256: String,
    val decryptedSummary: String
)

data class P2PSyncState(
    val lastSyncTimestamp: Long = 0L,
    val stateHash: String = "—",
    val syncedRecordsCount: Int = 0,
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

    private val _simplexMessages = MutableStateFlow<List<SimplexMessage>>(emptyList())
    val simplexMessages: StateFlow<List<SimplexMessage>> = _simplexMessages.asStateFlow()

    private val _p2pSyncState = MutableStateFlow(P2PSyncState())
    val p2pSyncState: StateFlow<P2PSyncState> = _p2pSyncState.asStateFlow()

    private val _roiCalc = MutableStateFlow(NodeRoiCalculation())
    val roiCalc: StateFlow<NodeRoiCalculation> = _roiCalc.asStateFlow()

    private val _isNetworkOnline = MutableStateFlow(true)
    val isNetworkOnline: StateFlow<Boolean> = _isNetworkOnline.asStateFlow()

    private val _pendingOfflineQueue = MutableStateFlow<List<SimplexMessage>>(emptyList())
    val pendingOfflineQueue: StateFlow<List<SimplexMessage>> = _pendingOfflineQueue.asStateFlow()

    fun updateNodeAddress(onion: String, local: String) {
        val cleanOnion = onion.trim()
        val cleanLocal = local.trim()
        _nodeStatus.value = _nodeStatus.value.copy(
            onionAddress = cleanOnion,
            localAddress = cleanLocal,
            isConnected = false,
            latencyMs = -1L,
            diagnosticMessage = "Адрес обновлён. Нажмите «Проверить связь»."
        )
        val fp = if (cleanLocal.isNotBlank()) sha256(cleanLocal).take(24).uppercase().chunked(4).joinToString(":") else "—"
        _simplexQueue.value = _simplexQueue.value.copy(
            queueAddress = if (cleanOnion.isNotBlank()) "smp://$cleanOnion" else if (cleanLocal.isNotBlank()) "smp://$cleanLocal" else "smp://не настроено",
            fingerprint = fp
        )
    }

    fun toggleTorRouting(enabled: Boolean) {
        _nodeStatus.value = _nodeStatus.value.copy(
            isTorRoutingEnabled = enabled,
            diagnosticMessage = if (enabled) {
                "Tor SOCKS5 режим включён (требуется Orbot/Tor на 127.0.0.1:9050)"
            } else {
                "Прямое локальное TCP-подключение"
            }
        )
    }

    suspend fun pingNode(): Boolean = withContext(Dispatchers.IO) {
        val current = _nodeStatus.value
        val (targetHost, targetPort) = if (current.isTorRoutingEnabled) {
            "127.0.0.1" to 9050
        } else {
            parseHostPort(current.localAddress)
        }

        if (targetHost.isBlank() || targetPort <= 0) {
            _nodeStatus.value = current.copy(
                isConnected = false,
                latencyMs = -1L,
                lastCheckedAt = System.currentTimeMillis(),
                diagnosticMessage = "Некорректный адрес узла (укажите host:port, например 192.168.1.50:3000)"
            )
            return@withContext false
        }

        val startNanos = System.nanoTime()
        return@withContext try {
            Socket().use { socket ->
                socket.connect(InetSocketAddress(targetHost, targetPort), 2500)
            }
            val elapsedMs = ((System.nanoTime() - startNanos) / 1_000_000L).coerceAtLeast(1L)
            _nodeStatus.value = current.copy(
                isConnected = true,
                latencyMs = elapsedMs,
                lastCheckedAt = System.currentTimeMillis(),
                diagnosticMessage = "TCP соединение с $targetHost:$targetPort успешно (${elapsedMs} мс)"
            )
            true
        } catch (e: Exception) {
            _nodeStatus.value = current.copy(
                isConnected = false,
                latencyMs = -1L,
                lastCheckedAt = System.currentTimeMillis(),
                diagnosticMessage = "Офлайн ($targetHost:$targetPort): ${e.localizedMessage ?: "узел недоступен"}"
            )
            false
        }
    }

    fun parseHostPort(address: String): Pair<String, Int> {
        val trimmed = address.trim().removePrefix("http://").removePrefix("https://").substringBefore("/")
        val parts = trimmed.split(":")
        val host = parts.getOrNull(0)?.trim().orEmpty()
        val port = parts.getOrNull(1)?.trim()?.toIntOrNull() ?: 3000
        return host to port
    }

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
                totalMessages = _simplexMessages.value.size,
                pendingOfflineCount = 0
            )
            _pendingOfflineQueue.value = emptyList()
        }
    }

    fun recordLocalBookingEvent(bookingId: Int, serviceName: String, clientName: String, date: String, time: String) {
        val rawSummary = "Запись #$bookingId: $serviceName ($date $time) — $clientName"
        val newMsg = SimplexMessage(
            id = "bk_$bookingId",
            timestamp = System.currentTimeMillis(),
            senderFingerprint = "LOCAL:${sha256(clientName).take(8).uppercase()}",
            type = "BOOKING_EVENT",
            payloadSha256 = sha256(rawSummary),
            decryptedSummary = rawSummary
        )
        if (!_isNetworkOnline.value) {
            _pendingOfflineQueue.value = _pendingOfflineQueue.value + newMsg
            _simplexQueue.value = _simplexQueue.value.copy(
                pendingOfflineCount = _pendingOfflineQueue.value.size
            )
        } else {
            _simplexMessages.value = listOf(newMsg) + _simplexMessages.value
            _simplexQueue.value = _simplexQueue.value.copy(
                totalMessages = _simplexMessages.value.size
            )
        }
    }

    suspend fun computeRoomStateSnapshot(
        bookings: List<Booking>,
        services: List<ServiceItem>,
        nailArts: List<MasterNailArtEntity>
    ): P2PSyncState = withContext(Dispatchers.Default) {
        _p2pSyncState.value = _p2pSyncState.value.copy(isSyncActive = true)
        val canonicalPayload = buildSnapshotJson(bookings, services, nailArts).toString()
        val digestHex = sha256(canonicalPayload)
        val updated = P2PSyncState(
            lastSyncTimestamp = System.currentTimeMillis(),
            stateHash = digestHex,
            syncedRecordsCount = bookings.size + services.size + nailArts.size,
            pendingConflicts = _pendingOfflineQueue.value.size,
            isSyncActive = false
        )
        _p2pSyncState.value = updated
        updated
    }

    fun exportSnapshotJson(
        bookings: List<Booking>,
        services: List<ServiceItem>,
        nailArts: List<MasterNailArtEntity>
    ): String {
        val snapshot = buildSnapshotJson(bookings, services, nailArts)
        val stateHash = sha256(snapshot.toString())
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
        return JSONObject().apply {
            put("protocol", "NailiX-LocalSnapshot-v1")
            put("export_timestamp", sdf.format(Date()))
            put("node_local_address", _nodeStatus.value.localAddress)
            put("node_onion", _nodeStatus.value.onionAddress)
            put("sha256_digest", stateHash)
            put("records_count", bookings.size + services.size + nailArts.size)
            put("snapshot", snapshot)
        }.toString(2)
    }

    private fun buildSnapshotJson(
        bookings: List<Booking>,
        services: List<ServiceItem>,
        nailArts: List<MasterNailArtEntity>
    ): JSONObject {
        val bookingsArr = JSONArray().apply {
            bookings.forEach { b ->
                put(JSONObject().apply {
                    put("id", b.id)
                    put("service", b.serviceName)
                    put("date", b.date)
                    put("time", b.timeSlot)
                    put("master", b.masterName)
                    put("price", b.price)
                    put("status", b.status)
                })
            }
        }
        val servicesArr = JSONArray().apply {
            services.forEach { s ->
                put(JSONObject().apply {
                    put("id", s.id)
                    put("name", s.name)
                    put("category", s.category)
                    put("price", s.price)
                    put("durationMin", s.durationMin)
                })
            }
        }
        val artsArr = JSONArray().apply {
            nailArts.forEach { a ->
                put(JSONObject().apply {
                    put("id", a.id)
                    put("title", a.title)
                    put("master", a.masterName)
                    put("likes", a.likes)
                })
            }
        }
        return JSONObject().apply {
            put("bookings", bookingsArr)
            put("services", servicesArr)
            put("nailArts", artsArr)
        }
    }

    fun updateRoiParams(masters: Int, fee: Double, aiGens: Int) {
        _roiCalc.value = _roiCalc.value.copy(
            hostedMastersCount = masters,
            monthlyPricePerMaster = fee,
            monthlyAiGenerations = aiGens
        )
    }

    fun sha256(input: String): String {
        val md = MessageDigest.getInstance("SHA-256")
        val bytes = md.digest(input.toByteArray(Charsets.UTF_8))
        return bytes.joinToString("") { "%02x".format(it) }
    }
}
