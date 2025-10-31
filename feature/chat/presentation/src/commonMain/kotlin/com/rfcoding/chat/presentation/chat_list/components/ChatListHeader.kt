package com.rfcoding.chat.presentation.chat_list.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import chirp.core.designsystem.generated.resources.log_out_icon
import chirp.core.designsystem.generated.resources.users_icon
import chirp.feature.chat.presentation.generated.resources.Res
import chirp.feature.chat.presentation.generated.resources.logout
import chirp.feature.chat.presentation.generated.resources.profile_settings
import com.rfcoding.chat.presentation.components.ChatHeader
import com.rfcoding.core.designsystem.components.avatar.ChatParticipantUi
import com.rfcoding.core.designsystem.components.avatar.ChirpAvatarPhoto
import com.rfcoding.core.designsystem.components.brand.ChirpBrandLogo
import com.rfcoding.core.designsystem.components.brand.ChirpHorizontalDivider
import com.rfcoding.core.designsystem.theme.ChirpTheme
import com.rfcoding.core.designsystem.theme.extended
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.resources.vectorResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import chirp.core.designsystem.generated.resources.Res as DesignSystemRes

@Composable
fun ChatListHeader(
    localParticipant: ChatParticipantUi,
    isMenuOpen: Boolean,
    onUserAvatarClick: () -> Unit,
    onDismissMenu: () -> Unit,
    onProfileSettingsClick: () -> Unit,
    onLogoutClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    ChatHeader(
        modifier = modifier
            .fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ChirpBrandLogo(
                tint = MaterialTheme.colorScheme.tertiary
            )
            Text(
                text = "Chirp",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.extended.textPrimary
            )
            Spacer(modifier = Modifier.weight(1f))
            ProfileAvatarSection(
                localParticipant = localParticipant,
                isMenuOpen = isMenuOpen,
                onClick = onUserAvatarClick,
                onDismissMenu = onDismissMenu,
                onProfileSettingsClick = onProfileSettingsClick,
                onLogoutClick = onLogoutClick
            )
        }
    }
}

@Composable
fun ProfileAvatarSection(
    localParticipant: ChatParticipantUi,
    isMenuOpen: Boolean,
    onClick: () -> Unit,
    onDismissMenu: () -> Unit,
    onProfileSettingsClick: () -> Unit,
    onLogoutClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
    ) {
        ChirpAvatarPhoto(
            displayText = localParticipant.initial,
            imageUrl = localParticipant.imageUrl,
            onClick = onClick
        )

        DropdownMenu(
            expanded = isMenuOpen,
            onDismissRequest = onDismissMenu,
            containerColor = MaterialTheme.colorScheme.surface,
            border = BorderStroke(
                width = 1.dp,
                color = MaterialTheme.colorScheme.extended.surfaceOutline
            ),
            shape = RoundedCornerShape(16.dp),
            offset = DpOffset(x = 0.dp, y = 8.dp)
        ) {
            DropdownMenuItem(
                text = {
                    val profileSettingsStr = stringResource(Res.string.profile_settings)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.semantics(mergeDescendants = true) {
                            contentDescription = profileSettingsStr
                        }
                    ) {
                        Icon(
                            imageVector = vectorResource(DesignSystemRes.drawable.users_icon),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.extended.textSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = profileSettingsStr,
                            color = MaterialTheme.colorScheme.extended.textSecondary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                },
                onClick = {
                    onDismissMenu()
                    onProfileSettingsClick()
                }
            )
            ChirpHorizontalDivider()
            DropdownMenuItem(
                text = {
                    val logoutStr = stringResource(Res.string.logout)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.semantics(mergeDescendants = true) {
                            contentDescription = logoutStr
                        }
                    ) {
                        Icon(
                            imageVector = vectorResource(DesignSystemRes.drawable.log_out_icon),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.extended.destructiveHover,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = logoutStr,
                            color = MaterialTheme.colorScheme.extended.destructiveHover,
                            fontWeight = FontWeight.Medium
                        )
                    }
                },
                onClick = {
                    onDismissMenu()
                    onLogoutClick()
                }
            )
        }
    }
}

@Composable
@Preview(showBackground = true)
private fun ChatListHeaderPreview() {
    ChirpTheme {
        var isMenuOpen by remember { mutableStateOf(false) }

        Box(
            modifier = Modifier
                .fillMaxSize()
        ) {
            ChatListHeader(
                localParticipant = ChatParticipantUi(
                    id = "1",
                    username = "chinley1",
                    initial = "CH",
                    imageUrl = null
                ),
                isMenuOpen = isMenuOpen,
                onUserAvatarClick = {
                    isMenuOpen = !isMenuOpen
                },
                onDismissMenu = {
                    isMenuOpen = false
                },
                onProfileSettingsClick = {
                    isMenuOpen = false
                },
                onLogoutClick = {
                    isMenuOpen = false
                }
            )
        }
    }
}