package com.lzinnbarbosa.komadeck.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "midias")
data class MidiaEntity(
    @PrimaryKey
    val id: String,
    val titulo: String,
    val categoria: String,
    val imagemUrl: String,
    val nota: Double?,
    val status: String,
    val horarioTransmissao: String,
    val sinopse: String,
    val sinopseOriginal: String = "",
    val generos: String = "",
    val totalEpisodiosOuCapitulos: Int? = null,
    val estudioOuEmissora: String = "",
    val anoLancamento: Int? = null,
    val popularidade: Int = 0,
    val isFavorito: Boolean = false,
    val statusBiblioteca: String = "QUERO_VER",
    val ordemCatalogo: Int = 0,
    val atualizadoEm: Long = System.currentTimeMillis(),
)