package com.lzinnbarbosa.komadeck.data.remote

import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface AniListApiService {

    @Headers(
        "Content-Type: application/json",
        "Accept: application/json",
    )
    @POST(".")
    suspend fun executarQuery(
        @Body request: AniListGraphQLRequest,
    ): AniListPageResponse

    companion object {
        private const val MEDIA_FIELDS = """
            id
            idMal
            type
            title {
              romaji
              english
            }
            coverImage {
              extraLarge
              large
            }
            averageScore
            popularity
            status
            genres
            episodes
            chapters
            seasonYear
            startDate {
              year
            }
            studios(isMain: true) {
              nodes {
                name
              }
            }
            nextAiringEpisode {
              airingAt
              episode
            }
            description(asHtml: false)
        """

        fun criarQueryCatalogo(tipo: String, perPage: Int = 25): AniListGraphQLRequest {
            val query = """
                query (${'$'}type: MediaType, ${'$'}perPage: Int) {
                  Page(page: 1, perPage: ${'$'}perPage) {
                    media(
                      type: ${'$'}type,
                      sort: [TRENDING_DESC, POPULARITY_DESC],
                      isAdult: false
                    ) {
                      $MEDIA_FIELDS
                    }
                  }
                }
            """.trimIndent()
            return AniListGraphQLRequest(
                query = query,
                variables = mapOf(
                    "type" to tipo,
                    "perPage" to perPage,
                ),
            )
        }

        fun criarQueryBusca(termo: String, tipo: String, perPage: Int = 15): AniListGraphQLRequest {
            val query = """
                query (${'$'}search: String, ${'$'}type: MediaType, ${'$'}perPage: Int) {
                  Page(page: 1, perPage: ${'$'}perPage) {
                    media(
                      search: ${'$'}search,
                      type: ${'$'}type,
                      sort: [POPULARITY_DESC, SCORE_DESC],
                      isAdult: false
                    ) {
                      $MEDIA_FIELDS
                    }
                  }
                }
            """.trimIndent()
            return AniListGraphQLRequest(
                query = query,
                variables = mapOf(
                    "search" to termo,
                    "type" to tipo,
                    "perPage" to perPage,
                ),
            )
        }
    }
}