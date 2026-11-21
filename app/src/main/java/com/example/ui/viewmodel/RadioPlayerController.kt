package com.example.ui.viewmodel

import android.media.AudioAttributes
import android.media.MediaPlayer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class RadioStation(
    val id: String,
    val name: String,
    val genre: String,
    val country: String,
    val url: String
)

class RadioPlayerController(private val scope: CoroutineScope) {

    val topRadioStations = listOf(
        RadioStation("lofigirl", "Lofi Girl Beats 🌸", "Lofi / Chill", "Global", "https://ice1.somafm.com/lofi-128-mp3"),
        RadioStation("groovesalad", "SomaFM Groove Salad 🥗", "Ambient / Chillout", "Global", "https://ice1.somafm.com/groovesalad-128-mp3"),
        RadioStation("lounge", "SomaFM Lounge 🛋️", "Jazz / Lounge", "Global", "https://ice1.somafm.com/lounge-128-mp3"),
        RadioStation("paris", "Café de Paris 🥐", "Chanson / Acoustic", "France", "https://chantefrance.ice.infomaniak.ch/chantefrance-96.mp3"),
        RadioStation("swissjazz", "Radio Swiss Jazz 🎷", "Jazz / Smooth", "Switzerland", "https://stream.srg-ssr.ch/m/rsj/mp3_128"),
        RadioStation("swissclassic", "Radio Swiss Classic 🎻", "Classical", "Switzerland", "https://stream.srg-ssr.ch/m/rsc_de/mp3_128"),
        RadioStation("antenne", "Antenne Bayern Chill 🍹", "Lounge / Relax", "Germany", "https://stream.antenne.de/chillout/stream/mp3"),
        RadioStation("secretagent", "SomaFM Secret Agent 🕶️", "Beat / Lounge", "Global", "https://ice1.somafm.com/secretagent-128-mp3"),
        RadioStation("goa", "SomaFM Suburbs of Goa 👳", "World / Ethno-Chill", "Global", "https://ice1.somafm.com/suburbsofgoa-128-mp3"),
        RadioStation("deepspace", "SomaFM Deep Space One 🚀", "Ambient / Space", "Global", "https://ice1.somafm.com/deepspaceone-128-mp3"),
        RadioStation("poptron", "SomaFM PopTron ⚡", "Pop / Synthwave", "Global", "https://ice1.somafm.com/poptron-128-mp3"),
        RadioStation("indie", "SomaFM Indie Pop 🎸", "Indie / Alternative", "Global", "https://ice1.somafm.com/indiepop-128-mp3"),
        RadioStation("illinois", "Illinois Street Lounge 🍹", "Retro Lounge", "Global", "https://ice1.somafm.com/illinois-128-mp3"),
        RadioStation("bootliquor", "SomaFM Boot Liquor 🤠", "Americana / Roots", "Global", "https://ice1.somafm.com/bootliquor-128-mp3"),
        RadioStation("sonic", "SomaFM Sonic Universe 🌌", "Avant-garde Jazz", "Global", "https://ice1.somafm.com/sonicuniverse-128-mp3"),
        RadioStation("cliqhop", "SomaFM Cliqhop 👾", "Electronic / IDM", "Global", "https://ice1.somafm.com/cliqhop-128-mp3"),
        RadioStation("fluid", "SomaFM Fluid 🌊", "Instrumental Hip Hop", "Global", "https://ice1.somafm.com/fluid-128-mp3"),
        RadioStation("christmas", "Cozy Christmas Radio ❄️", "Holiday / Jazz", "Global", "https://ice1.somafm.com/christmas-128-mp3"),
        RadioStation("lush", "SomaFM Lush 💋", "Sensuous Vocals", "Global", "https://ice1.somafm.com/lush-128-mp3"),
        RadioStation("covers", "SomaFM Covers 🎤", "Acoustic Covers", "Global", "https://ice1.somafm.com/covers-128-mp3")
    )

    private var mediaPlayer: MediaPlayer? = null
    private var isMediaPlayerPrepared = false
    private var isMediaPlayerInError = false
    private var radioSessionId = 0L

    private val _radioIsPlaying = MutableStateFlow(false)
    val radioIsPlaying: StateFlow<Boolean> = _radioIsPlaying.asStateFlow()

    private val _radioIsLoading = MutableStateFlow(false)
    val radioIsLoading: StateFlow<Boolean> = _radioIsLoading.asStateFlow()

    private val _selectedStation = MutableStateFlow<RadioStation?>(topRadioStations.first())
    val selectedStation: StateFlow<RadioStation?> = _selectedStation.asStateFlow()

    private val _radioError = MutableStateFlow<String?>(null)
    val radioError: StateFlow<String?> = _radioError.asStateFlow()

    private fun releaseMediaPlayer() {
        val currentMp = mediaPlayer ?: return
        val canStop = isMediaPlayerPrepared && !isMediaPlayerInError
        mediaPlayer = null
        isMediaPlayerPrepared = false
        isMediaPlayerInError = false

        runCatching {
            currentMp.setOnPreparedListener(null)
            currentMp.setOnErrorListener(null)
            currentMp.setOnCompletionListener(null)
            if (canStop) {
                if (currentMp.isPlaying) {
                    currentMp.pause()
                }
                currentMp.stop()
            }
        }
        runCatching {
            currentMp.release()
        }
    }

    fun playRadioStation(station: RadioStation) {
        val sessionId = ++radioSessionId
        scope.launch {
            var newMp: MediaPlayer? = null
            try {
                _radioError.value = null
                _selectedStation.value = station
                _radioIsLoading.value = true
                _radioIsPlaying.value = false

                releaseMediaPlayer()

                val mp = MediaPlayer()
                newMp = mp
                mp.setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build()
                )
                mp.setDataSource(station.url)

                if (sessionId != radioSessionId) {
                    runCatching { mp.release() }
                    return@launch
                }

                isMediaPlayerPrepared = false
                isMediaPlayerInError = false
                mediaPlayer = mp

                mp.setOnPreparedListener { preparedMp ->
                    if (mediaPlayer == preparedMp && sessionId == radioSessionId) {
                        isMediaPlayerPrepared = true
                        isMediaPlayerInError = false
                        _radioIsLoading.value = false
                        _radioIsPlaying.value = true
                        runCatching {
                            preparedMp.start()
                        }.onFailure { e ->
                            isMediaPlayerPrepared = false
                            isMediaPlayerInError = true
                            _radioIsPlaying.value = false
                            _radioError.value = e.localizedMessage ?: "Ошибка запуска радио"
                            releaseMediaPlayer()
                        }
                    } else {
                        runCatching { preparedMp.release() }
                    }
                }

                mp.setOnErrorListener { failedMp, what, extra ->
                    if (mediaPlayer == failedMp && sessionId == radioSessionId) {
                        isMediaPlayerPrepared = false
                        isMediaPlayerInError = true
                        _radioIsLoading.value = false
                        _radioIsPlaying.value = false
                        _radioError.value = "Ошибка стрима ($what, $extra)"
                        releaseMediaPlayer()
                    } else {
                        runCatching { failedMp.release() }
                    }
                    true
                }

                mp.setOnCompletionListener { completedMp ->
                    if (mediaPlayer == completedMp && sessionId == radioSessionId) {
                        _radioIsPlaying.value = false
                    }
                }

                mp.prepareAsync()
            } catch (e: Exception) {
                if (sessionId == radioSessionId) {
                    releaseMediaPlayer()
                    _radioIsLoading.value = false
                    _radioIsPlaying.value = false
                    _radioError.value = e.localizedMessage ?: "Ошибка подключения к радио"
                } else {
                    runCatching { newMp?.release() }
                }
            }
        }
    }

    fun toggleRadioPlayback() {
        val current = _selectedStation.value ?: return
        when {
            _radioIsLoading.value -> stopRadio()
            _radioIsPlaying.value -> pauseRadio()
            isMediaPlayerPrepared && !isMediaPlayerInError && mediaPlayer != null -> {
                runCatching {
                    mediaPlayer?.start()
                    _radioIsPlaying.value = true
                }.onFailure {
                    playRadioStation(current)
                }
            }
            else -> playRadioStation(current)
        }
    }

    fun pauseRadio() {
        try {
            mediaPlayer?.let { mp ->
                if (isMediaPlayerPrepared && !isMediaPlayerInError && mp.isPlaying) {
                    mp.pause()
                }
            }
            _radioIsPlaying.value = false
        } catch (e: Exception) {
            _radioError.value = e.localizedMessage
        }
    }

    fun stopRadio() {
        radioSessionId++
        releaseMediaPlayer()
        _radioIsPlaying.value = false
        _radioIsLoading.value = false
    }

    fun playRandomStation() {
        if (topRadioStations.isEmpty()) return
        val current = _selectedStation.value
        val available = topRadioStations.filter { it.id != current?.id }
        val nextStation = if (available.isNotEmpty()) available.random() else topRadioStations.random()
        playRadioStation(nextStation)
    }
}
