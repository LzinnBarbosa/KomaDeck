# KomaDeck — Catálogo Offline-First de Animes, Mangás e Séries

> **Status do Projeto:** Documentação SDD concluída (`v0.1-docs`). Iniciando etapa de implementação.

Aplicação Android nativa desenvolvida em **Kotlin** com **Jetpack Compose** e **Clean Architecture**, seguindo a metodologia **Spec-Driven Development (SDD)**. O **KomaDeck** centraliza lançamentos da temporada atual de animes, ranking de mangás populares e a grade diária de séries, oferecendo busca unificada e acesso offline aos favoritos.

---

## 1. Problema e Objetivo

* **Problema:** Acompanhantes de animes, mangás e séries precisam alternar entre múltiplos sites para conferir notas da comunidade, horários de exibição e sinopses, além de perderem acesso às suas listas pessoais quando estão sem conexão com a internet.
* **Objetivo:** Unificar animes da temporada, mangás e séries do dia em uma aplicação Android modular e reativa, utilizando arquitetura **Offline First** para garantir consulta contínua ao catálogo sincronizado e aos favoritos mesmo sem internet.

---

## 2. Funcionalidades Planejadas

* **Catálogo de Animes da Temporada:** Listagem de animes em exibição atual com capa, título, avaliação média e dia/horário de transmissão.
* **Catálogo de Mangás Populares:** Ranking de mangás com capa, título, avaliação e status de publicação.
* **Grade Diária de Séries:** Exibição das séries com episódios programados para a data corrente.
* **Busca Unificada:** Pesquisa paralela por título nas bases de animes e séries com validação mínima de 3 caracteres e *debounce* de 500ms.
* **Detalhes da Obra:** Tela dedicada exibindo imagem, sinopse completa sanitizada, status, nota e horário de exibição.
* **Favoritos Offline:** Persistência local em SQLite (Room) para consulta integral sem conexão.

---

## 3. Stack Tecnológica

* **Linguagem:** Kotlin 2.0+ (Android API 26+)
* **Interface e Navegação:** Jetpack Compose + Material Design 3 + Navigation Compose
* **Arquitetura:** Clean Architecture (`domain`, `data`, `presentation`, `di`) + MVVM + UDF
* **Injeção de Dependências:** Dagger Hilt
* **Concorrência e Reatividade:** Kotlin Coroutines + `StateFlow` / `Flow`
* **Rede:** Retrofit 2 + Gson + OkHttp
* **Banco de Dados Local:** Room Database (SQLite)
* **Carregamento de Imagens:** Coil Compose
* **Testes Automatizados:** JUnit 4 + `kotlinx-coroutines-test`

---

## 4. APIs REST Utilizadas

1. **Jikan API v4 (MyAnimeList)** — `https://api.jikan.moe/v4/` ([Documentação](https://docs.api.jikan.moe/))
   * *Endpoints:* `GET /seasons/now`, `GET /top/manga` e `GET /anime?q={query}`.
   * *Justificativa:* API REST pública e gratuita sem exigência de chave de autenticação (*API Key*), fornecendo metadados completos de animes e mangás.
2. **TVMaze API** — `https://api.tvmaze.com/` ([Documentação](https://www.tvmaze.com/api))
   * *Endpoints:* `GET /schedule` e `GET /search/shows?q={query}`.
   * *Justificativa:* API REST aberta para consulta da grade diária e busca de séries de TV/streaming.

---

## 5. Arquitetura do Projeto

O projeto (`com.lzinnbarbosa.komadeck`) segue a regra de dependência da **Clean Architecture** (`Presentation -> Domain <- Data`):

* **`domain/`:** Modelos de negócio (`MidiaItem`), contratos de repositório (`GuiaRepository`) e casos de uso em Kotlin puro.
* **`data/`:** Implementação do repositório (`GuiaRepositoryImpl`), entidades/DAOs do Room, serviços Retrofit, DTOs e *Mappers* com sanitização HTML.
* **`presentation/`:** Telas em Jetpack Compose, grafo de navegação (`Navigation Compose`) e `ViewModels` expondo estados imutáveis via `StateFlow`.
* **`di/`:** Módulos Hilt responsáveis pelo provisionamento de rede, banco local e repositórios.

---

## 6. Estratégia Offline First

O **Room Database** atua como *Single Source of Truth (SSOT)*. A interface observa exclusivamente os fluxos reativos (`Flow`) persistidos localmente no SQLite. A sincronização com as APIs ocorre de forma paralela em segundo plano, utilizando transações (`@Transaction`) para atualizar o catálogo sem sobrescrever os itens marcados como favoritos pelo usuário. Em modo avião ou falha de rede, o aplicativo exibe um indicador discreto de estado offline e mantém a navegação plenamente operacional.

---

## 7. Documentação Spec-Driven Development (SDD)

* [01 - Especificação de Requisitos](docs/01-ESPECIFICACAO.md)
* [02 - Planejamento Arquitetural](docs/02-PLANEJAMENTO.md)
* [03 - Decomposição de Tarefas](docs/03-DECOMPOSICAO.md)

---

## 8. Execução, Testes e Evidências

* **Como executar o projeto:** *Será documentado após a configuração do ambiente Android Studio e Gradle.*
* **Testes unitários e relatório:** *Será documentado em `TEST_RESULTS.md` na etapa de validação.*
* **Evidências visuais (Online e Offline):** *Serão adicionadas em `docs/evidencias/` após os testes funcionais.*
* **Limitações conhecidas:** *Serão registradas após a bateria de testes de integração.*