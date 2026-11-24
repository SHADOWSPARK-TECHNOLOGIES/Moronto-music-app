package com.shadowspark.morontomornica.core.state

import android.os.Handler
import android.os.Looper

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

  var remainingMinutes: Int = 0
    private set

  val active: Boolean get() = pending != null

  fun start(minutes: Int) {
    cancel()
    if (minutes <= 0) return
    val runnable =
      Runnable {
        pending = null
        remainingMinutes = 0
        onFire()
      }
    pending = runnable
    remainingMinutes = minutes
    handler.postDelayed(runnable, minutes * 60_000L)
  }

  fun cancel() {
    pending?.let { handler.removeCallbacks(it) }
    pending = null
    remainingMinutes = 0
  }
}
