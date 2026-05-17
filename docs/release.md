# Android Release

## Local Verification

Run these commands before publishing an APK to `titipin.me/android`:

```bash
./gradlew test
./gradlew assembleRelease
```

Without signing environment variables, `assembleRelease` produces an unsigned APK for beta checks:

```text
app/build/outputs/apk/release/app-release-unsigned.apk
```

## Signed Release Inputs

The release build signs the APK when all of these environment variables are present:

- `ANDROID_KEYSTORE_PATH`
- `ANDROID_KEYSTORE_PASSWORD`
- `ANDROID_KEY_ALIAS`
- `ANDROID_KEY_PASSWORD`

For GitHub Actions, store the keystore as base64 in `ANDROID_KEYSTORE_BASE64`, then add the other three values as repository secrets.

## GitHub Actions

- `Android CI` runs on branch pushes and pull requests. It runs unit tests and builds the release APK.
- `Android Release` runs manually or when pushing tags like `android-v1.0.0`. It requires signing secrets and uploads a signed APK artifact.

## Publish Checklist

- Unit tests pass.
- Release APK builds with R8/minify enabled.
- APK is signed for public download.
- App version code/name are bumped when replacing a public APK.
- Manual smoke test passes on a real Android device: login, list, detail, create, edit, close/reopen, upload image, WhatsApp bridge, profile update, logout.
- Download page includes version, build date, APK size, and beta/stable label.
