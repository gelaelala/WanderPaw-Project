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
