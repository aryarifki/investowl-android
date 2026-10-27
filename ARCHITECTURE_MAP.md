# INVESTOWL ANDROID: ARCHITECTURAL CONTRACT & MAP

> **Dokumen Arsitektur & Peta Kontrak API Ground-Truth (2026)**  
> Memetakan seluruh kontrak data FastAPI (`investowl/backend/`), UI Frontend (`investowl/frontend/`), serta integrasi kepemilikan saham KSEI & IDX BAE ke dalam arsitektur Native Android (Kotlin + Jetpack Compose).

---

## 1. Arsitektur Sistem Android Multi-Module

Aplikasi Android The Investowl dirancang dengan standar **Modern Android Architecture (2026)** berbasis Gradle Multi-Module (Kotlin DSL & Version Catalog `gradle/libs.versions.toml`), Kotlin 2.0+ (K2 Compiler), dan Jetpack Compose Material 3 Expressive (Seed `#F9EDE1`).

```
investowl-android/
├── .github/
│   └── workflows/
│       └── release.yml                # CI/CD: Lint, Unit Test, Assemble, Sign & GitHub Release
├── app/                               # Entry Point: MainActivity, NavHost, AdaptiveScaffold Shell, App Links
├── core/
│   ├── model/                         # Domain models (@Immutable / @Stable) sesuai skema FastAPI
│   ├── common/                        # Porting 1:1 format.ts (fmtRp, fmtPct, signedColor, signalColor) + Tests
│   ├── designsystem/                  # Material Design 3 Expressive (Seed #F9EDE1, 8dp Spacing, Color Tokens)
│   ├── network/                       # Retrofit 2 + OkHttp 4 (Cache-Control Interceptor) + kotlinx.serialization
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

## 2. Peta Endpoint API & Kontrak Data Ground-Truth

Berikut adalah inventarisasi lengkap endpoint dari backend FastAPI (`investowl/backend/`):

### A. Autentikasi & Manajemen Pengguna (`routers/auth.py` — prefix `/api/users`)
| Method | Path | Request Body | Response Body | Android Feature Module |
|---|---|---|---|---|
| `POST` | `/api/users/sync` | `UserSyncRequest(email, name, image, provider)` | `UserRead(id, email, name, image, provider, role, is_approved, created_at)` | `:feature:auth` |
| `GET` | `/api/users/` | None | `List[UserRead]` | `:feature:admin` |
| `PATCH` | `/api/users/{user_id}/approve` | `UserApprovalRequest(is_approved)` | `UserRead` | `:feature:admin` |
| `DELETE` | `/api/users/{user_id}` | None | `{"status": "ok", "message": str}` | `:feature:admin` |
| `GET` | `/api/users/{email}/watchlist` | None | `{"watchlist": List[str]}` | `:feature:auth`, `:feature:home` |
| `PUT` | `/api/users/{email}/watchlist` | `UserWatchlistUpdate(watchlist: List[str])` | `{"status": "ok", "watchlist": List[str]}` | `:feature:auth`, `:feature:home` |
| `GET` | `/api/users/settings/auth-gate` | None | `AuthGateStatus(require_login_full_feature: bool)` | `:feature:auth` |
| `PATCH` | `/api/users/settings/auth-gate` | `AuthGateUpdateRequest(require_login_full_feature, admin_email)` | `AuthGateStatus` | `:feature:admin` |

### B. Pergerakan Harga & Saham (`routers/stocks.py` — prefix `/api/stocks`)
| Method | Path | Query Params | Response Body | Android Feature Module |
|---|---|---|---|---|
| `GET` | `/api/stocks/{ticker}/history` | `start_date`, `end_date`, `limit=500` | `PriceHistoryResponse(ticker, count, data: List[PriceRow], sort_order)` | `:feature:ticker` |

### C. Broker Flow & Whales Tracker (`routers/broker.py` — prefix `/api/broker-flow`)
| Method | Path | Query Params | Response Body | Android Feature Module |
|---|---|---|---|---|
| `GET` | `/api/broker-flow/{ticker}/latest` | None | `BrokerFlowRow(ticker, date, foreign_net_broker, local_net_broker, bandar_signal, bandar_signal_score, ...)` | `:feature:ticker` |
| `GET` | `/api/broker-flow/{ticker}/history` | `start_date`, `end_date`, `limit=250` | `BrokerFlowHistoryResponse(ticker, count, data: List[BrokerFlowRow], sort_order)` | `:feature:ticker` |
| `GET` | `/api/broker-flow/{ticker}/summary` | `days=30` | `BrokerFlowSummary(ticker, period_days, trading_days, foreign_net_broker_sum, local_net_broker_sum, accumulation_days, distribution_days, ...)` | `:feature:ticker` |
| `GET` | `/api/broker-flow/whales` | None | `List[BrokerWhaleListItem(broker_code, profile, profile_label, profile_desc, is_smart)]` | `:feature:whales` |
| `GET` | `/api/broker-flow/whales/{broker_code}` | `days=20`, `limit=20` | `BrokerWhalesDetailResponse(broker_code, broker_name, profile, is_smart, top_accumulated, top_distributed, ...)` | `:feature:whales` |

### D. Bandarmology & Detail Emiten (`routers/bandarmology.py` — prefix `/api/bandar`)
| Method | Path | Query Params | Response Payload & Key Elements | Android Feature Module |
|---|---|---|---|---|
| `GET` | `/api/bandar/universe` | None | `{"tickers": List[str]}` | `:feature:home` |
| `GET` | `/api/bandar/tickers` | None | `{"tickers": List[str]}` | `:feature:home` |
| `GET` | `/api/bandar/universe/{mode}` | Path `{mode}` (idx30, lq45, idx80, watchlist) | `{"mode": str, "tickers": List[str], "count": int}` | `:feature:home` |
| `GET` | `/api/bandar/detail/{ticker}` | `analysis_date`, `window_days=20`, `horizon=10` | `TickerDetailResponse`: `company`, `close`, `ret_5d`, `ret_10d`, `signal`, `signal_score`, `foreign_5d`, `conviction_score`, `conviction_breakdown`, `alerts`, `verdict`, `broker_summary` (termasuk badge **Governance Breadcrumb** afiliasi), `affiliated_brokers_detected`, `price_performance`, `profile_flow`, `smart_daily`, `bandar_vwap`, `retail_summary` | `:feature:ticker` |
| `GET` | `/api/bandar/broker-flow/{ticker}` | `window_days=20` | `{"ticker", "summary", "profile_flow", "profile_broker_detail"}` | `:feature:ticker` |
| `GET` | `/api/bandar/causality/{ticker}` | None | `{"granger_test", "participant_causality", "top_brokers"}` | `:feature:ticker` |
| `GET` | `/api/bandar/validation-v2/{ticker}` | `min_events=5` | `{"broker_scan", "event_study"}` (Forward Return Alpha Scan) | `:feature:ticker` |
| `GET` | `/api/bandar/stocks/{ticker}/raw-tables` | None | `{"activity": List, "flow": List}` | `:feature:ticker` |
| `POST` | `/api/bandar/clear-cache` | None | `{"status": "ok", "cleared_keys": int}` | `:feature:admin`, `:feature:ticker` |

### E. Arus Modal Asing & Makro Forensik (`routers/foreign_flow.py` & `routers/forensic.py`)
| Method | Path | Query Params | Response Body | Android Feature Module |
|---|---|---|---|---|
| `GET` | `/api/forensic/foreign-exodus` | `days=45` | `ForeignExodusRadarResponse`: `market_state` (`NORMAL`, `WARNING`, `CRASH`, `BOTTOMING`, `RECOVERY_TESTING`, `CONFIRMED_BOTTOM`), `ihsg_current`, `ihsg_change_pct`, `timeline` (date, ihsg_close, daily_flow, cumulative_flow), `culprit_brokers` | `:feature:foreign` |
| `GET` | `/api/foreign-flow/{ticker}` | `lookback_days=60` | `ForeignFlowDeepDive`: `features` (HHI, Z-Score), `timeseries`, `models` (HMM Regime Probabilities, VAR Impulse Response Function, Broker Network Nodes/Edges, Broker Heatmap Matrix) | `:feature:foreign`, `:feature:ticker` |
| `GET` | `/api/foreign-flow/sectors/summary` | None | `SectorRotationSummary`: Relative Strength vs IHSG, Foreign Net Flow per sektor | `:feature:foreign` |
| `GET` | `/api/foreign-flow/sectors/history` | None | Time-series pergerakan sektor | `:feature:foreign` |

### F. AI Signal Scanner & Backtest Lab (`routers/signal.py` — prefix `/api/signal`)
| Method | Path | Query Params | Response Body | Android Feature Module |
|---|---|---|---|---|
| `GET` | `/api/signal/daily` | `limit=100` | `DailySignalsResponse`: `latest_date`, `count`, `data: List[DailySignalItem]` (ticker, ml_label, `ml_win_prob` [Honest Null: 0..1 atau null/NA], `composite_score`, scores: technical, smart_money, sector, fundamental; features: wyckoff_phase, wyckoff_name, persistence_5d, rr_ratio, gate_notes) | `:feature:signal` |
| `GET` | `/api/signal/active` | `limit=50` | `ActiveSignalsResponse`: `count`, `data: List[ActiveSignalItem]` (entry_price, tp_price, sl_price, target_gain_pct, stop_risk_pct, risk_reward_ratio, days_held, confirmation_count, composite_score, wyckoff_phase, ml_win_prob, vcp_grade) | `:feature:signal` |
| `POST` | `/api/signal/refresh` | None | `{"status": "ok", "cleared_keys": int}` | `:feature:signal` |
| `GET` | `/api/signal/backtest/stats` | `horizon=10` | `BacktestStatsResponse`: Win-rate, Profit Factor, Expected Return per sinyal | `:feature:backtest` |
| `GET` | `/api/signal/backtest/history` | `limit=100` | `List[BacktestHistoryItem]` | `:feature:backtest` |
| `GET` | `/api/signal/outcomes/summary` | None | `OutcomesSummaryResponse`: Multi-Horizon T+1..T+20 win-rate breakdown | `:feature:backtest` |

### G. Kepemilikan Saham KSEI, IDX BAE, Konglo & Explorer (`routers/ownership.py` — prefix `/api/v1/ownership`)
| Method | Path | Query Params | Response Body | Android Feature Module |
|---|---|---|---|---|
| `GET` | `/api/v1/ownership/staging/overview` | None | `StagingOverviewResponse`: Status cakupan KSEI Balancepos & IDX BAE | `:feature:ownership` |
| `GET` | `/api/v1/ownership/staging/stock/{ticker}` | Path `{ticker}` | `StagingStockProfileResponse`: Komposisi KSEI (Lokal vs Asing, Ritel murni, breakdown), Histori SID BAE bulanan | `:feature:ownership` |
| `GET` | `/api/v1/ownership/staging/parity/{ticker}` | Path `{ticker}` | `StagingParityResponse`: Audit paritas KSEI Staging vs Baseline Creative Trader (`EXCELLENT`, `ACCEPTABLE`) | `:feature:ownership` |
| `GET` | `/api/v1/ownership/stock/{ticker}` | Path `{ticker}` | `StockOwnershipResponse`: `holders` (>1% pemegang saham), `summary` (pengendali, publik, treasury), `composition`, `profile` (market cap, outstanding), `data_quality` | `:feature:ownership`, `:feature:ticker` |
| `GET` | `/api/v1/ownership/market` | `history_limit=60` | `MarketOwnershipResponse`: Indeks COMPOSITE, Breadth, Komposisi Top 20 | `:feature:home`, `:feature:ownership` |
| `GET` | `/api/v1/ownership/groups` | None | `GroupsResponse`: Matriks 8 Grup Konglomerasi IDX, Peringkat MTD, Total Market Cap | `:feature:konglo` |
| `GET` | `/api/v1/ownership/groups/{group_id}` | Path `{group_id}` | `GroupDetailResponse`: Konstituen saham grup, kepemilikan silang, transaksi T5 | `:feature:konglo` |
| `GET` | `/api/v1/ownership/investors` | `q=nama` | `InvestorSearchResponse`: Pencarian investor publik & institusi | `:feature:ownership` |
| `GET` | `/api/v1/ownership/investors/detail` | `name=nama` | `InvestorDetailResponse`: Detail portofolio saham yang dimiliki investor | `:feature:ownership` |
| `GET` | `/api/v1/ownership/explorer` | Filter params | `ExplorerResponse`: Penjelajah kepemilikan dan pergerakan investor | `:feature:ownership` |

### H. Kontrol Sistem & Linux Log Viewer (`routers/system.py` — prefix `/api/logs`)
| Method | Path | Request Body | Response Body | Android Feature Module |
|---|---|---|---|---|
| `GET` | `/api/logs/daily` | None | `DailyLogsResponse`: Timestamp dan log 4 proses harian (`scanner`, `auditor`, `backfill`, `etl_flow`) | `:feature:admin` |
| `GET` | `/api/logs/jobs/status` | None | `JobsStatusResponse`: Status running/idle 9 job SMtracker otomatis | `:feature:admin` |
| `POST` | `/api/logs/jobs/{job_name}/run` | `JobRunRequest` | `{"status": "started", "job": str, "pid": int}` | `:feature:admin` |
| `POST` | `/api/logs/jobs/{job_name}/stop` | None | `{"status": "stopped", "job": str}` | `:feature:admin` |
| `GET` | `/api/logs/view` | `file=filename`, `lines=200` | `{"filename": str, "content": str, "lines": int}` | `:feature:admin` |
| `GET` | `/api/logs/cache/stats` | None | `CacheStatsResponse`: Statistik Redis & in-memory cache | `:feature:admin` |
| `POST` | `/api/logs/cache/purge` | `{"pattern": str}` | `{"status": "ok", "cleared": int}` | `:feature:admin` |

---

## 3. Sistem Desain Material Design 3 Expressive (Seed `#F9EDE1`)

### A. Palet Warna (MD3 Expressive Tokens)
Diadaptasi 1:1 dari `investowl/frontend/app/globals.css`:

```kotlin
// Light Theme
val LightSurface = Color(0xFFF9EDE1)
val LightOnSurface = Color(0xFF1E1A17)
val LightOnSurfaceVariant = Color(0xFF4E453F)
val LightSurfaceContainerLowest = Color(0xFFFFFFFF)
val LightSurfaceContainerLow = Color(0xFFF3E7DB)
val LightSurfaceContainer = Color(0xFFEDDFD3)
val LightSurfaceContainerHigh = Color(0xFFE7D8CB)
val LightSurfaceContainerHighest = Color(0xFFE2D1C4)
val LightPrimary = Color(0xFF8C5000)
val LightOnPrimary = Color(0xFFFFFFFF)
val LightPrimaryContainer = Color(0xFFFFDCC2)
val LightOnPrimaryContainer = Color(0xFF2D1600)
val LightOutline = Color(0xFF84746A)
val LightOutlineVariant = Color(0xFFD7C2B4)
val LightColorPositive = Color(0xFF166534) // Dark Green
val LightColorNegative = Color(0xFF991B1B) // Dark Red

// Dark Theme
val DarkSurface = Color(0xFF141210)
val DarkOnSurface = Color(0xFFEBE0D9)
val DarkOnSurfaceVariant = Color(0xFFD7C2B4)
val DarkSurfaceContainerLowest = Color(0xFF0B0A08)
val DarkSurfaceContainerLow = Color(0xFF1C1916)
val DarkSurfaceContainer = Color(0xFF211D1A)
val DarkSurfaceContainerHigh = Color(0xFF2B2622)
val DarkSurfaceContainerHighest = Color(0xFF36302A)
val DarkPrimary = Color(0xFFFFB879)
val DarkOnPrimary = Color(0xFF4B2800)
val DarkPrimaryContainer = Color(0xFF6B3B00)
val DarkOnPrimaryContainer = Color(0xFFFFDCC2)
val DarkOutline = Color(0xFF9F8D83)
val DarkOutlineVariant = Color(0xFF52443C)
val DarkColorPositive = Color(0xFF10B981) // Bright Emerald
val DarkColorNegative = Color(0xFFF43F5E) // Bright Rose
```

### B. Aturan Tipografi & Spacing
- Spacing 8dp: `4.dp`, `8.dp`, `16.dp`, `24.dp`, `32.dp`.
- Tabular figures (`tnum`) di seluruh tampilan angka keuangan.

---

## 4. Keamanan, Caching & State Autentikasi

1. **State Mesin Autentikasi**:
   - `GUEST`: Akses penuh ke Home, Search, dan Detail Emiten publik. Watchlist disimpan di Room Database lokal (`watchlist_table`).
   - `PENDING_APPROVAL`: Pengguna login via Credential Manager tetapi belum disetujui admin. Ditampilkan layar status `/pending` dengan instruksi kontak admin.
   - `ACTIVE_USER`: Akses ke seluruh tab sinyal, arus asing, kepemilikan, dan backtest. Sinkronisasi dua arah otomatis antara Room DB dan `/api/users/{email}/watchlist`.
   - `ADMIN`: Akses tambahan ke menu Admin Pengguna (`/admin`) dan Linux Log Viewer / Job Control Center (`/logs`).
2. **Proto DataStore + Google Tink (`tink-android`)**:
   - Kunci enkripsi AES256-GCM dikelola oleh Android Keystore provider via Tink.
   - Token sesi dan kredensial pengguna terenkripsi secara aman saat perangkat terkunci.
3. **OkHttp Caching Interceptor**:
   - Menghormati header `Cache-Control: public, max-age=30, s-maxage=1800, stale-while-revalidate=30` dari backend FastAPI.
   - Menggunakan disk cache sebesar 50MB di direktori cache aplikasi untuk performa instan dan offline resilience.
