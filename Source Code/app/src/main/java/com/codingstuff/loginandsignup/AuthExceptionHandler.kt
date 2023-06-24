package com.codingstuff.loginandsignup

import com.google.firebase.auth.FirebaseAuthException

object AuthExceptionHandler {
    //The handleException function generates user-friendly error messages based on the provided exception codes
    fun handleException(e: FirebaseAuthException): String {
        val errorMessage = when(e.errorCode) {
            "ERROR_INVALID_EMAIL" -> "Invalid email address!"
            "ERROR_WRONG_PASSWORD" -> "Invalid password!"
            "ERROR_USER_NOT_FOUND" -> "User with this email doesn't exist."
            "ERROR_USER_DISABLED" -> "User with this email has been disabled."
            "ERROR_OPERATION_NOT_ALLOWED" -> "This action is currently unavailable."
            "ERROR_EMAIL_ALREADY_IN_USE" -> "The email has already been registered. Please login or reset your password."

            else -> "An undefined error happened."
        }
        return errorMessage
    }
}
// user mismatch might use
