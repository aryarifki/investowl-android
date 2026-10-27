# 🦉 The Investowl — Native Android Application

[![Build & Release Android App](https://github.com/aryarifki/investowl-android/actions/workflows/release.yml/badge.svg)](https://github.com/aryarifki/investowl-android/actions/workflows/release.yml)
[![GitHub Release](https://img.shields.io/github/v/release/aryarifki/investowl-android?color=8C5000)](https://github.com/aryarifki/investowl-android/releases)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.0.21%20(K2)-7F52FF.svg)](https://kotlinlang.org)
[![Compose](https://img.shields.io/badge/Jetpack%20Compose-M3%20Expressive-4285F4.svg)](https://developer.android.com/jetpack/compose)

Aplikasi **Native Android (Kotlin + Jetpack Compose)** kelas perusahaan untuk ekosistem platform **The Investowl** ([web](https://theinvestowl.cugarete.me)). Mengintegrasikan analitik Bandarmology, Foreign Flow Deep Dive, AI Wyckoff & VCP Signal Scanner, Strategy Alpha Backtest Lab, serta pemantauan kepemilikan saham resmi publik **KSEI (Balancepos & Statis Efek)** dan **IDX BAE**.

---

## 🏛️ Arsitektur Multi-Module (Standard 2026)

Proyek ini menggunakan arsitektur **Multi-Module Gradle (Kotlin DSL + Version Catalog `gradle/libs.versions.toml`)** dengan isolasi domain yang ketat:

```text
investowl-android/
├── .github/
│   └── workflows/
│       └── release.yml                # CI/CD: Lint, Unit Test, Assemble, Sign & GitHub Release
├── app/                               # Entry Point: MainActivity, NavHost, AdaptiveScaffold Shell, App Links
├── core/
│   ├── model/                         # Domain models (@Immutable / @Stable) sesuai skema FastAPI
│   ├── common/                        # Porting 1:1 format.ts (fmtRp, fmtPct, signedColor, signalColor) + Tests
│   ├── designsystem/                  # Material Design 3 Expressive (Seed #F9EDE1, 8dp Spacing, Color Tokens)
│   ├── network/                       # Retrofit 2 + OkHttp 4 (Cache-Control 30s/1800s interceptor) + kotlinx.serialization
│   └── database/                      # Room DB (Watchlist offline) + Proto DataStore (Google Tink Encrypted)
└── feature/
    ├── home/                          # Quick Market Monitor, IHSG Summary, Global Ticker Search
    ├── auth/                          # Google Credential Manager, Auth-Gate State Machine (/pending), Watchlist Sync
    ├── ticker/                        # Detail Emiten (/[ticker]) — 6 Tab Analitik + Governance Breadcrumb Engine
    ├── ownership/                     # KSEI Balancepos & Statis, IDX BAE, Major Holders, Composition & UBO (/api/ownership/...)
    ├── konglo/                        # Matriks 8 Grup Konglomerasi IDX & Interactive Network Graph (/konglo)
    ├── foreign/                       # Makro Arus Asing (/foreign) — Exodus Radar 6-Fase, RRG, HMM, VAR IRF
    ├── whales/                        # Broker Whales Tracker (/whales) — Pencarian & Portofolio Akumulasi Broker
    ├── signal/                        # AI Signal Scanner (/signal) — Wyckoff/VCP, AI Gatekeeper P(WIN), Half-Kelly
    ├── backtest/                      # Strategy Backtest & Win-Rate Lab (/backtest) — Multi-Horizon T+1..T+20
    └── admin/                         # Admin User Approval (/admin) & Linux Log Emulator + 9-Job Control (/logs)
```

---

## 🎨 Sistem Desain Material Design 3 Expressive (Seed `#F9EDE1`)

Sistem desain diimplementasikan murni mengikuti spesifikasi Material Design 3 Expressive dan selaras 1:1 dengan tema web InvestOwl:
- **Seed Pastel**: `#F9EDE1` (Anti AI-Slop, tanpa border neon atau pelangi).
- **Light Theme**: Surface `#F9EDE1`, Primary `#8C5000`, OnPrimary `#FFFFFF`, ColorPositive `#166534`, ColorNegative `#991B1B`.
- **Dark Theme**: Surface `#141210`, Primary `#FFB879`, OnPrimary `#4B2800`, ColorPositive `#10B981`, ColorNegative `#F43F5E`.
- **Adaptive Layout**: Menggunakan `currentWindowAdaptiveInfo()` dan `NavigationSuiteScaffold` untuk adaptasi otomatis antara NavigationBar (Smartphone), NavigationRail (Foldables/Tablets), dan List-Detail Pane.
- **Bi-Directional 2D Data Grid**: Komponen tabel dengan Sticky Header dan Pinned Left Column pada tabel transaksi broker, kepemilikan saham, dan backtest trades.

---

## 🚀 Fitur Unggulan

1. **Governance Breadcrumb Engine**:
   Mendeteksi afiliasi tersembunyi antara broker transaksi dan pengendali / orang dalam emiten (misal: keterkaitan grup konglomerasi, nominee, broker internal).
2. **KSEI & IDX BAE Official Ownership**:
   Menyajikan data resmi kepemilikan KSEI (Balancepos & Statis: pergerakan lokal vs asing, institusi vs ritel murni) dan laporan bulanan registrasi SID IDX BAE.
3. **Foreign Exodus Radar Market State Machine v3.1**:
   Mesin 6-fase makro bursa: `NORMAL`, `WARNING`, `CRASH`, `BOTTOMING`, `RECOVERY_TESTING`, dan `CONFIRMED_BOTTOM` berbasis Value-Weighted VWAP.
4. **AI Signal Scanner & Win Rate Gatekeeper**:
   Deteksi fase akumulasi Wyckoff & Volatility Contraction Pattern (VCP) dengan honest null handling pada probabilitas AI (`P(WIN)`), simulasi Dynamic Half-Kelly, dan proteksi MAE/MFE.
5. **Keamanan Google Tink & Omnichannel App Links**:
   Penyimpanan sesi aman dengan enkripsi AES256-GCM via Google Tink (`tink-android`) dan integrasi Android App Links untuk domain `investowl.cugarete.me` agar tautan sinyal dari Telegram langsung membuka halaman native.

---

## 🛠️ Kompilasi, Pengujian & Rilis

### Menjalankan Pengujian Unit
```bash
./gradlew testDebugUnitTest
```

### Membangun Release APK & Bundle (AAB)
```bash
./gradlew assembleRelease bundleRelease
```

Artefak hasil build:
- APK: `app/build/outputs/apk/release/app-release.apk`
- AAB: `app/build/outputs/bundle/release/app-release.aab`
