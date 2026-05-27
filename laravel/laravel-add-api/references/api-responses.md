# Laravel API Response Format Guide

This document explains the expected response format for all API endpoints.

## Standard JSON Response Structure

### Successful Responses

#### List/Paginated Response (200 OK)
```json
{
  "data": [
    {
      "id": 1,
      "name": "Item Name",
      "created_at": "2024-03-20T10:30:00Z",
      "relationships": {
        "user": { "id": 1, "name": "User Name" }
      }
    }
  ],
  "links": {
    "first": "http://localhost:8000/api/v1/reviews?page=1",
    "last": "http://localhost:8000/api/v1/reviews?page=3",
    "prev": null,
    "next": "http://localhost:8000/api/v1/reviews?page=2"
  },
  "meta": {
    "current_page": 1,
    "from": 1,
    "last_page": 3,
    "per_page": 15,
    "to": 15,
    "total": 45
  }
}
```

#### Single Item Response (200 OK)
```json
{
  "data": {
    "id": 1,
    "name": "Item Name",
    "description": "Item description",
    "rating": 5,
    "created_at": "2024-03-20T10:30:00Z",
    "user": {
      "id": 1,
      "name": "John Doe"
    }
  }
}
```

#### Create Response (201 Created)
```json
{
  "data": {
    "id": 1,
    "name": "New Item",
    "created_at": "2024-03-20T10:30:00Z"
  }
}
```

#### Delete Response (204 No Content)
```
[Empty Body - No JSON response]
```

### Error Responses

#### Validation Error (422 Unprocessable Entity)
```json
{
  "message": "The given data was invalid.",
  "errors": {
    "email": ["The email field is required."],
    "rating": ["The rating must be between 1 and 5."]
  }
}
```

#### Authentication Error (401 Unauthorized)
```json
{
  "message": "Unauthenticated."
}
```

#### Authorization Error (403 Forbidden)
```json
{
  "message": "This action is unauthorized."
}
```

#### Not Found Error (404 Not Found)
```json
{
  "message": "Not found."
}
```

#### Server Error (500 Internal Server Error)
```json
{
  "message": "Server error message",
  "exception": "ExceptionName",
  "file": "/path/to/file.php",
  "line": 123
}
```

## HTTP Status Codes

| Code | Meaning | Use When |
|------|---------|----------|
| 200 | OK | Successful GET, PUT, PATCH |
| 201 | Created | Successful POST |
| 204 | No Content | Successful DELETE |
| 400 | Bad Request | Invalid request format |
| 401 | Unauthorized | Not authenticated |
| 403 | Forbidden | Authenticated but not authorized |
| 404 | Not Found | Resource doesn't exist |
| 422 | Unprocessable Entity | Validation failed |
| 500 | Server Error | Internal server error |

## Response Formatting Tips

### 1. Include Relationships
```php
// In controller
return Review::with(['user', 'product'])->get();
```

### 2. Pagination Configuration
```php
// In controller - 15 items per page
$reviews = Review::with(['user', 'product'])
    ->paginate(15);
```

### 3. Filtering in List
```php
// In controller
$reviews = Review::with(['user', 'product'])
    ->where('rating', '>=', request('min_rating', 1))
    ->latest()
    ->paginate(15);
```

### 4. Field Selection
```php
// Return only needed fields
$reviews = Review::select('id', 'user_id', 'rating', 'created_at')
    ->with('user:id,name')
    ->paginate(15);
```

### 5. Custom Data Format
```php
// Using resources
return ReviewResource::collection($reviews);

// OR in controller
return $reviews->map(function ($review) {
    return [
        'id' => $review->id,
        'rating' => $review->rating,
        'author' => $review->user->name,
    ];
});
```

## DateTime Format

All timestamps should be ISO 8601 format:
```
2024-03-20T10:30:45Z
```

Configure in model:
```php
protected $dateFormat = 'Y-m-d\TH:i:s\Z';
```

## Response Headers

Include in all responses:
```
Content-Type: application/json
Cache-Control: no-cache, private
X-Content-Type-Options: nosniff
```

## BigBoss Project Response Examples

### Review List
```json
{
  "data": [
    {
      "id": 1,
      "rating": 5,
      "comment": "Great show!",
      "user": { "id": 1, "name": "John" },
      "created_at": "2024-03-20T10:30:00Z"
    }
  ],
  "meta": { "total": 45, "per_page": 15 }
}
```

### Participant List
```json
{
  "data": [
    {
      "id": 1,
      "name": "Contestant Name",
      "dial_number": 1,
      "entry_date": "2024-03-20",
      "show": { "id": 1, "name": "BigBoss" }
    }
  ]
}
```
