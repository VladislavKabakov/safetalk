package com.example.messengerchat.constants

object HttpClientConstants {
    // URL
    const val BASE_URL = "http://10.0.2.2:8080/" // for emulator, use 10.0.2.2 for localhost

    // auth
    const val AUTH_HEADER = "Authorization"

    // timeouts
    const val CONNECT_TIMEOUT = 30L
    const val READ_TIMEOUT = 30L
    const val WRITE_TIMEOUT = 30L
}