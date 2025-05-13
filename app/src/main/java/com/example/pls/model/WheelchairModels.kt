package com.example.pls.model

data class WheelchairResponse(
    val id: String,
    val name: String,
    val status: String,
    val lastConnected: String?,
    val ipAddress: String?
)

data class WheelchairCommand(
    val wheelchairId: String,
    val command: String
)

data class CommandResponse(
    val success: Boolean,
    val message: String
)