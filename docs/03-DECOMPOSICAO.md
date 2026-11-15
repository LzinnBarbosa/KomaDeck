# 03. Decomposição das Tarefas (Spec-Driven Development) — KomaDeck

Este documento organiza a execução do projeto nas **5 etapas da metodologia SDD**, divididas em tarefas atômicas e sequenciais.

---

## Etapa 1 — Especificação
* [x] **T1.1:** Definir proposta da aplicação, objetivos gerais e específicos.
* [x] **T1.2:** Especificar funcionalidades, Requisitos Funcionais (`RF01`–`RF09`) e Não Funcionais (`RNF01`–`RNF09`).
* [x] **T1.3:** Estabelecer Regras de Negócio (`RN01`–`RN06`) e critérios de aceitação em `docs/01-ESPECIFICACAO.md`.

## Etapa 2 — Planejamento
* [x] **T2.1:** Definir arquitetura Clean Architecture (`domain`, `data`, `presentation`, `di`) e regra de dependência.
* [x] **T2.2:** Selecionar stack tecnológica (Kotlin, Compose, Navigation, Hilt, Retrofit, Room, Coil).
* [x] **T2.3:** Projetar estratégia Offline First com Room como *Single Source of Truth* e documentar em `docs/02-PLANEJAMENTO.md`.

## Etapa 3 — Decomposição e Setup Inicial
* [x] **T3.1:** Documentar a sequência lógica de tarefas e plano de versionamento em `docs/03-DECOMPOSICAO.md`.
* [x] **T3.2:** Criar versão inicial do `README.md` na raiz do repositório.
* [ ] **T3.3:** Inicializar projeto Android no Android Studio (`com.lzinnbarbosa.komadeck`) e configurar Gradle (plugins KSP/Hilt e dependências).

## Etapa 4 — Implementação Incremental
* [ ] **T4.1 (Camada Domain):** Implementar `MidiaItem`, `CategoriaCatalogo`, `GuiaRepository` e os `UseCases`.
* [ ] **T4.2 (Camada Data - Local):** Implementar `MidiaEntity`, `MidiaDao` (com transação de preservação de favoritos) e `AppDatabase`.
* [ ] **T4.3 (Camada Data - Remoto & DI):** Implementar DTOs, `JikanApiService`, `TvMazeApiService`, `MidiaMappers`, `GuiaRepositoryImpl` e módulos Hilt (`AppModules`).
* [ ] **T4.4 (Camada Presentation):** Implementar `CatalogoViewModel`, `DetalhesViewModel`, `KomaDeckNavGraph` e telas Compose (`Animes`, `Mangás`, `Séries`, `Busca`, `Favoritos`, `Detalhes`).

## Etapa 5 — Validação e Entrega
* [ ] **T5.1 (Testes Unitários):** Implementar e executar testes para `BuscarMidiasUseCase`, `MidiaMappers` e `CatalogoViewModel`.
* [ ] **T5.2 (Validação Offline First):** Testar sincronização online e navegação completa em Modo Avião no emulador Android.
* [ ] **T5.3 (Relatório e Empacotamento):** Registrar resultados em `TEST_RESULTS.md`, salvar capturas de tela em `docs/evidencias/`, atualizar `README.md` final e gerar APK Debug (`assembleDebug`).