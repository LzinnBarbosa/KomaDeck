package com.lzinnbarbosa.komadeck.di

import android.content.Context
import androidx.room.Room
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.lzinnbarbosa.komadeck.data.local.AppDatabase
import com.lzinnbarbosa.komadeck.data.local.MidiaDao
import com.lzinnbarbosa.komadeck.data.remote.AniListApiService
import com.lzinnbarbosa.komadeck.data.remote.GoogleTraducaoService
import com.lzinnbarbosa.komadeck.data.remote.JikanApiService
import com.lzinnbarbosa.komadeck.data.remote.TraducaoApiService
import com.lzinnbarbosa.komadeck.data.remote.TraducaoService
import com.lzinnbarbosa.komadeck.data.remote.TvMazeApiService
import com.lzinnbarbosa.komadeck.data.repository.GuiaRepositoryImpl
import com.lzinnbarbosa.komadeck.domain.repository.GuiaRepository
import com.lzinnbarbosa.komadeck.domain.usecase.AlternarFavoritoUseCase
import com.lzinnbarbosa.komadeck.domain.usecase.AtualizarStatusBibliotecaUseCase
import com.lzinnbarbosa.komadeck.domain.usecase.BuscarMidiasUseCase
import com.lzinnbarbosa.komadeck.domain.usecase.KomaDeckUseCases
import com.lzinnbarbosa.komadeck.domain.usecase.ObterDetalhesMidiaUseCase
import com.lzinnbarbosa.komadeck.domain.usecase.ObterFavoritosUseCase
import com.lzinnbarbosa.komadeck.domain.usecase.ObterMidiasUseCase
import com.lzinnbarbosa.komadeck.domain.usecase.SalvarMidiaSobDemandaUseCase
import com.lzinnbarbosa.komadeck.domain.usecase.SincronizarCategoriaUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.Dns
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.net.Inet4Address
import java.net.InetAddress
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModules {

    private const val ANILIST_BASE_URL = "https://graphql.anilist.co/"
    private const val JIKAN_BASE_URL = "https://api.jikan.moe/v4/"
    private const val TVMAZE_BASE_URL = "https://api.tvmaze.com/"
    private const val TRANSLATE_BASE_URL = "https://translate.googleapis.com/"

    private val ipv4FirstDns = object : Dns {
        override fun lookup(hostname: String): List<InetAddress> =
            Dns.SYSTEM.lookup(hostname).sortedBy { it !is Inet4Address }
    }

    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext context: Context,
    ): AppDatabase =
        Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "komadeck_database",
        )
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    @Singleton
    fun provideMidiaDao(database: AppDatabase): MidiaDao =
        database.midiaDao()

    @Provides
    @Singleton
    fun provideGson(): Gson =
        GsonBuilder().create()

    @Provides
    @Singleton
    @Named("CatalogoOkHttp")
    fun provideCatalogoOkHttpClient(): OkHttpClient {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }
        return OkHttpClient.Builder()
            .dns(ipv4FirstDns)
            .addInterceptor { chain ->
                val request = chain.request().newBuilder()
                    .header("User-Agent", "KomaDeck/1.0 (Android)")
                    .build()
                chain.proceed(request)
            }
            .addInterceptor(logging)
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    @Named("TraducaoOkHttp")
    fun provideTraducaoOkHttpClient(): OkHttpClient =
        OkHttpClient.Builder()
            .dns(ipv4FirstDns)
            .connectTimeout(6, TimeUnit.SECONDS)
            .readTimeout(6, TimeUnit.SECONDS)
            .build()

    @Provides
    @Singleton
    @Named("AniListRetrofit")
    fun provideAniListRetrofit(
        @Named("CatalogoOkHttp") okHttpClient: OkHttpClient,
        gson: Gson,
    ): Retrofit =
        Retrofit.Builder()
            .baseUrl(ANILIST_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()

    @Provides
    @Singleton
    @Named("JikanRetrofit")
    fun provideJikanRetrofit(
        @Named("CatalogoOkHttp") okHttpClient: OkHttpClient,
        gson: Gson,
    ): Retrofit =
        Retrofit.Builder()
            .baseUrl(JIKAN_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()

    @Provides
    @Singleton
    @Named("TvMazeRetrofit")
    fun provideTvMazeRetrofit(
        @Named("CatalogoOkHttp") okHttpClient: OkHttpClient,
        gson: Gson,
    ): Retrofit =
        Retrofit.Builder()
            .baseUrl(TVMAZE_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()

    @Provides
    @Singleton
    @Named("TraducaoRetrofit")
    fun provideTraducaoRetrofit(
        @Named("TraducaoOkHttp") okHttpClient: OkHttpClient,
        gson: Gson,
    ): Retrofit =
        Retrofit.Builder()
            .baseUrl(TRANSLATE_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()

    @Provides
    @Singleton
    fun provideAniListApiService(
        @Named("AniListRetrofit") retrofit: Retrofit,
    ): AniListApiService = retrofit.create(AniListApiService::class.java)

    @Provides
    @Singleton
    fun provideJikanApiService(
        @Named("JikanRetrofit") retrofit: Retrofit,
    ): JikanApiService = retrofit.create(JikanApiService::class.java)

    @Provides
    @Singleton
    fun provideTvMazeApiService(
        @Named("TvMazeRetrofit") retrofit: Retrofit,
    ): TvMazeApiService = retrofit.create(TvMazeApiService::class.java)

    @Provides
    @Singleton
    fun provideTraducaoApiService(
        @Named("TraducaoRetrofit") retrofit: Retrofit,
    ): TraducaoApiService = retrofit.create(TraducaoApiService::class.java)

    @Provides
    @Singleton
    fun provideTraducaoService(
        impl: GoogleTraducaoService,
    ): TraducaoService = impl

    @Provides
    @Singleton
    fun provideGuiaRepository(
        impl: GuiaRepositoryImpl,
    ): GuiaRepository = impl

    @Provides
    @Singleton
    fun provideKomaDeckUseCases(
        repository: GuiaRepository,
    ): KomaDeckUseCases =
        KomaDeckUseCases(
            obterMidias = ObterMidiasUseCase(repository),
            obterFavoritos = ObterFavoritosUseCase(repository),
            obterDetalhesMidia = ObterDetalhesMidiaUseCase(repository),
            sincronizarCategoria = SincronizarCategoriaUseCase(repository),
            buscarMidias = BuscarMidiasUseCase(repository),
            alternarFavorito = AlternarFavoritoUseCase(repository),
            atualizarStatusBiblioteca = AtualizarStatusBibliotecaUseCase(repository),
            salvarMidiaSobDemanda = SalvarMidiaSobDemandaUseCase(repository),
        )
}