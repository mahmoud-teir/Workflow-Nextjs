<a name="phase-m13"></a>
# 📌 MOBILE PHASE M13: CI/CD & BUILD PIPELINE (DevOps Engineer)

> **Build System:** We use GitHub Actions combined with Fastlane for automating test, build, and deploy processes to the Google Play Console.

---

### Prompt M13.1: Fastlane Setup for Android

```text
You are a Mobile DevOps Engineer. Set up Fastlane for [AppName] Android.

Requirements:
- Define lanes for testing, building a debug APK, and building a release App Bundle (.aab).
- Provide the `Fastfile`.

Required Output Format: Provide complete code for `fastlane/Fastfile`:

```ruby
default_platform(:android)

platform :android do
  desc "Run unit tests"
  lane :test do
    gradle(task: "testDebugUnitTest")
  end

  desc "Build debug APK for internal testing"
  lane :build_debug do
    gradle(
      task: "assemble",
      build_type: "Debug"
    )
  end

  desc "Build and sign release AAB"
  lane :build_release do
    # Requires KEYSTORE_FILE, KEYSTORE_PASSWORD, KEY_ALIAS, KEY_PASSWORD env vars
    gradle(
      task: "bundle",
      build_type: "Release",
      properties: {
        "android.injected.signing.store.file" => ENV["KEYSTORE_FILE"],
        "android.injected.signing.store.password" => ENV["KEYSTORE_PASSWORD"],
        "android.injected.signing.key.alias" => ENV["KEY_ALIAS"],
        "android.injected.signing.key.password" => ENV["KEY_PASSWORD"],
      }
    )
  end

  desc "Submit to Google Play Internal Track"
  lane :deploy_internal do
    build_release
    upload_to_play_store(
      track: 'internal',
      json_key: ENV["PLAY_STORE_JSON_KEY"],
      package_name: "com.example.app"
    )
  end
end
```

⚠️ Common Pitfalls:
- Pitfall: Committing the `keystore.jks` file to a public repository.
- Solution: Ignore `*.jks` in `.gitignore`. Inject it during CI using base64 decoding from GitHub Secrets.
```

---

### Prompt M13.2: GitHub Actions CI/CD Pipeline

```text
You are a DevOps Engineer. Create a GitHub Actions workflow to build and test the app.

Requirements:
- Trigger on PR to main.
- Setup JDK 17.
- Run unit tests and linting.
- Provide a separate workflow for pushing to Google Play Internal track when merging to main.

Required Output Format: Provide complete code for `.github/workflows/android-pr.yml`:

```yaml
name: Android PR Check

on:
  pull_request:
    branches: [ "main" ]

jobs:
  test:
    name: Run Unit Tests
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      
      - name: set up JDK 17
        uses: actions/setup-java@v4
        with:
          java-version: '17'
          distribution: 'temurin'
          cache: gradle

      - name: Grant execute permission for gradlew
        run: chmod +x gradlew
        
      - name: Run Detekt / Lint
        run: ./gradlew lintDebug
        
      - name: Run Unit Tests
        run: ./gradlew testDebugUnitTest
```

And for release `.github/workflows/android-release.yml`:

```yaml
name: Play Store Deploy

on:
  push:
    branches: [ "main" ]

jobs:
  deploy:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      
      - name: set up JDK 17
        uses: actions/setup-java@v4
        with:
          java-version: '17'
          distribution: 'temurin'
          cache: gradle

      - name: Setup Ruby for Fastlane
        uses: ruby/setup-ruby@v1
        with:
          ruby-version: '3.2'
          bundler-cache: true

      - name: Decode Keystore
        env:
          ENCODED_KEYSTORE: ${{ secrets.KEYSTORE_BASE64 }}
        run: echo $ENCODED_KEYSTORE | base64 -di > app/keystore.jks

      - name: Deploy via Fastlane
        env:
          KEYSTORE_FILE: "app/keystore.jks"
          KEYSTORE_PASSWORD: ${{ secrets.KEYSTORE_PASSWORD }}
          KEY_ALIAS: ${{ secrets.KEY_ALIAS }}
          KEY_PASSWORD: ${{ secrets.KEY_PASSWORD }}
          PLAY_STORE_JSON_KEY: ${{ secrets.PLAY_STORE_JSON }}
        run: bundle exec fastlane deploy_internal
```
```

---

✅ **Verification Checklist:**
- [ ] PR checks complete successfully in GitHub Actions.
- [ ] Running `fastlane build_release` locally produces an `app-release.aab`.
- [ ] The keystore is excluded from version control (`.gitignore`).

---

📎 **Related Phases:**
- Prerequisites: [Phase M9: Testing](./MOBILE_PHASE_9_TESTING_QA_QA_Engineer.md)
- Proceeds to: [Phase M14: App Store Launch](./MOBILE_PHASE_14_APP_STORE_SUBMISSION_LAUNCH_All_Roles.md)
