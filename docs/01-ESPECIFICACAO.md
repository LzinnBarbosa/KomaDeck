# 01. Especificação do Projeto — KomaDeck

## 1. Proposta da Aplicação
O **KomaDeck** (`com.lzinnbarbosa.komadeck`) é uma aplicação Android nativa desenvolvida em **Kotlin** com **Jetpack Compose** e **Clean Architecture**, projetada para centralizar a descoberta, o acompanhamento de lançamentos e o gerenciamento pessoal de **animes, mangás e séries**.

A aplicação opera sob a arquitetura **Offline First**, garantindo que o catálogo sincronizado, o progresso de leitura/visualização (`StatusBiblioteca`) e a lista pessoal de favoritos permaneçam acessíveis para consulta e atualização mesmo na ausência total de conexão com a internet.

---

## 2. Objetivos

### 2.1. Objetivo Geral
Desenvolver uma aplicação Android modular, reativa, testável e resiliente a falhas de rede, aplicando boas práticas de engenharia de software e a metodologia **Spec-Driven Development (SDD)**.

### 2.2. Objetivos Específicos
* Consumir e normalizar dados de múltiplas APIs públicas (**AniList GraphQL API**, **Jikan API v4** como fallback automático e **TVMaze API**) em um modelo unificado de domínio (`MidiaItem`).
* Implementar persistência local com **Room Database** atuando como *Single Source of Truth* (SSOT).
* Garantir sincronização não-destrutiva entre dados remotos, favoritos e status da biblioteca do usuário (`StatusBiblioteca`).
* Traduzir metadados e sinopses para **Português do Brasil (PT-BR)** antes da persistência local, assegurando leitura em português mesmo sem conexão.
* Construir uma interface declarativa e responsiva com **Jetpack Compose**, **Material Design 3** e **Navigation Compose**.
* Validar regras de negócio, mapeamentos e gerenciamento de estado por meio de **testes unitários automatizados**.

---

## 3. Funcionalidades do Sistema

1. **Catálogo de Animes em Alta:** Exibição dos animes em tendência/temporada com capa em alta resolução, título, nota média, status traduzido, gêneros e previsão do próximo episódio.
2. **Catálogo de Mangás Populares:** Listagem dos mangás mais populares com capa, título, nota média, status de publicação e total de capítulos.
3. **Catálogo de Séries Populares:** Exibição das séries mais relevantes da TVMaze API (`weight >= 85`) com capa, título, nota, emissora e dia/horário de exibição.
4. **Ordenação Dinâmica nas Abas:** Filtros rápidos por **Em Alta**, **Melhores Notas** e **Em Exibição / Publicação**.
5. **Busca Unificada Multi-Fonte:** Pesquisa simultânea e paralela (`async/await`) por título nas bases de animes (AniList), mangás (AniList) e séries (TVMaze), com chips de filtro por categoria (*Todos*, *Animes*, *Mangás*, *Séries*).
6. **Tela de Detalhes e Ficha Técnica:** Navegação dedicada exibindo capa, título, categoria, nota, status, horário de transmissão, sinopse traduzida para PT-BR, gêneros, total de episódios/capítulos, ano de lançamento e estúdio/emissora.
7. **Gerenciamento de Favoritos e Progresso Offline (`StatusBiblioteca`):** Ação para favoritar obras (`isFavorito`) e classificar o progresso pessoal (**Quero Ver / Ler**, **Acompanhando** ou **Concluído**), com armazenamento integral no SQLite local e filtro por status na aba Favoritos.
8. **Sincronização Automática e Manual:** Atualização do catálogo local com cache de sessão por aba, botão de sincronização manual no topo e banner discreto indicando modo offline quando sem internet.

---

## 4. Requisitos Funcionais (RF)

| Código | Nome | Descrição Detalhada |
| :--- | :--- | :--- |
| **RF01** | Listar Animes em Alta | O sistema deve consultar os animes em tendência via AniList GraphQL (com fallback automático para Jikan API v4) e exibi-los com título, capa, nota e previsão de exibição. |
| **RF02** | Listar Mangás Populares | O sistema deve consultar os mangás em destaque via AniList GraphQL (com fallback para Jikan API v4) e exibi-los com título, capa, nota e status de publicação. |
| **RF03** | Listar Séries Populares | O sistema deve consultar as séries populares na TVMaze API (`weight >= 85`, ordenadas por relevância e nota) e exibi-las sem duplicidade de IDs. |
| **RF04** | Ordenar Catálogo | O usuário deve poder ordenar e filtrar os itens das abas principais por *Em Alta*, *Melhores Notas* ou *Em Exibição / Publicação*. |
| **RF05** | Realizar Busca Unificada | O sistema deve executar consultas paralelas nas APIs de animes, mangás e séries, intercalando os resultados e permitindo filtragem por categoria. |
| **RF06** | Exibir Detalhes e Ficha Técnica | Ao selecionar qualquer obra, o sistema deve navegar para a tela de detalhes apresentando sinopse traduzida em PT-BR, ficha técnica completa, status, nota e horário. |
| **RF07** | Favoritar e Gerenciar Progresso | O usuário deve poder favoritar obras (`isFavorito`) e atualizar seu progresso (`StatusBiblioteca`: *Quero Ver / Ler*, *Acompanhando*, *Concluído*). |
| **RF08** | Consultar Favoritos Offline | O sistema deve disponibilizar uma aba exclusiva com os favoritos salvos no Room, permitindo filtrar por status de progresso sem internet. |
| **RF09** | Sincronizar Catálogo | O sistema deve sincronizar os dados remotos com o banco local na navegação entre abas (com controle de cache de sessão) e via botão manual no topo. |
| **RF10** | Exibir Estados de Interface | Todas as telas devem representar explicitamente os estados de **Carregamento (Loading)**, **Sucesso (Success)**, **Lista Vazia (Empty)** e **Erro (Error)**, além do indicador de modo offline. |

---

## 5. Requisitos Não Funcionais (RNF)

| Código | Categoria | Descrição Detalhada |
| :--- | :--- | :--- |
| **RNF01** | Plataforma | Desenvolvido nativamente em **Kotlin 2.2.10** para Android, com compatibilidade mínima para **API 24 (Android 7.0)** e target **API 36**. |
| **RNF02** | Arquitetura | Adoção estrita de **Clean Architecture** com separação nas camadas `domain`, `data`, `presentation` e `di`, respeitando a regra de dependência unidirecional (`Presentation -> Domain <- Data`). |
| **RNF03** | Injeção de Dependências | Gerenciamento de instâncias e escopos via **Dagger Hilt 2.59.2** com processamento de anotações **KSP 2.3.5**. |
| **RNF04** | Concorrência | Execução assíncrona fora da *Main Thread* com **Kotlin Coroutines 1.10.2** (`async/await`, `Semaphore`) e exposição reativa via **StateFlow** / **Flow**. |
| **RNF05** | Interface e Navegação | UI declarativa e responsiva em **Jetpack Compose** (BOM `2024.09.00`, Material 3) com **Navigation Compose 2.8.9** e otimização de recomposição (`key = { it.id }`). |
| **RNF06** | Persistência Offline First | Uso do **Room Database 2.7.0** como *Single Source of Truth* (SSOT) aliado ao cache em disco/memória do **Coil 2.7.0**. |
| **RNF07** | Resiliência e Rede | Timeouts configurados no OkHttp (15s/20s para catálogo e 6s para tradução), resolvedor DNS IPv4-first e fallback automático AniList $\rightarrow$ Jikan. |
| **RNF08** | Prevenção de Memory Leaks | Uso de escopos atrelados ao ciclo de vida (`viewModelScope`, `collectAsStateWithLifecycle`) e `@ApplicationContext` em singletons. |
| **RNF09** | Testabilidade | Implementação de 14 testes unitários automatizados com **JUnit 4.13.2** e **kotlinx-coroutines-test 1.10.2** cobrindo mappers, casos de uso e ViewModel. |

---

## 6. Regras de Negócio (RN)

* **RN01 — Validação e Debounce na Busca:** Consultas remotas de pesquisa só são disparadas quando o termo digitado possuir **no mínimo 3 caracteres** (após `trim()`) e após um intervalo de **500ms** sem nova digitação (*debounce*).
* **RN02 — Sanitização de Conteúdo HTML:** Marcações HTML (`<p>`, `<b>`, `<i>`, `<br>`) e entidades HTML (`&amp;`, `&quot;`, `&#39;`, etc.) retornadas pelas APIs devem ser removidas/decodificadas antes do armazenamento e exibição.
* **RN03 — Preservação de Favoritos e Status na Sincronização:** Durante a atualização do catálogo remoto, a transação SQLite (`sincronizarCategoriaPreservandoFavoritos`) nunca deve sobrescrever `isFavorito = true` nem o `statusBiblioteca` previamente salvos pelo usuário.
* **RN04 — Identificador Único Híbrido:** Cada registro recebe um prefixo identificador de categoria (`anime_{id}`, `manga_{id}` ou `serie_{id}`) para evitar colisão de chaves primárias na tabela `midias`.
* **RN05 — Tratamento de Notas Ausentes ou Zeradas:** Obras sem avaliação registrada (`nota == null` ou `<= 0.0`) exibem `"N/A"` na interface.
* **RN06 — Persistência Sob Demanda na Busca:** Ao abrir os detalhes ou favoritar um item retornado pela busca, o registro completo é persistido no Room (`salvarSobDemandaPreservandoFavorito`) e sua sinopse é traduzida para PT-BR, garantindo acesso offline posterior.
* **RN07 — Localização e Tradução PT-BR:** Status, gêneros e dias da semana são traduzidos deterministicamente nos *Mappers*, e sinopses em inglês são traduzidas para português com cache em memória/SQLite e fallback offline para o texto sanitizado.

---

## 7. Critérios de Aceitação
1. O aplicativo compila e executa sem erros em dispositivo/emulador Android (API 24+).
2. Com conexão ativa, as abas **Animes**, **Mangás** e **Séries** carregam dados reais das APIs, traduzem metadados/sinopses para PT-BR e armazenam no banco local.
3. Ao ativar o **Modo Avião**, o usuário visualiza o banner discreto de modo offline e continua acessando o catálogo cacheado, seus favoritos, filtros de progresso (`StatusBiblioteca`) e telas de detalhes sem falhas ou encerramentos inesperados.
4. Todos os 14 testes unitários executam via `./gradlew testDebugUnitTest` com 100% de aprovação.