package com.example.messengerchat.ui.auth

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.messengerchat.R
import com.example.messengerchat.constants.ErrorsTexts.ON_INVALID_KEYWORD_FORMAT
import com.example.messengerchat.constants.ErrorsTexts.ON_INVALID_LOGIN
import com.example.messengerchat.constants.ErrorsTexts.ON_INVALID_LOGIN_FORMAT
import com.example.messengerchat.constants.ErrorsTexts.ON_INVALID_PASSWORD
import com.example.messengerchat.constants.ErrorsTexts.ON_INVALID_PASSWORD_FORMAT
import com.example.messengerchat.ui.viewmodels.AuthViewModel
import com.example.messengerchat.utils.Validators

class RegisterActivity : AppCompatActivity() {

    private lateinit var viewModel: AuthViewModel

    private lateinit var etLogin: EditText
    private lateinit var etPassword: EditText
    private lateinit var etKeyword: EditText
    private lateinit var btnRegister: Button
    private lateinit var btnBack: ImageButton


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        viewModel = ViewModelProvider(this)[AuthViewModel::class.java]

        initViews()
        observeViewModel()
    }

    private fun initViews() {
        etLogin = findViewById(R.id.etLogin)
        etPassword = findViewById(R.id.etPassword)
        etKeyword = findViewById(R.id.etKeyword)
        btnRegister = findViewById(R.id.btnRegister)
        btnBack = findViewById(R.id.btnBack)

        etLogin.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                if (!Validators.validateLogin(s.toString())) {
                    etLogin.error = ON_INVALID_LOGIN
                }
            }
        })

        etPassword.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                if (!Validators.validatePassword(s.toString())) {
                    etPassword.error = ON_INVALID_PASSWORD
                }
            }
        })

        etKeyword.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                if (!Validators.validateKeyword(s.toString())) {
                    etKeyword.error = ON_INVALID_PASSWORD
                }
            }
        })

        btnBack.setOnClickListener {
            finish()
        }

        btnRegister.setOnClickListener {
            val login = etLogin.text.toString()
            val password = etPassword.text.toString()
            val keyword = etKeyword.text.toString()

            if (validateInput(login, password, keyword)) {
                viewModel.signUp(login, password, keyword)
            }
        }
    }

    private fun validateInput(login: String, password: String, keyword: String): Boolean {
        var isValid = true

        if (!Validators.validateLogin(login)) {
            etLogin.error = ON_INVALID_LOGIN_FORMAT
            isValid = false
        }

        if (!Validators.validatePassword(password)) {
            etPassword.error = ON_INVALID_PASSWORD_FORMAT
            isValid = false
        }

        if (!Validators.validateKeyword(keyword)) {
            etKeyword.error = ON_INVALID_KEYWORD_FORMAT
            isValid = false
        }

        return isValid
    }

    private fun observeViewModel() {
        viewModel.signUpResult.observe(this) { result ->
            result.fold(
                onSuccess = { message ->
                    Toast.makeText(this, message, Toast.LENGTH_LONG).show()
                    finish()
                },
                onFailure = { error ->
                    Toast.makeText(this, "Registration failed: ${error.message}", Toast.LENGTH_LONG).show()
                }
            )
        }
    }
}