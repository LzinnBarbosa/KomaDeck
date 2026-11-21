package com.lzinnbarbosa.komadeck.domain.usecase

import com.lzinnbarbosa.komadeck.domain.model.CategoriaMidia
import com.lzinnbarbosa.komadeck.domain.model.MidiaItem
import com.lzinnbarbosa.komadeck.domain.model.StatusBiblioteca
import com.lzinnbarbosa.komadeck.domain.repository.GuiaRepository
import kotlinx.coroutines.flow.Flow

class ObterMidiasUseCase(
    private val repository: GuiaRepository,
) {
    operator fun invoke(categoria: CategoriaMidia): Flow<List<MidiaItem>> =
        repository.observarMidiasPorCategoria(categoria)
}

class ObterFavoritosUseCase(
    private val repository: GuiaRepository,
) {
    operator fun invoke(): Flow<List<MidiaItem>> =
        repository.observarFavoritos()
}

class ObterDetalhesMidiaUseCase(
    private val repository: GuiaRepository,
) {
    operator fun invoke(id: String): Flow<MidiaItem?> =
        repository.observarPorId(id)
}

class SincronizarCategoriaUseCase(
    private val repository: GuiaRepository,
) {
    suspend operator fun invoke(categoria: CategoriaMidia): Result<Unit> =
        repository.sincronizarCategoria(categoria)
}

class BuscarMidiasUseCase(
    private val repository: GuiaRepository,
) {
    suspend operator fun invoke(query: String): Result<List<MidiaItem>> {
        val termo = query.trim()
        if (termo.length < MIN_QUERY_LENGTH) {
            return Result.success(emptyList())
        }
        return repository.buscarMidias(termo)
    }

    companion object {
        const val MIN_QUERY_LENGTH = 3
    }
}

class AlternarFavoritoUseCase(
    private val repository: GuiaRepository,
) {
    suspend operator fun invoke(id: String) {
        repository.alternarFavorito(id)
    }
}

class AtualizarStatusBibliotecaUseCase(
    private val repository: GuiaRepository,
) {
    suspend operator fun invoke(id: String, status: StatusBiblioteca) {
        repository.atualizarStatusBiblioteca(id, status)
    }
}

class SalvarMidiaSobDemandaUseCase(
    private val repository: GuiaRepository,
) {
    suspend operator fun invoke(item: MidiaItem): MidiaItem =
        repository.salvarSobDemanda(item)
}

data class KomaDeckUseCases(
    val obterMidias: ObterMidiasUseCase,
    val obterFavoritos: ObterFavoritosUseCase,
    val obterDetalhesMidia: ObterDetalhesMidiaUseCase,
    val sincronizarCategoria: SincronizarCategoriaUseCase,
    val buscarMidias: BuscarMidiasUseCase,
    val alternarFavorito: AlternarFavoritoUseCase,
    val atualizarStatusBiblioteca: AtualizarStatusBibliotecaUseCase,
    val salvarMidiaSobDemanda: SalvarMidiaSobDemandaUseCase,
)