package com.lzinnbarbosa.komadeck

import com.lzinnbarbosa.komadeck.data.mapper.MidiaMappers
import com.lzinnbarbosa.komadeck.data.mapper.MidiaMappers.toDomain
import com.lzinnbarbosa.komadeck.data.mapper.MidiaMappers.toEntity
import com.lzinnbarbosa.komadeck.data.mapper.MidiaMappers.toEntityOrNull
import com.lzinnbarbosa.komadeck.data.remote.JikanAnimeDto
import com.lzinnbarbosa.komadeck.data.remote.JikanBroadcastDto
import com.lzinnbarbosa.komadeck.data.remote.JikanImageSetDto
import com.lzinnbarbosa.komadeck.data.remote.JikanImagesDto
import com.lzinnbarbosa.komadeck.data.remote.JikanMangaDto
import com.lzinnbarbosa.komadeck.data.remote.JikanPublishedDto
import com.lzinnbarbosa.komadeck.data.remote.TvMazeImageDto
import com.lzinnbarbosa.komadeck.data.remote.TvMazeRatingDto
import com.lzinnbarbosa.komadeck.data.remote.TvMazeScheduleDto
import com.lzinnbarbosa.komadeck.data.remote.TvMazeScheduleInfoDto
import com.lzinnbarbosa.komadeck.data.remote.TvMazeShowDto
import com.lzinnbarbosa.komadeck.domain.model.CategoriaMidia
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class MidiaMappersTest {

    @Test
    fun `mapeamento gera ids unicos prefixados por categoria`() {
        val anime = JikanAnimeDto(malId = 42, title = "Frieren").toEntity()
        val manga = JikanMangaDto(malId = 42, title = "Vagabond").toEntity()
        val serie = TvMazeScheduleDto(
            id = 99,
            show = TvMazeShowDto(id = 42, name = "Severance"),
        ).toEntityOrNull()!!

        assertEquals("anime_42", anime.id)
        assertEquals(CategoriaMidia.ANIME.name, anime.categoria)
        assertEquals("manga_42", manga.id)
        assertEquals(CategoriaMidia.MANGA.name, manga.categoria)
        assertEquals("serie_42", serie.id)
        assertEquals(CategoriaMidia.SERIE.name, serie.categoria)
    }

    @Test
    fun `limparHtml remove tags p b i br e decodifica entidades`() {
        val html = "<p>Uma história <b>épica</b> sobre <i>coragem</i>.<br>Novo episódio &amp; aventura!</p>"
        val limpo = MidiaMappers.limparHtml(html)

        assertEquals(
            "Uma história épica sobre coragem.\nNovo episódio & aventura!",
            limpo,
        )
    }

    @Test
    fun `traduzirStatus traduz todos os status obrigatorios para pt-BR`() {
        assertEquals("Em Exibição", MidiaMappers.traduzirStatus("Currently Airing"))
        assertEquals("Em Exibição", MidiaMappers.traduzirStatus("Running"))
        assertEquals("Finalizado", MidiaMappers.traduzirStatus("Finished Airing"))
        assertEquals("Finalizado", MidiaMappers.traduzirStatus("Ended"))
        assertEquals("Em Publicação", MidiaMappers.traduzirStatus("Publishing"))
        assertEquals("Em Hiato", MidiaMappers.traduzirStatus("On Hiatus"))
        assertEquals("Em Breve", MidiaMappers.traduzirStatus("Not yet aired"))
    }

    @Test
    fun `traduzirDiasSemana traduz todos os dias da semana para pt-BR`() {
        assertEquals("Segundas às 23:00", MidiaMappers.traduzirDiasSemana("Mondays at 23:00"))
        assertEquals("Terças às 22:00", MidiaMappers.traduzirDiasSemana("Tuesdays at 22:00"))
        assertEquals("Quartas às 21:00", MidiaMappers.traduzirDiasSemana("Wednesdays at 21:00"))
        assertEquals("Quintas às 20:00", MidiaMappers.traduzirDiasSemana("Thursdays at 20:00"))
        assertEquals("Sextas às 19:00", MidiaMappers.traduzirDiasSemana("Fridays at 19:00"))
        assertEquals("Sábados às 18:00", MidiaMappers.traduzirDiasSemana("Saturdays at 18:00"))
        assertEquals("Domingos às 17:00", MidiaMappers.traduzirDiasSemana("Sundays at 17:00"))
    }

    @Test
    fun `mapeamento trata nota nula ou zero e normaliza url de imagem`() {
        val anime = JikanAnimeDto(
            malId = 7,
            title = "Dandadan",
            images = JikanImagesDto(jpg = JikanImageSetDto(largeImageUrl = "https://cdn.example/img.jpg")),
            score = 0.0,
            status = "Currently Airing",
            broadcast = JikanBroadcastDto(string = "Thursdays at 00:26 (JST)"),
            synopsis = null,
        ).toDomain()

        assertNull(anime.nota)
        assertEquals("Em Exibição", anime.status)
        assertEquals("Quintas às 00:26 (JST)", anime.horarioTransmissao)
        assertEquals("Sinopse não disponível no momento.", anime.sinopse)

        val manga = JikanMangaDto(
            malId = 1,
            title = "Berserk",
            score = 9.47,
            status = "Publishing",
            published = JikanPublishedDto(string = "Aug 25, 1989 to ?"),
        ).toEntity()
        assertEquals(9.47, manga.nota!!, 0.001)
        assertEquals("Em Publicação", manga.status)

        val serie = TvMazeShowDto(
            id = 10,
            name = "Dark",
            status = "Ended",
            schedule = TvMazeScheduleInfoDto(time = "21:00", days = listOf("Friday")),
            rating = TvMazeRatingDto(average = null),
            image = TvMazeImageDto(original = "http://static.tvmaze.com/dark.jpg"),
            summary = "<p>Mistério no <b>tempo</b>.</p>",
        ).toDomain()

        assertNull(serie.nota)
        assertEquals("Finalizado", serie.status)
        assertEquals("Sexta às 21:00", serie.horarioTransmissao)
        assertEquals("https://static.tvmaze.com/dark.jpg", serie.imagemUrl)
        assertEquals("Mistério no tempo.", serie.sinopse)
    }
}
