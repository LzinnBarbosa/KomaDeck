package com.lzinnbarbosa.komadeck.domain.model

enum class CategoriaMidia {
    ANIME,
    MANGA,
    SERIE,
}

enum class StatusBiblioteca(val rotulo: String) {
    QUERO_VER("Quero Ver / Ler"),
    ACOMPANHANDO("Acompanhando"),
    CONCLUIDO("Concluído"),
}

data class MidiaItem(
    val id: String,
    val titulo: String,
    val categoria: CategoriaMidia,
    val imagemUrl: String,
    val nota: Double?,
    val status: String,
    val horarioTransmissao: String,
    val sinopse: String,
    val generos: List<String> = emptyList(),
    val totalEpisodiosOuCapitulos: Int? = null,
    val estudioOuEmissora: String = "",
    val anoLancamento: Int? = null,
    val popularidade: Int = 0,
    val isFavorito: Boolean = false,
    val statusBiblioteca: StatusBiblioteca = StatusBiblioteca.QUERO_VER,
)