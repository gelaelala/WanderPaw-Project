package com.codingstuff.loginandsignup

import com.google.firebase.auth.FirebaseAuthException

class AuthExceptionHandler {
    companion object {
        //The handleException function generates user-friendly error messages based on the provided exception codes
        fun handleException(e: FirebaseAuthException): String {
            val errorMessage = when (e.errorCode) {
                "ERROR_INVALID_EMAIL" -> "Invalid email address!" // utilized in sign up
                "ERROR_WRONG_PASSWORD" -> "Invalid password!"
                "ERROR_USER_NOT_FOUND" -> "User with this credentials doesn't exist."
                "ERROR_USER_DISABLED" -> "User with this email has been disabled."
                "ERROR_OPERATION_NOT_ALLOWED" -> "This action is currently unavailable."
                "ERROR_EMAIL_ALREADY_IN_USE" -> "The email has already been registered. Please log in or reset your password." // utilized in sign up
                // user mismatch might use

                else -> "An undefined error happened."
            }
            return errorMessage
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
}
