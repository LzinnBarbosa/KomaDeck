package com.lzinnbarbosa.komadeck.data.mapper

import com.lzinnbarbosa.komadeck.data.local.MidiaEntity
import com.lzinnbarbosa.komadeck.data.remote.AniListMediaDto
import com.lzinnbarbosa.komadeck.data.remote.JikanAnimeDto
import com.lzinnbarbosa.komadeck.data.remote.JikanMangaDto
import com.lzinnbarbosa.komadeck.data.remote.TvMazeScheduleDto
import com.lzinnbarbosa.komadeck.data.remote.TvMazeShowDto
import com.lzinnbarbosa.komadeck.domain.model.CategoriaMidia
import com.lzinnbarbosa.komadeck.domain.model.MidiaItem
import com.lzinnbarbosa.komadeck.domain.model.StatusBiblioteca
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object MidiaMappers {

    private const val SINOPSE_PADRAO = "Sinopse não disponível no momento."
    private const val HORARIO_PADRAO = "Horário não informado"
    private const val STATUS_PADRAO = "Status não informado"

    private val STATUS_MAP = mapOf(
        "currently airing" to "Em Exibição",
        "releasing" to "Em Exibição",
        "running" to "Em Exibição",
        "finished airing" to "Finalizado",
        "finished" to "Finalizado",
        "ended" to "Finalizado",
        "publishing" to "Em Publicação",
        "on hiatus" to "Em Hiato",
        "hiatus" to "Em Hiato",
        "not yet aired" to "Em Breve",
        "not_yet_released" to "Em Breve",
        "discontinued" to "Descontinuado",
        "cancelled" to "Cancelado",
        "to be determined" to "A Definir",
        "in development" to "Em Desenvolvimento",
    )

    private val GENEROS_MAP = mapOf(
        "Action" to "Ação",
        "Adventure" to "Aventura",
        "Comedy" to "Comédia",
        "Drama" to "Drama",
        "Fantasy" to "Fantasia",
        "Horror" to "Terror",
        "Mystery" to "Mistério",
        "Psychological" to "Psicológico",
        "Romance" to "Romance",
        "Sci-Fi" to "Ficção Científica",
        "Science-Fiction" to "Ficção Científica",
        "Slice of Life" to "Cotidiano",
        "Sports" to "Esportes",
        "Supernatural" to "Sobrenatural",
        "Thriller" to "Suspense",
        "Crime" to "Policial",
        "Family" to "Família",
        "War" to "Guerra",
        "History" to "História",
        "Music" to "Música",
        "Mecha" to "Mecha",
    )

    private val DIAS_SEMANA_REPLACEMENTS = listOf(
        "Mondays" to "Segundas",
        "Tuesdays" to "Terças",
        "Wednesdays" to "Quartas",
        "Thursdays" to "Quintas",
        "Fridays" to "Sextas",
        "Saturdays" to "Sábados",
        "Sundays" to "Domingos",
        "Monday" to "Segunda",
        "Tuesday" to "Terça",
        "Wednesday" to "Quarta",
        "Thursday" to "Quinta",
        "Friday" to "Sexta",
        "Saturday" to "Sábado",
        "Sunday" to "Domingo",
    )

    private val HTML_LINE_BREAK_REGEX = Regex("<\\s*br\\s*/?>", RegexOption.IGNORE_CASE)
    private val HTML_PARAGRAPH_CLOSE_REGEX = Regex("</\\s*p\\s*>", RegexOption.IGNORE_CASE)
    private val HTML_TAG_REGEX = Regex("<[^>]*>")
    private val MULTI_NEWLINE_REGEX = Regex("\\n{3,}")
    private val MULTI_SPACE_REGEX = Regex("[ \\t]{2,}")

    fun limparHtml(raw: String?): String {
        if (raw.isNullOrBlank()) return SINOPSE_PADRAO
        val comQuebras = raw
            .replace(HTML_LINE_BREAK_REGEX, "\n")
            .replace(HTML_PARAGRAPH_CLOSE_REGEX, "\n\n")
        val semTags = comQuebras.replace(HTML_TAG_REGEX, "")
        val decodificado = semTags
            .replace("&amp;", "&")
            .replace("&quot;", "\"")
            .replace("&#39;", "'")
            .replace("&apos;", "'")
            .replace("&lt;", "<")
            .replace("&gt;", ">")
            .replace("&nbsp;", " ")
            .replace("&mdash;", "—")
            .replace("&ndash;", "–")
        val normalizado = decodificado
            .lines()
            .joinToString("\n") { it.replace(MULTI_SPACE_REGEX, " ").trim() }
            .replace(MULTI_NEWLINE_REGEX, "\n\n")
            .trim()
        return normalizado.ifBlank { SINOPSE_PADRAO }
    }

    fun traduzirStatus(status: String?, isManga: Boolean = false): String {
        val limpo = status?.trim().orEmpty()
        if (limpo.isEmpty()) return STATUS_PADRAO
        if (isManga && limpo.equals("RELEASING", ignoreCase = true)) {
            return "Em Publicação"
        }
        return STATUS_MAP[limpo.lowercase()] ?: limpo
    }

    fun traduzirGeneros(generos: List<String>?): List<String> =
        generos.orEmpty()
            .filter { it.isNotBlank() }
            .map { GENEROS_MAP[it] ?: it }

    fun traduzirDiasSemana(texto: String?): String {
        val limpo = texto?.trim().orEmpty()
        if (limpo.isEmpty() || limpo.equals("Unknown", ignoreCase = true)) {
            return HORARIO_PADRAO
        }
        var resultado = limpo
        for ((ingles, portugues) in DIAS_SEMANA_REPLACEMENTS) {
            resultado = resultado.replace(Regex("\\b$ingles\\b", RegexOption.IGNORE_CASE), portugues)
        }
        resultado = resultado
            .replace(Regex("\\bat\\b", RegexOption.IGNORE_CASE), "às")
            .replace(Regex("\\bto\\b", RegexOption.IGNORE_CASE), "até")
            .trim()
        return resultado.ifBlank { HORARIO_PADRAO }
    }

    private fun normalizarUrlImagem(url: String?): String {
        val limpa = url?.trim().orEmpty()
        return if (limpa.startsWith("http://")) {
            limpa.replaceFirst("http://", "https://")
        } else {
            limpa
        }
    }

    private fun normalizarNota(score: Double?): Double? =
        score?.takeIf { it > 0.0 }

    private fun formatarProximoEpisodioAniList(dto: AniListMediaDto, isManga: Boolean): String {
        if (isManga) {
            val ano = dto.startDate?.year
            return if (ano != null) "Lançado em $ano" else "Publicação seriada"
        }
        val airingAt = dto.nextAiringEpisode?.airingAt
        val ep = dto.nextAiringEpisode?.episode
        if (airingAt != null && ep != null) {
            val sdf = SimpleDateFormat("EEEE 'às' HH:mm", Locale("pt", "BR"))
            val formatado = sdf.format(Date(airingAt * 1000L))
                .replaceFirstChar { it.titlecase(Locale("pt", "BR")) }
            return "Ep. $ep • $formatado"
        }
        return dto.seasonYear?.let { "Temporada $it" } ?: HORARIO_PADRAO
    }

    fun AniListMediaDto.toEntity(ordem: Int = 0): MidiaEntity {
        val isManga = type.equals("MANGA", ignoreCase = true)
        val categoriaEnum = if (isManga) CategoriaMidia.MANGA else CategoriaMidia.ANIME
        val prefixo = if (isManga) "manga" else "anime"
        val idNumerico = idMal ?: id
        val sinopseLimpa = limparHtml(description)
        val tituloEscolhido = title?.english?.takeIf { it.isNotBlank() }
            ?: title?.romaji?.takeIf { it.isNotBlank() }
            ?: "${if (isManga) "Mangá" else "Anime"} #$idNumerico"
        val generosPt = traduzirGeneros(genres).joinToString(",")
        val estudio = studios?.nodes?.firstOrNull()?.name.orEmpty()

        return MidiaEntity(
            id = "${prefixo}_$idNumerico",
            titulo = tituloEscolhido.trim(),
            categoria = categoriaEnum.name,
            imagemUrl = normalizarUrlImagem(coverImage?.extraLarge ?: coverImage?.large),
            nota = averageScore?.takeIf { it > 0 }?.let { it / 10.0 },
            status = traduzirStatus(status, isManga = isManga),
            horarioTransmissao = formatarProximoEpisodioAniList(this, isManga),
            sinopse = sinopseLimpa,
            sinopseOriginal = sinopseLimpa,
            generos = generosPt,
            totalEpisodiosOuCapitulos = if (isManga) chapters else episodes,
            estudioOuEmissora = estudio,
            anoLancamento = seasonYear ?: startDate?.year,
            popularidade = popularity ?: 0,
            isFavorito = false,
            statusBiblioteca = StatusBiblioteca.QUERO_VER.name,
            ordemCatalogo = ordem,
            atualizadoEm = System.currentTimeMillis(),
        )
    }

    fun AniListMediaDto.toDomain(isFavorito: Boolean = false): MidiaItem =
        toEntity(ordem = 0).toDomain().copy(isFavorito = isFavorito)

    fun JikanAnimeDto.toEntity(ordem: Int = 0): MidiaEntity {
        val sinopseLimpa = limparHtml(synopsis)
        return MidiaEntity(
            id = "anime_$malId",
            titulo = title?.trim().orEmpty().ifBlank { "Anime #$malId" },
            categoria = CategoriaMidia.ANIME.name,
            imagemUrl = normalizarUrlImagem(
                images?.jpg?.largeImageUrl
                    ?: images?.jpg?.imageUrl
                    ?: images?.webp?.largeImageUrl
                    ?: images?.webp?.imageUrl,
            ),
            nota = normalizarNota(score),
            status = traduzirStatus(status),
            horarioTransmissao = traduzirDiasSemana(broadcast?.string),
            sinopse = sinopseLimpa,
            sinopseOriginal = sinopseLimpa,
            isFavorito = false,
            ordemCatalogo = ordem,
            atualizadoEm = System.currentTimeMillis(),
        )
    }

    fun JikanAnimeDto.toDomain(isFavorito: Boolean = false): MidiaItem =
        toEntity(ordem = 0).toDomain().copy(isFavorito = isFavorito)

    fun JikanMangaDto.toEntity(ordem: Int = 0): MidiaEntity {
        val sinopseLimpa = limparHtml(synopsis)
        return MidiaEntity(
            id = "manga_$malId",
            titulo = title?.trim().orEmpty().ifBlank { "Mangá #$malId" },
            categoria = CategoriaMidia.MANGA.name,
            imagemUrl = normalizarUrlImagem(
                images?.jpg?.largeImageUrl
                    ?: images?.jpg?.imageUrl
                    ?: images?.webp?.largeImageUrl
                    ?: images?.webp?.imageUrl,
            ),
            nota = normalizarNota(score),
            status = traduzirStatus(status, isManga = true),
            horarioTransmissao = traduzirDiasSemana(published?.string),
            sinopse = sinopseLimpa,
            sinopseOriginal = sinopseLimpa,
            isFavorito = false,
            ordemCatalogo = ordem,
            atualizadoEm = System.currentTimeMillis(),
        )
    }

    fun TvMazeScheduleDto.toEntityOrNull(ordem: Int = 0): MidiaEntity? {
        val showData = show ?: return null
        return showData.toEntity(
            horarioFallback = airtime,
            sinopseFallback = summary,
            ordem = ordem,
        )
    }

    fun TvMazeShowDto.toEntity(
        horarioFallback: String? = null,
        sinopseFallback: String? = null,
        ordem: Int = 0,
    ): MidiaEntity {
        val sinopseLimpa = limparHtml(summary ?: sinopseFallback)
        val generosPt = traduzirGeneros(genres).joinToString(",")
        val emissora = network?.name?.takeIf { it.isNotBlank() }
            ?: webChannel?.name?.takeIf { it.isNotBlank() }
            ?: ""
        val ano = premiered?.take(4)?.toIntOrNull()

        return MidiaEntity(
            id = "serie_$id",
            titulo = name?.trim().orEmpty().ifBlank { "Série #$id" },
            categoria = CategoriaMidia.SERIE.name,
            imagemUrl = normalizarUrlImagem(image?.original ?: image?.medium),
            nota = normalizarNota(rating?.average),
            status = traduzirStatus(status),
            horarioTransmissao = formatarHorarioTvMaze(this, horarioFallback),
            sinopse = sinopseLimpa,
            sinopseOriginal = sinopseLimpa,
            generos = generosPt,
            totalEpisodiosOuCapitulos = null,
            estudioOuEmissora = emissora,
            anoLancamento = ano,
            popularidade = weight ?: 0,
            isFavorito = false,
            statusBiblioteca = StatusBiblioteca.QUERO_VER.name,
            ordemCatalogo = ordem,
            atualizadoEm = System.currentTimeMillis(),
        )
    }

    fun TvMazeShowDto.toDomain(isFavorito: Boolean = false): MidiaItem =
        toEntity(ordem = 0).toDomain().copy(isFavorito = isFavorito)

    private fun formatarHorarioTvMaze(show: TvMazeShowDto, horarioFallback: String?): String {
        val dias = show.schedule?.days.orEmpty()
            .filter { it.isNotBlank() }
            .joinToString(", ") { traduzirDiasSemana(it) }
        val hora = show.schedule?.time?.takeIf { it.isNotBlank() }
            ?: horarioFallback?.takeIf { it.isNotBlank() }
            ?: ""
        return when {
            dias.isNotBlank() && hora.isNotBlank() -> "$dias às $hora"
            dias.isNotBlank() -> dias
            hora.isNotBlank() -> "Às $hora"
            else -> HORARIO_PADRAO
        }
    }

    fun MidiaEntity.toDomain(): MidiaItem =
        MidiaItem(
            id = id,
            titulo = titulo,
            categoria = CategoriaMidia.valueOf(categoria),
            imagemUrl = imagemUrl,
            nota = nota,
            status = status,
            horarioTransmissao = horarioTransmissao,
            sinopse = sinopse,
            generos = generos.split(",").map { it.trim() }.filter { it.isNotEmpty() },
            totalEpisodiosOuCapitulos = totalEpisodiosOuCapitulos,
            estudioOuEmissora = estudioOuEmissora,
            anoLancamento = anoLancamento,
            popularidade = popularidade,
            isFavorito = isFavorito,
            statusBiblioteca = runCatching { StatusBiblioteca.valueOf(statusBiblioteca) }
                .getOrDefault(StatusBiblioteca.QUERO_VER),
        )

    fun MidiaItem.toEntity(
        sinopseOriginal: String = sinopse,
        ordemCatalogo: Int = 999,
    ): MidiaEntity =
        MidiaEntity(
            id = id,
            titulo = titulo,
            categoria = categoria.name,
            imagemUrl = imagemUrl,
            nota = nota,
            status = status,
            horarioTransmissao = horarioTransmissao,
            sinopse = sinopse,
            sinopseOriginal = sinopseOriginal,
            generos = generos.joinToString(","),
            totalEpisodiosOuCapitulos = totalEpisodiosOuCapitulos,
            estudioOuEmissora = estudioOuEmissora,
            anoLancamento = anoLancamento,
            popularidade = popularidade,
            isFavorito = isFavorito,
            statusBiblioteca = statusBiblioteca.name,
            ordemCatalogo = ordemCatalogo,
            atualizadoEm = System.currentTimeMillis(),
        )
}