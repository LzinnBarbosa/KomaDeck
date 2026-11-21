package com.lzinnbarbosa.komadeck.data.remote

import com.google.gson.annotations.SerializedName

data class AniListGraphQLRequest(
    @SerializedName("query")
    val query: String,
    @SerializedName("variables")
    val variables: Map<String, Any?> = emptyMap(),
)

data class AniListPageResponse(
    @SerializedName("data")
    val data: AniListPageData?,
)

data class AniListPageData(
    @SerializedName("Page")
    val page: AniListPage?,
)

data class AniListPage(
    @SerializedName("media")
    val media: List<AniListMediaDto> = emptyList(),
)

data class AniListMediaDto(
    @SerializedName("id")
    val id: Int,
    @SerializedName("idMal")
    val idMal: Int? = null,
    @SerializedName("type")
    val type: String? = null,
    @SerializedName("title")
    val title: AniListTitleDto? = null,
    @SerializedName("coverImage")
    val coverImage: AniListCoverImageDto? = null,
    @SerializedName("averageScore")
    val averageScore: Int? = null,
    @SerializedName("popularity")
    val popularity: Int? = null,
    @SerializedName("status")
    val status: String? = null,
    @SerializedName("genres")
    val genres: List<String>? = null,
    @SerializedName("episodes")
    val episodes: Int? = null,
    @SerializedName("chapters")
    val chapters: Int? = null,
    @SerializedName("seasonYear")
    val seasonYear: Int? = null,
    @SerializedName("startDate")
    val startDate: AniListDateDto? = null,
    @SerializedName("studios")
    val studios: AniListStudioConnectionDto? = null,
    @SerializedName("nextAiringEpisode")
    val nextAiringEpisode: AniListAiringScheduleDto? = null,
    @SerializedName("description")
    val description: String? = null,
)

data class AniListTitleDto(
    @SerializedName("romaji")
    val romaji: String? = null,
    @SerializedName("english")
    val english: String? = null,
)

data class AniListCoverImageDto(
    @SerializedName("extraLarge")
    val extraLarge: String? = null,
    @SerializedName("large")
    val large: String? = null,
)

data class AniListDateDto(
    @SerializedName("year")
    val year: Int? = null,
)

data class AniListStudioConnectionDto(
    @SerializedName("nodes")
    val nodes: List<AniListStudioNodeDto>? = null,
)

data class AniListStudioNodeDto(
    @SerializedName("name")
    val name: String? = null,
)

data class AniListAiringScheduleDto(
    @SerializedName("airingAt")
    val airingAt: Long? = null,
    @SerializedName("episode")
    val episode: Int? = null,
)