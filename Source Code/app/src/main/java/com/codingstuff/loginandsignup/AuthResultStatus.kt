package com.codingstuff.loginandsignup

// enum set of firebase auth error ids
enum class AuthResultStatus {
    EMAIL_ALREADY_EXISTS,
    WRONG_PASSWORD,
    INVALID_EMAIL,
    USER_NOT_FOUND,
    USER_DISABLED,
    OPERATION_NOT_ALLOWED,
    TOO_MANY_REQUESTS,
    UNDEFINED
}


object AuthExceptionHandler {
    //The handleException function maps specific error codes to these statuses
    fun handleException(e: Exception): AuthResultStatus {
        println(e.message)
        val status: AuthResultStatus = when (e.message) {
            "ERROR_INVALID_EMAIL" -> AuthResultStatus.INVALID_EMAIL
            "ERROR_WRONG_PASSWORD" -> AuthResultStatus.WRONG_PASSWORD
            "ERROR_USER_NOT_FOUND" -> AuthResultStatus.USER_NOT_FOUND
            "ERROR_USER_DISABLED" -> AuthResultStatus.USER_DISABLED
            "ERROR_TOO_MANY_REQUESTS" -> AuthResultStatus.TOO_MANY_REQUESTS
            "ERROR_OPERATION_NOT_ALLOWED" -> AuthResultStatus.OPERATION_NOT_ALLOWED
            "ERROR_EMAIL_ALREADY_IN_USE" -> AuthResultStatus.EMAIL_ALREADY_EXISTS
            else -> AuthResultStatus.UNDEFINED
        }
        return status
    }

//  the generateExceptionMessage function generates user-friendly error messages based on the provided exception codes
    fun generateExceptionMessage(exceptionCode: AuthResultStatus): String {
        val errorMessage: String = when (exceptionCode) {
            AuthResultStatus.INVALID_EMAIL -> "Invalid email address!"
            AuthResultStatus.WRONG_PASSWORD -> "Invalid password!"
            AuthResultStatus.USER_NOT_FOUND -> "User with this email doesn't exist."
            AuthResultStatus.USER_DISABLED -> "User with this email has been disabled."
            AuthResultStatus.TOO_MANY_REQUESTS -> "Too many requests. Try again later."
            AuthResultStatus.OPERATION_NOT_ALLOWED -> "Signing in with Email and Password is not enabled."
            AuthResultStatus.EMAIL_ALREADY_EXISTS -> "The email has already been registered. Please log in or reset your password."
            else -> "An undefined error happened."
        }
        return errorMessage
    }
}
