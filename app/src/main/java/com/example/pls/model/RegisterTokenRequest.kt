package com.example.pls.model

data class RegisterTokenRequest(
    val userId: String,
    val token: String
)

data class RegisterTokenResponse(
    val success: Boolean,
    val message: String
)