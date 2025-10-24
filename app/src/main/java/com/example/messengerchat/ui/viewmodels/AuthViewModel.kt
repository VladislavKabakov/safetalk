package com.example.messengerchat.ui.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.messengerchat.data.repository.AuthRepository
import kotlinx.coroutines.launch

class AuthViewModel : ViewModel() {
    companion object {
        private const val REGISTRATION_SUCCESS = "Registration success"
        private const val REGISTRATION_FAILURE = "Login already exists"
        private const val LOGIN_FAILURE = "Incorrect login or password"
    }

    private val repository = AuthRepository()

    private val _signUpResult = MutableLiveData<Result<String>>()
    val signUpResult: LiveData<Result<String>> = _signUpResult

    private val _signInResult = MutableLiveData<Result<Pair<String, String>>>()
    val signInResult: LiveData<Result<Pair<String, String>>> = _signInResult

    private val _resetPasswordResult = MutableLiveData<Result<String>>()
    val resetPasswordResult: LiveData<Result<String>> = _resetPasswordResult

    fun signUp(login: String, password: String, keyword: String) {
        viewModelScope.launch {
            try {
                val response = repository.signUp(login, password, keyword)
                if (response.isSuccessful) {
                    _signUpResult.value = Result.success(REGISTRATION_SUCCESS)
                } else {
                    _signUpResult.value = Result.failure(Exception(REGISTRATION_FAILURE))
                }
            } catch (e: Exception) {
                _signUpResult.value = Result.failure(e)
            }
        }
    }

    fun signIn(login: String, password: String) {
        viewModelScope.launch {
            try {
                val response = repository.signIn(login, password)
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null) {
                        _signInResult.value = Result.success(
                            Pair(body.message.token, body.message.userId)
                        )
                    } else {
                        _signInResult.value = Result.failure(Exception("Empty response"))
                    }
                } else {
                    _signInResult.value = Result.failure(Exception(LOGIN_FAILURE))
                }
            } catch (e: Exception) {
                _signInResult.value = Result.failure(e)
            }
        }
    }

    fun resetPassword(login: String, password: String, keyword: String) {
        viewModelScope.launch {
            try {
                val response = repository.resetPassword(login, password, keyword)
                if (response.isSuccessful) {
                    _resetPasswordResult.value = Result.success(response.body()?.message ?: "Success")
                } else {
                    _resetPasswordResult.value = Result.failure(Exception("Invalid login or keyword"))
                }
            } catch (e: Exception) {
                _resetPasswordResult.value = Result.failure(e)
            }
        }
    }
}