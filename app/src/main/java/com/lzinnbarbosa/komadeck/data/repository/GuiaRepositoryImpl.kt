package com.lzinnbarbosa.komadeck.data.repository

import android.util.Log
import com.lzinnbarbosa.komadeck.data.local.MidiaDao
import com.lzinnbarbosa.komadeck.data.local.MidiaEntity
import com.lzinnbarbosa.komadeck.data.mapper.MidiaMappers.toDomain
import com.lzinnbarbosa.komadeck.data.mapper.MidiaMappers.toEntity
import com.lzinnbarbosa.komadeck.data.remote.AniListApiService
import com.lzinnbarbosa.komadeck.data.remote.JikanApiService
import com.lzinnbarbosa.komadeck.data.remote.TraducaoService
import com.lzinnbarbosa.komadeck.data.remote.TvMazeApiService
import com.lzinnbarbosa.komadeck.domain.model.CategoriaMidia
import com.lzinnbarbosa.komadeck.domain.model.MidiaItem
import com.lzinnbarbosa.komadeck.domain.model.StatusBiblioteca
import com.lzinnbarbosa.komadeck.domain.repository.GuiaRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GuiaRepositoryImpl @Inject constructor(
    private val midiaDao: MidiaDao,
    private val aniListApiService: AniListApiService,
    private val jikanApiService: JikanApiService,
    private val tvMazeApiService: TvMazeApiService,
    private val traducaoService: TraducaoService,
) : GuiaRepository {

    override fun observarMidiasPorCategoria(categoria: CategoriaMidia): Flow<List<MidiaItem>> =
        midiaDao.observarPorCategoria(categoria.name).map { entities ->
            entities.map { it.toDomain() }
        }

    override fun observarFavoritos(): Flow<List<MidiaItem>> =
        midiaDao.observarFavoritos().map { entities ->
            entities.map { it.toDomain() }
        }

    override fun observarPorId(id: String): Flow<MidiaItem?> =
        midiaDao.observarPorId(id).map { it?.toDomain() }

    override suspend fun sincronizarCategoria(categoria: CategoriaMidia): Result<Unit> =
        try {
            val entidadesBrutas: List<MidiaEntity> = when (categoria) {
                CategoriaMidia.ANIME -> buscarCatalogoAnimes()

                CategoriaMidia.MANGA -> buscarCatalogoMangas()

                CategoriaMidia.SERIE -> {
                    tvMazeApiService.getShowsPopulares(page = 0)
                        .filter { it.image != null && (it.weight ?: 0) >= 85 }
                        .sortedWith(
                            compareByDescending<com.lzinnbarbosa.komadeck.data.remote.TvMazeShowDto> {
                                it.weight ?: 0
                            }.thenByDescending { it.rating?.average ?: 0.0 },
                        )
                        .take(30)
                        .mapIndexed { index, dto -> dto.toEntity(ordem = index) }
                        .distinctBy { it.id }
                }
            }

            val cacheAtual = midiaDao.obterPorCategoriaSnapshot(categoria.name)
                .associateBy { it.id }
            val entidadesTraduzidas = traduzirEntidades(entidadesBrutas, cacheAtual)

            midiaDao.sincronizarCategoriaPreservandoFavoritos(
                categoria = categoria.name,
                novosItens = entidadesTraduzidas,
            )
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e("GuiaRepositoryImpl", "Falha ao sincronizar $categoria", e)
            Result.failure(e)
        }

    private suspend fun buscarCatalogoAnimes(): List<MidiaEntity> =
        runCatching {
            val resposta = aniListApiService.executarQuery(
                AniListApiService.criarQueryCatalogo(tipo = "ANIME", perPage = 25),
            )
            resposta.data?.page?.media.orEmpty()
                .mapIndexed { index, dto -> dto.toEntity(ordem = index) }
                .distinctBy { it.id }
                .also { if (it.isEmpty()) error("AniList retornou lista vazia de animes") }
        }.getOrElse { erroAniList ->
            if (erroAniList is CancellationException) throw erroAniList
            Log.w("GuiaRepositoryImpl", "AniList falhou para ANIME, tentando Jikan", erroAniList)
            jikanApiService.getAnimesTemporada().data
                .mapIndexed { index, dto -> dto.toEntity(ordem = index) }
        }

    private suspend fun buscarCatalogoMangas(): List<MidiaEntity> =
        runCatching {
            val resposta = aniListApiService.executarQuery(
                AniListApiService.criarQueryCatalogo(tipo = "MANGA", perPage = 25),
            )
            resposta.data?.page?.media.orEmpty()
                .mapIndexed { index, dto -> dto.toEntity(ordem = index) }
                .distinctBy { it.id }
                .also { if (it.isEmpty()) error("AniList retornou lista vazia de mangás") }
        }.getOrElse { erroAniList ->
            if (erroAniList is CancellationException) throw erroAniList
            Log.w("GuiaRepositoryImpl", "AniList falhou para MANGA, tentando Jikan", erroAniList)
            jikanApiService.getTopMangas().data
                .mapIndexed { index, dto -> dto.toEntity(ordem = index) }
        }

    override suspend fun buscarMidias(query: String): Result<List<MidiaItem>> = coroutineScope {
        val favoritosIds = runCatching { midiaDao.obterIdsFavoritos().toSet() }
            .getOrDefault(emptySet())

        val animesDeferred = async {
            runCatching {
                val resposta = aniListApiService.executarQuery(
                    AniListApiService.criarQueryBusca(termo = query, tipo = "ANIME", perPage = 12),
                )
                resposta.data?.page?.media.orEmpty().map { dto ->
                    val item = dto.toDomain()
                    item.copy(isFavorito = item.id in favoritosIds)
                }.distinctBy { it.id }
            }
        }
        val mangasDeferred = async {
            runCatching {
                val resposta = aniListApiService.executarQuery(
                    AniListApiService.criarQueryBusca(termo = query, tipo = "MANGA", perPage = 12),
                )
                resposta.data?.page?.media.orEmpty().map { dto ->
                    val item = dto.toDomain()
                    item.copy(isFavorito = item.id in favoritosIds)
                }.distinctBy { it.id }
            }
        }
        val seriesDeferred = async {
            runCatching {
                tvMazeApiService.buscarSeries(query).mapNotNull { result ->
                    val show = result.show ?: return@mapNotNull null
                    val id = "serie_${show.id}"
                    show.toDomain(isFavorito = id in favoritosIds)
                }.distinctBy { it.id }
            }
        }

        val animesResult = animesDeferred.await()
        val mangasResult = mangasDeferred.await()
        val seriesResult = seriesDeferred.await()

        val erroAnimes = animesResult.exceptionOrNull()
        val erroMangas = mangasResult.exceptionOrNull()
        val erroSeries = seriesResult.exceptionOrNull()
        if (erroAnimes is CancellationException) throw erroAnimes
        if (erroMangas is CancellationException) throw erroMangas
        if (erroSeries is CancellationException) throw erroSeries

        if (animesResult.isFailure && mangasResult.isFailure && seriesResult.isFailure) {
            return@coroutineScope Result.failure(
                erroAnimes ?: erroMangas ?: erroSeries ?: IllegalStateException("Falha na busca remota"),
            )
        }

        val animes = animesResult.getOrDefault(emptyList())
        val mangas = mangasResult.getOrDefault(emptyList())
        val series = seriesResult.getOrDefault(emptyList())
        val combinados = intercalarResultados(animes, mangas, series)
        Result.success(combinados)
    }

    override suspend fun alternarFavorito(id: String) {
        midiaDao.alternarFavorito(id)
    }

    override suspend fun atualizarStatusBiblioteca(id: String, status: StatusBiblioteca) {
        midiaDao.atualizarStatusBiblioteca(id = id, statusBiblioteca = status.name)
    }

    override suspend fun salvarSobDemanda(item: MidiaItem): MidiaItem {
        val existente = midiaDao.buscarPorId(item.id)
        if (existente == null) {
            midiaDao.salvarSobDemandaPreservandoFavorito(
                item.toEntity(
                    sinopseOriginal = item.sinopse,
                    ordemCatalogo = 999,
                ),
            )
        }

        val jaTraduzido = existente != null &&
                existente.sinopseOriginal == item.sinopse &&
                existente.sinopse.isNotBlank() &&
                existente.sinopse != existente.sinopseOriginal

        val sinopseTraduzida = if (jaTraduzido) {
            existente!!.sinopse
        } else {
            traducaoService.traduzirParaPtBr(item.sinopse)
        }

        val entidadeAtualizada = item.copy(sinopse = sinopseTraduzida).toEntity(
            sinopseOriginal = item.sinopse,
            ordemCatalogo = existente?.ordemCatalogo ?: 999,
        )
        midiaDao.salvarSobDemandaPreservandoFavorito(entidadeAtualizada)
        return midiaDao.buscarPorId(item.id)?.toDomain() ?: item.copy(sinopse = sinopseTraduzida)
    }

    private suspend fun traduzirEntidades(
        entidades: List<MidiaEntity>,
        cacheAtual: Map<String, MidiaEntity>,
    ): List<MidiaEntity> = coroutineScope {
        val semaforo = Semaphore(permits = 4)
        entidades.map { entidade ->
            async {
                val emCache = cacheAtual[entidade.id]
                val jaTraduzido = emCache != null &&
                        emCache.sinopseOriginal == entidade.sinopseOriginal &&
                        emCache.sinopse.isNotBlank() &&
                        emCache.sinopse != emCache.sinopseOriginal

                if (jaTraduzido) {
                    entidade.copy(sinopse = emCache!!.sinopse)
                } else {
                    val traduzida = semaforo.withPermit {
                        traducaoService.traduzirParaPtBr(entidade.sinopseOriginal)
                    }
                    entidade.copy(sinopse = traduzida)
                }
            }
        }.awaitAll()
    }

    private fun intercalarResultados(
        animes: List<MidiaItem>,
        mangas: List<MidiaItem>,
        series: List<MidiaItem>,
    ): List<MidiaItem> {
        val resultado = ArrayList<MidiaItem>(animes.size + mangas.size + series.size)
        val max = maxOf(animes.size, mangas.size, series.size)
        for (i in 0 until max) {
            if (i < animes.size) resultado.add(animes[i])
            if (i < mangas.size) resultado.add(mangas[i])
            if (i < series.size) resultado.add(series[i])
        }
        return resultado
    }
}