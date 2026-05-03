# Phase 14: CI/CD Pipeline

## Overview
Set up automated build, test, and deployment pipelines using EAS Build, GitHub Actions, and automated store submissions.

## Pipeline Architecture

### 1. EAS Build Configuration
```json
// eas.json
{
  "cli": {
    "version": ">= 5.0.0",
    "promptToConfigurePushNotifications": false
  },
  "build": {
    "development": {
      "developmentClient": true,
      "distribution": "internal",
      "env": {
        "APP_ENV": "development"
      },
      "ios": {
        "simulator": true,
        "resourceClass": "m-medium"
      },
      "android": {
        "buildType": "apk",
        "gradleCommand": ":app:assembleDebug"
      }
    },
    "preview": {
      "distribution": "internal",
      "channel": "preview",
      "env": {
        "APP_ENV": "staging"
      },
      "ios": {
        "resourceClass": "m-medium"
      },
      "android": {
        "buildType": "apk"
      }
    },
    "production": {
      "distribution": "store",
      "channel": "production",
      "env": {
        "APP_ENV": "production"
      },
      "ios": {
        "resourceClass": "m-large",
        "autoIncrement": true
      },
      "android": {
        "buildType": "app-bundle",
        "autoIncrement": true
      }
    }
  },
  "submit": {
    "production": {
      "ios": {
        "appleId": "your-apple-id@example.com",
        "ascAppId": "${APPLE_APP_ID}",
        "appleTeamId": "${APPLE_TEAM_ID}"
      },
      "android": {
        "serviceAccountKeyPath": "./google-service-account.json",
        "track": "internal",
        "releaseNotes": "Automated release from CI/CD"
      }
    }
  }
}
```

### 2. GitHub Actions Workflow
```yaml
# .github/workflows/ci-cd.yml
name: React Native CI/CD

on:
  push:
    branches: [main, develop]
  pull_request:
    branches: [main]

env:
  EXPO_TOKEN: ${{ secrets.EXPO_TOKEN }}
  APPLE_APP_ID: ${{ secrets.APPLE_APP_ID }}
  APPLE_TEAM_ID: ${{ secrets.APPLE_TEAM_ID }}

jobs:
  lint-and-test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      
      - name: Setup Node.js
        uses: actions/setup-node@v3
        with:
          node-version: '18'
          cache: 'npm'
      
      - name: Install dependencies
        run: npm ci
      
      - name: Run linter
        run: npm run lint
      
      - name: Run TypeScript check
        run: npm run type-check
      
      - name: Run unit tests
        run: npm run test -- --coverage
      
      - name: Upload coverage
        uses: codecov/codecov-action@v3
        with:
          file: ./coverage/lcov.info

  build-preview:
    needs: lint-and-test
    if: github.event_name == 'push' && github.ref == 'refs/heads/develop'
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      
      - name: Setup Node.js
        uses: actions/setup-node@v3
        with:
          node-version: '18'
          cache: 'npm'
      
      - name: Install dependencies
        run: npm ci
      
      - name: Setup Expo
        uses: expo/expo-github-action@v8
        with:
          expo-version: latest
          eas-version: latest
          token: ${{ secrets.EXPO_TOKEN }}
      
      - name: Build preview app
        run: eas build --platform all --profile preview --non-interactive
      
      - name: Notify Slack
        uses: slackapi/slack-github-action@v1
        with:
          payload: |
            {
              "text": "Preview build completed successfully! 🎉"
            }
        env:
          SLACK_WEBHOOK_URL: ${{ secrets.SLACK_WEBHOOK_URL }}

  build-production:
    needs: lint-and-test
    if: github.event_name == 'push' && github.ref == 'refs/heads/main'
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      
      - name: Setup Node.js
        uses: actions/setup-node@v3
        with:
          node-version: '18'
          cache: 'npm'
      
      - name: Install dependencies
        run: npm ci
      
      - name: Setup Expo
        uses: expo/expo-github-action@v8
        with:
          expo-version: latest
          eas-version: latest
          token: ${{ secrets.EXPO_TOKEN }}
      
      - name: Configure Google Service Account
        run: echo '${{ secrets.GOOGLE_SERVICE_ACCOUNT }}' | base64 -d > google-service-account.json
      
      - name: Build production app
        run: eas build --platform all --profile production --non-interactive
      
      - name: Submit to stores
        run: eas submit --platform all --profile production --non-interactive
      
      - name: Create GitHub Release
        uses: softprops/action-gh-release@v1
        with:
          tag_name: v${{ github.run_number }}
          name: Release v${{ github.run_number }}
          body: |
            Automated release from CI/CD pipeline
            
            ## Changes
            - See commit history for details
          draft: false
          prerelease: false
      
      - name: Notify Slack
        uses: slackapi/slack-github-action@v1
        with:
          payload: |
            {
              "text": "Production build submitted to stores! 🚀"
            }
        env:
          SLACK_WEBHOOK_URL: ${{ secrets.SLACK_WEBHOOK_URL }}

  update-ota:
    needs: build-production
    if: github.event_name == 'push' && github.ref == 'refs/heads/main'
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      
      - name: Setup Node.js
        uses: actions/setup-node@v3
        with:
          node-version: '18'
          cache: 'npm'
      
      - name: Install dependencies
        run: npm ci
      
      - name: Setup Expo
        uses: expo/expo-github-action@v8
        with:
          expo-version: latest
          token: ${{ secrets.EXPO_TOKEN }}
      
      - name: Publish OTA update
        run: eas update --branch production --message "Release v${{ github.run_number }}" --non-interactive
```

### 3. Environment Secrets Setup
```bash
# Required GitHub Secrets:
# EXPO_TOKEN - Expo authentication token
# APPLE_APP_ID - Apple App Store Connect App ID
# APPLE_TEAM_ID - Apple Developer Team ID
# GOOGLE_SERVICE_ACCOUNT - Base64 encoded Google service account JSON
# SLACK_WEBHOOK_URL - Slack incoming webhook URL

# Generate Expo token:
npx expo login
# Then create token at: https://expo.dev/settings/access-tokens
```

### 4. Fastlane Integration (Optional)
```ruby
# fastlane/Fastfile
default_platform :react_native

platform :ios do
  desc "Build and submit to TestFlight"
  lane :beta do
    increment_build_number
    build_app(
      workspace: "ios/YourApp.xcworkspace",
      scheme: "YourApp",
      export_method: "app-store"
    )
    upload_to_testflight
  end
  
  lane :production do
    build_app(
      workspace: "ios/YourApp.xcworkspace",
      scheme: "YourApp",
      export_method: "app-store"
    )
    upload_to_app_store
  end
end

platform :android do
  desc "Build and submit to Google Play Internal Testing"
  lane :beta do
    increment_version_code
    build_gradle(task: "bundle")
    upload_to_play_store(
      track: "internal",
      skip_upload_apk: false
    )
  end
  
  lane :production do
    build_gradle(task: "bundle")
    upload_to_play_store(
      track: "production"
    )
  end
end
```

### 5. Build Status Dashboard
```typescript
// scripts/build-status.ts
import { Octokit } from '@octokit/rest';

interface BuildStatus {
  workflow: string;
  status: 'success' | 'failure' | 'pending';
  branch: string;
  commit: string;
  timestamp: Date;
  url: string;
}

class BuildMonitor {
  private octokit: Octokit;
  
  constructor(token: string) {
    this.octokit = new Octokit({ auth: token });
  }

  async getLatestBuilds(owner: string, repo: string): Promise<BuildStatus[]> {
    const workflows = await this.octokit.actions.listRepoWorkflows({
      owner,
      repo
    });

    const builds: BuildStatus[] = [];

    for (const workflow of workflows.data.workflows) {
      const runs = await this.octokit.actions.listWorkflowRuns({
        owner,
        repo,
        workflow_id: workflow.id,
        per_page: 1
      });

      if (runs.data.workflow_runs.length > 0) {
        const run = runs.data.workflow_runs[0];
        builds.push({
          workflow: workflow.name,
          status: run.conclusion as any || run.status,
          branch: run.head_branch || 'unknown',
          commit: run.head_sha.substring(0, 7),
          timestamp: new Date(run.updated_at),
          url: run.html_url
        });
      }
    }

    return builds;
  }

  async triggerBuild(owner: string, repo: string, workflowId: number, branch: string): Promise<void> {
    await this.octokit.actions.createWorkflowDispatch({
      owner,
      repo,
      workflow_id: workflowId,
      ref: branch
    });
  }
}

export const buildMonitor = new BuildMonitor(process.env.GITHUB_TOKEN!);
```

## Deliverables Checklist

- [ ] EAS Build configuration (eas.json)
- [ ] GitHub Actions workflow files
- [ ] Environment secrets configured
- [ ] Build notifications set up (Slack/Email)
- [ ] Automated testing in CI
- [ ] Code coverage reporting
- [ ] Automated store submission
- [ ] OTA update pipeline
- [ ] Build status dashboard
- [ ] Rollback procedures documented

## Best Practices

1. **Keep builds fast** - Use caching and parallel jobs
2. **Test before building** - Run linting and tests first
3. **Secure secrets** - Never commit sensitive data
4. **Notify on failure** - Set up immediate alerts
5. **Version automatically** - Auto-increment build numbers
6. **Maintain changelog** - Generate release notes automatically
7. **Support rollbacks** - Keep previous versions available
8. **Monitor costs** - Optimize build minutes usage

## Next Steps
- Configure production secrets
- Set up monitoring dashboards
- Train team on CI/CD processes
- Document troubleshooting procedures
- Schedule regular pipeline optimization reviews
