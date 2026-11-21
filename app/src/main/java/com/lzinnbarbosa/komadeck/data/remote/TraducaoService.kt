package com.lzinnbarbosa.komadeck.data.remote

import com.google.gson.JsonElement
import retrofit2.http.GET
import retrofit2.http.Query
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

interface TraducaoApiService {
    @GET("translate_a/single")
    suspend fun traduzir(
        @Query("client") client: String = "gtx",
        @Query("sl") sourceLang: String = "en",
        @Query("tl") targetLang: String = "pt-BR",
        @Query("dt") dataType: String = "t",
        @Query("q") query: String,
    ): JsonElement
}

interface TraducaoService {
    suspend fun traduzirParaPtBr(textoSanitizado: String): String
}

@Singleton
class GoogleTraducaoService @Inject constructor(
    private val api: TraducaoApiService,
) : TraducaoService {

    private val cacheMemoria = ConcurrentHashMap<String, String>()

    override suspend fun traduzirParaPtBr(textoSanitizado: String): String {
        val limpo = textoSanitizado.trim()
        if (limpo.isBlank() || limpo == SINOPSE_INDISPONIVEL) {
            return SINOPSE_INDISPONIVEL
        }
        cacheMemoria[limpo]?.let { return it }

        return runCatching {
            val textoLimitado = if (limpo.length > MAX_TRANSLATE_CHARS) {
                limpo.take(MAX_TRANSLATE_CHARS)
            } else {
                limpo
            }
            val json = api.traduzir(query = textoLimitado)
            val traduzido = extrairTextoTraduzido(json)
            if (traduzido.isNotBlank()) {
                cacheMemoria[limpo] = traduzido
                traduzido
            } else {
                limpo
            }
        }.getOrDefault(limpo)
    }

    internal fun extrairTextoTraduzido(json: JsonElement): String {
        if (!json.isJsonArray) return ""
        val rootArray = json.asJsonArray
        if (rootArray.size() == 0 || !rootArray[0].isJsonArray) return ""
        val segmentos = rootArray[0].asJsonArray
        val builder = StringBuilder()
        for (segmento in segmentos) {
            if (segmento.isJsonArray) {
                val par = segmento.asJsonArray
                if (par.size() > 0 && par[0].isJsonPrimitive) {
                    builder.append(par[0].asString)
                }
            }
        }
        return builder.toString().trim()
    }

    companion object {
        const val SINOPSE_INDISPONIVEL = "Sinopse não disponível no momento."
        private const val MAX_TRANSLATE_CHARS = 1800
    }
}
