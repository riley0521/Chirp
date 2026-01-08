package com.rfcoding.chat.presentation.chat_detail.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import chirp.core.designsystem.generated.resources.clip_icon
import coil3.compose.AsyncImage
import org.jetbrains.compose.resources.vectorResource
import chirp.core.designsystem.generated.resources.Res as DesignSystemRes

@Composable
fun MessageThumbnails(
    urls: List<String>,
    onImageClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    if (urls.isNotEmpty()) {
        FlowRow(
            modifier = modifier,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            urls.forEach { url ->
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .clickable(
                            enabled = true,
                            onClick = {
                                onImageClick(url)
                            }
                        )
                        .background(MaterialTheme.colorScheme.onBackground),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = vectorResource(DesignSystemRes.drawable.clip_icon),
                        contentDescription = null
                    )

                    AsyncImage(
                        model = url,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .matchParentSize()
                    )
                }

            }
        }
    }
}