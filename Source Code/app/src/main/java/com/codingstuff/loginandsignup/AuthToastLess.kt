package com.codingstuff.loginandsignup

import android.content.Context
import android.widget.Toast

class AuthToastLess (private val context: Context) {
    private var toast: Toast? = null
    private var toastCount: Int = 0

    fun showToast(message: String, duration: Int = Toast.LENGTH_SHORT) {
        toastCount++
        toast?.cancel() // closes toasts -- Close the view if it's showing, or don't show it if it isn't showing yet. You do not normally have to call
        // this. Normally view will disappear on its own after the appropriate duration.

        // case for when there are many triggers of a toast the duration will be divided
        val adjustedDuration = when {
            toastCount <= 1 -> duration  // Use the original duration for the first toast
            duration == Toast.LENGTH_SHORT -> Toast.LENGTH_SHORT / 2  // Adjusted duration for subsequent toasts (e.g., half of the default duration)
            duration == Toast.LENGTH_LONG -> Toast.LENGTH_LONG / 2
            else -> duration
        }

        toast = Toast.makeText(context, message, adjustedDuration)
        toast?.show()
    }

    fun cancelToast() {
        toastCount = 0
        toast?.cancel()
    }


}