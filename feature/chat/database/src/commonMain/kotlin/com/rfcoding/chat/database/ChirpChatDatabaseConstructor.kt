package com.rfcoding.chat.database

import androidx.room.RoomDatabaseConstructor

@Suppress("KotlinNoActualForExpect")
expect object ChirpChatDatabaseConstructor: RoomDatabaseConstructor<ChirpChatDatabase> {
    override fun initialize(): ChirpChatDatabase
}