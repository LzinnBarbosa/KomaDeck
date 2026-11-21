---

## Arquivo 3: `docs/03-DECOMPOSICAO.md`

## 03. Decomposição das Tarefas (Spec-Driven Development) — KomaDeck

Este documento organiza a execução do projeto nas **5 etapas da metodologia SDD**, divididas em tarefas atômicas e sequenciais com rastreamento de conclusão.

---

## Etapa 1 — Especificação
* [x] **T1.1:** Definir proposta da aplicação, objetivos gerais e específicos.
* [x] **T1.2:** Especificar funcionalidades, Requisitos Funcionais (`RF01`–`RF10`) e Não Funcionais (`RNF01`–`RNF09`).
* [x] **T1.3:** Estabelecer Regras de Negócio (`RN01`–`RN07`) e critérios de aceitação em `docs/01-ESPECIFICACAO.md`.

## Etapa 2 — Planejamento
* [x] **T2.1:** Definir arquitetura Clean Architecture (`domain`, `data`, `presentation`, `di`) e regra de dependência.
* [x] **T2.2:** Selecionar stack tecnológica (`AGP 9.1.1`, `Kotlin 2.2.10`, `Compose`, `Navigation`, `Hilt`, `Retrofit`, `Room`, `Coil`).
* [x] **T2.3:** Projetar estratégia Offline First com Room como *Single Source of Truth* e documentar em `docs/02-PLANEJAMENTO.md`.

## Etapa 3 — Decomposição e Setup Inicial
* [x] **T3.1:** Documentar a sequência lógica de tarefas e plano de versionamento em `docs/03-DECOMPOSICAO.md`.
* [x] **T3.2:** Criar versão inicial do `README.md` na raiz do repositório.
* [x] **T3.3:** Inicializar projeto Android (`com.lzinnbarbosa.komadeck`), configurar catálogo de versões (`libs.versions.toml`), plugins KSP/Hilt e recursos visuais/tipográficos.

## Etapa 4 — Implementação Incremental
* [x] **T4.1 (Camada Domain):** Implementar `MidiaItem`, `CategoriaMidia`, `StatusBiblioteca`, `GuiaRepository` e os 8 `UseCases` (`KomaDeckUseCases`).
* [x] **T4.2 (Camada Data - Local):** Implementar `MidiaEntity`, `MidiaDao` (com transações `sincronizarCategoriaPreservandoFavoritos`, `salvarSobDemandaPreservandoFavorito` e `alternarFavorito`) e `AppDatabase`.
* [x] **T4.3 (Camada Data - Remoto & DI):** Implementar DTOs, `AniListApiService` (GraphQL), `JikanApiService` (fallback), `TvMazeApiService`, `GoogleTraducaoService`, `MidiaMappers`, `GuiaRepositoryImpl` e módulo Hilt (`AppModules`).
* [x] **T4.4 (Camada Presentation):** Implementar `KomaDeckTheme`, `MidiaComponents`, `CatalogoViewModel`, `CatalogoScreen` (4 abas, ordenação, busca unificada e favoritos offline), `DetalhesViewModel`, `DetalhesScreen` (ficha técnica e `StatusBiblioteca`) e `KomaDeckNavGraph`.

## Etapa 5 — Validação e Entrega
* [x] **T5.1 (Testes Unitários):** Implementar e executar os 14 testes unitários das suítes `MidiaMappersTest`, `BuscarMidiasUseCaseTest` e `CatalogoViewModelTest` via `./gradlew testDebugUnitTest`.
* [x] **T5.2 (Validação Offline First):** Validar sincronização online, persistência de favoritos/progresso e navegação completa em Modo Avião no emulador Android.
* [x] **T5.3 (Relatório e Empacotamento):** Registrar resultados em `TEST_RESULTS.md`, atualizar documentos SDD e `README.md` final, organizar evidências em `docs/evidencias/` e gerar APK Debug (`./gradlew assembleDebug`).