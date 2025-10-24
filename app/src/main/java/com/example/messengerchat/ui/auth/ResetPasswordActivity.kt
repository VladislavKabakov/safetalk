package com.example.messengerchat.ui.auth

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.messengerchat.R
import com.example.messengerchat.constants.ErrorsTexts.ON_INVALID_LOGIN
import com.example.messengerchat.constants.ErrorsTexts.ON_INVALID_PASSWORD
import com.example.messengerchat.ui.viewmodels.AuthViewModel
import com.example.messengerchat.utils.Validators

class ResetPasswordActivity : AppCompatActivity() {
    private lateinit var viewModel: AuthViewModel

    private lateinit var etLogin: EditText
    private lateinit var etNewPassword: EditText
    private lateinit var etKeyword: EditText
    private lateinit var btnReset: Button
    private lateinit var btnBack: ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reset_password)

        viewModel = ViewModelProvider(this)[AuthViewModel::class.java]

        initViews()
        observeViewModel()
    }

    private fun initViews() {
        etLogin = findViewById(R.id.etLogin)
        etNewPassword = findViewById(R.id.etNewPassword)
        etKeyword = findViewById(R.id.etKeyword)
        btnReset = findViewById(R.id.btnReset)
        btnBack = findViewById(R.id.btnBack)

        btnBack.setOnClickListener {
            finish()
        }

        btnReset.setOnClickListener {
            val login = etLogin.text.toString()
            val password = etNewPassword.text.toString()
            val keyword = etKeyword.text.toString()

            if (validateInput(login, password, keyword)) {
                viewModel.resetPassword(login, password, keyword)
            }
        }
    }

    private fun validateInput(login: String, password: String, keyword: String): Boolean {
        var isValid = true

        if (!Validators.validateLogin(login)) {
            etLogin.error = ON_INVALID_LOGIN
            isValid = false
        }

        if (!Validators.validatePassword(password)) {
            etNewPassword.error = ON_INVALID_PASSWORD
            isValid = false
        }

        if (!Validators.validateKeyword(keyword)) {
            etKeyword.error = ON_INVALID_PASSWORD
            isValid = false
        }

        return isValid
    }

    private fun observeViewModel() {
        viewModel.resetPasswordResult.observe(this) { result ->
            result.fold(
                onSuccess = { message ->
                    Toast.makeText(this, message, Toast.LENGTH_LONG).show()
                    finish()
                },
                onFailure = { error ->
                    Toast.makeText(this, "Reset failed: ${error.message}", Toast.LENGTH_LONG).show()
                }
            )
        }
    }
}