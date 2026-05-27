# Laravel Model Relationships Guide

This document explains how to define relationships between models in this project.

## Relationship Types

### 1. One-to-Many (belongsTo / hasMany)

**Use when:** One resource has many of another resource.

Example: A User has many Reviews

**In Review Model (belongsTo):**
```php
public function user()
{
    return $this->belongsTo(User::class);
}
```

**In User Model (hasMany):**
```php
public function reviews()
{
    return $this->hasMany(Review::class);
}
```

### 2. Many-to-One (hasMany / belongsTo)

**Use when:** Many resources belong to one resource.

Example: Many Comments belong to one Post

**In Comment Model:**
```php
public function post()
{
    return $this->belongsTo(Post::class);
}
```

**In Post Model:**
```php
public function comments()
{
    return $this->hasMany(Comment::class);
}
```

### 3. Many-to-Many (belongsToMany)

**Use when:** Two resources have a many-to-many relationship through a pivot table.

Example: Students enroll in many Courses, Courses have many Students

**Requires pivot table:** `course_student`

**In Student Model:**
```php
public function courses()
{
    return $this->belongsToMany(Course::class);
}
```

**In Course Model:**
```php
public function students()
{
    return $this->belongsToMany(Student::class);
}
```

### 4. Inverse One-to-Many (hasMany / belongsTo)

**Use when:** Accessing from the "one" side.

Example: Show has many Participants

**In ShowDetail Model:**
```php
public function participants()
{
    return $this->hasMany(Participant::class, 'show_id', 'id');
}
```

**In Participant Model:**
```php
public function show()
{
    return $this->belongsTo(ShowDetail::class, 'show_id', 'id');
}
```

## Eager Loading (Preventing N+1 Queries)

**Problem:** Each item loops creates a new query
```php
// Bad: N+1 queries
$reviews = Review::all();
foreach ($reviews as $review) {
    echo $review->user->name; // Query for each review
}
```

**Solution:** Use eager loading
```php
// Good: 2 queries (one for reviews, one for users)
$reviews = Review::with('user')->get();
foreach ($reviews as $review) {
    echo $review->user->name;
}

// Multiple relationships
$reviews = Review::with(['user', 'product'])->get();
```

## Accessing Related Data

```php
// Get user's reviews
$user = User::find(1);
$reviews = $user->reviews;

// With filtering
$reviews = $user->reviews()->where('rating', '>=', 4)->get();

// Count related items
$review_count = $user->reviews()->count();

// Create related item
$user->reviews()->create([
    'product_id' => 1,
    'rating' => 5,
    'comment' => 'Great!'
]);
```

## Migration Foreign Keys

```php
// Add foreign key in migration
$table->unsignedBigInteger('user_id');
$table->foreign('user_id')
    ->references('id')
    ->on('users')
    ->onDelete('cascade');  // Delete reviews if user deleted

// Alternative: Use foreignId shortcut
$table->foreignId('user_id')
    ->constrained()
    ->cascadeOnDelete();
```

## Project Relationships

Based on the BigBoss project:

```
User (1) -----> (Many) BehaviorTracker
     -----> (Many) Vote

Participant (1) -----> (Many) Note
            -----> (Many) Nomination
            -----> (Many) Vote

ShowDetail (1) -----> (Many) Participant
           -----> (Many) Poll
           -----> (Many) Task
           -----> (Many) Category
           -----> (Many) Nomination

Poll (1) -----> (Many) PollOption
     -----> (Many) Vote

PollOption (1) -----> (Many) Vote

Category (1) -----> (Many) Note

PromoUrl (1) -----> (Many) PromoDetail

PromoDetail (1) -----> (Many) PromoMetric
```

## Best Practices

1. ✅ Always eager load related data in index()
2. ✅ Use specific relationship selection to avoid loading unnecessary data
3. ✅ Define foreign keys in migrations
4. ✅ Use cascadeOnDelete() or set('onDelete', 'cascade') for data integrity
5. ✅ Test relationships in unit tests
6. ✅ Use inverse relationships for navigation
