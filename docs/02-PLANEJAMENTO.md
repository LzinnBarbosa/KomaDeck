# 02. Planejamento Arquitetural e Técnico — KomaDeck

## 1. Arquitetura de Software (Clean Architecture + MVVM)

A aplicação é estruturada em **Clean Architecture**, dividida em três camadas principais com fluxo de dependência apontando exclusivamente para o centro (`Presentation -> Domain <- Data`):

1. **Camada `domain` (Núcleo de Negócio):**
   * **Independência:** Módulo 100% Kotlin puro, sem dependências de bibliotecas Android, Retrofit, Room ou Compose.
   * **Componentes:**
     * `model/`: Entidades e enumerações de domínio imutáveis (`MidiaItem`, `CategoriaMidia` [`ANIME`, `MANGA`, `SERIE`], `StatusBiblioteca` [`QUERO_VER`, `ACOMPANHANDO`, `CONCLUIDO`]).
     * `repository/`: Contrato abstrato `GuiaRepository` definindo operações reativas (`Flow`) e suspensas (`suspend fun`).
     * `usecase/`: Casos de uso encapsulando regras de negócio específicas (`ObterMidiasUseCase`, `ObterFavoritosUseCase`, `ObterDetalhesMidiaUseCase`, `SincronizarCategoriaUseCase`, `BuscarMidiasUseCase`, `AlternarFavoritoUseCase`, `AtualizarStatusBibliotecaUseCase`, `SalvarMidiaSobDemandaUseCase`), agrupados na fachada `KomaDeckUseCases`.

2. **Camada `data` (Infraestrutura e Dados):**
   * **Responsabilidade:** Implementar a interface `GuiaRepository`, coordenar fontes de dados remotas e locais e realizar conversões de modelos.
   * **Componentes:**
     * `local/`: Persistência SQLite via Room (`AppDatabase` v2, `MidiaDao`, `MidiaEntity`).
     * `remote/`: Clientes HTTP via Retrofit (`AniListApiService` GraphQL, `JikanApiService` REST fallback, `TvMazeApiService` REST, `GoogleTraducaoService` para tradução `en -> pt-BR`) e modelos de transferência (`AniListDtos.kt`, `ApiDtos.kt`).
     * `mapper/`: `MidiaMappers` responsável por transformar `DTO -> Entity -> Domain`, sanitizar HTML (RN02), prefixar IDs (RN04) e traduzir status, gêneros e dias da semana (RN07).
     * `repository/`: `GuiaRepositoryImpl`, orquestrando requisições paralelas (`async/await`), fallback automático AniList $\rightarrow$ Jikan, semáforo de tradução (`Semaphore(4)`) e transações atômicas no banco.

3. **Camada `presentation` (Interface e Estado):**
   * **Responsabilidade:** Renderizar a interface declarativa, gerenciar navegação e transformar fluxos do domínio em estados imutáveis de tela.
   * **Componentes:**
     * `catalogo/`: `CatalogoViewModel`, `CatalogoScreen`, estados selados (`ConteudoState`: `Loading`, `Success`, `Empty`, `Error`) e filtros (`FiltroOrdenacao`).
     * `detalhes/`: `DetalhesViewModel` (utilizando `SavedStateHandle` para recuperação segura de argumentos de rota) e `DetalhesScreen`.
     * `components/`: Componentes reutilizáveis Jetpack Compose (`MidiaComponents.kt`).
     * `theme/`: Identidade visual Material Design 3 (`Color.kt`, `Theme.kt`, `Type.kt`).
     * `navigation/`: `KomaDeckNavGraph.kt` gerenciando as rotas `catalogo` e `detalhes/{midiaId}`.

4. **Camada `di` (Injeção de Dependências):**
   * Módulo Hilt (`AppModules.kt`) instalado em `SingletonComponent`, provendo `AppDatabase`, `MidiaDao`, `Gson`, clientes `OkHttpClient` dedicados (`CatalogoOkHttp` com DNS IPv4-first e `TraducaoOkHttp`), instâncias `Retrofit` nomeadas, serviços de API, `TraducaoService`, `GuiaRepository` e `KomaDeckUseCases`.

---

## 2. Estrutura Completa de Diretórios do Projeto

```text
KomaDeck/
├── docs/
│   ├── 01-ESPECIFICACAO.md
│   ├── 02-PLANEJAMENTO.md
│   ├── 03-DECOMPOSICAO.md
│   └── evidencias/
├── app/
│   └── src/
│       ├── main/
│       │   ├── java/com/lzinnbarbosa/komadeck/
│       │   │   ├── data/
│       │   │   │   ├── local/
│       │   │   │   │   ├── AppDatabase.kt
│       │   │   │   │   ├── MidiaDao.kt
│       │   │   │   │   └── MidiaEntity.kt
│       │   │   │   ├── mapper/
│       │   │   │   │   └── MidiaMappers.kt
│       │   │   │   ├── remote/
│       │   │   │   │   ├── AniListApiService.kt
│       │   │   │   │   ├── AniListDtos.kt
│       │   │   │   │   ├── ApiDtos.kt
│       │   │   │   │   ├── JikanApiService.kt
│       │   │   │   │   ├── TraducaoService.kt
│       │   │   │   │   └── TvMazeApiService.kt
│       │   │   │   └── repository/
│       │   │   │       └── GuiaRepositoryImpl.kt
│       │   │   ├── di/
│       │   │   │   └── AppModules.kt
│       │   │   ├── domain/
│       │   │   │   ├── model/
│       │   │   │   │   └── MidiaItem.kt
│       │   │   │   ├── repository/
│       │   │   │   │   └── GuiaRepository.kt
│       │   │   │   └── usecase/
│       │   │   │       └── KomaDeckUseCases.kt
│       │   │   ├── presentation/
│       │   │   │   ├── catalogo/
│       │   │   │   │   ├── CatalogoScreen.kt
│       │   │   │   │   └── CatalogoViewModel.kt
│       │   │   │   ├── components/
│       │   │   │   │   └── MidiaComponents.kt
│       │   │   │   ├── detalhes/
│       │   │   │   │   ├── DetalhesScreen.kt
│       │   │   │   │   └── DetalhesViewModel.kt
│       │   │   │   ├── navigation/
│       │   │   │   │   └── KomaDeckNavGraph.kt
│       │   │   │   └── theme/
│       │   │   │       ├── Color.kt
│       │   │   │       ├── Theme.kt
│       │   │   │       └── Type.kt
│       │   │   ├── KomaDeckApp.kt
│       │   │   └── MainActivity.kt
│       │   ├── res/
│       │   └── AndroidManifest.xml
│       └── test/
│           └── java/com/lzinnbarbosa/komadeck/
│               ├── BuscarMidiasUseCaseTest.kt
│               ├── CatalogoViewModelTest.kt
│               └── MidiaMappersTest.kt
├── README.md
└── TEST_RESULTS.md
```

---

## 3. Tecnologias e Justificativas Técnicas

Tecnologia | Versão / Biblioteca	| Papel no Projeto |
Android Gradle Plugin (AGP) |	9.1.1	|Sistema de build e empacotamento Android (minSdk 24, compileSdk 36, targetSdk 36).|
Kotlin |	2.2.10	| Linguagem principal com suporte nativo a Coroutines, Flow e Null-Safety.|
KSP (Kotlin Symbol Processing)|	2.3.5 |	Geração de código em tempo de compilação para Room e Dagger Hilt. |
Jetpack Compose |	BOM 2024.09.00	| Construção de UI declarativa reativa com Material Design 3. |
Navigation Compose|	2.8.9	| Gerenciamento de rotas entre o catálogo principal e a tela de detalhes. |
Dagger Hilt |	2.59.2	| Injeção de dependências (SingletonComponent e HiltViewModel). |
Retrofit + Gson + OkHttp |	2.12.0 / 2.11.0 / 4.10.0 |	Clientes HTTP/GraphQL declarativos, desserialização JSON e interceptores. |
Room Database |	2.7.0	| Persistência SQLite local com suporte reativo a Flow e @Transaction. |
Coil Compose |	2.7.0	| Carregamento assíncrono de imagens com cache automático em memória e disco. |
Coroutines & Test |	1.10.2 |	Execução assíncrona (async/await, Semaphore) e testes determinísticos. |
JUnit 4 & Robolectric |	4.13.2 / 4.16.1 |	Execução de testes unitários automatizados na JVM. |

---

## 4. APIs REST Integradas

1. **AniList GraphQL API (`https://graphql.anilist.co/`) — Fonte Principal de Animes e Mangás**
   * `POST /`: Executa queries GraphQL parametrizadas 
      (`Page(page: 1, perPage: 25)`)  ordenadas por 
      `[TRENDING_DESC, POPULARITY_DESC]` para catálogo e     
      `[POPULARITY_DESC, SCORE_DESC]` para busca (perPage: 12).

2. **Jikan API v4 (https://api.jikan.moe/v4/) — Fallback Automático**
   * `GET seasons/now?limit=25`: Acionado automaticamente caso a consulta de animes na AniList falhe.   
   * `GET top/manga?limit=25`: Acionado automaticamente caso a consulta de mangás na AniList falhe.

3. **TVMaze API (`[https://api.tvmaze.com/](https://api.tvmaze.com/)`)**
   * `GET shows?page=0`: Retorna séries populares filtradas localmente por relevância `(weight >= 85)`.
   * `GET search/shows?q={query}`: Pesquisa séries por título.

4. **Google Translate API (`https://translate.googleapis.com/`) — Localização de Sinopses**
   * `GET translate_a/single?client=gtx&sl=en&tl=pt-BR&dt=t&q={texto}`: Traduz sinopses em inglês para
     T-BR com controle de concorrência `(Semaphore(4))` e persistência no Room.

---

## 5. Estratégia Offline First e Sincronização

```text
[APIs Remotas: AniList GraphQL + Jikan Fallback + TVMaze + Translate]
           │ (1) Sincronização Assíncrona + Tradução PT-BR (Semaphore = 4)
           ▼
  [GuiaRepositoryImpl] ──► Sanitização HTML + Mapeamento para MidiaEntity
           │ (2) @Transaction sincronizarCategoriaPreservandoFavoritos()
           ▼
   [Room Database (SSOT)] ◄── Única Fonte da Verdade (komadeck_database)
           │ (3) Flow<List<MidiaEntity>> reativo
           ▼
   [CatalogoViewModel / DetalhesViewModel] ──► StateFlow<UiState>
           │ (4) collectAsStateWithLifecycle()
           ▼
   [Jetpack Compose UI] + [Coil Disk/Memory Image Cache]
```

* **Leitura Reativa:** A UI nunca lê dados diretamente da rede; todas as listas são alimentadas pelos fluxos contínuos (`Flow`) do Room `(komadeck_database)`.

* **Sincronização Não-Destrutiva:** Na atualização de catálogo `@Transaction sincronizarCategoriaPreservandoFavoritos`lê previamente os registros favoritados, preserva `isFavorito = true` e `statusBiblioteca`, limpa apenas os itens não favoritados daquela categoria e insere o catálogo atualizado.

* **Persistência Sob Demanda:** Itens encontrados na busca remota são gravados no Room via `salvarSobDemandaPreservandoFavorito`ao serem favoritados ou abertos nos detalhes, tornando-se disponíveis offline.

* **Fallback Offline:** Falhas de rede (`IOException` ou `HttpException`) são encapsuladas em `Result.failure`, ativando um banner informativo não bloqueante enquanto os dados locais e capas cacheadas pelo Coil permanecem acessíveis.

---

## 6. Estratégia de Implementação e Validação

* **Desenvolvimento Incremental (SDD):** Cada camada será implementada, compilada e validada isoladamente antes de avançar para a próxima etapa, com registro granular no histórico Git.
* **Prevenção de Erros, Recomposição e Memory Leaks:**
  * Uso exclusivo de `collectAsStateWithLifecycle()` na UI para interromper coletas quando o app vai para segundo plano.
  * Uso de `key = { it.id }` nos componentes `LazyColumn` e estados imutáveis (`data class`) para evitar recomposições desnecessárias no Compose.
  * Tratamento defensivo de campos nulos vindos das APIs (`score`, `synopsis`, `images`, `broadcast`).
* **Validação Automatizada e Manual:** Execução da suíte JUnit via Gradle (`./gradlew testDebugUnitTest`) seguida de teste funcional em emulador com ativação do **Modo Avião**.

---

## 7. Refinamento de Interface e Localização PT-BR

* **Identidade Visual (Tema Claro Editorial):** Esquema de cores claro exclusivo (`Background #F7F8FC`, `Surface #FFFFFF`, `Primary #3949AB`) com badges semânticos por categoria (Coral para Animes, Esmeralda para Mangás e Azul Cobalto para Séries) e banner discreto em âmbar suave para o modo offline.
* **Layout e Navegação:** Cards horizontais detalhados nas abas inferiores (*Animes*, *Mangás*, *Séries* e *Favoritos Offline*) com ícones semânticos (`SmartDisplay`, `AutoStories`, `Tv`, `Favorite`) e busca unificada com chips de filtro por categoria (*Todos*, *Animes*, *Séries*).
* **Localização 100% PT-BR:** Tradução determinística de status e dias da semana na camada `data/mapper` e tradução automática de sinopses (`en` $\rightarrow$ `pt-BR`) antes da persistência no Room, com fallback offline para o texto sanitizado.  