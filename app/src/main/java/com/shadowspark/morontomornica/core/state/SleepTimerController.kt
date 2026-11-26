package com.shadowspark.morontomornica.core.state

import android.os.Handler
import android.os.Looper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Fires one callback after a number of minutes; cancel removes the pending callback.
 * UI-side only: the resulting pause must flow through PlaybackRouter.toggleLocalPlayPause()
 * so the real Media3 controller and its state remain authoritative. No second player.
 */
class SleepTimerController(
  private val handler: Handler = Handler(Looper.getMainLooper()),
  private val onFire: () -> Unit,
) {
  private var pending: Runnable? = null
  private val _remaining = MutableStateFlow(0)

  /** Observe this in Compose so the countdown label recomposes; plain fields never trigger it. */
  val remainingMinutesFlow: StateFlow<Int> = _remaining.asStateFlow()
  val remainingMinutes: Int get() = _remaining.value
  val active: Boolean get() = pending != null

  fun start(minutes: Int) {
    cancel()
    if (minutes <= 0) return
    val runnable =
      Runnable {
        pending = null
        _remaining.value = 0
        onFire()
      }
    pending = runnable
    _remaining.value = minutes
    handler.postDelayed(runnable, minutes * 60_000L)
  }

  fun cancel() {
    pending?.let { handler.removeCallbacks(it) }
    pending = null
    _remaining.value = 0
  }
}
