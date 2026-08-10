# Carrinho de Compras

Aplicativo Android de lista de compras e carrinho, com histórico, importação de listas, comparador de preços e notificações.

## Stack tecnológica

| Camada | Tecnologia |
|---|---|
| Linguagem | Kotlin |
| UI | Jetpack Compose (Material 3), AndroidX, ViewBinding |
| Arquitetura | Clean Architecture + MVVM (Single-Activity, UDF com State/Action/Event) |
| Injeção de dependência | Koin |
| Persistência | Room |
| Rede | Retrofit + Gson + OkHttp |
| Imagens | Coil |
| Notificações | Firebase Cloud Messaging |
| Análises/Erros | Firebase Analytics / Crashlytics, AppsFlyer, Ads (AdMob) |
| Testes | JUnit, MockK, Turbine, kotlinx-coroutines-test, Robolectric |

## Estrutura do projeto

Módulo único (`app`), organizado por pacotes na pasta `app/src/main/java/com/renobile/carrinho/`:

```
├── database/          # Room: AppDatabase, DAOs e entidades (carts, products, purchase_lists)
├── di/                # Módulos Koin (App, Common, Database, Network, Repository, ViewModel)
├── features/          # Features: cart, list, history, comparator, notifications, removeads, about, more, start
│   ├── <feature>/     # ViewModel + State + Screen (+ Navigation)
│   ├── components/    # Composables específicos da feature
│   └── detail/        # Tela de detalhes da feature
├── network/           # Services Retrofit e models de resposta
├── repositories/      # Interfaces + implementações de repositório
├── service/           # Serviços (FirebaseMessaging)
├── ui/theme/          # Tema, cores e tipografia
└── util/              # Utilitários e preferências (Prefs)
```

Padrão por feature: `ViewModel` expõe um único `StateFlow` de estado (UDF), ações por lambdas em `data class <Feature>Actions` e eventos por `Channel`.

## Pré-requisitos

- Android Studio (ou JDK 21 + Android SDK 37)
- `keystore.properties` + `keystore.jks` na raiz (necessários apenas para build de release; não versionados)
- `service_account_key.json` na raiz (necessário apenas para upload via fastlane; não versionado)

## Comandos

```bash
# Build de desenvolvimento
./gradlew assembleDebug

# Build de release (assinado com keystore.properties)
./gradlew assembleRelease

# Testes unitários
./gradlew testDebugUnitTest

# Testes instrumentados (requer emulador/dispositivo)
./gradlew connectedDebugAndroidTest

# Relatório de cobertura (Kover)
./gradlew koverReport

# Verificação de cobertura (falha se a cobertura cair abaixo do mínimo)
./gradlew koverVerify

# Verificação completa (build + testes)
./gradlew build
```

## Testes

### Convenções

- Nome dos testes: `given [contexto], when [ação], then [resultado esperado]` (em inglês)
- Corrotinas: `runTest` de `kotlinx-coroutines-test`; `Dispatchers.setMain`/`resetMain` no setup/teardown
- Flows/StateFlow: `Turbine` (`flow.test { }`)
- Mocks: `mockk`; `mockkObject(Prefs)` quando o ViewModel lê preferências
- Cobertura de código: relatório Kover em `build/reports/kover`

### Estratégia por camada

| Camada | Abordagem |
|---|---|
| Utils / MaskMoney | Testes unitários puros (JVM) |
| Repositórios | Unit com DAOs/services mockados (JVM) |
| ViewModels | Unit com repositórios mockados + Turbine (JVM); `Prefs` via `mockkObject` |
| DAOs Room | Robolectric + banco em memória (`Room.inMemoryDatabaseBuilder`) |
| Módulos Koin | `checkModules()` (teste de validação de dependências) |
| UI Compose | Instrumented (`androidTest`) com `createComposeRule` para os fluxos principais |

## Publicação

O upload para a Google Play é automatizado com **fastlane**:

```bash
# Build do AAB + upload para a faixa internal (teste interno)
fastlane upload

# Upload direto para produção
fastlane upload track:production
```

Requisitos: `service_account_key.json` na raiz e a service account com permissão de versões no Play Console.

## Versionamento

- Commits seguem a especificação [Conventional Commits](https://www.conventionalcommits.org/en/v1.0.0/)
- Branches criados a partir de `main`; PRs direcionados a `main`
- `versionCode`/`versionName` em `app/build.gradle.kts`
