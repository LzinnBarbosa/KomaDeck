package com.lzinnbarbosa.komadeck.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface MidiaDao {

    @Query("SELECT * FROM midias WHERE categoria = :categoria ORDER BY ordemCatalogo ASC, titulo ASC")
    fun observarPorCategoria(categoria: String): Flow<List<MidiaEntity>>

    @Query("SELECT * FROM midias WHERE isFavorito = 1 ORDER BY atualizadoEm DESC, titulo ASC")
    fun observarFavoritos(): Flow<List<MidiaEntity>>

    @Query("SELECT * FROM midias WHERE id = :id LIMIT 1")
    fun observarPorId(id: String): Flow<MidiaEntity?>

    @Query("SELECT * FROM midias WHERE id = :id LIMIT 1")
    suspend fun buscarPorId(id: String): MidiaEntity?

    @Query("SELECT id FROM midias WHERE isFavorito = 1")
    suspend fun obterIdsFavoritos(): List<String>

    @Query("SELECT * FROM midias WHERE isFavorito = 1")
    suspend fun obterFavoritosSnapshot(): List<MidiaEntity>

    @Query("SELECT * FROM midias WHERE categoria = :categoria")
    suspend fun obterPorCategoriaSnapshot(categoria: String): List<MidiaEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertTodos(itens: List<MidiaEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertItem(item: MidiaEntity)

    @Query("UPDATE midias SET isFavorito = :isFavorito, atualizadoEm = :atualizadoEm WHERE id = :id")
    suspend fun atualizarFavorito(
        id: String,
        isFavorito: Boolean,
        atualizadoEm: Long = System.currentTimeMillis(),
    )

    @Query("UPDATE midias SET statusBiblioteca = :statusBiblioteca, atualizadoEm = :atualizadoEm WHERE id = :id")
    suspend fun atualizarStatusBiblioteca(
        id: String,
        statusBiblioteca: String,
        atualizadoEm: Long = System.currentTimeMillis(),
    )

    @Query("DELETE FROM midias WHERE categoria = :categoria AND isFavorito = 0")
    suspend fun limparNaoFavoritosPorCategoria(categoria: String)

    @Transaction
    suspend fun sincronizarCategoriaPreservandoFavoritos(
        categoria: String,
        novosItens: List<MidiaEntity>,
    ) {
        val favoritosExistentes = obterFavoritosSnapshot().associateBy { it.id }
        limparNaoFavoritosPorCategoria(categoria)
        val itensComFavoritoPreservado = novosItens.map { item ->
            val existente = favoritosExistentes[item.id]
            if (existente != null) {
                item.copy(
                    isFavorito = true,
                    statusBiblioteca = existente.statusBiblioteca,
                )
            } else {
                item.copy(isFavorito = false)
            }
        }
        upsertTodos(itensComFavoritoPreservado)
    }

    @Transaction
    suspend fun salvarSobDemandaPreservandoFavorito(item: MidiaEntity) {
        val existente = buscarPorId(item.id)
        val favoritoPreservado = existente?.isFavorito ?: item.isFavorito
        val statusPreservado = existente?.statusBiblioteca ?: item.statusBiblioteca
        upsertItem(
            item.copy(
                isFavorito = favoritoPreservado,
                statusBiblioteca = statusPreservado,
            ),
        )
    }

    @Transaction
    suspend fun alternarFavorito(id: String) {
        val existente = buscarPorId(id) ?: return
        atualizarFavorito(id = id, isFavorito = !existente.isFavorito)
    }
}