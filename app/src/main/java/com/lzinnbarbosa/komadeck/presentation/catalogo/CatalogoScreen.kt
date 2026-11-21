package com.lzinnbarbosa.komadeck.presentation.catalogo

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoStories
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.SmartDisplay
import androidx.compose.material.icons.rounded.Sync
import androidx.compose.material.icons.rounded.Tv
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lzinnbarbosa.komadeck.R
import com.lzinnbarbosa.komadeck.domain.model.MidiaItem
import com.lzinnbarbosa.komadeck.presentation.components.EstadoCarregandoView
import com.lzinnbarbosa.komadeck.presentation.components.EstadoErroView
import com.lzinnbarbosa.komadeck.presentation.components.EstadoVazioView
import com.lzinnbarbosa.komadeck.presentation.components.MidiaHorizontalCard
import com.lzinnbarbosa.komadeck.presentation.components.OfflineModeBanner
import com.lzinnbarbosa.komadeck.presentation.theme.CardBorderLight
import com.lzinnbarbosa.komadeck.presentation.theme.TextSecondarySlate

@Composable
fun CatalogoRoute(
    onAbrirDetalhes: (String) -> Unit,
    viewModel: CatalogoViewModel = hiltViewModel(),
) {
    val screenState by viewModel.screenState.collectAsStateWithLifecycle()

    CatalogoScreen(
        screenState = screenState,
        onSelecionarAba = viewModel::selecionarAba,
        onQueryChange = viewModel::onQueryChange,
        onLimparBusca = viewModel::limparBusca,
        onSelecionarFiltroBusca = viewModel::selecionarFiltroBusca,
        onSelecionarOrdenacao = viewModel::selecionarOrdenacao,
        onSelecionarFiltroStatusBiblioteca = viewModel::selecionarFiltroStatusBiblioteca,
        onSincronizarManual = viewModel::sincronizarManual,
        onAlternarFavorito = viewModel::alternarFavorito,
        onItemClick = { item ->
            viewModel.prepararDetalhes(item)
            onAbrirDetalhes(item.id)
        },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CatalogoScreen(
    screenState: CatalogoScreenState,
    onSelecionarAba: (AbaCatalogo) -> Unit,
    onQueryChange: (String) -> Unit,
    onLimparBusca: () -> Unit,
    onSelecionarFiltroBusca: (FiltroBusca) -> Unit,
    onSelecionarOrdenacao: (FiltroOrdenacao) -> Unit,
    onSelecionarFiltroStatusBiblioteca: (FiltroStatusBiblioteca) -> Unit,
    onSincronizarManual: () -> Unit,
    onAlternarFavorito: (MidiaItem) -> Unit,
    onItemClick: (MidiaItem) -> Unit,
) {
    val focusManager = LocalFocusManager.current
    val tituloAtual = if (screenState.isModoBuscaAtivo) {
        "Busca Unificada"
    } else {
        when (screenState.abaSelecionada) {
            AbaCatalogo.ANIMES -> stringResource(R.string.titulo_animes_temporada)
            AbaCatalogo.MANGAS -> stringResource(R.string.titulo_top_mangas)
            AbaCatalogo.SERIES -> stringResource(R.string.titulo_series_dia)
            AbaCatalogo.FAVORITOS -> stringResource(R.string.titulo_favoritos_offline)
        }
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val isTelaExpandida = maxWidth >= 840.dp

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = MaterialTheme.colorScheme.background,
            contentWindowInsets = WindowInsets.safeDrawing,
            topBar = {
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    shadowElevation = 2.dp,
                ) {
                    Column {
                        TopAppBar(
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = MaterialTheme.colorScheme.surface,
                                titleContentColor = MaterialTheme.colorScheme.onSurface,
                            ),
                            title = {
                                Column {
                                    Text(
                                        text = stringResource(R.string.app_name),
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.primary,
                                    )
                                    Text(
                                        text = tituloAtual,
                                        style = MaterialTheme.typography.titleLarge,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                }
                            },
                            actions = {
                                val podeSincronizar =
                                    screenState.isModoBuscaAtivo ||
                                            screenState.abaSelecionada != AbaCatalogo.FAVORITOS
                                if (podeSincronizar) {
                                    SincronizarIconButton(
                                        isSincronizando = screenState.isSincronizando,
                                        onClick = onSincronizarManual,
                                    )
                                }
                            },
                        )

                        if (screenState.isSincronizando) {
                            LinearProgressIndicator(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(2.dp),
                                color = MaterialTheme.colorScheme.primary,
                                trackColor = MaterialTheme.colorScheme.primaryContainer,
                            )
                        }
                    }
                }
            },
            bottomBar = {
                if (!isTelaExpandida) {
                    KomaDeckNavigationBar(
                        abaSelecionada = screenState.abaSelecionada,
                        onSelecionarAba = onSelecionarAba,
                    )
                }
            },
        ) { innerPadding ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
            ) {
                if (isTelaExpandida) {
                    KomaDeckNavigationRail(
                        abaSelecionada = screenState.abaSelecionada,
                        onSelecionarAba = onSelecionarAba,
                    )
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    contentAlignment = Alignment.TopCenter,
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxHeight()
                            .widthIn(max = 680.dp)
                            .fillMaxWidth(),
                    ) {
                        BarraBuscaUnificada(
                            query = screenState.queryBusca,
                            onQueryChange = onQueryChange,
                            onLimparBusca = {
                                onLimparBusca()
                                focusManager.clearFocus()
                            },
                            onImeSearch = { focusManager.clearFocus() },
                        )

                        if (screenState.isModoBuscaAtivo && screenState.queryBusca.trim().length >= 3) {
                            FiltrosBuscaRow(
                                filtroSelecionado = screenState.filtroBusca,
                                onSelecionarFiltro = onSelecionarFiltroBusca,
                            )
                        } else if (!screenState.isModoBuscaAtivo) {
                            if (screenState.abaSelecionada == AbaCatalogo.FAVORITOS) {
                                FiltrosFavoritosRow(
                                    filtroSelecionado = screenState.filtroStatusBiblioteca,
                                    onSelecionarFiltro = onSelecionarFiltroStatusBiblioteca,
                                )
                            } else {
                                FiltrosOrdenacaoRow(
                                    ordenacaoSelecionada = screenState.filtroOrdenacao,
                                    onSelecionarOrdenacao = onSelecionarOrdenacao,
                                )
                            }
                        }

                        OfflineModeBanner(visible = screenState.isModoOffline)

                        ConteudoCatalogo(
                            screenState = screenState,
                            onItemClick = onItemClick,
                            onAlternarFavorito = onAlternarFavorito,
                            onTentarNovamente = onSincronizarManual,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SincronizarIconButton(
    isSincronizando: Boolean,
    onClick: () -> Unit,
) {
    val infiniteTransition = rememberInfiniteTransition(label = "sync_rotation")
    val angulo by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "sync_angle",
    )

    IconButton(
        onClick = onClick,
        enabled = !isSincronizando,
        modifier = Modifier
            .minimumInteractiveComponentSize()
            .testTag("sync_button"),
    ) {
        Icon(
            imageVector = Icons.Rounded.Sync,
            contentDescription = stringResource(R.string.acao_sincronizar),
            tint = MaterialTheme.colorScheme.primary,
            modifier = if (isSincronizando) Modifier.rotate(angulo) else Modifier,
        )
    }
}

@Composable
private fun BarraBuscaUnificada(
    query: String,
    onQueryChange: (String) -> Unit,
    onLimparBusca: () -> Unit,
    onImeSearch: () -> Unit,
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp)
            .testTag("search_input"),
        placeholder = {
            Text(
                text = stringResource(R.string.busca_placeholder),
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondarySlate,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        },
        leadingIcon = {
            Icon(
                imageVector = Icons.Rounded.Search,
                contentDescription = stringResource(R.string.busca_icone_desc),
                tint = MaterialTheme.colorScheme.primary,
            )
        },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(
                    onClick = onLimparBusca,
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .testTag("clear_search_button"),
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = stringResource(R.string.busca_limpar_desc),
                        tint = TextSecondarySlate,
                    )
                }
            }
        },
        singleLine = true,
        shape = RoundedCornerShape(14.dp),
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(onSearch = { onImeSearch() }),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.surface,
            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = CardBorderLight,
        ),
    )
}

@Composable
private fun FiltrosBuscaRow(
    filtroSelecionado: FiltroBusca,
    onSelecionarFiltro: (FiltroBusca) -> Unit,
) {
    val opcoes = listOf(
        Triple(FiltroBusca.TODOS, stringResource(R.string.filtro_todos), "filter_chip_todos"),
        Triple(FiltroBusca.ANIMES, stringResource(R.string.filtro_animes), "filter_chip_animes"),
        Triple(FiltroBusca.MANGAS, stringResource(R.string.filtro_mangas), "filter_chip_mangas"),
        Triple(FiltroBusca.SERIES, stringResource(R.string.filtro_series), "filter_chip_series"),
    )
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp),
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(opcoes) { (filtro, label, tag) ->
            ChipFiltroEstilizado(
                selecionado = filtroSelecionado == filtro,
                label = label,
                tag = tag,
                onClick = { onSelecionarFiltro(filtro) },
            )
        }
    }
}

@Composable
private fun FiltrosOrdenacaoRow(
    ordenacaoSelecionada: FiltroOrdenacao,
    onSelecionarOrdenacao: (FiltroOrdenacao) -> Unit,
) {
    val opcoes = listOf(
        Triple(FiltroOrdenacao.EM_ALTA, stringResource(R.string.ordenar_em_alta), "sort_chip_em_alta"),
        Triple(FiltroOrdenacao.MELHORES_NOTAS, stringResource(R.string.ordenar_melhores_notas), "sort_chip_notas"),
        Triple(FiltroOrdenacao.EM_EXIBICAO, stringResource(R.string.ordenar_em_exibicao), "sort_chip_exibicao"),
    )
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp),
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(opcoes) { (ordenacao, label, tag) ->
            ChipFiltroEstilizado(
                selecionado = ordenacaoSelecionada == ordenacao,
                label = label,
                tag = tag,
                onClick = { onSelecionarOrdenacao(ordenacao) },
            )
        }
    }
}

@Composable
private fun FiltrosFavoritosRow(
    filtroSelecionado: FiltroStatusBiblioteca,
    onSelecionarFiltro: (FiltroStatusBiblioteca) -> Unit,
) {
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp),
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(FiltroStatusBiblioteca.entries) { filtro ->
            ChipFiltroEstilizado(
                selecionado = filtroSelecionado == filtro,
                label = filtro.rotulo,
                tag = "library_filter_${filtro.name.lowercase()}",
                onClick = { onSelecionarFiltro(filtro) },
            )
        }
    }
}

@Composable
private fun ChipFiltroEstilizado(
    selecionado: Boolean,
    label: String,
    tag: String,
    onClick: () -> Unit,
) {
    FilterChip(
        selected = selecionado,
        onClick = onClick,
        label = {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
            )
        },
        modifier = Modifier.testTag(tag),
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = MaterialTheme.colorScheme.primary,
            selectedLabelColor = Color.White,
            containerColor = MaterialTheme.colorScheme.surface,
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

@Composable
private fun ConteudoCatalogo(
    screenState: CatalogoScreenState,
    onItemClick: (MidiaItem) -> Unit,
    onAlternarFavorito: (MidiaItem) -> Unit,
    onTentarNovamente: () -> Unit,
) {
    when (val uiState = screenState.uiState) {
        is CatalogoUiState.Loading -> {
            EstadoCarregandoView()
        }

        is CatalogoUiState.Error -> {
            EstadoErroView(
                mensagem = uiState.mensagem,
                onTentarNovamente = onTentarNovamente,
            )
        }

        is CatalogoUiState.Empty -> {
            val (icone, subtitulo) = when {
                screenState.isModoBuscaAtivo -> {
                    Icons.Rounded.Search to stringResource(R.string.vazio_busca_subtitulo)
                }

                screenState.abaSelecionada == AbaCatalogo.FAVORITOS -> {
                    Icons.Rounded.FavoriteBorder to stringResource(R.string.vazio_favoritos_subtitulo)
                }

                else -> {
                    iconeParaAba(screenState.abaSelecionada, selecionado = true) to
                            "Altere o filtro acima ou toque em sincronizar no topo para atualizar o catálogo."
                }
            }
            EstadoVazioView(
                titulo = uiState.mensagem,
                subtitulo = subtitulo,
                icone = icone,
            )
        }

        is CatalogoUiState.Success -> {
            val mostrarStatusBiblioteca =
                screenState.abaSelecionada == AbaCatalogo.FAVORITOS && !screenState.isModoBuscaAtivo
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("catalogo_list"),
                contentPadding = PaddingValues(
                    start = 16.dp,
                    end = 16.dp,
                    top = 4.dp,
                    bottom = 20.dp,
                ),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(
                    items = uiState.itens,
                    key = { it.id },
                ) { item ->
                    MidiaHorizontalCard(
                        item = item,
                        onClick = { onItemClick(item) },
                        onAlternarFavorito = { onAlternarFavorito(item) },
                        mostrarStatusBiblioteca = mostrarStatusBiblioteca,
                    )
                }
            }
        }
    }
}

@Composable
private fun KomaDeckNavigationBar(
    abaSelecionada: AbaCatalogo,
    onSelecionarAba: (AbaCatalogo) -> Unit,
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, CardBorderLight),
        shadowElevation = 4.dp,
    ) {
        NavigationBar(
            containerColor = MaterialTheme.colorScheme.surface,
            tonalElevation = 0.dp,
        ) {
            AbaCatalogo.entries.forEach { aba ->
                val selecionada = abaSelecionada == aba
                val label = labelParaAba(aba)
                NavigationBarItem(
                    selected = selecionada,
                    onClick = { onSelecionarAba(aba) },
                    modifier = Modifier.testTag(tagParaAba(aba)),
                    icon = {
                        Icon(
                            imageVector = iconeParaAba(aba, selecionada),
                            contentDescription = label,
                        )
                    },
                    label = {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelMedium,
                            maxLines = 1,
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                        indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                        unselectedIconColor = TextSecondarySlate,
                        unselectedTextColor = TextSecondarySlate,
                    ),
                )
            }
        }
    }
}

@Composable
private fun KomaDeckNavigationRail(
    abaSelecionada: AbaCatalogo,
    onSelecionarAba: (AbaCatalogo) -> Unit,
) {
    NavigationRail(
        containerColor = MaterialTheme.colorScheme.surface,
    ) {
        Spacer(modifier = Modifier.height(12.dp))
        AbaCatalogo.entries.forEach { aba ->
            val selecionada = abaSelecionada == aba
            val label = labelParaAba(aba)
            NavigationRailItem(
                selected = selecionada,
                onClick = { onSelecionarAba(aba) },
                modifier = Modifier.testTag(tagParaAba(aba)),
                icon = {
                    Icon(
                        imageVector = iconeParaAba(aba, selecionada),
                        contentDescription = label,
                    )
                },
                label = {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelMedium,
                    )
                },
                colors = NavigationRailItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                    unselectedIconColor = TextSecondarySlate,
                    unselectedTextColor = TextSecondarySlate,
                ),
            )
        }
    }
}

@Composable
private fun labelParaAba(aba: AbaCatalogo): String = when (aba) {
    AbaCatalogo.ANIMES -> stringResource(R.string.aba_animes)
    AbaCatalogo.MANGAS -> stringResource(R.string.aba_mangas)
    AbaCatalogo.SERIES -> stringResource(R.string.aba_series)
    AbaCatalogo.FAVORITOS -> stringResource(R.string.aba_favoritos)
}

private fun iconeParaAba(aba: AbaCatalogo, selecionado: Boolean) = when (aba) {
    AbaCatalogo.ANIMES -> Icons.Rounded.SmartDisplay
    AbaCatalogo.MANGAS -> Icons.Rounded.AutoStories
    AbaCatalogo.SERIES -> Icons.Rounded.Tv
    AbaCatalogo.FAVORITOS -> if (selecionado) {
        Icons.Rounded.Favorite
    } else {
        Icons.Rounded.FavoriteBorder
    }
}

private fun tagParaAba(aba: AbaCatalogo): String = when (aba) {
    AbaCatalogo.ANIMES -> "nav_tab_animes"
    AbaCatalogo.MANGAS -> "nav_tab_mangas"
    AbaCatalogo.SERIES -> "nav_tab_series"
    AbaCatalogo.FAVORITOS -> "nav_tab_favoritos"
}