# BigQuery SQL Reference for Digital Diary

Use these queries in the BigQuery console to extract deep insights from your exported Firebase Analytics data. 

> **Note**: Replace `your-project.analytics_1234567.events_*` with your actual BigQuery table ID.

## 1. Productive Engagement (DAU vs. Entry Creation)
This query measures how many of your daily active users (DAU) are actually saving diary entries.

```sql
SELECT
  event_date,
  count(DISTINCT user_pseudo_id) as total_users,
  countif(event_name = 'event_entry_edit_saved') as entries_saved,
  SAFE_DIVIDE(countif(event_name = 'event_entry_edit_saved'), count(DISTINCT user_pseudo_id)) as conversion_rate
FROM `your-project.analytics_1234567.events_*`
GROUP BY 1
ORDER BY 1 DESC
```

## 2. Content Depth by Platform
Analyze if users on iOS or Android are writing more detailed entries. This helps in understanding if one platform's keyboard or UI is more conducive to long-form writing.

```sql
SELECT
  (SELECT value.string_value FROM UNNEST(user_properties) WHERE key = 'os') as os,
  AVG(CAST((SELECT value.int_value FROM UNNEST(event_params) WHERE key = 'char_count') AS INT64)) as avg_char_count,
  MAX(CAST((SELECT value.int_value FROM UNNEST(event_params) WHERE key = 'char_count') AS INT64)) as max_char_count
FROM `your-project.analytics_1234567.events_*`
WHERE event_name = 'event_entry_edit_saved'
GROUP BY 1
```

## 3. Splash Screen Performance (P90 Latency)
Monitor the 90th percentile of loading times. If this number spikes, it indicates a performance regression in your startup logic or database migrations.

```sql
SELECT
  percentile_cont(duration, 0.9) OVER() as p90_loading_ms
FROM (
  SELECT
    (SELECT value.int_value FROM UNNEST(event_params) WHERE key = 'duration_ms') as duration
  FROM `your-project.analytics_1234567.events_*`
  WHERE event_name = 'event_feature_performance'
    AND (SELECT value.string_value FROM UNNEST(event_params) WHERE key = 'feature_name') = 'SplashLoading'
)
LIMIT 1
```

## 4. Security Funnel: PIN Failures
Identify if users are frequently entering incorrect PINs, which might suggest the PIN entry UI is frustrating or confusing.

```sql
SELECT
  (SELECT value.string_value FROM UNNEST(event_params) WHERE key = 'error_code') as failure_reason,
  COUNT(*) as occurrence_count
FROM `your-project.analytics_1234567.events_*`
WHERE event_name = 'event_login_failed'
GROUP BY 1
ORDER BY 2 DESC
```
