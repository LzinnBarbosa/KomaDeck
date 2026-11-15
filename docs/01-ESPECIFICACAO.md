# 01. Especificação do Projeto — KomaDeck

## 1. Proposta da Aplicação
O **KomaDeck** é uma aplicação Android nativa desenvolvida em **Kotlin** com **Jetpack Compose** e **Clean Architecture**, projetada para centralizar a descoberta, o acompanhamento de lançamentos e o gerenciamento pessoal de **animes, mangás e séries**.

A aplicação opera sob a arquitetura **Offline First**, garantindo que o catálogo sincronizado e a lista pessoal de favoritos permaneçam acessíveis para consulta e leitura de detalhes mesmo na ausência total de conexão com a internet.

---

## 2. Objetivos

### 2.1. Objetivo Geral
Desenvolver uma aplicação Android modular, reativa, testável e resiliente a falhas de rede, aplicando boas práticas de engenharia de software e a metodologia **Spec-Driven Development (SDD)**.

### 2.2. Objetivos Específicos
* Consumir e normalizar dados de duas APIs REST públicas distintas (**Jikan API v4** e **TVMaze API**) em um modelo unificado de domínio.
* Implementar persistência local com **Room Database** atuando como *Single Source of Truth* (SSOT).
* Garantir sincronização não-destrutiva entre dados remotos e preferências locais do usuário.
* Construir uma interface moderna e declarativa com **Jetpack Compose**, **Material Design 3** e **Navigation Compose**.
* Validar regras de negócio, mapeamentos e gerenciamento de estado por meio de **testes unitários automatizados**.

---

## 3. Funcionalidades do Sistema

1. **Catálogo de Animes da Temporada:** Exibição dos animes atualmente em exibição, apresentando capa, título, nota média da comunidade e dia/horário de transmissão.
2. **Catálogo de Mangás Populares:** Listagem dos mangás mais bem avaliados da comunidade com capa, título, nota e status de publicação.
3. **Grade Diária de Séries:** Exibição das séries com episódios programados para a data atual.
4. **Busca Unificada Multi-Fonte:** Pesquisa simultânea por título nas bases de animes (Jikan) e séries (TVMaze), consolidando os resultados em uma única lista.
5. **Tela de Detalhes da Obra:** Navegação dedicada exibindo imagem, título completo, categoria, status, avaliação média, horário/periodicidade e sinopse completa.
6. **Gerenciamento de Favoritos Offline:** Ação rápida para favoritar ou remover obras dos favoritos, armazenando os registros integralmente no banco de dados local.
7. **Sincronização Manual e Automática:** Atualização do catálogo local ao iniciar a aplicação (quando online) e via botão explícito na barra superior, com feedback visual de progresso e status de conectividade.

---

## 4. Requisitos Funcionais (RF)

| Código | Nome | Descrição Detalhada |
| :--- | :--- | :--- |
| **RF01** | Listar Animes da Temporada | O sistema deve consultar os animes da temporada vigente e exibi-los ordenados por avaliação média, contendo título, capa, nota e horário de transmissão. |
| **RF02** | Listar Top Mangás | O sistema deve consultar o ranking de mangás populares e exibi-los com título, capa, nota e status de publicação. |
| **RF03** | Listar Séries do Dia | O sistema deve consultar a grade televisiva da data atual (`YYYY-MM-DD`) e listar as séries programadas sem duplicidade de títulos. |
| **RF04** | Realizar Busca Unificada | O sistema deve permitir que o usuário digite um termo de busca e execute consultas paralelas nas APIs de animes e séries, exibindo os resultados combinados. |
| **RF05** | Exibir Detalhes da Obra | Ao selecionar qualquer item nas listas de catálogo, busca ou favoritos, o sistema deve navegar para a tela de detalhes apresentando sinopse completa, status, nota e horário. |
| **RF06** | Favoritar e Desfavoritar Obras | O usuário deve poder alternar o estado de favorito (`isFavorito`) tanto nos cards das listas quanto na tela de detalhes, refletindo a alteração imediatamente na UI. |
| **RF07** | Consultar Favoritos Offline | O sistema deve disponibilizar uma seção exclusiva listando todas as obras favoritadas armazenadas no banco local, funcionando integralmente sem internet. |
| **RF08** | Sincronizar Catálogo | O sistema deve sincronizar os dados remotos com o banco local automaticamente na abertura do app e manualmente quando acionado pelo usuário. |
| **RF09** | Exibir Estados de Interface | Todas as telas de listagem e busca devem representar explicitamente os estados de **Carregamento (Loading)**, **Sucesso (Success)**, **Lista Vazia (Empty)** e **Erro (Error)**. |

---

## 5. Requisitos Não Funcionais (RNF)

| Código | Categoria | Descrição Detalhada |
| :--- | :--- | :--- |
| **RNF01** | Plataforma | Desenvolvido nativamente em **Kotlin 2.0+** para Android, com compatibilidade mínima para **API 26 (Android 8.0)** e target **API 35**. |
| **RNF02** | Arquitetura | Adoção estrita de **Clean Architecture** com separação nas camadas `domain`, `data` e `presentation`, respeitando a regra de dependência unidirecional. |
| **RNF03** | Injeção de Dependências | Gerenciamento de instâncias e escopos via **Dagger Hilt**. |
| **RNF04** | Concorrência | Execução de chamadas de rede e operações de banco fora da *Main Thread* utilizando **Kotlin Coroutines** e exposição reativa via **StateFlow** / **Flow**. |
| **RNF05** | Interface e Navegação | UI declarativa construída em **Jetpack Compose** (Material 3) com navegação tipada via **Navigation Compose** e otimização de recomposição (`key` estável em listas e estados imutáveis). |
| **RNF06** | Persistência Offline First | Uso do **Room Database** como *Single Source of Truth* (SSOT), garantindo disponibilidade dos dados cacheados e favoritos em modo offline. |
| **RNF07** | Resiliência e Rede | Configuração de timeouts (10s) no cliente HTTP e tratamento gracioso de exceções (`IOException`, `HttpException`) sem encerramento inesperado (*crash*). |
| **RNF08** | Prevenção de Memory Leaks | Uso de escopos atrelados ao ciclo de vida (`viewModelScope`, `collectAsStateWithLifecycle`) e injeção de `@ApplicationContext` em singletons. |
| **RNF09** | Testabilidade | Implementação de testes unitários automatizados com **JUnit 4** e **kotlinx-coroutines-test** cobrindo casos de uso, mappers e ViewModels. |

---

## 6. Regras de Negócio (RN)

* **RN01 — Validação e Debounce na Busca:** Consultas remotas de pesquisa só devem ser disparadas quando o termo digitado possuir **no mínimo 3 caracteres** (após remoção de espaços nas extremidades) e após um intervalo de **500ms** sem nova digitação (*debounce*), respeitando o limite de 3 requisições/segundo da Jikan API.
* **RN02 — Sanitização de Conteúdo HTML:** Strings de sinopse retornadas pela TVMaze API contêm marcações HTML (`<p>`, `<b>`, `<i>`, `<br>`). Todas as tags HTML devem ser removidas antes do armazenamento no banco local e da exibição na interface.
* **RN03 — Preservação de Favoritos na Sincronização:** Durante a atualização do catálogo remoto, o processo de gravação no banco local nunca deve sobrescrever o atributo `isFavorito = true` de obras previamente marcadas pelo usuário.
* **RN04 — Identificador Único Híbrido:** Como as APIs possuem sequências numéricas independentes, cada registro deve receber um prefixo identificador de origem (`anime_{id}`, `manga_{id}` ou `serie_{id}`) para evitar colisão de chaves primárias no Room.
* **RN05 — Tratamento de Notas Ausentes:** Obras recém-anunciadas ou sem avaliação registrada (`score == null`) devem exibir o indicador `"N/A"` em vez de valores zerados.
* **RN06 — Persistência Sob Demanda na Busca:** Ao abrir os detalhes ou favoritar um item retornado exclusivamente pela busca, o registro completo deve ser gravado no banco local para garantir que a tela de detalhes funcione offline.

---

## 7. Critérios de Aceitação
1. O aplicativo compila e executa sem erros em emulador/dispositivo Android (API 26+).
2. Com conexão ativa, as abas **Animes**, **Mangás** e **Séries** carregam dados reais das APIs e armazenam no banco local.
3. Ao ativar o **Modo Avião** e reiniciar o aplicativo, o usuário visualiza um aviso discreto de modo offline e continua acessando o catálogo cacheado, seus favoritos e as telas de detalhes sem falhas.
4. Todos os testes unitários executam via `./gradlew testDebugUnitTest` com 100% de aprovação.