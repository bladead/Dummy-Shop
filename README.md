# DummyShop (KMP) — Offline-first Products App

A Kotlin Multiplatform (KMP) demo app that loads products from DummyJSON API, supports offline-first behavior, product detail deep links, favorites persistence, and basic MVI state management.

## Deep Link Testing (Android)
Manifest scheme/host:
- `dummyshop://product/{id}`

ADB:
```bash
adb shell am force-stop com.example.dummyshop
adb shell am start -W -a android.intent.action.VIEW -d "dummyshop://product/2" com.example.dummyshop