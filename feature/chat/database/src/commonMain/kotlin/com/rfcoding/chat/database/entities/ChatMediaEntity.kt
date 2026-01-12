package com.rfcoding.chat.database.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.rfcoding.chat.database.model.MediaStatus
import com.rfcoding.chat.domain.models.MediaType

@Entity(
    tableName = "chat_medias",
    foreignKeys = [
        ForeignKey(
            entity = ChatMessageEntity::class,
            parentColumns = ["id"],
            childColumns = ["messageId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["name"], unique = true)
    ]
)
data class ChatMediaEntity(
    val messageId: String,
    val name: String,
    val bytes: ByteArray,
    val progress: Float,
    val type: MediaType,
    val status: MediaStatus = MediaStatus.SENDING,
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as ChatMediaEntity

        if (progress != other.progress) return false
        if (id != other.id) return false
        if (messageId != other.messageId) return false
        if (name != other.name) return false
        if (!bytes.contentEquals(other.bytes)) return false
        if (type != other.type) return false
        if (status != other.status) return false

        return true
    }

    override fun hashCode(): Int {
        var result = progress.hashCode()
        result = 31 * result + id.hashCode()
        result = 31 * result + messageId.hashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + bytes.contentHashCode()
        result = 31 * result + type.hashCode()
        result = 31 * result + status.hashCode()
        return result
    }
}
