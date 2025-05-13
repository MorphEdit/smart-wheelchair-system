// PairWithCodeResponse.kt
package com.example.pls.model

data class PairWithCodeResponse(
    val success: Boolean,
    val pairing: PairingData?,
    val error: String?
)

data class PairingData(
    val id: String,
    val wheelchairId: String,
    val userId: String,
    val status: String,
    val createdAt: String
)