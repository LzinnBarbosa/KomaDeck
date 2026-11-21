# KomaDeck — Catálogo Offline-First de Animes, Mangás e Séries

> **Status do Projeto:** Implementação concluída (`v1.0.0`). Arquitetura Clean Architecture + Offline First validada com 14 testes unitários automatizados e build Debug (`assembleDebug`).

Aplicação Android nativa desenvolvida em **Kotlin** com **Jetpack Compose** e **Clean Architecture**, seguindo a metodologia **Spec-Driven Development (SDD)**. O **KomaDeck** centraliza animes em alta, mangás populares e séries em destaque, oferecendo busca unificada multi-fonte, controle de progresso pessoal (`StatusBiblioteca`) e acesso offline completo ao catálogo sincronizado e aos favoritos.

---

## 1. Problema e Objetivo

* **Problema:** Acompanhantes de animes, mangás e séries precisam alternar entre diferentes plataformas para consultar notas da comunidade, horários de exibição, ficha técnica e sinopses em português, além de perderem acesso às suas listas pessoais e ao histórico de progresso quando estão sem conexão com a internet.
* **Objetivo:** Unificar a descoberta de animes, mangás e séries em uma aplicação Android modular e reativa, utilizando arquitetura **Offline First** com **Room Database** como *Single Source of Truth (SSOT)* para garantir navegação, consulta de detalhes e gerenciamento da biblioteca pessoal mesmo sem acesso à rede.

---

## 2. Funcionalidades Implementadas

* **Catálogo de Animes em Alta:** Listagem sincronizada via **AniList GraphQL API** (com fallback automático para **Jikan API v4**), exibindo capa, título, avaliação média, status traduzido e previsão do próximo episódio.
* **Catálogo de Mangás Populares:** Ranking de mangás com capa, título, nota média, status de publicação e total de capítulos.
* **Catálogo de Séries Populares:** Listagem de séries via **TVMaze API** filtradas por relevância (`weight >= 85`), com emissora, nota e dia/horário de exibição.
* **Ordenação Dinâmica nas Abas:** Filtros rápidos por **Em Alta**, **Melhores Notas** e **Em Exibição / Publicação**.
* **Busca Unificada Multi-Fonte:** Pesquisa paralela (`async/await`) nas bases de animes, mangás e séries com validação mínima de **3 caracteres**, *debounce* de **500ms** e chips de filtro por categoria (*Todos*, *Animes*, *Mangás*, *Séries*).
* **Tela de Detalhes e Ficha Técnica:** Exibição de capa, sinopse traduzida automaticamente para **PT-BR**, gêneros, estúdio/emissora, ano de lançamento e número de episódios ou capítulos.
* **Favoritos e Progresso Offline (`StatusBiblioteca`):** Persistência local em SQLite (Room) permitindo favoritar obras e classificar o progresso em **Quero Ver / Ler**, **Acompanhando** ou **Concluído**, com filtro por status na aba de Favoritos.
* **Sincronização Manual e Automática:** Atualização automática por aba com cache de sessão e botão de sincronização manual na barra superior, acompanhado de banner discreto indicando modo offline quando sem internet.

---

## 3. Tecnologias e Bibliotecas Utilizadas

* **Linguagem e SDK:** Kotlin `2.2.10` | Android Gradle Plugin `9.1.1` | `minSdk 24` / `compileSdk 36` / `targetSdk 36`
* **Interface e Navegação:** Jetpack Compose (BOM `2024.09.00`) + Material Design 3 + Navigation Compose `2.8.9`
* **Arquitetura:** Clean Architecture (`domain`, `data`, `presentation`, `di`) + MVVM + UDF (*Unidirectional Data Flow*)
* **Injeção de Dependências:** Dagger Hilt `2.59.2` + KSP `2.3.5`
* **Concorrência e Reatividade:** Kotlin Coroutines `1.10.2` (`async/await`, `Semaphore`) + `StateFlow` / `Flow`
* **Camada de Rede:** Retrofit `2.12.0` + Gson `2.11.0` + OkHttp `4.10.0` (com DNS IPv4-first e timeouts dedicados)
* **Banco de Dados Local:** Room Database `2.7.0` (SQLite com `@Transaction`)
* **Carregamento de Imagens:** Coil Compose `2.7.0` (cache em memória e disco)
* **Testes Automatizados:** JUnit `4.13.2` + `kotlinx-coroutines-test` `1.10.2` + Robolectric `4.16.1`

---

## 4. APIs REST e GraphQL Utilizadas

1. **AniList GraphQL API (Fonte Principal de Animes e Mangás)** — `https://graphql.anilist.co/` ([Documentação Oficial](https://docs.anilist.co/))
   * *Operação:* `POST /` enviando queries GraphQL tipadas (`Page(page: 1, perPage: 25)`) ordenadas por `[TRENDING_DESC, POPULARITY_DESC]` para catálogo e `[POPULARITY_DESC, SCORE_DESC]` para busca.
   * *Justificativa:* API pública sem necessidade de *API Key*, com alta disponibilidade, imagens em alta resolução (`extraLarge`) e dados estruturados de episódios, capítulos, estúdios e próximo episódio no ar.
2. **Jikan API v4 / MyAnimeList (Fallback Automático de Animes e Mangás)** — `https://api.jikan.moe/v4/` ([Documentação Oficial](https://docs.api.jikan.moe/))
   * *Endpoints:* `GET /seasons/now?limit=25` e `GET /top/manga?limit=25`.
   * *Justificativa:* Garante redundância automática caso a AniList apresente instabilidade ou limite de taxa (*rate limit*).
3. **TVMaze API (Catálogo e Busca de Séries)** — `https://api.tvmaze.com/` ([Documentação Oficial](https://www.tvmaze.com/api))
   * *Endpoints:* `GET /shows?page=0` e `GET /search/shows?q={query}`.
   * *Justificativa:* API REST aberta para consulta de séries, notas, emissoras, horários de transmissão e sinopses.
4. **Google Translate API (Tradução de Sinopses para PT-BR)** — `https://translate.googleapis.com/`
   * *Endpoint:* `GET /translate_a/single?client=gtx&sl=en&tl=pt-BR&dt=t&q={texto}`.
   * *Justificativa:* Traduz automaticamente sinopses em inglês antes da persistência no Room, evitando retraduções repetidas e garantindo leitura em português mesmo offline.

---

## 5. Descrição da Arquitetura e Separação das Camadas

O projeto (`com.lzinnbarbosa.komadeck`) segue estritamente a regra de dependência da **Clean Architecture** (`Presentation -> Domain <- Data`):

* **`domain/` (Núcleo de Negócio Puro):**
  * `model/MidiaItem.kt`: Entidade de domínio imutável `MidiaItem` e enumerações `CategoriaMidia` (`ANIME`, `MANGA`, `SERIE`) e `StatusBiblioteca` (`QUERO_VER`, `ACOMPANHANDO`, `CONCLUIDO`).
  * `repository/GuiaRepository.kt`: Interface de contrato desacoplada da infraestrutura.
  * `usecase/KomaDeckUseCases.kt`: Casos de uso especializados (`ObterMidiasUseCase`, `ObterFavoritosUseCase`, `ObterDetalhesMidiaUseCase`, `SincronizarCategoriaUseCase`, `BuscarMidiasUseCase`, `AlternarFavoritoUseCase`, `AtualizarStatusBibliotecaUseCase`, `SalvarMidiaSobDemandaUseCase`).
* **`data/` (Persistência, Rede e Mapeamento):**
  * `local/`: Banco relacional Room (`AppDatabase`, `MidiaDao`, `MidiaEntity`).
  * `remote/`: Serviços Retrofit (`AniListApiService`, `JikanApiService`, `TvMazeApiService`, `GoogleTraducaoService`) e DTOs (`AniListDtos.kt`, `ApiDtos.kt`).
  * `mapper/MidiaMappers.kt`: Conversão `DTO -> Entity -> Domain`, sanitização de HTML (`limparHtml`), prefixação única de IDs (`anime_`, `manga_`, `serie_`) e tradução determinística de status, gêneros e dias da semana.
  * `repository/GuiaRepositoryImpl.kt`: Orquestração entre rede e Room, fallback AniList $\rightarrow$ Jikan, busca paralela (`async/await`) e semáforo de tradução (`Semaphore(4)`).
* **`presentation/` (Interface Reativa e Estado):**
  * `catalogo/`: `CatalogoViewModel` e `CatalogoScreen` com gerenciamento de abas, ordenação, busca com *debounce* e filtro de favoritos.
  * `detalhes/`: `DetalhesViewModel` (via `SavedStateHandle`) e `DetalhesScreen`.
  * `components/`: Componentes visuais reutilizáveis (`MidiaComponents.kt`).
  * `theme/`: Paleta editorial clara, tipografia e tema Material Design 3 (`Color.kt`, `Theme.kt`, `Type.kt`).
  * `navigation/`: Grafo de navegação Compose (`KomaDeckNavGraph.kt`).
* **`di/` (Injeção de Dependências):**
  * `AppModules.kt`: Módulo Hilt `SingletonComponent` provendo Room, clientes OkHttp dedicados, instâncias Retrofit e repositórios/casos de uso.

---

## 6. Estratégia de Funcionamento Offline First

O aplicativo adota o **Room Database** como *Single Source of Truth (SSOT)*, dividindo o armazenamento local em dois mecanismos privados no dispositivo Android (`/data/data/com.lzinnbarbosa.komadeck/`):

1. **Banco Relacional SQLite (`databases/komadeck_database`):**
   * A interface observa exclusivamente fluxos reativos (`Flow`) provenientes da tabela `midias`.
   * Durante a sincronização online, a transação `@Transaction sincronizarCategoriaPreservandoFavoritos` atualiza o catálogo remoto sem sobrescrever as marcações de `isFavorito` nem o `statusBiblioteca` definidos pelo usuário.
   * Ao abrir os detalhes ou favoritar um item vindo da busca, `salvarSobDemandaPreservandoFavorito` grava o registro completo no SQLite para consulta posterior sem internet.
2. **Cache de Imagens em Memória e Disco (`cache/image_cache/`):**
   * A biblioteca **Coil** armazena automaticamente em memória RAM e disco interno as capas carregadas, mantendo o visual íntegro em modo avião.
3. **Comportamento Sem Internet:**
   * Falhas de rede são tratadas via `Result.failure` sem encerramento inesperado (*crash*). O aplicativo ativa um banner discreto de **Modo Offline** e mantém totalmente operacionais as abas já sincronizadas, os favoritos, o filtro de progresso e as telas de detalhes.

---

## 7. Documentação Spec-Driven Development (SDD)

* [01 - Especificação de Requisitos](docs/01-ESPECIFICACAO.md)
* [02 - Planejamento Arquitetural](docs/02-PLANEJAMENTO.md)
* [03 - Decomposição de Tarefas](docs/03-DECOMPOSICAO.md)
* [Relatório de Testes Unitários](TEST_RESULTS.md)

---

## 8. Instruções para Configuração e Execução do Projeto

### Pré-requisitos
* **Android Studio** Ladybug / Meerkat ou superior (compatível com AGP `9.1.1`)
* **JDK 17** (ou JDK 21 configurado no Gradle Daemon)
* Emulador Android ou dispositivo físico com **Android 7.0 (API 24)** ou superior

### Passos para Execução
1. Clone o repositório e acesse a pasta raiz:
   ```bash
   git clone [https://github.com/LzinnBarbosa/KomaDeck.git](https://github.com/LzinnBarbosa/KomaDeck.git)
   cd KomaDeck