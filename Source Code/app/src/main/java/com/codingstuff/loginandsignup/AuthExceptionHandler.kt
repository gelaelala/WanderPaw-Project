package com.codingstuff.loginandsignup

import com.google.firebase.FirebaseApiNotAvailableException
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.database.DatabaseException

class AuthExceptionHandler {

    companion object {
        fun handleException(exception: Exception): String {
            return when (exception) {
                is DatabaseException -> "A database exception happened. Please try again."
                is FirebaseApiNotAvailableException -> "The requested API is not available."
                is FirebaseAuthException -> handleFirebaseAuthException(exception)
                is FirebaseNetworkException -> "There is a network connectivity issue. Please check your network."
                is FirebaseTooManyRequestsException -> "Too many requests. Try again later."
                else -> "An undefined error happened."
            }
        }

        private fun handleFirebaseAuthException(exception: FirebaseAuthException): String {
            return when (exception.errorCode) {
                "ERROR_ACCOUNT_EXISTS_WITH_DIFFERENT_CREDENTIAL" -> "This Google account is already associated with a different authentication method."
                "ERROR_CREDENTIAL_ALREADY_IN_USE" -> "This Google credential is already associated with a different user account."
                "ERROR_INVALID_CREDENTIAL" -> "Invalid Google credential. Please sign in again."
                "ERROR_INVALID_EMAIL" -> "Invalid email address!"
                "ERROR_WRONG_PASSWORD" -> "Invalid password!"
                "ERROR_USER_NOT_FOUND" -> "User with these credentials doesn't exist."
                "ERROR_USER_DISABLED" -> "User with this email has been disabled."
                "ERROR_OPERATION_NOT_ALLOWED" -> "This action is currently unavailable."
                "ERROR_EMAIL_ALREADY_IN_USE" -> "The email has already been registered. Please log in or reset your password."
                "ERROR_INVALID_USER_TOKEN" -> "Your Google credential is no longer valid. Please sign in again."
                "ERROR_USER_TOKEN_EXPIRED" -> "Your Google credential has expired. Please sign in again."
                else -> "An undefined error happened."
            }
        }
    }

    fun validatePassword(password: String): List<String> {
        val missingConditions = mutableListOf<String>()

        if (password.length < 6) {
            missingConditions.add("at least 6 characters")
        }
        if (!password.matches(Regex(".*[a-z].*"))) {
            missingConditions.add("one lowercase letter")
        }
        if (!password.matches(Regex(".*[A-Z].*"))) {
            missingConditions.add("one uppercase letter")
        }
        if (!password.matches(Regex(".*\\d.*"))) {
            missingConditions.add("one numerical digit")
        }
        if (!password.matches(Regex(".*[@\$!%*?&].*"))) {
            missingConditions.add("one special character")
        }

        return missingConditions
    }
}

