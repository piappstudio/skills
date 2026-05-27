# Laravel Authorization & Policies Guide

This document explains how to implement authorization in your Laravel API.

## Authorization vs Authentication

- **Authentication:** Who are you? (Login, tokens)
- **Authorization:** What can you do? (Permissions, policies)

## Policy Basics

A Policy is a class that defines authorization rules for a model.

### Create a Policy
```bash
php artisan make:policy ReviewPolicy --model=Review
```

### Generate with Model
```bash
php artisan make:model Review -c --api -p
# -p flag creates the policy
```

## Policy Methods

### 7 Standard Methods

```php
// View all (in index)
public function viewAny(User $user): bool

// View single (in show)
public function view(User $user, Model $model): bool

// Create new
public function create(User $user): bool

// Update existing (usually only creator)
public function update(User $user, Model $model): bool

// Delete (usually only creator)
public function delete(User $user, Model $model): bool

// Restore soft-deleted (admin only)
public function restore(User $user, Model $model): bool

// Force delete permanently (admin only)
public function forceDelete(User $user, Model $model): bool
```

## Authorization in Controllers

### Using authorizeResource()

Register in controller constructor:
```php
public function __construct()
{
    $this->authorizeResource(Review::class);
}
```

This automatically checks authorization for:
- `index()` → viewAny()
- `show()` → view()
- `store()` → create()
- `update()` → update()
- `destroy()` → delete()

### Manual Authorization

```php
// Throw 403 if unauthorized
$this->authorize('update', $review);

// Or check and redirect
if (!auth()->user()->can('update', $review)) {
    abort(403, 'Unauthorized');
}

// Get boolean result
if (auth()->user()->can('delete', $review)) {
    // Do something
}
```

## Policy Examples

### Example 1: Owner-Only Update
```php
public function update(User $user, Review $review): bool
{
    return $user->id === $review->user_id;
}
```

### Example 2: Owner or Admin
```php
public function delete(User $user, Review $review): bool
{
    return $user->id === $review->user_id || $user->role === 'admin';
}
```

### Example 3: Admin Only
```php
public function restore(User $user, Review $review): bool
{
    return $user->role === 'admin';
}
```

### Example 4: Based on Status
```php
public function update(User $user, Review $review): bool
{
    // Can't update reviews marked as final
    return $review->status !== 'final';
}
```

### Example 5: Complex Logic
```php
public function create(User $user): bool
{
    // Only users with email verified can create
    return $user->email_verified_at !== null
        && !$user->banned;
}
```

## Role-Based Authorization

### Using User Role

In your User model:
```php
protected $fillable = [
    'name',
    'email',
    'password',
    'role', // 'user', 'admin', 'moderator'
];
```

In Policy:
```php
public function delete(User $user, Review $review): bool
{
    // Admin can delete any, user can delete own
    if ($user->role === 'admin') {
        return true;
    }
    return $user->id === $review->user_id;
}
```

## Authorization in Requests

Use Form Requests for authorization:

```php
namespace App\Http\Requests;

use Illuminate\Foundation\Http\FormRequest;

class UpdateReviewRequest extends FormRequest
{
    public function authorize(): bool
    {
        return auth()->user()->can('update', $this->review);
    }

    public function rules(): array
    {
        return [
            'rating' => 'sometimes|integer|between:1,5',
            'comment' => 'nullable|string|max:1000',
        ];
    }
}
```

Use in controller:
```php
public function update(UpdateReviewRequest $request, Review $review)
{
    // Authorization already checked!
    $review->update($request->validated());
    return $review;
}
```

## Registering Policies

In `app/Providers/AuthServiceProvider.php`:

```php
use App\Models\Review;
use App\Policies\ReviewPolicy;

protected $policies = [
    Review::class => ReviewPolicy::class,
];
```

Or auto-discover:
```php
public function boot(): void
{
    // Automatically discovers policies in App\Policies
    Gate::discover();
}
```

## Common Authorization Patterns

### Pattern 1: Anyone Can View, Owner Can Edit
```php
class ReviewPolicy
{
    public function view(User $user, Review $review): bool
    {
        return true; // Anyone can view
    }

    public function update(User $user, Review $review): bool
    {
        return $user->id === $review->user_id;
    }

    public function delete(User $user, Review $review): bool
    {
        return $user->id === $review->user_id;
    }
}
```

### Pattern 2: Admin Can Do Everything, Moderators Some Things
```php
class ReviewPolicy
{
    public function delete(User $user, Review $review): bool
    {
        if ($user->role === 'admin') {
            return true;
        }
        if ($user->role === 'moderator') {
            return true; // Moderators can delete anything
        }
        return $user->id === $review->user_id;
    }
}
```

### Pattern 3: Hierarchical Roles
```php
public function update(User $user, Review $review): bool
{
    $userRoles = ['user', 'moderator', 'admin'];
    $reviewerRoles = ['moderator', 'admin'];
    
    $userRole = array_search($user->role, $userRoles);
    $requiredRole = array_search('moderator', $userRoles);
    
    return $userRole >= $requiredRole
        || $user->id === $review->user_id;
}
```

## Testing Authorization

```php
class ReviewTest extends TestCase
{
    public function test_user_cannot_update_others_review(): void
    {
        $review = Review::factory()->create();
        $otherUser = User::factory()->create();

        $response = $this->actingAs($otherUser)
            ->putJson("/api/v1/reviews/{$review->id}", ['rating' => 1]);

        $response->assertStatus(403);
    }

    public function test_admin_can_delete_any_review(): void
    {
        $admin = User::factory()->create(['role' => 'admin']);
        $review = Review::factory()->create();

        $response = $this->actingAs($admin)
            ->deleteJson("/api/v1/reviews/{$review->id}");

        $response->assertStatus(204);
    }
}
```

## BigBoss Project Authorization

Based on the project roles:
- `admin` - Can manage all show content
- `moderator` - Can approve content
- `user` - Can create personal content (reviews, comments)

Example policy for Participant editing:
```php
public function update(User $user, Participant $participant): bool
{
    if ($user->role === 'admin') {
        return true; // Admin can edit any
    }
    if ($user->role === 'moderator') {
        return true; // Moderator can edit any
    }
    return false; // Regular users cannot edit participants
}
```
