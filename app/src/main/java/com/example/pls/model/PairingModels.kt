package com.example.pls.model

data class PairingResponse(
    val id: String,
    val wheelchairId: String,
    val userId: String,
    val status: String,
    val createdAt: String
)