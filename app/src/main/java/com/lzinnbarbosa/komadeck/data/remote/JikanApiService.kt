package com.lzinnbarbosa.komadeck.data.remote

import retrofit2.http.GET
import retrofit2.http.Query

interface JikanApiService {

    @GET("seasons/now")
    suspend fun getAnimesTemporada(
        @Query("limit") limit: Int = 25,
    ): JikanListResponse<JikanAnimeDto>

    @GET("top/manga")
    suspend fun getTopMangas(
        @Query("limit") limit: Int = 25,
    ): JikanListResponse<JikanMangaDto>

    @GET("anime")
    suspend fun buscarAnimes(
        @Query("q") query: String,
        @Query("limit") limit: Int = 15,
    ): JikanListResponse<JikanAnimeDto>
}
