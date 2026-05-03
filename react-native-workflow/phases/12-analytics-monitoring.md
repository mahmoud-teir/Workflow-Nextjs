# Phase 12: Analytics & Monitoring

## Overview
Implement comprehensive analytics, crash reporting, and performance monitoring to understand user behavior and app health.

## Key Components

### 1. Analytics Setup (Firebase/Mixpanel)
```typescript
// services/analytics.ts
import analytics from '@react-native-firebase/analytics';
import { Platform } from 'react-native';

interface AnalyticsEvent {
  name: string;
  params?: Record<string, any>;
}

class AnalyticsService {
  async logEvent({ name, params }: AnalyticsEvent): Promise<void> {
    try {
      await analytics().logEvent(name, params);
    } catch (error) {
      console.error('Analytics error:', error);
    }
  }

  // Screen tracking
  async logScreenView(screenName: string): Promise<void> {
    await analytics().logScreenView({
      screen_name: screenName,
      screen_class: screenName
    });
  }

  // User engagement
  async logUserEngagement(duration: number, screenName: string): Promise<void> {
    await this.logEvent({
      name: 'user_engagement',
      params: {
        duration_seconds: Math.round(duration),
        screen_name: screenName
      }
    });
  }

  // Purchase tracking
  async logPurchase(
    productId: string,
    price: number,
    currency: string,
    transactionId: string
  ): Promise<void> {
    await analytics().logPurchase({
      value: price,
      currency,
      transaction_id: transactionId,
      items: [{ item_id: productId }]
    });
  }

  // Set user properties
  async setUserProperties(properties: Record<string, string>): Promise<void> {
    for (const [key, value] of Object.entries(properties)) {
      await analytics().setUserProperty(key, value);
    }
  }

  // Set user ID (after authentication)
  async setUserId(userId: string): Promise<void> {
    await analytics().setUserId(userId);
  }
}

export const analyticsService = new AnalyticsService();
```

### 2. Crash Reporting (Sentry)
```typescript
// services/crashReporting.ts
import * as Sentry from '@sentry/react-native';

export const initCrashReporting = () => {
  Sentry.init({
    dsn: process.env.SENTRY_DSN,
    environment: __DEV__ ? 'development' : 'production',
    enableAutoSessionTracking: true,
    sessionTrackingIntervalMillis: 30000,
    tracesSampleRate: __DEV__ ? 1.0 : 0.2,
    profilesSampleRate: __DEV__ ? 1.0 : 0.2,
    
    beforeSend(event, hint) {
      // Filter out sensitive data
      if (event.user) {
        delete event.user.email;
        delete event.user.ip_address;
      }
      return event;
    },

    integrations: [
      new Sentry.ReactNativeTracing({
        routingInstrumentation: new Sentry.ReactNavigationInstrumentation()
      })
    ]
  });
};

export const captureException = (error: Error, context?: any) => {
  Sentry.withScope(scope => {
    if (context) {
      scope.setContext('additional_info', context);
    }
    Sentry.captureException(error);
  });
};

export const captureMessage = (message: string, level: Sentry.SeverityLevel = 'info') => {
  Sentry.captureMessage(message, level);
};

export const addBreadcrumb = (breadcrumb: Sentry.Breadcrumb) => {
  Sentry.addBreadcrumb(breadcrumb);
};
```

### 3. Performance Monitoring
```typescript
// services/performanceMonitoring.ts
import perf from '@react-native-firebase/perf';

class PerformanceMonitor {
  private traces: Map<string, any> = new Map();

  async startTrace(traceName: string): Promise<void> {
    const trace = await perf().newTrace(traceName);
    await trace.start();
    this.traces.set(traceName, trace);
  }

  async stopTrace(traceName: string): Promise<void> {
    const trace = this.traces.get(traceName);
    if (trace) {
      await trace.stop();
      this.traces.delete(traceName);
    }
  }

  async recordHttpMetric(
    url: string,
    method: string,
    responseCode: number,
    durationMs: number
  ): Promise<void> {
    const httpMetric = await perf().newHttpMetric(url, method as any);
    await httpMetric.setResponseCode(responseCode);
    await httpMetric.putAttribute('platform', Platform.OS);
    await httpMetric.start();
    // Simulate network request completion
    await httpMetric.stop();
  }

  async recordCustomMetric(metricName: string, value: number): Promise<void> {
    // Custom metrics implementation
    console.log(`Recording metric: ${metricName} = ${value}`);
  }
}

export const performanceMonitor = new PerformanceMonitor();
```

### 4. User Journey Tracking
```typescript
// hooks/useUserJourney.ts
import { useEffect, useRef } from 'react';
import { analyticsService } from '../services/analytics';

interface JourneyStep {
  stepName: string;
  timestamp: number;
  metadata?: Record<string, any>;
}

export const useUserJourney = (journeyName: string) => {
  const journeySteps = useRef<JourneyStep[]>([]);
  const journeyStart = useRef<number>(Date.now());

  const addStep = (stepName: string, metadata?: Record<string, any>) => {
    const step: JourneyStep = {
      stepName,
      timestamp: Date.now(),
      metadata
    };
    journeySteps.current.push(step);

    analyticsService.logEvent({
      name: `${journeyName}_step`,
      params: {
        step_name: stepName,
        step_index: journeySteps.current.length,
        ...metadata
      }
    });
  };

  const completeJourney = () => {
    const duration = Date.now() - journeyStart.current;
    analyticsService.logEvent({
      name: `${journeyName}_completed`,
      params: {
        duration_ms: duration,
        total_steps: journeySteps.current.length,
        steps: journeySteps.current.map(s => s.stepName)
      }
    });

    journeySteps.current = [];
    journeyStart.current = Date.now();
  };

  const abandonJourney = (reason: string) => {
    analyticsService.logEvent({
      name: `${journeyName}_abandoned`,
      params: {
        reason,
        completed_steps: journeySteps.current.length,
        steps: journeySteps.current.map(s => s.stepName)
      }
    });

    journeySteps.current = [];
  };

  return { addStep, completeJourney, abandonJourney };
};
```

### 5. A/B Testing Integration
```typescript
// services/abTesting.ts
import { getRemoteConfig, getValue, fetchAndActivate } from '@react-native-firebase/remote-config';

class ABTestingService {
  constructor() {
    this.initialize();
  }

  private async initialize() {
    const remoteConfig = getRemoteConfig();
    
    remoteConfig.defaultConfig = {
      'feature_new_ui': false,
      'feature_checkout_flow': 'default',
      'onboarding_variant': 'a'
    };

    remoteConfig.fetchTimeout = 60;
    remoteConfig.minimumFetchInterval = 3600;

    try {
      await fetchAndActivate();
    } catch (error) {
      console.error('Remote config fetch failed:', error);
    }
  }

  getBooleanValue(key: string): boolean {
    return getValue(key).asBoolean();
  }

  getStringValue(key: string): string {
    return getValue(key).asString();
  }

  getNumberValue(key: string): number {
    return getValue(key).asNumber();
  }

  isInTestGroup(testName: string, group: string): boolean {
    const variant = this.getStringValue(testName);
    return variant === group;
  }
}

export const abTestingService = new ABTestingService();
```

## Deliverables Checklist

- [ ] Firebase Analytics integration
- [ ] Sentry crash reporting setup
- [ ] Performance monitoring implementation
- [ ] User journey tracking hooks
- [ ] A/B testing framework
- [ ] Custom dashboard configuration
- [ ] Alert rules for critical issues
- [ ] Privacy-compliant data collection

## Best Practices

1. **Respect user privacy** - Implement opt-in/out mechanisms
2. **Don't track sensitive data** - Avoid PII in analytics
3. **Batch events** when possible to reduce network calls
4. **Use meaningful event names** - Follow consistent naming conventions
5. **Set up alerts** for crash rate spikes
6. **Monitor app startup time** continuously
7. **Track user retention** metrics
8. **Regular review** of analytics dashboards

## Next Steps
- Configure analytics dashboards
- Set up automated alerts
- Create weekly/monthly reports
- Integrate with marketing tools
