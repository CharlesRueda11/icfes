package com.charlesdev.icfes.student.duelo.utils



import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.charlesdev.icfes.student.duelo.Player
import com.charlesdev.icfes.student.duelo.Team
import com.charlesdev.icfes.R

import kotlin.collections.chunked
import kotlin.collections.forEach
import kotlin.collections.isNotEmpty
import kotlin.text.isNotEmpty
import kotlin.text.take

// ✅ COMPONENTE SEGURO: Solo muestra avatar, no modifica lógica de juego
// ✅ SOLO REEMPLAZA LA FUNCIÓN PlayerAvatar EN TU PlayerAvatarComponents.kt

@Composable
fun PlayerAvatar(
    player: Player,
    teamColor: Color,
    size: Dp = 40.dp,
    showOnlineIndicator: Boolean = false,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Box(
        modifier = modifier.size(size)
    ) {
        // ✅ EXACTAMENTE LA MISMA LÓGICA QUE EN HOMESCREEN
        if (player.profileImageUrl.isNotEmpty()) {
            val painter = rememberAsyncImagePainter(
                ImageRequest.Builder(context)
                    .data(player.profileImageUrl)
                    .crossfade(true)
                    .placeholder(R.drawable.ic_face)
                    .error(R.drawable.ic_face)
                    .build()
            )

            Image(
                painter = painter,
                contentDescription = "Foto de perfil de ${player.name}",
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
        } else {
            // ✅ FALLBACK: Avatar con iniciales (mismo que tienes)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(teamColor.copy(alpha = 0.7f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = player.getInitials(),
                    color = Color.White,
                    fontSize = (size.value * 0.35f).sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // ✅ MANTENER IGUAL: Indicador online
        if (showOnlineIndicator) {
            Box(
                modifier = Modifier
                    .size((size.value * 0.3f).dp)
                    .background(Color(0xFF4CAF50), CircleShape)
                    .align(Alignment.BottomEnd)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(1.dp)
                        .background(Color.White, CircleShape)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(1.dp)
                            .background(Color(0xFF4CAF50), CircleShape)
                    )
                }
            }
        }
    }
}

// ✅ COMPONENTE SEGURO: Solo mejora visual del PlayerItem, no toca lógica
@Composable
fun EnhancedPlayerItem(
    player: Player,
    team: String,
    teamColor: Color,
    isMe: Boolean,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isMe)
                teamColor.copy(alpha = 0.15f)
            else
                Color.White
        ),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isMe) 6.dp else 2.dp
        ),
        border = if (isMe)
            BorderStroke(2.dp, teamColor)
        else
            BorderStroke(1.dp, Color(0xFFE0E0E0))
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            // Avatar con foto de perfil o iniciales
            PlayerAvatar(
                player = player,
                teamColor = teamColor,
                size = 42.dp,
                showOnlineIndicator = true
            )

            Spacer(modifier = Modifier.width(12.dp))

            // Información del jugador
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = player.name,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = if (isMe) FontWeight.Bold else FontWeight.Medium,
                        color = if (isMe) teamColor else Color(0xFF424242),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )

                    if (isMe) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            color = teamColor,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "TÚ",
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }

                // Email truncado si es diferente al nombre
                if (player.email.isNotEmpty() && player.email != player.name) {
                    Text(
                        text = player.email.take(25) + if (player.email.length > 25) "..." else "",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF666666),
                        fontSize = 11.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Badge del equipo
            Surface(
                color = teamColor,
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = "EQUIPO $team",
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}

// ✅ COMPONENTE SEGURO: Chip compacto para resúmenes
@Composable
fun PlayerAvatarChip(
    player: Player,
    teamColor: Color,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        // Avatar pequeño
        PlayerAvatar(
            player = player,
            teamColor = teamColor,
            size = 32.dp,
            showOnlineIndicator = false
        )

        Spacer(modifier = Modifier.height(4.dp))

        // Nombre truncado
        Text(
            text = player.name.take(8) + if (player.name.length > 8) "..." else "",
            fontSize = 9.sp,
            color = teamColor,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

// ✅ COMPONENTE SEGURO: Resumen visual de equipo con avatares
@Composable
fun TeamSummaryWithAvatars(
    team: Team,
    teamName: String,
    teamLetter: String,
    teamColor: Color,
    isBalanced: Boolean,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = teamColor.copy(alpha = if (isBalanced) 0.15f else 0.1f)
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(if (isBalanced) 6.dp else 4.dp),
        border = BorderStroke(
            2.dp,
            teamColor.copy(alpha = if (isBalanced) 0.7f else 0.5f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header del equipo
            Text(
                text = teamName,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = teamColor
            )

            Text(
                text = "${team.players.size}/4 jugadores",
                fontSize = 12.sp,
                color = teamColor.copy(alpha = 0.8f),
                modifier = Modifier.padding(bottom = 12.dp)
            )

            // Grid de avatares
            if (team.players.isNotEmpty()) {
                // Organizar en filas de 2
                val rows = team.players.chunked(2)

                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    rows.forEach { playersInRow ->
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            playersInRow.forEach { player ->
                                PlayerAvatarChip(
                                    player = player,
                                    teamColor = teamColor
                                )
                            }
                        }
                    }
                }
            } else {
                // Placeholder cuando no hay jugadores
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp)
                        .background(
                            teamColor.copy(alpha = 0.1f),
                            RoundedCornerShape(8.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.PersonAdd,
                            contentDescription = "Sin jugadores",
                            tint = teamColor.copy(alpha = 0.6f),
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "Sin jugadores",
                            fontSize = 11.sp,
                            color = teamColor.copy(alpha = 0.8f)
                        )
                    }
                }
            }

            // Indicador de estado listo
            if (isBalanced) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = "Equipo listo",
                        tint = Color(0xFF4CAF50),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "¡Listo!",
                        fontSize = 12.sp,
                        color = Color(0xFF4CAF50),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}