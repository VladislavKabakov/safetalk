package com.example.messengerchat.ui.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.messengerchat.R
import com.example.messengerchat.constants.ErrorsTexts.ON_INVALID_LOGIN
import com.example.messengerchat.constants.ErrorsTexts.ON_EMPTY_PASSWORD
import com.example.messengerchat.data.api.RetrofitClient
import com.example.messengerchat.data.preferences.PreferencesManager
import com.example.messengerchat.ui.chat.ChatListActivity
import com.example.messengerchat.ui.viewmodels.AuthViewModel
import com.example.messengerchat.utils.Validators

class LoginActivity : AppCompatActivity() {
    private lateinit var viewModel: AuthViewModel
    private lateinit var prefsManager: PreferencesManager

    private lateinit var etLogin: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnLogin: Button
    private lateinit var tvRegister: TextView
    private lateinit var tvForgotPassword: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        viewModel = ViewModelProvider(this)[AuthViewModel::class.java]
        prefsManager = PreferencesManager(this)

        initViews()
        observeViewModel()
    }

    private fun initViews() {
        etLogin = findViewById(R.id.etLogin)
        etPassword = findViewById(R.id.etPassword)
        btnLogin = findViewById(R.id.btnLogin)
        tvRegister = findViewById(R.id.tvRegister)
        tvForgotPassword = findViewById(R.id.tvForgotPassword)

        btnLogin.setOnClickListener {
            val login = etLogin.text.toString().trim()
            val password = etPassword.text.toString()

            if (validateInput(login, password)) {
                btnLogin.isEnabled = false
                viewModel.signIn(login, password)
            }
        }

        tvRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        tvForgotPassword.setOnClickListener {
            startActivity(Intent(this, ResetPasswordActivity::class.java))
        }
    }

    private fun validateInput(login: String, password: String): Boolean {
        var isValid = true

        if (!Validators.validateLogin(login)) {
            etLogin.error = ON_INVALID_LOGIN
            isValid = false
        }

        if (password.isEmpty()) {
            etPassword.error = ON_EMPTY_PASSWORD
            isValid = false
        }

        return isValid
    }

    private fun observeViewModel() {
        viewModel.signInResult.observe(this) { result ->
            btnLogin.isEnabled = true

            result.fold(
                onSuccess = { (token, userId) ->
                    if (token.isNotBlank() && userId.isNotBlank()) {
                        prefsManager.saveToken(token)
                        prefsManager.saveUserId(userId)
                        prefsManager.saveUserLogin(etLogin.text.toString().trim())
                        RetrofitClient.setAuthToken(token)

                        startActivity(Intent(this, ChatListActivity::class.java))
                        finish()
                    } else {
                        Toast.makeText(this, "Invalid response from server", Toast.LENGTH_LONG).show()
                    }
                },
                onFailure = { error ->
                    Toast.makeText(this, "Login failed: ${error.message}", Toast.LENGTH_LONG).show()
                }
            )
        }
    }
}