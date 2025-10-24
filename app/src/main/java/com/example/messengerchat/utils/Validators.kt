package com.example.messengerchat.utils

object Validators {

    private val specialCharacters = setOf(
        '!', '@', '#', '$', '%', '^', '&', '*', '(', ')', '-', '_',
        '=', '+', '[', ']', '{', '}', '|', ';', ':', '\'', '\"', ',',
        '.', '<', '>', '/', '?', '`', '~'
    )

    fun validateLogin(login: String): Boolean {
        val pattern = "^[a-zA-Z0-9]+$".toRegex()
        return login.length >= 2 && pattern.matches(login)
    }

    fun validatePassword(password: String): Boolean {
        val hasLowerCase = password.any { it.isLowerCase() }
        val hasUpperCase = password.any { it.isUpperCase() }
        val hasDigit = password.any { it.isDigit() }
        val hasSpecialChar = password.any { it in specialCharacters }

        return password.length >= 8 && hasLowerCase && hasUpperCase && hasDigit && hasSpecialChar
    }

    fun validateKeyword(keyword: String): Boolean {
        return validatePassword(keyword)
    }
}