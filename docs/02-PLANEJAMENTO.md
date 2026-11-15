# 02. Planejamento Arquitetural e Técnico — KomaDeck

## 1. Arquitetura de Software (Clean Architecture + MVVM)

A aplicação é estruturada em **Clean Architecture**, dividida em três camadas principais com fluxo de dependência apontando exclusivamente para o centro (`Presentation -> Domain <- Data`):

1. **Camada `domain` (Núcleo de Negócio):**
   * **Independência:** Módulo 100% Kotlin puro, sem dependências de bibliotecas Android, Retrofit, Room ou Compose.
   * **Componentes:**
     * `model/`: Entidades de domínio imutáveis (`MidiaItem`, `CategoriaCatalogo`).
     * `repository/`: Contrato abstrato `GuiaRepository` definindo operações reativas (`Flow`) e suspensas (`suspend fun`).
     * `usecase/`: Casos de uso encapsulando regras de negócio específicas (`ObservarCatalogoUseCase`, `SincronizarCatalogoUseCase`, `BuscarMidiasUseCase`, `ObservarFavoritosUseCase`, `AlternarFavoritoUseCase`, `ObservarDetalhesUseCase`, `SalvarMidiaCacheUseCase`).

2. **Camada `data` (Infraestrutura e Dados):**
   * **Responsabilidade:** Implementar a interface `GuiaRepository`, coordenar fontes de dados remotas e locais e realizar conversões de modelos.
   * **Componentes:**
     * `local/`: Persistência SQLite via Room (`AppDatabase`, `MidiaDao`, `MidiaEntity`).
     * `remote/`: Clientes REST via Retrofit (`JikanApiService`, `TvMazeApiService`) e modelos de transferência (`DTOs`).
     * `mapper/`: Funções de extensão responsáveis por transformar `DTO -> Entity -> Domain` e aplicar a sanitização HTML (RN02) e prefixação de IDs (RN04).
     * `repository/`: `GuiaRepositoryImpl`, orquestrando requisições paralelas e transações de banco.

3. **Camada `presentation` (Interface e Estado):**
   * **Responsabilidade:** Renderizar a interface declarativa, gerenciar navegação e transformar fluxos do domínio em estados imutáveis de tela.
   * **Componentes:**
     * `catalogo/`: `CatalogoViewModel` e estados selados (`ConteudoState`: `Loading`, `Success`, `Empty`, `Error`).
     * `detalhes/`: `DetalhesViewModel` utilizando `SavedStateHandle` para recuperação segura de argumentos de rota.
     * `navigation/`: `KomaDeckNavGraph` e componentes reutilizáveis em Jetpack Compose.

4. **Camada `di` (Injeção de Dependências):**
   * Módulos Hilt (`NetworkModule`, `DatabaseModule`, `RepositoryModule`) instalados em `SingletonComponent`.

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
│       │   └── java/com/lzinnbarbosa/komadeck/
│       │       ├── data/
│       │       │   ├── local/
│       │       │   │   ├── AppDatabase.kt
│       │       │   │   ├── MidiaDao.kt
│       │       │   │   └── MidiaEntity.kt
│       │       │   ├── mapper/
│       │       │   │   └── MidiaMappers.kt
│       │       │   ├── remote/
│       │       │   │   ├── ApiDtos.kt
│       │       │   │   ├── JikanApiService.kt
│       │       │   │   └── TvMazeApiService.kt
│       │       │   └── repository/
│       │       │       └── GuiaRepositoryImpl.kt
│       │       ├── di/
│       │       │   └── AppModules.kt
│       │       ├── domain/
│       │       │   ├── model/
│       │       │   │   └── MidiaItem.kt
│       │       │   ├── repository/
│       │       │   │   └── GuiaRepository.kt
│       │       │   └── usecase/
│       │       │       └── KomaDeckUseCases.kt
│       │       ├── presentation/
│       │       │   ├── catalogo/
│       │       │   │   └── CatalogoViewModel.kt
│       │       │   ├── detalhes/
│       │       │   │   └── DetalhesViewModel.kt
│       │       │   └── navigation/
│       │       │       └── KomaDeckNavGraph.kt
│       │       ├── KomaDeckApp.kt
│       │       └── MainActivity.kt
│       └── test/
│           └── java/com/lzinnbarbosa/komadeck/
│               ├── data/mapper/MidiaMappersTest.kt
│               ├── domain/usecase/BuscarMidiasUseCaseTest.kt
│               └── presentation/catalogo/CatalogoViewModelTest.kt
├── README.md
└── TEST_RESULTS.md
```

---

## 3. Tecnologias e Justificativas Técnicas

| Tecnologia | Versão / Biblioteca | Papel no Projeto |
| :--- | :--- | :--- |
| **Kotlin** | 2.0+ | Linguagem principal com suporte nativo a Coroutines, Flow e Null-Safety. |
| **Jetpack Compose** | BOM 2025.02.00 | Construção de UI declarativa reativa com Material Design 3. |
| **Navigation Compose** | 2.8.7 | Gerenciamento de rotas entre abas principais e tela de detalhes. |
| **Dagger Hilt** | 2.55 | Injeção de dependências em tempo de compilação, reduzindo acoplamento. |
| **Retrofit + Gson** | 2.11.0 | Cliente HTTP declarativo e desserialização JSON para as APIs REST. |
| **Room Database** | 2.6.1 (com KSP) | Abstração sobre SQLite com suporte nativo a `Flow` e `@Transaction`. |
| **Coil** | 2.7.0 | Carregamento assíncrono de imagens com cache automático em memória e disco. |
| **JUnit 4 + Coroutines Test** | 4.13.2 / 1.9.0 | Execução determinística de testes unitários com `StandardTestDispatcher`. |

---

## 4. APIs REST Integradas

1. **Jikan API v4 (`[https://api.jikan.moe/v4/](https://api.jikan.moe/v4/)`)**
   * `GET seasons/now?limit=20`: Retorna os animes da temporada atual.
   * `GET top/manga?limit=20`: Retorna o ranking de mangás populares.
   * `GET anime?q={query}&limit=10`: Pesquisa animes por título.
2. **TVMaze API (`[https://api.tvmaze.com/](https://api.tvmaze.com/)`)**
   * `GET schedule?country=US&date={YYYY-MM-DD}`: Retorna a grade diária de séries.
   * `GET search/shows?q={query}`: Pesquisa séries por título.

---

## 5. Estratégia Offline First e Sincronização

```text
[APIs REST: Jikan + TVMaze]
           │ (1) Sincronização Paralela (async/await)
           ▼
  [GuiaRepositoryImpl] ──► Sanitização HTML + Mapeamento para MidiaEntity
           │ (2) @Transaction sincronizarPreservandoFavoritos()
           ▼
   [Room Database (SSOT)] ◄── Única Fonte da Verdade
           │ (3) Flow<List<MidiaEntity>> reativo
           ▼
   [CatalogoViewModel] ──► StateFlow<KomaDeckUiState>
           │ (4) collectAsStateWithLifecycle()
           ▼
   [Jetpack Compose UI]
```

* **Leitura Reativa:** A UI nunca lê dados diretamente da rede; todas as listas são alimentadas pelos fluxos contínuos (`Flow`) do Room.
* **Sincronização Segura:** Ao sincronizar, uma transação SQLite lê previamente todos os `idUnico` com `isFavorito = true` e reaplica essa flag nos registros atualizados antes do `INSERT OR REPLACE`.
* **Fallback Offline:** Falhas de rede (`IOException` ou `HttpException`) são encapsuladas em `Result.failure`, acionando um banner informativo não bloqueante enquanto os dados locais permanecem visíveis.

---

## 6. Estratégia de Implementação e Validação

* **Desenvolvimento Incremental (SDD):** Cada camada será implementada, compilada e validada isoladamente antes de avançar para a próxima etapa, com registro granular no histórico Git.
* **Prevenção de Erros, Recomposição e Memory Leaks:**
  * Uso exclusivo de `collectAsStateWithLifecycle()` na UI para interromper coletas quando o app vai para segundo plano.
  * Uso de `key = { it.idUnico }` nos componentes `LazyColumn` e estados imutáveis (`data class`) para evitar recomposições desnecessárias no Compose.
  * Tratamento defensivo de campos nulos vindos das APIs (`score`, `synopsis`, `images`, `broadcast`).
* **Validação Automatizada e Manual:** Execução da suíte JUnit via Gradle (`./gradlew testDebugUnitTest`) seguida de teste funcional em emulador com ativação do **Modo Avião**.