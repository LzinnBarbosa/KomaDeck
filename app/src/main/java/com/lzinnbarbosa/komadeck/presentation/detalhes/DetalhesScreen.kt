package com.lzinnbarbosa.komadeck.presentation.detalhes

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lzinnbarbosa.komadeck.R
import com.lzinnbarbosa.komadeck.domain.model.CategoriaMidia
import com.lzinnbarbosa.komadeck.domain.model.MidiaItem
import com.lzinnbarbosa.komadeck.domain.model.StatusBiblioteca
import com.lzinnbarbosa.komadeck.presentation.components.CapaMidia
import com.lzinnbarbosa.komadeck.presentation.components.CategoriaBadge
import com.lzinnbarbosa.komadeck.presentation.components.EstadoCarregandoView
import com.lzinnbarbosa.komadeck.presentation.components.EstadoErroView
import com.lzinnbarbosa.komadeck.presentation.components.EstadoVazioView
import com.lzinnbarbosa.komadeck.presentation.components.formatarNota
import com.lzinnbarbosa.komadeck.presentation.theme.AnimeBadgeContent
import com.lzinnbarbosa.komadeck.presentation.theme.CardBorderLight
import com.lzinnbarbosa.komadeck.presentation.theme.StarAmber
import com.lzinnbarbosa.komadeck.presentation.theme.SurfaceSubtle
import com.lzinnbarbosa.komadeck.presentation.theme.TextSecondarySlate

@Composable
fun DetalhesRoute(
    onVoltar: () -> Unit,
    viewModel: DetalhesViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    DetalhesScreen(
        uiState = uiState,
        onVoltar = onVoltar,
        onAlternarFavorito = viewModel::alternarFavorito,
        onAtualizarStatusBiblioteca = viewModel::atualizarStatusBiblioteca,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetalhesScreen(
    uiState: DetalhesUiState,
    onVoltar: () -> Unit,
    onAlternarFavorito: () -> Unit,
    onAtualizarStatusBiblioteca: (StatusBiblioteca) -> Unit = {},
) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 2.dp,
            ) {
                TopAppBar(
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        titleContentColor = MaterialTheme.colorScheme.onSurface,
                    ),
                    navigationIcon = {
                        IconButton(
                            onClick = onVoltar,
                            modifier = Modifier
                                .minimumInteractiveComponentSize()
                                .testTag("detalhes_back_button"),
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                                contentDescription = stringResource(R.string.acao_voltar),
                            )
                        }
                    },
                    title = {
                        Text(
                            text = stringResource(R.string.titulo_detalhes),
                            style = MaterialTheme.typography.titleLarge,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    },
                )
            }
        },
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.TopCenter,
        ) {
            when (uiState) {
                is DetalhesUiState.Loading -> {
                    EstadoCarregandoView()
                }

                is DetalhesUiState.Empty -> {
                    EstadoVazioView(
                        titulo = uiState.mensagem,
                        subtitulo = "Volte para o catálogo e selecione outra obra.",
                        icone = Icons.Rounded.Info,
                    )
                }

                is DetalhesUiState.Error -> {
                    EstadoErroView(
                        mensagem = uiState.mensagem,
                        onTentarNovamente = onVoltar,
                    )
                }

                is DetalhesUiState.Success -> {
                    DetalhesConteudo(
                        midia = uiState.midia,
                        onAlternarFavorito = onAlternarFavorito,
                        onAtualizarStatusBiblioteca = onAtualizarStatusBiblioteca,
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun DetalhesConteudo(
    midia: MidiaItem,
    onAlternarFavorito: () -> Unit,
    onAtualizarStatusBiblioteca: (StatusBiblioteca) -> Unit,
) {
    val possuiFichaTecnica = midia.generos.isNotEmpty() ||
            midia.totalEpisodiosOuCapitulos != null ||
            midia.estudioOuEmissora.isNotBlank() ||
            midia.anoLancamento != null

    Column(
        modifier = Modifier
            .widthIn(max = 680.dp)
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(20.dp),
            border = BorderStroke(1.dp, CardBorderLight),
            shadowElevation = 2.dp,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.Top,
                ) {
                    CapaMidia(
                        imagemUrl = midia.imagemUrl,
                        titulo = midia.titulo,
                        categoria = midia.categoria,
                        modifier = Modifier
                            .width(124.dp)
                            .height(182.dp),
                    )

                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        CategoriaBadge(categoria = midia.categoria)

                        Text(
                            text = midia.titulo,
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                        )

                        Surface(
                            color = SurfaceSubtle,
                            shape = RoundedCornerShape(10.dp),
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Star,
                                    contentDescription = stringResource(R.string.label_nota),
                                    tint = StarAmber,
                                    modifier = Modifier.size(18.dp),
                                )
                                Text(
                                    text = "${stringResource(R.string.label_nota)}: ${formatarNota(midia.nota)}",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.onSurface,
                                )
                            }
                        }

                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = stringResource(R.string.label_status),
                                style = MaterialTheme.typography.labelMedium,
                                color = TextSecondarySlate,
                            )
                            Text(
                                text = midia.status,
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.primary,
                            )
                        }
                    }
                }

                HorizontalDivider(color = CardBorderLight)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Schedule,
                        contentDescription = stringResource(R.string.label_transmissao),
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp),
                    )
                    Column {
                        Text(
                            text = stringResource(R.string.label_transmissao),
                            style = MaterialTheme.typography.labelMedium,
                            color = TextSecondarySlate,
                        )
                        Text(
                            text = midia.horarioTransmissao,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                }

                BotaoFavoritoDetalhes(
                    isFavorito = midia.isFavorito,
                    onClick = onAlternarFavorito,
                )
            }
        }

        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(20.dp),
            border = BorderStroke(1.dp, CardBorderLight),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Text(
                    text = stringResource(R.string.label_meu_progresso),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    StatusBiblioteca.entries.forEach { statusOption ->
                        val selecionado = midia.isFavorito && midia.statusBiblioteca == statusOption
                        FilterChip(
                            selected = selecionado,
                            onClick = { onAtualizarStatusBiblioteca(statusOption) },
                            label = {
                                Text(
                                    text = statusOption.rotulo,
                                    style = MaterialTheme.typography.labelLarge,
                                )
                            },
                            leadingIcon = if (selecionado) {
                                {
                                    Icon(
                                        imageVector = Icons.Rounded.Check,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp),
                                    )
                                }
                            } else {
                                null
                            },
                            modifier = Modifier.testTag("library_status_${statusOption.name.lowercase()}"),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = Color.White,
                                selectedLeadingIconColor = Color.White,
                                containerColor = SurfaceSubtle,
                                labelColor = MaterialTheme.colorScheme.onSurface,
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = selecionado,
                                borderColor = CardBorderLight,
                            ),
                            shape = RoundedCornerShape(10.dp),
                        )
                    }
                }
            }
        }

        if (possuiFichaTecnica) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.dp, CardBorderLight),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Text(
                        text = stringResource(R.string.label_ficha_tecnica),
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                    )

                    if (midia.generos.isNotEmpty()) {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = stringResource(R.string.label_generos),
                                style = MaterialTheme.typography.labelMedium,
                                color = TextSecondarySlate,
                            )
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp),
                            ) {
                                midia.generos.forEach { genero ->
                                    Surface(
                                        color = SurfaceSubtle,
                                        shape = RoundedCornerShape(8.dp),
                                    ) {
                                        Text(
                                            text = genero,
                                            style = MaterialTheme.typography.labelMedium,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        midia.totalEpisodiosOuCapitulos?.let { total ->
                            val rotulo = if (midia.categoria == CategoriaMidia.MANGA) {
                                stringResource(R.string.label_capitulos)
                            } else {
                                stringResource(R.string.label_episodios)
                            }
                            ItemFichaTecnica(
                                rotulo = rotulo,
                                valor = total.toString(),
                                modifier = Modifier.weight(1f),
                            )
                        }

                        midia.anoLancamento?.let { ano ->
                            ItemFichaTecnica(
                                rotulo = stringResource(R.string.label_ano),
                                valor = ano.toString(),
                                modifier = Modifier.weight(1f),
                            )
                        }
                    }

                    if (midia.estudioOuEmissora.isNotBlank()) {
                        val rotuloOrigem = if (midia.categoria == CategoriaMidia.SERIE) {
                            stringResource(R.string.label_emissora)
                        } else {
                            stringResource(R.string.label_estudio)
                        }
                        ItemFichaTecnica(
                            rotulo = rotuloOrigem,
                            valor = midia.estudioOuEmissora,
                        )
                    }
                }
            }
        }

        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(20.dp),
            border = BorderStroke(1.dp, CardBorderLight),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Text(
                    text = stringResource(R.string.label_sinopse),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = midia.sinopse.ifBlank { stringResource(R.string.sinopse_indisponivel) },
                    style = MaterialTheme.typography.bodyLarge,
                    color = TextSecondarySlate,
                )
            }
        }
    }
}

@Composable
private fun ItemFichaTecnica(
    rotulo: String,
    valor: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Text(
            text = rotulo,
            style = MaterialTheme.typography.labelMedium,
            color = TextSecondarySlate,
        )
        Text(
            text = valor,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
private fun BotaoFavoritoDetalhes(
    isFavorito: Boolean,
    onClick: () -> Unit,
) {
    if (isFavorito) {
        OutlinedButton(
            onClick = onClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("detalhes_favorite_button"),
            shape = RoundedCornerShape(14.dp),
            border = BorderStroke(1.5.dp, AnimeBadgeContent),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = AnimeBadgeContent,
            ),
        ) {
            Icon(
                imageVector = Icons.Rounded.Favorite,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = stringResource(R.string.acao_desfavoritar),
                style = MaterialTheme.typography.labelLarge,
            )
        }
    } else {
        Button(
            onClick = onClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("detalhes_favorite_button"),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White,
            ),
        ) {
            Icon(
                imageVector = Icons.Rounded.FavoriteBorder,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = stringResource(R.string.acao_favoritar),
                style = MaterialTheme.typography.labelLarge,
            )
        }
    }
}