package com.lzinnbarbosa.komadeck.presentation.detalhes

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.lzinnbarbosa.komadeck.domain.model.MidiaItem
import com.lzinnbarbosa.komadeck.domain.model.StatusBiblioteca
import com.lzinnbarbosa.komadeck.domain.usecase.KomaDeckUseCases
import com.lzinnbarbosa.komadeck.presentation.navigation.RotaDetalhes
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface DetalhesUiState {
    data object Loading : DetalhesUiState
    data class Success(val midia: MidiaItem) : DetalhesUiState
    data class Empty(val mensagem: String) : DetalhesUiState
    data class Error(val mensagem: String) : DetalhesUiState
}

@HiltViewModel
class DetalhesViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val useCases: KomaDeckUseCases,
) : ViewModel() {

    private val rota: RotaDetalhes = savedStateHandle.toRoute()
    private val midiaId: String = rota.midiaId

    val uiState: StateFlow<DetalhesUiState> = useCases.obterDetalhesMidia(midiaId)
        .map<MidiaItem?, DetalhesUiState> { midia ->
            if (midia != null) {
                DetalhesUiState.Success(midia)
            } else {
                DetalhesUiState.Empty("Obra não encontrada no catálogo local.")
            }
        }
        .catch { erro ->
            emit(
                DetalhesUiState.Error(
                    erro.localizedMessage ?: "Erro ao carregar os detalhes da obra.",
                ),
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = DetalhesUiState.Loading,
        )

    fun alternarFavorito() {
        viewModelScope.launch {
            useCases.alternarFavorito(midiaId)
        }
    }

    fun atualizarStatusBiblioteca(status: StatusBiblioteca) {
        viewModelScope.launch {
            val midiaAtual = (uiState.value as? DetalhesUiState.Success)?.midia
            if (midiaAtual != null && !midiaAtual.isFavorito) {
                useCases.alternarFavorito(midiaId)
            }
            useCases.atualizarStatusBiblioteca(midiaId, status)
        }
    }
}