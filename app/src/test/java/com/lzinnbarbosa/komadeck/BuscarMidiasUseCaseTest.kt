package com.lzinnbarbosa.komadeck

import com.lzinnbarbosa.komadeck.domain.model.CategoriaMidia
import com.lzinnbarbosa.komadeck.domain.model.MidiaItem
import com.lzinnbarbosa.komadeck.domain.model.StatusBiblioteca
import com.lzinnbarbosa.komadeck.domain.repository.GuiaRepository
import com.lzinnbarbosa.komadeck.domain.usecase.BuscarMidiasUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class BuscarMidiasUseCaseTest {

    @Test
    fun `nao consulta repositorio quando query tem menos de 3 caracteres`() = runTest {
        val fakeRepo = FakeBuscaRepository()
        val useCase = BuscarMidiasUseCase(fakeRepo)

        val r1 = useCase("")
        val r2 = useCase("ab")
        val r3 = useCase("  a ")

        assertTrue(r1.isSuccess)
        assertTrue(r1.getOrThrow().isEmpty())
        assertTrue(r2.getOrThrow().isEmpty())
        assertTrue(r3.getOrThrow().isEmpty())
        assertEquals(0, fakeRepo.chamadasBusca)
    }

    @Test
    fun `consulta repositorio com termo aparado quando query tem 3 ou mais caracteres`() = runTest {
        val itensEsperados = listOf(
            MidiaItem(
                id = "anime_1",
                titulo = "Cowboy Bebop",
                categoria = CategoriaMidia.ANIME,
                imagemUrl = "",
                nota = 8.8,
                status = "Finalizado",
                horarioTransmissao = "Sextas às 01:00",
                sinopse = "Caçadores de recompensa.",
            ),
            MidiaItem(
                id = "manga_2",
                titulo = "Cowboy Bebop: Shooting Star",
                categoria = CategoriaMidia.MANGA,
                imagemUrl = "",
                nota = 7.4,
                status = "Finalizado",
                horarioTransmissao = "Lançado em 1997",
                sinopse = "Adaptação em mangá.",
            ),
            MidiaItem(
                id = "serie_3",
                titulo = "Cowboy Series",
                categoria = CategoriaMidia.SERIE,
                imagemUrl = "",
                nota = null,
                status = "Em Exibição",
                horarioTransmissao = "Segundas às 21:00",
                sinopse = "Série espacial.",
            ),
        )
        val fakeRepo = FakeBuscaRepository(resultado = Result.success(itensEsperados))
        val useCase = BuscarMidiasUseCase(fakeRepo)

        val resultado = useCase("  cow  ")

        assertTrue(resultado.isSuccess)
        assertEquals(itensEsperados, resultado.getOrThrow())
        assertEquals(1, fakeRepo.chamadasBusca)
        assertEquals("cow", fakeRepo.ultimaQuery)
    }

    @Test
    fun `propaga falha quando busca remota falha`() = runTest {
        val fakeRepo = FakeBuscaRepository(
            resultado = Result.failure(IllegalStateException("Sem internet")),
        )
        val useCase = BuscarMidiasUseCase(fakeRepo)

        val resultado = useCase("naruto")

        assertTrue(resultado.isFailure)
        assertEquals(1, fakeRepo.chamadasBusca)
    }

    private class FakeBuscaRepository(
        private val resultado: Result<List<MidiaItem>> = Result.success(emptyList()),
    ) : GuiaRepository {
        var chamadasBusca = 0
        var ultimaQuery: String? = null

        override fun observarMidiasPorCategoria(categoria: CategoriaMidia): Flow<List<MidiaItem>> =
            emptyFlow()

        override fun observarFavoritos(): Flow<List<MidiaItem>> = emptyFlow()

        override fun observarPorId(id: String): Flow<MidiaItem?> = emptyFlow()

        override suspend fun sincronizarCategoria(categoria: CategoriaMidia): Result<Unit> =
            Result.success(Unit)

        override suspend fun buscarMidias(query: String): Result<List<MidiaItem>> {
            chamadasBusca++
            ultimaQuery = query
            return resultado
        }

        override suspend fun alternarFavorito(id: String) = Unit

        override suspend fun atualizarStatusBiblioteca(id: String, status: StatusBiblioteca) = Unit

        override suspend fun salvarSobDemanda(item: MidiaItem): MidiaItem = item
    }
}