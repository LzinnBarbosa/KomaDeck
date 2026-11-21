package com.lzinnbarbosa.komadeck.data.remote

import retrofit2.http.GET
import retrofit2.http.Query

interface TvMazeApiService {

    @GET("shows")
    suspend fun getShowsPopulares(
        @Query("page") page: Int = 0,
    ): List<TvMazeShowDto>

    @GET("schedule")
    suspend fun getSeriesDoDia(): List<TvMazeScheduleDto>

    @GET("search/shows")
    suspend fun buscarSeries(
        @Query("q") query: String,
    ): List<TvMazeSearchResultDto>
}