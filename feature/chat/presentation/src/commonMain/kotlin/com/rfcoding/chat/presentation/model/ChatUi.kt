package com.rfcoding.chat.presentation.model

import androidx.compose.runtime.Composable
import chirp.feature.chat.presentation.generated.resources.Res
import chirp.feature.chat.presentation.generated.resources.account_deleted
import chirp.feature.chat.presentation.generated.resources.you
import com.rfcoding.chat.domain.models.ChatMessage
import com.rfcoding.core.designsystem.components.avatar.ChatParticipantUi
import org.jetbrains.compose.resources.stringResource

data class ChatUi(
    val id: String,
    val localParticipant: ChatParticipantUi,
    val participants: List<ChatParticipantUi?>,
    val lastMessage: ChatMessage?,
    val lastMessageUsername: String?,
    val affectedUsernamesForEvent: List<String>,
    val isGroupChat: Boolean,
    val creatorId: String?,
    private val name: String?
) {

    val isCreator: Boolean get() = localParticipant.id == creatorId

    val chatName: String
        @Composable
        get() {
            return if (isGroupChat) {
                val you = stringResource(Res.string.you)
                name ?: ("$you, " + participants.filterNotNull().joinToString { it.username })
            } else {
                participants.first()?.username ?: stringResource(Res.string.account_deleted)
            }
        }
}
