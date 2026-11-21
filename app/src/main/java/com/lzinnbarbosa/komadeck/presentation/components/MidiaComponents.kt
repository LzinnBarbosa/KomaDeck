package com.lzinnbarbosa.komadeck.presentation.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoStories
import androidx.compose.material.icons.rounded.CloudOff
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.SmartDisplay
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.Tv
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import com.lzinnbarbosa.komadeck.R
import com.lzinnbarbosa.komadeck.domain.model.CategoriaMidia
import com.lzinnbarbosa.komadeck.domain.model.MidiaItem
import com.lzinnbarbosa.komadeck.presentation.theme.AnimeBadgeContainer
import com.lzinnbarbosa.komadeck.presentation.theme.AnimeBadgeContent
import com.lzinnbarbosa.komadeck.presentation.theme.CardBorderLight
import com.lzinnbarbosa.komadeck.presentation.theme.MangaBadgeContainer
import com.lzinnbarbosa.komadeck.presentation.theme.MangaBadgeContent
import com.lzinnbarbosa.komadeck.presentation.theme.OfflineBannerBorder
import com.lzinnbarbosa.komadeck.presentation.theme.OfflineBannerContainer
import com.lzinnbarbosa.komadeck.presentation.theme.OfflineBannerContent
import com.lzinnbarbosa.komadeck.presentation.theme.SerieBadgeContainer
import com.lzinnbarbosa.komadeck.presentation.theme.SerieBadgeContent
import com.lzinnbarbosa.komadeck.presentation.theme.StarAmber
import com.lzinnbarbosa.komadeck.presentation.theme.SurfaceSubtle
import com.lzinnbarbosa.komadeck.presentation.theme.TextSecondarySlate
import java.util.Locale

fun formatarNota(nota: Double?): String {
    if (nota == null || nota <= 0.0) return "N/A"
    return String.format(Locale.forLanguageTag("pt-BR"), "%.1f", nota)
}

fun CategoriaMidia.iconeSemantico(): ImageVector = when (this) {
    CategoriaMidia.ANIME -> Icons.Rounded.SmartDisplay
    CategoriaMidia.MANGA -> Icons.Rounded.AutoStories
    CategoriaMidia.SERIE -> Icons.Rounded.Tv
}

fun CategoriaMidia.labelPtBr(): String = when (this) {
    CategoriaMidia.ANIME -> "Anime"
    CategoriaMidia.MANGA -> "Mangá"
    CategoriaMidia.SERIE -> "Série"
}

@Composable
fun CategoriaBadge(
    categoria: CategoriaMidia,
    modifier: Modifier = Modifier,
) {
    val (containerColor, contentColor) = when (categoria) {
        CategoriaMidia.ANIME -> AnimeBadgeContainer to AnimeBadgeContent
        CategoriaMidia.MANGA -> MangaBadgeContainer to MangaBadgeContent
        CategoriaMidia.SERIE -> SerieBadgeContainer to SerieBadgeContent
    }
    Surface(
        modifier = modifier,
        color = containerColor,
        contentColor = contentColor,
        shape = RoundedCornerShape(8.dp),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Icon(
                imageVector = categoria.iconeSemantico(),
                contentDescription = null,
                modifier = Modifier.size(13.dp),
            )
            Text(
                text = categoria.labelPtBr(),
                style = MaterialTheme.typography.labelMedium,
            )
        }
    }
}

@Composable
fun CapaMidia(
    imagemUrl: String,
    titulo: String,
    categoria: CategoriaMidia,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(SurfaceSubtle),
        contentAlignment = Alignment.Center,
    ) {
        if (imagemUrl.isBlank()) {
            Icon(
                imageVector = categoria.iconeSemantico(),
                contentDescription = titulo,
                tint = TextSecondarySlate,
                modifier = Modifier.size(32.dp),
            )
        } else {
            SubcomposeAsyncImage(
                model = imagemUrl,
                contentDescription = titulo,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
                loading = {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(SurfaceSubtle),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                },
                error = {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(SurfaceSubtle),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = categoria.iconeSemantico(),
                            contentDescription = titulo,
                            tint = TextSecondarySlate,
                            modifier = Modifier.size(28.dp),
                        )
                    }
                },
            )
        }
    }
}

@Composable
fun MidiaHorizontalCard(
    item: MidiaItem,
    onClick: () -> Unit,
    onAlternarFavorito: () -> Unit,
    modifier: Modifier = Modifier,
    mostrarStatusBiblioteca: Boolean = false,
) {
    val subtituloMetadados = buildList {
        if (item.estudioOuEmissora.isNotBlank()) add(item.estudioOuEmissora)
        if (item.generos.isNotEmpty()) add(item.generos.take(2).joinToString(" • "))
    }.joinToString(" · ")

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("midia_card_${item.id}")
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, CardBorderLight),
        tonalElevation = 0.dp,
        shadowElevation = 1.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CapaMidia(
                imagemUrl = item.imagemUrl,
                titulo = item.titulo,
                categoria = item.categoria,
                modifier = Modifier
                    .width(84.dp)
                    .height(126.dp),
            )

            Spacer(modifier = Modifier.width(14.dp))

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(5.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    CategoriaBadge(categoria = item.categoria)

                    Surface(
                        color = SurfaceSubtle,
                        shape = RoundedCornerShape(8.dp),
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(3.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Star,
                                contentDescription = stringResource(R.string.label_nota),
                                tint = StarAmber,
                                modifier = Modifier.size(14.dp),
                            )
                            Text(
                                text = formatarNota(item.nota),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                        }
                    }
                }

                Text(
                    text = item.titulo,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )

                if (subtituloMetadados.isNotBlank()) {
                    Text(
                        text = subtituloMetadados,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondarySlate,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }

                if (mostrarStatusBiblioteca) {
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(6.dp),
                    ) {
                        Text(
                            text = item.statusBiblioteca.rotulo,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        )
                    }
                } else {
                    Text(
                        text = item.status,
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Schedule,
                        contentDescription = stringResource(R.string.label_transmissao),
                        tint = TextSecondarySlate,
                        modifier = Modifier.size(14.dp),
                    )
                    Text(
                        text = item.horarioTransmissao,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondarySlate,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }

            Spacer(modifier = Modifier.width(4.dp))

            IconButton(
                onClick = onAlternarFavorito,
                modifier = Modifier
                    .minimumInteractiveComponentSize()
                    .testTag("favorite_button_${item.id}"),
            ) {
                Icon(
                    imageVector = if (item.isFavorito) {
                        Icons.Rounded.Favorite
                    } else {
                        Icons.Rounded.FavoriteBorder
                    },
                    contentDescription = if (item.isFavorito) {
                        stringResource(R.string.acao_desfavoritar)
                    } else {
                        stringResource(R.string.acao_favoritar)
                    },
                    tint = if (item.isFavorito) {
                        AnimeBadgeContent
                    } else {
                        TextSecondarySlate
                    },
                )
            }
        }
    }
}

@Composable
fun OfflineModeBanner(
    visible: Boolean,
    modifier: Modifier = Modifier,
) {
    AnimatedVisibility(
        visible = visible,
        enter = expandVertically() + fadeIn(),
        exit = shrinkVertically() + fadeOut(),
        modifier = modifier,
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp)
                .testTag("offline_banner"),
            color = OfflineBannerContainer,
            contentColor = OfflineBannerContent,
            border = BorderStroke(1.dp, OfflineBannerBorder),
            shape = RoundedCornerShape(12.dp),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Icon(
                    imageVector = Icons.Rounded.CloudOff,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                )
                Text(
                    text = stringResource(R.string.banner_modo_offline),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Medium,
                )
            }
        }
    }
}

@Composable
fun EstadoCarregandoView(
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .testTag("loading_state"),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            CircularProgressIndicator(
                color = MaterialTheme.colorScheme.primary,
            )
            Text(
                text = "Carregando catálogo…",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondarySlate,
            )
        }
    }
}

@Composable
fun EstadoVazioView(
    titulo: String,
    subtitulo: String,
    icone: ImageVector,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp)
            .testTag("empty_state"),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = icone,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(34.dp),
                )
            }
            Text(
                text = titulo,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
            )
            Text(
                text = subtitulo,
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondarySlate,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
fun EstadoErroView(
    mensagem: String,
    onTentarNovamente: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp)
            .testTag("error_state"),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.errorContainer),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Rounded.ErrorOutline,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(34.dp),
                )
            }
            Text(
                text = mensagem,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
            )
            Button(
                onClick = onTentarNovamente,
                modifier = Modifier
                    .minimumInteractiveComponentSize()
                    .testTag("retry_button"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = Color.White,
                ),
                shape = RoundedCornerShape(12.dp),
            ) {
                Icon(
                    imageVector = Icons.Rounded.Refresh,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = stringResource(R.string.acao_tentar_novamente))
            }
        }
    }
}