---
name: firebase-analytics-reporting
description: 'Guidance for generating business insights from Firebase Analytics and BigQuery. Use when: building dashboards, writing BigQuery SQL queries, setting up custom definitions, analyzing user funnels, or monitoring app performance via analytics.'
argument-hint: 'Describe your reporting task: BigQuery query help, dashboard setup, or funnel analysis'
---

# Firebase Analytics Reporting & BigQuery Insights

Expert guidance for transforming raw Firebase events into actionable business intelligence using the Firebase Console and BigQuery.

## When to Use

- **Dashboard Setup**: Registering custom parameters to make them visible in the Firebase UI.
- **Funnel Analysis**: Measuring user progression through key app journeys (e.g., writing a diary).
- **Deep Analysis**: Writing SQL queries in BigQuery for cross-platform data mining.
- **Performance Monitoring**: Analyzing latency and error rates across the user base.

## Core Concepts

1. **Custom Definitions**: Custom parameters (like `entry_id`) are not visible in reports until registered in the Firebase Console.
2. **Explorations**: Interactive techniques in Google Analytics 4 (GA4) to visualize user paths and funnels.
3. **BigQuery Export**: Raw event data accessible via SQL for unlimited analysis flexibility.

## Reporting Workflow

### 1. Setup Custom Dimensions
Ensure all parameters defined in `AnalyticConstant.kt` are registered.
Refer to [Dashboard Setup](./references/dashboard-setup.md).

### 2. Define Business Funnels
Map user steps to event names to measure drop-off.
Example: **Diary Creation Funnel**
1. `event_screen_viewed` (Splash)
2. `event_screen_viewed` (DiaryDashboard)
3. `event_entry_edit_started`
4. `event_entry_edit_saved`

### 3. Advanced SQL Analysis
Use BigQuery for complex joins or historical trends.
Refer to [BigQuery Queries](./references/bigquery-queries.md).

## Best Practices

### Analytics Strategy
- **Segment by Platform**: Always compare Android vs. iOS behavior using the `os` property.
- **Monitor Errors**: Use the `error_code` parameter to prioritize bug fixes based on user impact.
- **Track Latency**: Monitor `duration_ms` for critical actions to ensure a smooth UX.

### Data Governance
- **Privacy First**: Never export PII to analytics.
- **Consistency**: Stick to snake_case naming as established in the [Firebase Analytics Skill](../firebase-analytics/SKILL.md).

---

**Learn More**: [GA4 Exploration Basics](https://support.google.com/analytics/answer/7579450) | [BigQuery Export Schema](https://support.google.com/analytics/answer/7029846)
