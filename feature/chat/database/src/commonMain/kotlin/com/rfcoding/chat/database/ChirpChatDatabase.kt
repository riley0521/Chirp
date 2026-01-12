package com.rfcoding.chat.database

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.rfcoding.chat.database.converter.Converters
import com.rfcoding.chat.database.dao.ChatDao
import com.rfcoding.chat.database.dao.ChatMediaDao
import com.rfcoding.chat.database.dao.ChatMessageDao
import com.rfcoding.chat.database.dao.ChatParticipantCrossRefDao
import com.rfcoding.chat.database.dao.ChatParticipantDao
import com.rfcoding.chat.database.entities.ChatEntity
import com.rfcoding.chat.database.entities.ChatMediaEntity
import com.rfcoding.chat.database.entities.ChatMessageEntity
import com.rfcoding.chat.database.entities.ChatParticipantCrossRef
import com.rfcoding.chat.database.entities.ChatParticipantEntity
import com.rfcoding.chat.database.view.LastMessageView

@Database(
    entities = [
        ChatEntity::class,
        ChatMessageEntity::class,
        ChatParticipantEntity::class,
        ChatParticipantCrossRef::class,
        ChatMediaEntity::class
    ],
    views = [
        LastMessageView::class
    ],
    version = 1
)
@TypeConverters(Converters::class)
@ConstructedBy(ChirpChatDatabaseConstructor::class)
abstract class ChirpChatDatabase : RoomDatabase() {

    abstract val chatDao: ChatDao
    abstract val chatMessageDao: ChatMessageDao
    abstract val chatParticipantDao: ChatParticipantDao
    abstract val chatParticipantCrossRefDao: ChatParticipantCrossRefDao
    abstract val chatMediaDao: ChatMediaDao

    companion object {
        const val DB_NAME = "chirp.db"
    }
}