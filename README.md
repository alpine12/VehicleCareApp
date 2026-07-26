# 🚗 VehicleCareApp

Aplikasi Android untuk manajemen perawatan kendaraan. Catat riwayat servis, pantau interval ganti oli, dan atur pengingat perawatan — semua dalam satu aplikasi.

## Fitur

- **Manajemen Kendaraan** — tambah, edit, hapus kendaraan (motor/mobil)
- **Riwayat Servis** — catat setiap servis dengan 5 tipe layanan
- **Tracking Interval Oli** — progress bar ganti oli per kendaraan (interval bisa diatur sendiri)
- **Pengingat Servis** — pengingat berdasarkan tanggal atau kilometer
- **Statistik Biaya** — ringkasan biaya servis per bulan
- **Filter & Sortir** — filter riwayat per kendaraan, urutkan berdasarkan tanggal/biaya
- **Material 3** — support Light & Dark mode
- **Pull-to-Refresh** — refresh data dengan swipe

### Tipe Servis

| Tipe | Deskripsi |
|------|-----------|
| Ganti Oli | Tracking progress oli |
| Servis Motor | Servis umum, bisa isi part |
| Tambah Angin | Isi angin ban |
| Ganti Air Radiator | Coolant replacement |
| Lainnya | Servis lain yang tidak masuk kategori |

## Tech Stack

| Teknologi | Kegunaan |
|-----------|----------|
| **Kotlin** | Bahasa pemrograman utama |
| **Jetpack Compose** | UI deklaratif |
| **Material 3** | Design system (M3) |
| **Room** | Local database (SQLite) |
| **Hilt** | Dependency injection |
| **KSP** | Code generation (DAO, Hilt) |
| **Navigation Compose** | Routing & navigasi |
| **Coroutines + Flow** | Async & reactive streams |
| **StateFlow** | State management |

## Arsitektur: Clean Architecture + MVVM

```
┌─────────────────────────────────────────────────────┐
│                    UI LAYER                         │
│  ┌──────────┐  ┌────────────┐  ┌────────────────┐  │
│  │ Screens  │  │ ViewModels │  │  Components    │  │
│  │ Compose  │  │ @HiltVM    │  │  Reusable UI   │  │
│  └────┬─────┘  └─────┬──────┘  └────────────────┘  │
│       │              │                              │
│       └──────┬───────┘                              │
│           observe                                    │
│          StateFlow                                   │
└──────────────┼──────────────────────────────────────┘
               │
┌──────────────┼──────────────────────────────────────┐
│           DOMAIN LAYER                              │
│  ┌──────────────┐    ┌────────────────────────────┐ │
│  │ Domain Model │    │ Repository Interface       │ │
│  │ Vehicle, etc │◄───│ VehicleRepository, etc     │ │
│  └──────────────┘    └────────────┬───────────────┘ │
└───────────────────────────────────┼─────────────────┘
                                    │ implements
┌───────────────────────────────────┼─────────────────┐
│              DATA LAYER           │                 │
│  ┌────────────────────────────────┴─────────────┐   │
│  │       Repository Implementation              │   │
│  │  VehicleRepositoryImpl, etc                   │   │
│  └──────────────────────┬───────────────────────┘   │
│                         │                           │
│  ┌──────────────────────┼───────────────────────┐   │
│  │         Mappers (Entity ↔ Domain)            │   │
│  └──────────────────────┬───────────────────────┘   │
│                         │                           │
│  ┌──────────────────────┼───────────────────────┐   │
│  │  DAOs  │  Entities   │  Room Database        │   │
│  └────────┴─────────────┴───────────────────────┘   │
└─────────────────────────────────────────────────────┘
```

### Alur Data (Unidirectional)

User action → Screen → ViewModel → Repository → DAO → Room

Room → DAO (Flow) → Repository → ViewModel → Screen recompose

```
                            ┌──────────┐
                            │   User   │
                            │  Action  │
                            └────┬─────┘
                                 │
                    ┌────────────▼────────────┐
                    │       Screen/UI         │
                    │  (Composable function)  │
                    │                        │
                    │  UiState ──── render ──►│
                    └─────────────────────────┘
                         ▲           │
                    observe       panggil
                   StateFlow     method
                         │           ▼
                    ┌─────────────────────────┐
                    │       ViewModel         │
                    │  MutableStateFlow<Ui>   │
                    │  viewModelScope.launch  │
                    └───────────┬─────────────┘
                                │
                        suspend / Flow
                                │
                    ┌───────────▼─────────────┐
                    │  Repository (interface) │
                    └───────────┬─────────────┘
                                │
                    ┌───────────▼─────────────┐
                    │  RepositoryImpl         │
                    │  mapping Entity ↔ Model │
                    └───────────┬─────────────┘
                                │
                    ┌───────────▼─────────────┐
                    │  DAO → Room Database    │
                    └─────────────────────────┘
```

### Dependency Injection (Hilt)

```
┌───────────────────────────┐
│     DatabaseModule        │
│  @Module @InstallIn(SingletonComponent)
├───────────────────────────┤
│ VehicleCareDatabase       │
│  ├── vehicleDao()         │
│  ├── serviceRecordDao()   │
│  └── reminderDao()        │
└───────────────────────────┘

┌───────────────────────────┐
│    RepositoryModule       │
│  @Module @Binds
├───────────────────────────┤
│ VehicleRepository         │ ← VehicleRepositoryImpl
│ ServiceRecordRepository   │ ← ServiceRecordRepositoryImpl
│ ReminderRepository        │ ← ReminderRepositoryImpl
└───────────────────────────┘
```

## Database Schema

### vehicles
| Column | Type | Keterangan |
|--------|------|------------|
| id | LONG (PK) | Auto increment |
| name | TEXT | Nama kendaraan |
| type | TEXT | MOTOR / MOBIL |
| brand | TEXT | Merk |
| model | TEXT | Model |
| year | TEXT | Tahun |
| licensePlate | TEXT | Plat nomor (opsional) |
| currentKm | INTEGER | KM saat ini |
| oilIntervalKm | INTEGER | Interval ganti oli (default 5000) |
| createdAt | LONG | Timestamp |
| updatedAt | LONG | Timestamp |

### service_records
| Column | Type | Keterangan |
|--------|------|------------|
| id | LONG (PK) | Auto increment |
| vehicleId | LONG (FK) | → vehicles(id) CASCADE |
| type | TEXT | ServiceType enum |
| date | LONG | Timestamp |
| kilometer | INTEGER | KM saat servis |
| productBrand | TEXT | Merk oli/part (opsional) |
| cost | LONG | Biaya (opsional) |
| workshop | TEXT | Nama bengkel |
| notes | TEXT | Catatan |
| createdAt | LONG | Timestamp |

### reminders
| Column | Type | Keterangan |
|--------|------|------------|
| id | LONG (PK) | Auto increment |
| vehicleId | LONG (FK) | → vehicles(id) CASCADE |
| title | TEXT | Judul pengingat |
| description | TEXT | Deskripsi (opsional) |
| targetDate | LONG | Target tanggal (opsional) |
| targetKm | LONG | Target KM (opsional) |
| isActive | BOOLEAN | Default true |
| isCompleted | BOOLEAN | Default false |
| createdAt | LONG | Timestamp |

## Navigation

```
Bottom Navigation (4 tabs):
┌──────┬─────────┬──────────┬───────┐
│Home  │ History │ Reminder │ Stats │
└──┬───┴─────────┴─────┬────┴───────┘
   │                   │
   ├── VehicleDetail   └── SelectVehicle
   │     ├── AddService      └── AddReminder
   │     ├── AddReminder
   │     └── EditVehicle
   └── AddVehicle
```

Routes didefinisikan sebagai `sealed class Screen` di `ui/navigation/Screen.kt`.

## ViewModel — Repository Mapping

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

## Struktur Project

```
app/src/main/java/id/alpine12/vehiclecare/
├── VehicleCareApp.kt              # @HiltAndroidApp
├── MainActivity.kt                # Single Activity
│
├── data/
│   ├── local/
│   │   ├── dao/                   # Room DAOs (VehicleDao, ServiceRecordDao, ReminderDao)
│   │   ├── database/              # Room Database + Migration
│   │   ├── entity/                # Room Entities
│   │   └── mapper/                # Entity ↔ Domain mapper
│   └── repository/                # Repository implementations
│
├── domain/
│   ├── model/                     # Domain models + enums
│   └── repository/                # Repository interfaces
│
├── di/                            # Hilt modules (DatabaseModule, RepositoryModule)
│
└── ui/
    ├── components/                # Reusable composables
    ├── navigation/                # NavHost + Screen sealed class
    ├── screens/                   # Feature screens
    │   ├── home/                  # Dashboard, vehicle list
    │   ├── vehicle/               # Add/Edit/Detail vehicle
    │   ├── service/               # Add service
    │   ├── history/               # Service history timeline
    │   ├── reminder/              # Reminder list & form
    │   ├── stats/                 # Statistics
    │   └── splash/                # Splash screen
    ├── theme/                     # Material 3 theme
    └── viewmodel/                 # ViewModels + UiStates
```

## Setup & Build

### Prasyarat
- Android Studio Hedgehog (2023.1.1) atau lebih baru
- JDK 17
- Android SDK 37

### Build Debug APK
```bash
./gradlew assembleDebug
```

### Build Release APK
```bash
./gradlew assembleRelease
```
> **Catatan:** Release signing butuh keystore di `app/key/VehicleCareApp` (tidak termasuk dalam repo).

### Run Tests
```bash
./gradlew test
./gradlew connectedAndroidTest
```
