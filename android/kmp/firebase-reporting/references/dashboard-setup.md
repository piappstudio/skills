# Firebase Dashboard Setup: Custom Definitions

To see custom parameters in the Firebase Analytics dashboard, you must register them as Custom Dimensions.

## Registration Steps
1. Log in to the [Firebase Console](https://console.firebase.google.com/).
2. Navigate to **Analytics** > **Custom Definitions**.
3. Click the **Custom dimensions** tab.
4. Click **Create custom dimensions**.
5. Enter the following details for each parameter:

| Dimension Name | Scope | Description | Event Parameter |
| :--- | :--- | :--- | :--- |
| `screen_name` | Event | The name of the screen viewed | `screen_name` |
| `entry_id` | Event | Unique ID of the diary/reminder | `entry_id` |
| `char_count` | Event | Length of the entry content | `char_count` |
| `error_code` | Event | Machine-readable error identifier | `error_code` |
| `source` | Event | Origin of the action (e.g., Sync, UI) | `source` |
| `feature_name` | Event | Name of the feature being measured | `feature_name` |

## Why this is required
Firebase collects custom parameters but does not process them for the standard UI reports unless they are registered. Once registered, data will begin appearing in the dashboard within 24 hours.
