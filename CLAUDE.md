# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

VehicleCareApp is an Android vehicle maintenance management app built with Kotlin and Jetpack Compose. It tracks vehicles, service history, and reminders for vehicle maintenance.

## Build Commands

```bash
# Build debug APK
./gradlew assembleDebug

# Build release APK (requires signing config)
./gradlew assembleRelease

# Run unit tests
./gradlew test

# Run connected Android tests
./gradlew connectedAndroidTest

# Clean and rebuild
./gradlew clean assembleDebug

# Run a single test class
./gradlew test --tests "id.alpine12.vehiclecare.*"
```

## Architecture

This project follows Clean Architecture with MVVM pattern:

```
app/src/main/java/id/alpine12/vehiclecare/
├── data/                    # Data layer
│   ├── local/
│   │   ├── dao/            # Room DAOs
│   │   ├── database/       # Room Database
│   │   ├── entity/         # Room entities (VehicleEntity, ServiceRecordEntity, ReminderEntity)
│   │   └── mapper/         # Entity to domain model mappers
│   └── repository/         # Repository implementations
├── domain/                  # Domain layer
│   ├── model/              # Domain models (Vehicle, ServiceRecord, Reminder, VehicleType, ServiceType)
│   └── repository/        # Repository interfaces
├── di/                     # Hilt modules (DatabaseModule, RepositoryModule)
└── ui/                     # Presentation layer
    ├── components/        # Reusable Compose components (CommonComponents, Dialogs, ErrorStates, LoadingStates, PullToRefresh)
    ├── navigation/        # Navigation setup (NavHost, Screen sealed class)
    ├── screens/           # Feature screens organized by domain
    │   ├── home/          # Dashboard, Home, SelectVehicle screens
    │   ├── history/       # Service history screen
    │   ├── reminder/      # Reminder list and add screens
    │   ├── service/       # Add service screen
    │   ├── splash/        # Splash screen
    │   ├── stats/         # Statistics screen
    │   └── vehicle/       # Add, edit, vehicle detail screens
    ├── theme/             # Material 3 theme (Color, Theme, Type)
    └── viewmodel/         # ViewModels (HomeViewModel, VehicleFormViewModel, ReminderViewModel, etc.)
```

### Key Patterns

- **Dependency Injection**: Hilt with `@HiltAndroidApp`, `@AndroidEntryPoint`, `@HiltViewModel`, `@Inject`
- **State Management**: ViewModels with `StateFlow` and `MutableStateFlow` using UI state data classes
- **Database**: Room with DAOs, entities, and Flow-based reactive queries
- **Navigation**: Jetpack Navigation Compose with sealed `Screen` class and route arguments
- **Async**: Kotlin Coroutines with `viewModelScope.launch` and Flow for reactive streams

## Navigation

The app uses bottom navigation with 4 tabs: Home, History, Reminder, and Stats. Routes are defined in `Screen.kt` as a sealed class with static routes and argument-building functions like `VehicleDetail.createRoute(vehicleId)`.

## Database Schema

Three main entities:
- **VehicleEntity**: id, name, type, brand, model, year, licensePlate, currentKm, oilIntervalKm, createdAt, updatedAt
- **ServiceRecordEntity**: id, vehicleId, serviceType, date, km, cost, notes, createdAt
- **ReminderEntity**: id, vehicleId, title, description, date, isActive, createdAt

## Features

### Service Interval Tracking
- Setiap kendaraan bisa punya interval ganti oli berbeda (default: 5000km)
- Field `oilIntervalKm` di Vehicle untuk konfigurasi per-kendaraan
- Progress bar di Home dan Detail pakai interval masing-masing kendaraan

### Service Types
Lima jenis servis utama:
- **Ganti Oli** - Tracking progress oli
- **Servis Motor** - Servis umum, bisa isi part
- **Tambah Angin** - Isi angin ban
- **Ganti Air Radiator** - coolant replacement
- **Lainnya** - Servis lain

## Architecture Flow

Data mengalir satu arah (unidirectional) dari UI ke database, dan reaktif kembali ke UI:

```
Screen (Composable)
    │  observes StateFlow
    ▼
ViewModel
    │  calls suspend fun / Flow dari repository interface
    ▼
Repository (interface) ─── Domain Layer ───
    │  diimplementasi oleh RepositoryImpl
    ▼
RepositoryImpl
    │  panggil DAO, mapping Entity ↔ Domain
    ▼
DAO (Room)
    │  query SQLite via Room
    ▼
Room Database
```

Arah sebaliknya (data → UI):
```
Room Database
    ▼
DAO → Flow<List<Entity>>
    ▼
RepositoryImpl → Flow<List<Domain>>
    │  mapping pakai Mapper
    ▼
ViewModel
    │  combine beberapa Flow jadi UiState
    ▼
Screen → recompose otomatis
```

## ViewModel Dependencies

| ViewModel | State Class | Repositories |
|---|---|---|
| HomeViewModel | HomeUiState | VehicleRepository, ServiceRecordRepository |
| VehicleDetailViewModel | VehicleDetailUiState | VehicleRepository, ServiceRecordRepository, ReminderRepository |
| VehicleFormViewModel | VehicleFormState | VehicleRepository |
| ServiceFormViewModel | ServiceFormState | ServiceRecordRepository, VehicleRepository |
| HistoryViewModel | HistoryUiState | ServiceRecordRepository, VehicleRepository |
| StatsViewModel | StatsUiState | VehicleRepository, ServiceRecordRepository |
| ReminderViewModel | ReminderUiState | ReminderRepository, VehicleRepository |
| ReminderFormViewModel | ReminderFormState | ReminderRepository, VehicleRepository |
| VehicleSelectionViewModel | VehicleSelectionUiState | VehicleRepository |

Setiap ViewModel menggunakan `@HiltViewModel` dan repository di-inject via constructor. UI state menggunakan `data class` dengan `StateFlow` + `collectAsStateWithLifecycle()`.

## Navigation Graph

```
Splash → Home (bottom nav tab)
            ├→ VehicleDetail/{vehicleId} → AddService/{vehicleId}
            │                              → AddReminder/{vehicleId}
            │                              → EditVehicle/{vehicleId}
            └→ AddVehicle
           
History (bottom nav tab) — filter & sort, no detail navigation

Reminder (bottom nav tab) → SelectVehicle → AddReminder/{vehicleId}

Stats (bottom nav tab)
```

Route definitions di `Screen.kt` — sealed class dengan method `createRoute(args)` untuk route yang butuh parameter.

## Database Schema Detail

### Table: vehicles
| Column | Type | Constraints |
|---|---|---|
| id | Long (PRIMARY KEY, autoGenerate) | NOT NULL |
| name | String | NOT NULL |
| type | String (enum: MOTOR/MOBIL) | NOT NULL |
| brand | String | NOT NULL |
| model | String | NOT NULL |
| year | String | NOT NULL |
| licensePlate | String | nullable |
| currentKm | Int | NOT NULL |
| oilIntervalKm | Int | NOT NULL, DEFAULT 5000 |
| createdAt | Long (timestamp) | NOT NULL |
| updatedAt | Long (timestamp) | NOT NULL |

### Table: service_records
| Column | Type | Constraints |
|---|---|---|
| id | Long (PRIMARY KEY, autoGenerate) | NOT NULL |
| vehicleId | Long | FK → vehicles(id) CASCADE DELETE |
| type | String (enum ServiceType) | NOT NULL |
| date | Long (timestamp) | NOT NULL |
| kilometer | Int | NOT NULL |
| productBrand | String | nullable |
| cost | Long | nullable |
| workshop | String | NOT NULL, DEFAULT "" |
| notes | String | NOT NULL, DEFAULT "" |
| createdAt | Long (timestamp) | NOT NULL |

### Table: reminders
| Column | Type | Constraints |
|---|---|---|
| id | Long (PRIMARY KEY, autoGenerate) | NOT NULL |
| vehicleId | Long | FK → vehicles(id) CASCADE DELETE |
| title | String | NOT NULL |
| description | String | nullable |
| targetDate | Long (timestamp) | nullable |
| targetKm | Long | nullable |
| isActive | Boolean | NOT NULL, DEFAULT 1 |
| isCompleted | Boolean | NOT NULL, DEFAULT 0 |
| createdAt | Long (timestamp) | NOT NULL |

### Foreign Key Relations
- `service_records.vehicleId` → `vehicles.id` ON DELETE CASCADE
- `reminders.vehicleId` → `vehicles.id` ON DELETE CASCADE
- Index on vehicleId di kedua tabel child untuk performa query

## Data Flow Pattern (Unidirectional)

1. **State** — setiap screen punya satu `data class UiState` yang merepresentasikan seluruh state screen
2. **Event** — user action → panggil method ViewModel
3. **Effect** — ViewModel update `MutableStateFlow<UiState>`
4. **Render** — Screen recompose otomatis karena observe state

Contoh flow tambah servis:
```
User tap "Simpan"
    → AddServiceScreen panggil viewModel.saveService()
    → ServiceFormViewModel call serviceRecordRepository.insertServiceRecord()
    → ServiceRecordRepositoryImpl call dao.insertServiceRecord(entity)
    → Room insert ke SQLite
    → back to screen sebelumnya via navigation
```

## Important Notes

- **Language**: UI text is in Indonesian (Bahasa Indonesia)
- **Material 3**: Using Material 3 design system with Compose
- **KSP**: Kotlin Symbol Processing for Room and Hilt code generation
- **Gradle**: Uses version catalog (`gradle/libs.versions.toml`) for dependency management
- **Min SDK**: 26, Target SDK: 37, Compile SDK: 37
- **Release signing**: Configured with keystore at `app/key/VehicleCareApp` (not committed)
