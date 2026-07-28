# Altan EduWeb — Android App

Aplikasi mobile resmi **SMK Alhikmah Tanon**  
Sistem Manajemen Sekolah berbasis EduWeb

> Developed by **Padiweb Web Application Services**

---

## Info Aplikasi

| | |
|---|---|
| **Package ID** | `id.padiweb.eduweb` |
| **Nama App** | Altan EduWeb |
| **Min Android** | Android 5.0 (API 21) |
| **Target** | Android 14 (API 34) |
| **Versi** | 1.0.0 |
| **URL** | https://eduweb.smkaltan.sch.id |

---

## Cara Build — Via GitHub Actions (Tanpa Android Studio)

### Build Debug APK (untuk testing)

1. Push kode ke branch `main`
2. GitHub Actions otomatis build
3. Setelah selesai (~5 menit), buka tab **Actions** di GitHub
4. Klik workflow yang selesai → scroll ke bawah → **Artifacts**
5. Download `altan-eduweb-debug.zip` → extract → install APK di HP

### Build Release AAB (untuk Play Store)

**Langkah 1 — Buat Keystore** (sekali saja, simpan baik-baik!)

Install Java dulu, lalu jalankan di terminal:
```bash
keytool -genkey -v -keystore altan-eduweb.jks \
  -alias altan-eduweb \
  -keyalg RSA -keysize 2048 \
  -validity 10000
```
Isi semua pertanyaan, catat password-nya.

**Langkah 2 — Encode keystore ke base64**
```bash
base64 -w 0 altan-eduweb.jks
```
Copy hasilnya.

**Langkah 3 — Tambah Secrets di GitHub**

Buka repo GitHub → Settings → Secrets → Actions → New secret:

| Secret Name | Value |
|---|---|
| `KEYSTORE_BASE64` | hasil base64 keystore |
| `KEYSTORE_PASSWORD` | password keystore |
| `KEY_ALIAS` | `altan-eduweb` |
| `KEY_PASSWORD` | password key |

**Langkah 4 — Buat Release**
```bash
git tag v1.0.0
git push origin v1.0.0
```
GitHub Actions otomatis build AAB dan upload ke GitHub Releases.

**Langkah 5 — Upload ke Play Store**

1. Buka https://play.google.com/console
2. Buat aplikasi baru
3. Upload file `.aab` dari GitHub Releases
4. Isi deskripsi, screenshot, dll
5. Submit untuk review (~3-7 hari)

---

## Fitur Aplikasi

- WebView full EduWeb
- GPS untuk absensi lokasi  
- Upload foto (selfie absensi, bukti bayar)
- Notifikasi push via Firebase
- Halaman offline dengan retry
- Back button navigate history
- Support Android 5.0+

---

## Catatan Penting

- **Keystore JANGAN hilang** — kalau hilang tidak bisa update app di Play Store
- **Simpan keystore** di tempat aman (Google Drive, email, dll)
- **Jangan upload keystore** ke GitHub (sudah ada di `.gitignore`)
