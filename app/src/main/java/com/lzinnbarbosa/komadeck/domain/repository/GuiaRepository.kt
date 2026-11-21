package com.lzinnbarbosa.komadeck.domain.repository

import com.lzinnbarbosa.komadeck.domain.model.CategoriaMidia
import com.lzinnbarbosa.komadeck.domain.model.MidiaItem
import com.lzinnbarbosa.komadeck.domain.model.StatusBiblioteca
import kotlinx.coroutines.flow.Flow

interface GuiaRepository {
    fun observarMidiasPorCategoria(categoria: CategoriaMidia): Flow<List<MidiaItem>>

    fun observarFavoritos(): Flow<List<MidiaItem>>

    fun observarPorId(id: String): Flow<MidiaItem?>

    suspend fun sincronizarCategoria(categoria: CategoriaMidia): Result<Unit>

    suspend fun buscarMidias(query: String): Result<List<MidiaItem>>

    suspend fun alternarFavorito(id: String)

    suspend fun atualizarStatusBiblioteca(id: String, status: StatusBiblioteca)

    suspend fun salvarSobDemanda(item: MidiaItem): MidiaItem
}