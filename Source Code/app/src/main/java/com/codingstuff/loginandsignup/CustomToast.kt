@file:Suppress("DEPRECATION")

package com.codingstuff.loginandsignup

import android.content.Context
import android.os.Handler
import android.widget.Toast

object CustomToast {
    private const val DEFAULT_TOAST_DURATION = Toast.LENGTH_SHORT
    private var previousToast: Toast? = null
    @JvmOverloads
    fun showToast(context: Context?, message: String?, duration: Int = DEFAULT_TOAST_DURATION) {
        // Cancel the previous toast if it exists
        cancelPreviousToast()

        // Create and show the new toast
        val toast = Toast.makeText(context, message, duration)
        toast.show()

        // Set the new toast as the previous toast
        previousToast = toast

        // Schedule a delay to clear the previous toast after its duration has elapsed
        Handler().postDelayed(
            { cancelPreviousToast() },
            (if (duration == Toast.LENGTH_SHORT) 2000 else 3500).toLong()
        )
    }

    private fun cancelPreviousToast() {
        if (previousToast != null) {
            previousToast!!.cancel()
            previousToast = null
        }
    }
}