package com.lzinnbarbosa.komadeck.presentation.catalogo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lzinnbarbosa.komadeck.domain.model.CategoriaMidia
import com.lzinnbarbosa.komadeck.domain.model.MidiaItem
import com.lzinnbarbosa.komadeck.domain.model.StatusBiblioteca
import com.lzinnbarbosa.komadeck.domain.usecase.BuscarMidiasUseCase
import com.lzinnbarbosa.komadeck.domain.usecase.KomaDeckUseCases
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class AbaCatalogo(val categoria: CategoriaMidia?) {
    ANIMES(CategoriaMidia.ANIME),
    MANGAS(CategoriaMidia.MANGA),
    SERIES(CategoriaMidia.SERIE),
    FAVORITOS(null),
}

enum class FiltroBusca {
    TODOS,
    ANIMES,
    MANGAS,
    SERIES,
}

enum class FiltroOrdenacao {
    EM_ALTA,
    MELHORES_NOTAS,
    EM_EXIBICAO,
}

enum class FiltroStatusBiblioteca(val rotulo: String, val status: StatusBiblioteca?) {
    TODOS("Todos", null),
    QUERO_VER("Quero Ver / Ler", StatusBiblioteca.QUERO_VER),
    ACOMPANHANDO("Acompanhando", StatusBiblioteca.ACOMPANHANDO),
    CONCLUIDO("Concluído", StatusBiblioteca.CONCLUIDO),
}

sealed interface CatalogoUiState {
    data object Loading : CatalogoUiState
    data class Success(val itens: List<MidiaItem>) : CatalogoUiState
    data class Empty(val mensagem: String) : CatalogoUiState
    data class Error(val mensagem: String) : CatalogoUiState
}

private data class FiltrosCatalogo(
    val filtroBusca: FiltroBusca = FiltroBusca.TODOS,
    val filtroOrdenacao: FiltroOrdenacao = FiltroOrdenacao.EM_ALTA,
    val filtroStatusBiblioteca: FiltroStatusBiblioteca = FiltroStatusBiblioteca.TODOS,
)

data class CatalogoScreenState(
    val abaSelecionada: AbaCatalogo = AbaCatalogo.ANIMES,
    val queryBusca: String = "",
    val filtroBusca: FiltroBusca = FiltroBusca.TODOS,
    val filtroOrdenacao: FiltroOrdenacao = FiltroOrdenacao.EM_ALTA,
    val filtroStatusBiblioteca: FiltroStatusBiblioteca = FiltroStatusBiblioteca.TODOS,
    val isSincronizando: Boolean = false,
    val isModoOffline: Boolean = false,
    val uiState: CatalogoUiState = CatalogoUiState.Loading,
) {
    val isModoBuscaAtivo: Boolean
        get() = queryBusca.isNotBlank()
}

@OptIn(FlowPreview::class)
@HiltViewModel
class CatalogoViewModel @Inject constructor(
    private val useCases: KomaDeckUseCases,
) : ViewModel() {

    private val abaFlow = MutableStateFlow(AbaCatalogo.ANIMES)
    private val queryFlow = MutableStateFlow("")
    private val filtrosFlow = MutableStateFlow(FiltrosCatalogo())

    private val cacheLocalFlow = MutableStateFlow<List<MidiaItem>>(emptyList())
    private val resultadosBuscaBrutosFlow = MutableStateFlow<List<MidiaItem>>(emptyList())
    private val buscandoRemotoFlow = MutableStateFlow(false)
    private val erroBuscaFlow = MutableStateFlow<String?>(null)
    private val erroSyncPorAbaFlow = MutableStateFlow(emptyMap<AbaCatalogo, String?>())

    private val categoriasJaSincronizadas = mutableSetOf<CategoriaMidia>()
    private var syncJob: Job? = null

    private val _screenState = MutableStateFlow(CatalogoScreenState())
    val screenState: StateFlow<CatalogoScreenState> = _screenState.asStateFlow()

    init {
        observarBancoLocal()
        observarBuscaUnificada()
        observarEstadoCombinado()
        sincronizarAbaSeNecessario(AbaCatalogo.ANIMES, forcar = false)
    }

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    private fun observarBancoLocal() {
        viewModelScope.launch {
            abaFlow
                .flatMapLatest { aba ->
                    val categoria = aba.categoria
                    if (categoria == null) {
                        useCases.obterFavoritos()
                    } else {
                        useCases.obterMidias(categoria)
                    }
                }
                .catch { emit(emptyList()) }
                .collect { itens ->
                    cacheLocalFlow.value = itens
                }
        }
    }

    private fun observarBuscaUnificada() {
        viewModelScope.launch {
            queryFlow
                .map { it.trim() }
                .distinctUntilChanged()
                .debounce(SEARCH_DEBOUNCE_MS)
                .collectLatest { termo ->
                    if (termo.isEmpty() || termo.length < BuscarMidiasUseCase.MIN_QUERY_LENGTH) {
                        buscandoRemotoFlow.value = false
                        erroBuscaFlow.value = null
                        resultadosBuscaBrutosFlow.value = emptyList()
                        return@collectLatest
                    }

                    buscandoRemotoFlow.value = true
                    erroBuscaFlow.value = null
                    val resultado = useCases.buscarMidias(termo)
                    buscandoRemotoFlow.value = false
                    resultado
                        .onSuccess { itens ->
                            erroBuscaFlow.value = null
                            resultadosBuscaBrutosFlow.value = itens
                            _screenState.update { it.copy(isModoOffline = false) }
                        }
                        .onFailure {
                            resultadosBuscaBrutosFlow.value = emptyList()
                            erroBuscaFlow.value = MSG_ERRO_BUSCA
                            _screenState.update { it.copy(isModoOffline = true) }
                        }
                }
        }
    }

    private fun observarEstadoCombinado() {
        viewModelScope.launch {
            combine(
                abaFlow,
                queryFlow,
                filtrosFlow,
                cacheLocalFlow,
                resultadosBuscaBrutosFlow,
                buscandoRemotoFlow,
                erroBuscaFlow,
                erroSyncPorAbaFlow,
                _screenState.map { it.isSincronizando }.distinctUntilChanged(),
            ) { valores ->
                val aba = valores[0] as AbaCatalogo
                val query = valores[1] as String
                val filtros = valores[2] as FiltrosCatalogo
                @Suppress("UNCHECKED_CAST")
                val itensLocais = valores[3] as List<MidiaItem>
                @Suppress("UNCHECKED_CAST")
                val resultadosBusca = valores[4] as List<MidiaItem>
                val buscando = valores[5] as Boolean
                val erroBusca = valores[6] as String?
                @Suppress("UNCHECKED_CAST")
                val errosSync = valores[7] as Map<AbaCatalogo, String?>
                val sincronizando = valores[8] as Boolean

                val uiState = if (query.isNotBlank()) {
                    resolverUiStateBusca(
                        query = query.trim(),
                        filtro = filtros.filtroBusca,
                        resultadosBrutos = resultadosBusca,
                        buscando = buscando,
                        erroBusca = erroBusca,
                    )
                } else {
                    resolverUiStateCatalogo(
                        aba = aba,
                        itensLocais = itensLocais,
                        filtroOrdenacao = filtros.filtroOrdenacao,
                        filtroStatusBiblioteca = filtros.filtroStatusBiblioteca,
                        sincronizando = sincronizando,
                        erroSync = errosSync[aba],
                    )
                }

                _screenState.update { atual ->
                    atual.copy(
                        abaSelecionada = aba,
                        queryBusca = query,
                        filtroBusca = filtros.filtroBusca,
                        filtroOrdenacao = filtros.filtroOrdenacao,
                        filtroStatusBiblioteca = filtros.filtroStatusBiblioteca,
                        uiState = uiState,
                    )
                }
            }.collect {}
        }
    }

    private fun resolverUiStateBusca(
        query: String,
        filtro: FiltroBusca,
        resultadosBrutos: List<MidiaItem>,
        buscando: Boolean,
        erroBusca: String?,
    ): CatalogoUiState {
        if (query.length < BuscarMidiasUseCase.MIN_QUERY_LENGTH) {
            return CatalogoUiState.Empty(MSG_BUSCA_MINIMO)
        }
        if (buscando) {
            return CatalogoUiState.Loading
        }
        if (erroBusca != null) {
            return CatalogoUiState.Error(erroBusca)
        }
        val filtrados = when (filtro) {
            FiltroBusca.TODOS -> resultadosBrutos
            FiltroBusca.ANIMES -> resultadosBrutos.filter { it.categoria == CategoriaMidia.ANIME }
            FiltroBusca.MANGAS -> resultadosBrutos.filter { it.categoria == CategoriaMidia.MANGA }
            FiltroBusca.SERIES -> resultadosBrutos.filter { it.categoria == CategoriaMidia.SERIE }
        }
        return if (filtrados.isEmpty()) {
            CatalogoUiState.Empty("Nenhum resultado encontrado para \"$query\".")
        } else {
            CatalogoUiState.Success(filtrados)
        }
    }

    private fun resolverUiStateCatalogo(
        aba: AbaCatalogo,
        itensLocais: List<MidiaItem>,
        filtroOrdenacao: FiltroOrdenacao,
        filtroStatusBiblioteca: FiltroStatusBiblioteca,
        sincronizando: Boolean,
        erroSync: String?,
    ): CatalogoUiState {
        if (aba == AbaCatalogo.FAVORITOS) {
            if (itensLocais.isEmpty()) {
                return CatalogoUiState.Empty(MSG_FAVORITOS_VAZIO)
            }
            val statusAlvo = filtroStatusBiblioteca.status
            val filtrados = if (statusAlvo == null) {
                itensLocais
            } else {
                itensLocais.filter { it.statusBiblioteca == statusAlvo }
            }
            return if (filtrados.isEmpty()) {
                CatalogoUiState.Empty("Nenhuma obra salva como \"${filtroStatusBiblioteca.rotulo}\".")
            } else {
                CatalogoUiState.Success(filtrados)
            }
        }

        if (itensLocais.isNotEmpty()) {
            val processados = when (filtroOrdenacao) {
                FiltroOrdenacao.EM_ALTA -> itensLocais
                FiltroOrdenacao.MELHORES_NOTAS -> itensLocais.sortedByDescending { it.nota ?: -1.0 }
                FiltroOrdenacao.EM_EXIBICAO -> itensLocais.filter {
                    it.status.equals("Em Exibição", ignoreCase = true) ||
                            it.status.equals("Em Publicação", ignoreCase = true)
                }
            }
            return if (processados.isEmpty()) {
                CatalogoUiState.Empty("Nenhuma obra em exibição encontrada nesta seção.")
            } else {
                CatalogoUiState.Success(processados)
            }
        }

        if (sincronizando) {
            return CatalogoUiState.Loading
        }
        if (erroSync != null) {
            return CatalogoUiState.Error(erroSync)
        }
        return CatalogoUiState.Empty(MSG_CATALOGO_VAZIO)
    }

    fun selecionarAba(novaAba: AbaCatalogo) {
        if (abaFlow.value != novaAba) {
            abaFlow.value = novaAba
        }
        if (queryFlow.value.isNotBlank()) {
            limparBusca()
        }
        sincronizarAbaSeNecessario(novaAba, forcar = false)
    }

    fun onQueryChange(novaQuery: String) {
        queryFlow.value = novaQuery
        if (novaQuery.trim().length >= BuscarMidiasUseCase.MIN_QUERY_LENGTH) {
            buscandoRemotoFlow.value = true
            erroBuscaFlow.value = null
        } else {
            buscandoRemotoFlow.value = false
            erroBuscaFlow.value = null
            resultadosBuscaBrutosFlow.value = emptyList()
        }
    }

    fun limparBusca() {
        queryFlow.value = ""
        filtrosFlow.update { it.copy(filtroBusca = FiltroBusca.TODOS) }
        buscandoRemotoFlow.value = false
        erroBuscaFlow.value = null
        resultadosBuscaBrutosFlow.value = emptyList()
    }

    fun selecionarFiltroBusca(filtro: FiltroBusca) {
        filtrosFlow.update { it.copy(filtroBusca = filtro) }
    }

    fun selecionarOrdenacao(ordenacao: FiltroOrdenacao) {
        filtrosFlow.update { it.copy(filtroOrdenacao = ordenacao) }
    }

    fun selecionarFiltroStatusBiblioteca(filtro: FiltroStatusBiblioteca) {
        filtrosFlow.update { it.copy(filtroStatusBiblioteca = filtro) }
    }

    fun sincronizarManual() {
        val queryAtual = queryFlow.value.trim()
        if (queryAtual.length >= BuscarMidiasUseCase.MIN_QUERY_LENGTH) {
            viewModelScope.launch {
                buscandoRemotoFlow.value = true
                erroBuscaFlow.value = null
                useCases.buscarMidias(queryAtual)
                    .onSuccess { itens ->
                        buscandoRemotoFlow.value = false
                        resultadosBuscaBrutosFlow.value = itens
                        _screenState.update { it.copy(isModoOffline = false) }
                    }
                    .onFailure {
                        buscandoRemotoFlow.value = false
                        erroBuscaFlow.value = MSG_ERRO_BUSCA
                        _screenState.update { it.copy(isModoOffline = true) }
                    }
            }
            return
        }

        val abaAtual = abaFlow.value
        if (abaAtual == AbaCatalogo.FAVORITOS) {
            return
        }
        sincronizarAbaSeNecessario(abaAtual, forcar = true)
    }

    private fun sincronizarAbaSeNecessario(aba: AbaCatalogo, forcar: Boolean) {
        val categoria = aba.categoria ?: return
        if (!forcar && categoria in categoriasJaSincronizadas) return

        syncJob?.cancel()
        syncJob = viewModelScope.launch {
            _screenState.update { it.copy(isSincronizando = true) }
            erroSyncPorAbaFlow.update { it + (aba to null) }

            val resultado = useCases.sincronizarCategoria(categoria)
            resultado
                .onSuccess {
                    categoriasJaSincronizadas.add(categoria)
                    erroSyncPorAbaFlow.update { it + (aba to null) }
                    _screenState.update {
                        it.copy(isSincronizando = false, isModoOffline = false)
                    }
                }
                .onFailure {
                    erroSyncPorAbaFlow.update { it + (aba to MSG_ERRO_SYNC) }
                    _screenState.update {
                        it.copy(isSincronizando = false, isModoOffline = true)
                    }
                }
        }
    }

    fun alternarFavorito(item: MidiaItem) {
        viewModelScope.launch {
            if (queryFlow.value.isNotBlank()) {
                val novoFavorito = !item.isFavorito
                resultadosBuscaBrutosFlow.update { lista ->
                    lista.map { if (it.id == item.id) it.copy(isFavorito = novoFavorito) else it }
                }
                useCases.salvarMidiaSobDemanda(item)
                useCases.alternarFavorito(item.id)
            } else {
                useCases.alternarFavorito(item.id)
            }
        }
    }

    fun prepararDetalhes(item: MidiaItem) {
        viewModelScope.launch {
            useCases.salvarMidiaSobDemanda(item)
        }
    }

    companion object {
        const val SEARCH_DEBOUNCE_MS = 500L
        const val MSG_BUSCA_MINIMO = "Digite pelo menos 3 caracteres para buscar."
        const val MSG_FAVORITOS_VAZIO = "Nenhum favorito salvo. Toque no coração para guardar obras offline."
        const val MSG_CATALOGO_VAZIO = "Nenhuma obra encontrada nesta seção."
        const val MSG_ERRO_SYNC = "Não foi possível carregar os dados. Verifique sua conexão e tente novamente."
        const val MSG_ERRO_BUSCA = "Não foi possível realizar a busca. Verifique sua conexão e tente novamente."
    }
}