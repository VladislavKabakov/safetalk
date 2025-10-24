package com.example.messengerchat.data.preferences

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.example.messengerchat.constants.PrefManagerConstants.KEY_TOKEN
import com.example.messengerchat.constants.PrefManagerConstants.KEY_USER_ID
import com.example.messengerchat.constants.PrefManagerConstants.KEY_USER_LOGIN
import com.example.messengerchat.constants.PrefManagerConstants.PREFS_NAME

class PreferencesManager(context: Context) {
    companion object {
        private const val CONTEXT_TAG = "PreferencesManager"
    }

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)


    fun saveToken(token: String) {
        Log.d(CONTEXT_TAG, "Saving token: ${token.take(5)}...")
        prefs.edit().putString(KEY_TOKEN, token).apply()
    }

    fun getToken(): String? {
        val token = prefs.getString(KEY_TOKEN, null)
        Log.d(CONTEXT_TAG, "Retrieved token: ${token?.take(5)}...")
        return token
    }

    fun saveUserId(userId: String) {
        Log.d(CONTEXT_TAG, "Saving userId: $userId")
        prefs.edit().putString(KEY_USER_ID, userId).apply()
    }

    fun getUserId(): String? {
        val userId = prefs.getString(KEY_USER_ID, null)
        Log.d(CONTEXT_TAG, "Retrieved userId: $userId")
        return userId
    }

    fun saveUserLogin(login: String) {
        Log.d(CONTEXT_TAG, "Saving login: $login")
        prefs.edit().putString(KEY_USER_LOGIN, login).apply()
    }

    fun getUserLogin(): String? {
        val login = prefs.getString(KEY_USER_LOGIN, null)
        Log.d(CONTEXT_TAG, "Retrieved login: $login")
        return login
    }

    fun clearAll() {
        Log.d(CONTEXT_TAG, "Clearing all preferences")
        prefs.edit().clear().apply()
    }
}