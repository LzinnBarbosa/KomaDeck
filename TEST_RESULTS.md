# 📋 Relatório de Testes e Qualidade (QA) — KomaDeck

Este documento registra os testes funcionais, de interface e de resiliência de rede executados no aplicativo **KomaDeck** (Android Nativo — Jetpack Compose, Material 3, Room, Hilt e Coil).

---

## 📊 Resumo da Execução

| Categoria de Teste | Total de Casos | Aprovados | Status |
| :--- | :---: | :---: | :---: |
| Catálogo Principal e UI | 3 | 3 | ✅ Concluído |
| Filtros de Catálogo (`Em Alta`, `Melhores Notas`, `Em Exibição`) | 3 | 3 | ✅ Concluído |
| Resiliência de Rede e Modo Offline | 3 | 3 | ✅ Concluído |
| Busca e Tela de Detalhes | 2 | 2 | ✅ Concluído |
| Favoritos Offline e Gestão de Status | 4 | 4 | ✅ Concluído |
| **Total Geral** | **15** | **15** | **100% Validado** |

---

## 🛠️ Ambiente de Testes

* **Plataforma:** Android (Emulador Android API moderna + Dispositivo Físico)
* **Arquitetura Validada:** Jetpack Compose, Navigation Compose, Room (persistência local), OkHttp3 e Coil
* **Temas Testados:** Tema Claro (Light Mode) e Tema Escuro (Dark Mode)
* **Condições de Rede:** Wi-Fi ativo, Rede sem acesso à internet (cache vazio) e Modo Avião (cache populado)

---

## 🧪 Detalhamento dos Casos de Teste

### 1. Catálogo Principal (Animes, Mangás e Séries)

| ID | Cenário | Passos Executados | Resultado Esperado | Status |
| :--- | :--- | :--- | :--- | :---: |
| **CT-01** | Listagem de Animes da Temporada | Abrir o app na aba **Animes**. | Carregar lista de animes com capa, selo de categoria, nota, estúdio, gêneros em português, status e horário de exibição. | ✅ Passou |
| **CT-02** | Listagem de Top Mangás | Navegar para a aba **Mangás**. | Carregar lista de mangás (*One Piece*, *Overgeared*, *Nano Machine*, etc.) com ano de lançamento e status (*Em Publicação* / *Finalizado*). | ✅ Passou |
| **CT-03** | Listagem de Séries em Destaque | Navegar para a aba **Séries**. | Carregar lista de séries (*Rick and Morty*, *Fargo*, *Person of Interest*, etc.) com emissora, gêneros e dia/horário. | ✅ Passou |

---

### 2. Filtros Superiores do Catálogo

| ID | Cenário | Passos Executados | Resultado Esperado | Status |
| :--- | :--- | :--- | :--- | :---: |
| **CT-04** | Filtros na aba **Animes** | Alternar entre `Em Alta`, `Melhores Notas` e `Em Exibição`. | `Melhores Notas` ordena de forma decrescente; `Em Exibição` oculta títulos finalizados (*Naruto: Shippuden*) e mantém apenas obras ativas. | ✅ Passou |
| **CT-05** | Filtros na aba **Mangás** | Alternar entre `Em Alta`, `Melhores Notas` e `Em Exibição`. | `Melhores Notas` ordena por avaliação; `Em Exibição` exibe exclusivamente mangás com status *Em Publicação*. | ✅ Passou |
| **CT-06** | Filtros na aba **Séries** | Alternar entre `Em Alta`, `Melhores Notas` e `Em Exibição`. | `Em Exibição` remove séries finalizadas ou sem data (*Fargo*, *Banshee*) e exibe apenas séries ativas (*Rick and Morty*, *South Park*, *True Detective*). | ✅ Passou |

---

### 3. Resiliência de Rede e Modo Offline

| ID | Cenário | Passos Executados | Resultado Esperado | Status |
| :--- | :--- | :--- | :--- | :---: |
| **CT-07** | Falha de rede com cache vazio | Iniciar o app sem acesso à internet antes da primeira sincronização. | Exibir mensagem amigável (*"Não foi possível carregar os dados. Verifique sua conexão e tente novamente."*) e botão **Tentar novamente**. | ✅ Passou |
| **CT-08** | Modo Offline (Modo Avião) com cache | Carregar o catálogo online, ativar o Modo Avião e navegar no app. | Exibir banner *"Modo Offline — exibindo dados salvos localmente"* e carregar normalmente os títulos armazenados no Room. | ✅ Passou |
| **CT-09** | Reconexão de rede | Reativar Wi-Fi / Dados móveis após queda de conexão. | Restabelecer comunicação com a API e atualizar o catálogo sem travamentos. | ✅ Passou |

---

### 4. Busca, Detalhes e Progresso

| ID | Cenário | Passos Executados | Resultado Esperado | Status |
| :--- | :--- | :--- | :--- | :---: |
| **CT-10** | Busca por palavra-chave | Digitar termo com 3 ou mais caracteres (ex.: `"hunter"`). | Exibir resultados filtrados com miniatura, status e contagem de episódios/capítulos, além de permitir limpar a busca. | ✅ Passou |
| **CT-11** | Tela de Detalhes e Progresso | Abrir detalhes de um título e incrementar episódios assistidos (`0 / 148` → `1 / 148`). | Atualizar contador e barra de progresso visualmente, mantendo os dados salvos localmente. | ✅ Passou |

---

### 5. Favoritos Offline e Gestão de Status

| ID | Cenário | Passos Executados | Resultado Esperado | Status |
| :--- | :--- | :--- | :--- | :---: |
| **CT-12** | Favoritar pelos cards do catálogo | Tocar no ícone de coração em *The Apothecary Diaries Season 3* (Anime), *My Blasted Reincarnated Life* (Mangá) e *Rick and Morty* (Série). | Ícone muda para vermelho preenchido e todos os itens aparecem consolidados na aba **Favoritos Offline** com status padrão *Quero Ver / Ler*. | ✅ Passou |
| **CT-13** | Remoção direta na aba Favoritos | Tocar no coração preenchido dentro da tela **Favoritos Offline**. | O item é removido imediatamente da lista local. | ✅ Passou |
| **CT-14** | Alteração de status de acompanhamento | Alterar status dos favoritos para *Acompanhando* (*Rick and Morty*, *The Apothecary Diaries*) e *Concluído* (*My Blasted Reincarnated Life*). | Selo de status é atualizado nos cards da aba **Favoritos Offline** e persistido no banco local. | ✅ Passou |
| **CT-15** | Filtros de status em Favoritos | Alternar entre os chips `Todos`, `Quero Ver / Ler`, `Acompanhando` e `Concluído`. | Cada chip filtra corretamente apenas os títulos marcados com o respectivo status (*Absolute Regression* em *Quero Ver / Ler*; *Rick and Morty* e *The Apothecary Diaries* em *Acompanhando*; *My Blasted Reincarnated Life* em *Concluído*). | ✅ Passou |

---

## 📝 Observações e Melhorias Futuras (Backlog de Polimento)

Durante os testes, foram mapeadas oportunidades de refinamento de UX/UI para versões futuras:

1. **Banner Offline com Cache Vazio:** Condicionar a exibição do aviso *"Modo Offline — exibindo dados salvos localmente"* apenas quando houver registros no banco local (Room), evitando exibição simultânea com a tela de erro no primeiro acesso sem internet.
2. **Placeholder de Imagem (Coil):** Configurar imagem padrão de fallback/erro para evitar quadros cinzas quando uma capa retornar nula ou falhar no download.
3. **Ordenação do Filtro `Melhores Notas`:** Unificar o critério de ordenação para que títulos de maior nota exibidos em `Em Alta` (como *One Piece* `9,1` e *Black Clover Season 2* `8,8`) também liderem `Melhores Notas`, além de diferenciar a consulta de `Melhores Notas` e `Em Alta` na aba de Séries.
4. **Terminologia e Espaçamento de Filtros:** Alterar o texto do chip `Em Exibição` para `Em Publicação` na aba **Mangás** e adicionar `contentPadding` horizontal na `LazyRow` de filtros da aba **Favoritos Offline** para evitar que o último chip (*Concluído*) fique rente à borda direita.
5. **Confirmação ao Desfavoritar:** Adicionar uma `Snackbar` com opção *"Desfazer"* ao remover um item diretamente na tela **Favoritos Offline**, prevenindo perda acidental de progresso registrado.