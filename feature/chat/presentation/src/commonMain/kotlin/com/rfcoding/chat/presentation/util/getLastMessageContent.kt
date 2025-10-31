package com.rfcoding.chat.presentation.util

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import chirp.feature.chat.presentation.generated.resources.Res
import chirp.feature.chat.presentation.generated.resources.x_added_x_to_chat
import chirp.feature.chat.presentation.generated.resources.x_left_chat
import chirp.feature.chat.presentation.generated.resources.x_removed_x_to_chat
import chirp.feature.chat.presentation.generated.resources.x_sent_voice_chat
import chirp.feature.chat.presentation.generated.resources.x_sent_x_image
import com.rfcoding.chat.domain.models.ChatMessage
import com.rfcoding.chat.domain.models.ChatMessageEvent
import com.rfcoding.chat.domain.models.ChatMessageEventType
import com.rfcoding.core.designsystem.theme.extended
import com.rfcoding.core.presentation.util.UiText
import org.jetbrains.compose.resources.pluralStringResource

@Composable
fun getLastMessageContent(message: ChatMessage?, username: String, affectedUsernames: List<String>): UiText? {
    if (message == null) {
        return null
    }

    return when {
        message.isTextOnly || message.isTextWithImages -> {
            val value = buildAnnotatedString {
                withStyle(
                    style = SpanStyle(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.extended.textSecondary
                    )
                ) {
                    append("$username: ")
                }
                append(message.content)
            }

            UiText.StyledText(value)
        }
        message.isImagesOnly -> {
            val usernameSentImageStr = pluralStringResource(
                Res.plurals.x_sent_x_image,
                message.imageUrls.size,
                message.imageUrls.size,
                username
            )

            UiText.DynamicText(usernameSentImageStr)
        }
        message.isVoiceOverOnly -> UiText.Resource(Res.string.x_sent_voice_chat, arrayOf(username))
        message.isEvent && affectedUsernames.isNotEmpty() -> getDescriptiveMessageEvent(
            event = message.event!!,
            username = username,
            affectedUsernames = affectedUsernames
        )
        else -> null
    }
}

@Composable
fun getDescriptiveMessageEvent(event: ChatMessageEvent, username: String, affectedUsernames: List<String>): UiText {
    return when(event.type) {
        ChatMessageEventType.PARTICIPANTS_ADDED -> UiText.Resource(Res.string.x_added_x_to_chat, arrayOf(username, affectedUsernames.joinToString()))
        ChatMessageEventType.PARTICIPANT_REMOVED_BY_CREATOR -> UiText.Resource(Res.string.x_removed_x_to_chat, arrayOf(username, affectedUsernames.first()))
        ChatMessageEventType.PARTICIPANT_LEFT -> UiText.Resource(Res.string.x_left_chat, arrayOf(affectedUsernames.first()))
    }
}