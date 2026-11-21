package com.lzinnbarbosa.komadeck.data.remote

import com.google.gson.annotations.SerializedName

data class JikanListResponse<T>(
    @SerializedName("data")
    val data: List<T> = emptyList(),
)

data class JikanImagesDto(
    @SerializedName("jpg")
    val jpg: JikanImageSetDto? = null,
    @SerializedName("webp")
    val webp: JikanImageSetDto? = null,
)

data class JikanImageSetDto(
    @SerializedName("image_url")
    val imageUrl: String? = null,
    @SerializedName("small_image_url")
    val smallImageUrl: String? = null,
    @SerializedName("large_image_url")
    val largeImageUrl: String? = null,
)

data class JikanBroadcastDto(
    @SerializedName("day")
    val day: String? = null,
    @SerializedName("time")
    val time: String? = null,
    @SerializedName("timezone")
    val timezone: String? = null,
    @SerializedName("string")
    val string: String? = null,
)

data class JikanPublishedDto(
    @SerializedName("string")
    val string: String? = null,
)

data class JikanAnimeDto(
    @SerializedName("mal_id")
    val malId: Int,
    @SerializedName("title")
    val title: String? = null,
    @SerializedName("images")
    val images: JikanImagesDto? = null,
    @SerializedName("score")
    val score: Double? = null,
    @SerializedName("status")
    val status: String? = null,
    @SerializedName("synopsis")
    val synopsis: String? = null,
    @SerializedName("broadcast")
    val broadcast: JikanBroadcastDto? = null,
)

data class JikanMangaDto(
    @SerializedName("mal_id")
    val malId: Int,
    @SerializedName("title")
    val title: String? = null,
    @SerializedName("images")
    val images: JikanImagesDto? = null,
    @SerializedName("score")
    val score: Double? = null,
    @SerializedName("status")
    val status: String? = null,
    @SerializedName("synopsis")
    val synopsis: String? = null,
    @SerializedName("published")
    val published: JikanPublishedDto? = null,
)

data class TvMazeScheduleDto(
    @SerializedName("id")
    val id: Int,
    @SerializedName("airtime")
    val airtime: String? = null,
    @SerializedName("summary")
    val summary: String? = null,
    @SerializedName("show")
    val show: TvMazeShowDto? = null,
)

data class TvMazeSearchResultDto(
    @SerializedName("score")
    val score: Double? = null,
    @SerializedName("show")
    val show: TvMazeShowDto? = null,
)

data class TvMazeShowDto(
    @SerializedName("id")
    val id: Int,
    @SerializedName("name")
    val name: String? = null,
    @SerializedName("status")
    val status: String? = null,
    @SerializedName("genres")
    val genres: List<String>? = null,
    @SerializedName("premiered")
    val premiered: String? = null,
    @SerializedName("weight")
    val weight: Int? = null,
    @SerializedName("schedule")
    val schedule: TvMazeScheduleInfoDto? = null,
    @SerializedName("rating")
    val rating: TvMazeRatingDto? = null,
    @SerializedName("network")
    val network: TvMazeNetworkDto? = null,
    @SerializedName("webChannel")
    val webChannel: TvMazeNetworkDto? = null,
    @SerializedName("image")
    val image: TvMazeImageDto? = null,
    @SerializedName("summary")
    val summary: String? = null,
)

data class TvMazeScheduleInfoDto(
    @SerializedName("time")
    val time: String? = null,
    @SerializedName("days")
    val days: List<String>? = null,
)

data class TvMazeRatingDto(
    @SerializedName("average")
    val average: Double? = null,
)

data class TvMazeNetworkDto(
    @SerializedName("name")
    val name: String? = null,
)

data class TvMazeImageDto(
    @SerializedName("medium")
    val medium: String? = null,
    @SerializedName("original")
    val original: String? = null,
)