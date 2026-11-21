package com.lzinnbarbosa.komadeck

import com.lzinnbarbosa.komadeck.domain.model.CategoriaMidia
import com.lzinnbarbosa.komadeck.domain.model.MidiaItem
import com.lzinnbarbosa.komadeck.domain.model.StatusBiblioteca
import com.lzinnbarbosa.komadeck.domain.repository.GuiaRepository
import com.lzinnbarbosa.komadeck.domain.usecase.AlternarFavoritoUseCase
import com.lzinnbarbosa.komadeck.domain.usecase.AtualizarStatusBibliotecaUseCase
import com.lzinnbarbosa.komadeck.domain.usecase.BuscarMidiasUseCase
import com.lzinnbarbosa.komadeck.domain.usecase.KomaDeckUseCases
import com.lzinnbarbosa.komadeck.domain.usecase.ObterDetalhesMidiaUseCase
import com.lzinnbarbosa.komadeck.domain.usecase.ObterFavoritosUseCase
import com.lzinnbarbosa.komadeck.domain.usecase.ObterMidiasUseCase
import com.lzinnbarbosa.komadeck.domain.usecase.SalvarMidiaSobDemandaUseCase
import com.lzinnbarbosa.komadeck.domain.usecase.SincronizarCategoriaUseCase
import com.lzinnbarbosa.komadeck.presentation.catalogo.AbaCatalogo
import com.lzinnbarbosa.komadeck.presentation.catalogo.CatalogoUiState
import com.lzinnbarbosa.komadeck.presentation.catalogo.CatalogoViewModel
import com.lzinnbarbosa.komadeck.presentation.catalogo.FiltroBusca
import com.lzinnbarbosa.komadeck.presentation.catalogo.FiltroOrdenacao
import com.lzinnbarbosa.komadeck.presentation.catalogo.FiltroStatusBiblioteca
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CatalogoViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `carregamento inicial sincroniza animes e exibe Success`() = runTest {
        val repo = FakeCatalogoRepository(
            dadosRemotos = mapOf(
                CategoriaMidia.ANIME to listOf(exemploAnime("anime_1", "Frieren")),
            ),
        )
        val viewModel = criarViewModel(repo)

        advanceUntilIdle()

        val state = viewModel.screenState.value
        assertFalse(state.isModoOffline)
        assertFalse(state.isSincronizando)
        assertTrue(state.uiState is CatalogoUiState.Success)
        val itens = (state.uiState as CatalogoUiState.Success).itens
        assertEquals(1, itens.size)
        assertEquals("Frieren", itens.first().titulo)
    }

    @Test
    fun `falha de rede com cache local exibe banner offline mantendo Success`() = runTest {
        val repo = FakeCatalogoRepository(
            cacheInicial = listOf(exemploAnime("anime_10", "Solo Leveling")),
            falharSync = true,
        )
        val viewModel = criarViewModel(repo)

        advanceUntilIdle()

        val state = viewModel.screenState.value
        assertTrue(state.isModoOffline)
        assertTrue(state.uiState is CatalogoUiState.Success)
        assertEquals("Solo Leveling", (state.uiState as CatalogoUiState.Success).itens.first().titulo)
    }

    @Test
    fun `falha de rede sem cache local exibe Error e ativa modo offline`() = runTest {
        val repo = FakeCatalogoRepository(falharSync = true)
        val viewModel = criarViewModel(repo)

        advanceUntilIdle()

        val state = viewModel.screenState.value
        assertTrue(state.isModoOffline)
        assertTrue(state.uiState is CatalogoUiState.Error)
    }

    @Test
    fun `busca unificada respeita minimo 3 caracteres debounce 500ms e filtros`() = runTest {
        val animeResultado = exemploAnime("anime_50", "Bleach")
        val mangaResultado = MidiaItem(
            id = "manga_55",
            titulo = "Bleach Manga",
            categoria = CategoriaMidia.MANGA,
            imagemUrl = "",
            nota = 8.4,
            status = "Finalizado",
            horarioTransmissao = "Lançado em 2001",
            sinopse = "Mangá de ação.",
        )
        val serieResultado = MidiaItem(
            id = "serie_60",
            titulo = "Breaking Bad",
            categoria = CategoriaMidia.SERIE,
            imagemUrl = "",
            nota = 9.5,
            status = "Finalizado",
            horarioTransmissao = "Domingos às 21:00",
            sinopse = "Química e drama.",
        )
        val repo = FakeCatalogoRepository(
            resultadosBusca = listOf(animeResultado, mangaResultado, serieResultado),
        )
        val viewModel = criarViewModel(repo)
        advanceUntilIdle()

        // Menos de 3 caracteres não dispara busca
        viewModel.onQueryChange("bl")
        advanceTimeBy(600L)
        advanceUntilIdle()
        assertEquals(0, repo.chamadasBusca)
        assertTrue(viewModel.screenState.value.uiState is CatalogoUiState.Empty)

        // 3+ caracteres dispara após debounce de 500ms
        viewModel.onQueryChange("ble")
        advanceTimeBy(300L)
        assertEquals(0, repo.chamadasBusca)

        advanceTimeBy(250L)
        advanceUntilIdle()
        assertEquals(1, repo.chamadasBusca)
        val todos = (viewModel.screenState.value.uiState as CatalogoUiState.Success).itens
        assertEquals(3, todos.size)

        // Filtro por Animes
        viewModel.selecionarFiltroBusca(FiltroBusca.ANIMES)
        advanceUntilIdle()
        val apenasAnimes = (viewModel.screenState.value.uiState as CatalogoUiState.Success).itens
        assertEquals(1, apenasAnimes.size)
        assertEquals(CategoriaMidia.ANIME, apenasAnimes.first().categoria)

        // Filtro por Mangás
        viewModel.selecionarFiltroBusca(FiltroBusca.MANGAS)
        advanceUntilIdle()
        val apenasMangas = (viewModel.screenState.value.uiState as CatalogoUiState.Success).itens
        assertEquals(1, apenasMangas.size)
        assertEquals(CategoriaMidia.MANGA, apenasMangas.first().categoria)

        // Filtro por Séries
        viewModel.selecionarFiltroBusca(FiltroBusca.SERIES)
        advanceUntilIdle()
        val apenasSeries = (viewModel.screenState.value.uiState as CatalogoUiState.Success).itens
        assertEquals(1, apenasSeries.size)
        assertEquals(CategoriaMidia.SERIE, apenasSeries.first().categoria)
    }

    @Test
    fun `aba favoritos le apenas favoritos do banco local e filtra por status`() = runTest {
        val favAcompanhando = exemploAnime("anime_9", "Dandadan").copy(
            isFavorito = true,
            statusBiblioteca = StatusBiblioteca.ACOMPANHANDO,
        )
        val favConcluido = exemploAnime("anime_7", "Fullmetal").copy(
            isFavorito = true,
            statusBiblioteca = StatusBiblioteca.CONCLUIDO,
        )
        val naoFav = exemploAnime("anime_8", "Outro").copy(isFavorito = false)
        val repo = FakeCatalogoRepository(
            cacheInicial = listOf(favAcompanhando, favConcluido, naoFav),
        )
        val viewModel = criarViewModel(repo)
        advanceUntilIdle()

        viewModel.selecionarAba(AbaCatalogo.FAVORITOS)
        advanceUntilIdle()

        val state = viewModel.screenState.value
        assertEquals(AbaCatalogo.FAVORITOS, state.abaSelecionada)
        assertTrue(state.uiState is CatalogoUiState.Success)
        val favoritos = (state.uiState as CatalogoUiState.Success).itens
        assertEquals(2, favoritos.size)

        viewModel.selecionarFiltroStatusBiblioteca(FiltroStatusBiblioteca.CONCLUIDO)
        advanceUntilIdle()
        val concluidos = (viewModel.screenState.value.uiState as CatalogoUiState.Success).itens
        assertEquals(1, concluidos.size)
        assertEquals("Fullmetal", concluidos.first().titulo)
    }

    @Test
    fun `ordenacao por melhores notas ordena catalogo decrescente`() = runTest {
        val menorNota = exemploAnime("anime_1", "Obra A").copy(nota = 7.2)
        val maiorNota = exemploAnime("anime_2", "Obra B").copy(nota = 9.4)
        val repo = FakeCatalogoRepository(
            dadosRemotos = mapOf(
                CategoriaMidia.ANIME to listOf(menorNota, maiorNota),
            ),
        )
        val viewModel = criarViewModel(repo)
        advanceUntilIdle()

        viewModel.selecionarOrdenacao(FiltroOrdenacao.MELHORES_NOTAS)
        advanceUntilIdle()

        val itens = (viewModel.screenState.value.uiState as CatalogoUiState.Success).itens
        assertEquals("Obra B", itens.first().titulo)
    }

    private fun criarViewModel(repo: GuiaRepository): CatalogoViewModel =
        CatalogoViewModel(
            KomaDeckUseCases(
                obterMidias = ObterMidiasUseCase(repo),
                obterFavoritos = ObterFavoritosUseCase(repo),
                obterDetalhesMidia = ObterDetalhesMidiaUseCase(repo),
                sincronizarCategoria = SincronizarCategoriaUseCase(repo),
                buscarMidias = BuscarMidiasUseCase(repo),
                alternarFavorito = AlternarFavoritoUseCase(repo),
                atualizarStatusBiblioteca = AtualizarStatusBibliotecaUseCase(repo),
                salvarMidiaSobDemanda = SalvarMidiaSobDemandaUseCase(repo),
            ),
        )

    private fun exemploAnime(id: String, titulo: String) = MidiaItem(
        id = id,
        titulo = titulo,
        categoria = CategoriaMidia.ANIME,
        imagemUrl = "",
        nota = 8.9,
        status = "Em Exibição",
        horarioTransmissao = "Quintas às 23:00",
        sinopse = "Sinopse de teste.",
    )

    private class FakeCatalogoRepository(
        cacheInicial: List<MidiaItem> = emptyList(),
        private val dadosRemotos: Map<CategoriaMidia, List<MidiaItem>> = emptyMap(),
        private val resultadosBusca: List<MidiaItem> = emptyList(),
        private val falharSync: Boolean = false,
    ) : GuiaRepository {

        private val bancoFlow = MutableStateFlow(cacheInicial)
        var chamadasBusca = 0

        override fun observarMidiasPorCategoria(categoria: CategoriaMidia): Flow<List<MidiaItem>> =
            bancoFlow.map { lista -> lista.filter { it.categoria == categoria } }

        override fun observarFavoritos(): Flow<List<MidiaItem>> =
            bancoFlow.map { lista -> lista.filter { it.isFavorito } }

        override fun observarPorId(id: String): Flow<MidiaItem?> =
            bancoFlow.map { lista -> lista.find { it.id == id } }

        override suspend fun sincronizarCategoria(categoria: CategoriaMidia): Result<Unit> {
            if (falharSync) {
                return Result.failure(IllegalStateException("Sem rede"))
            }
            val favoritos = bancoFlow.value.filter { it.isFavorito }.associateBy { it.id }
            val novos = dadosRemotos[categoria].orEmpty().map { item ->
                val salvo = favoritos[item.id]
                if (salvo != null) {
                    item.copy(isFavorito = true, statusBiblioteca = salvo.statusBiblioteca)
                } else {
                    item
                }
            }
            val outrasCategorias = bancoFlow.value.filter {
                it.categoria != categoria || it.isFavorito
            }.filterNot { item -> novos.any { it.id == item.id } }
            bancoFlow.value = outrasCategorias + novos
            return Result.success(Unit)
        }

        override suspend fun buscarMidias(query: String): Result<List<MidiaItem>> {
            chamadasBusca++
            return Result.success(resultadosBusca)
        }

        override suspend fun alternarFavorito(id: String) {
            bancoFlow.value = bancoFlow.value.map {
                if (it.id == id) it.copy(isFavorito = !it.isFavorito) else it
            }
        }

        override suspend fun atualizarStatusBiblioteca(id: String, status: StatusBiblioteca) {
            bancoFlow.value = bancoFlow.value.map {
                if (it.id == id) it.copy(statusBiblioteca = status) else it
            }
        }

        override suspend fun salvarSobDemanda(item: MidiaItem): MidiaItem {
            val existe = bancoFlow.value.any { it.id == item.id }
            if (!existe) {
                bancoFlow.value = bancoFlow.value + item
            }
            return item
        }
    }
}