# Laravel API Testing Best Practices

This document covers testing strategies for API endpoints.

## Test Structure

### Test Organization
```
tests/
├── Feature/
│   ├── ReviewTest.php
│   ├── ParticipantTest.php
│   └── PollTest.php
└── Unit/
    ├── ReviewModelTest.php
    └── UserModelTest.php
```

## Feature Tests (API Tests)

Feature tests test entire flows: HTTP request → database change → response.

### Basic Test Structure
```php
class ReviewTest extends TestCase
{
    use RefreshDatabase; // Fresh database for each test

    protected function setUp(): void
    {
        parent::setUp();
        // Create reusable test data
    }

    public function test_can_list_reviews(): void
    {
        // Arrange: Set up data
        $review = Review::factory()->create();

        // Act: Make request
        $response = $this->getJson('/api/v1/reviews');

        // Assert: Check response
        $response->assertStatus(200);
    }
}
```

## Running Tests

```bash
# Run all tests
php artisan test

# Run specific file
php artisan test tests/Feature/ReviewTest.php

# Run specific test
php artisan test --filter test_can_list_reviews

# Show output
php artisan test -v

# With coverage
php artisan test --coverage
```

## Authentication in Tests

### Acting As User
```php
$user = User::factory()->create();

// Make authenticated request
$response = $this->actingAs($user)
    ->getJson('/api/v1/reviews');
```

### Get Bearer Token
```php
$user = User::factory()->create();
$token = $user->createToken('test')->plainTextToken;

$response = $this->getJson('/api/v1/reviews', [
    'Authorization' => "Bearer $token"
]);
```

## Assertion Methods

### Status Assertions
```php
$response->assertStatus(200);         // Exact status
$response->assertOk();                // 200
$response->assertCreated();           // 201
$response->assertNoContent();         // 204
$response->assertBadRequest();        // 400
$response->assertUnauthorized();      // 401
$response->assertForbidden();         // 403
$response->assertNotFound();          // 404
$response->assertUnprocessable();     // 422
$response->assertServerError();       // 500
```

### JSON Assertions
```php
// Check JSON structure
$response->assertJsonStructure([
    'data' => [
        '*' => ['id', 'name', 'email']
    ]
]);

// Check specific value
$response->assertJsonPath('data.id', 1);

// Check value exists
$response->assertJsonPath('data.*.id', [1, 2, 3]);

// Check JSON count
$response->assertJsonCount(3, 'data');

// Full JSON match
$response->assertJson(['data' => ['id' => 1]]);
```

### Database Assertions
```php
// Check record exists
$this->assertDatabaseHas('reviews', [
    'id' => 1,
    'rating' => 5
]);

// Check record missing
$this->assertDatabaseMissing('reviews', [
    'id' => 1
]);

// Count records
$this->assertDatabaseCount('reviews', 10);
```

## Test Scenarios

### 1. List Endpoint
```php
public function test_can_list_reviews_with_pagination(): void
{
    // Create multiple reviews
    Review::factory()->count(20)->create();

    $response = $this->actingAs($user)
        ->getJson('/api/v1/reviews');

    $response->assertStatus(200)
        ->assertJsonStructure([
            'data' => [
                '*' => ['id', 'rating', 'user', 'product']
            ],
            'meta' => ['total', 'per_page', 'current_page']
        ]);

    $this->assertEquals(20, $response->json('meta.total'));
}
```

### 2. Show/View Single
```php
public function test_can_view_single_review(): void
{
    $review = Review::factory()
        ->for($user)
        ->for($product)
        ->create();

    $response = $this->actingAs($user)
        ->getJson("/api/v1/reviews/{$review->id}");

    $response->assertStatus(200)
        ->assertJsonPath('data.id', $review->id)
        ->assertJsonPath('data.rating', $review->rating);
}
```

### 3. Create/Store
```php
public function test_can_create_review(): void
{
    $data = [
        'user_id' => $user->id,
        'product_id' => $product->id,
        'rating' => 5,
        'comment' => 'Great!'
    ];

    $response = $this->actingAs($user)
        ->postJson('/api/v1/reviews', $data);

    $response->assertStatus(201)
        ->assertJsonPath('data.rating', 5);

    $this->assertDatabaseHas('reviews', $data);
}
```

### 4. Update/Edit
```php
public function test_can_update_review(): void
{
    $review = Review::factory()->create();
    $updateData = ['rating' => 4, 'comment' => 'Updated'];

    $response = $this->actingAs($review->user)
        ->putJson("/api/v1/reviews/{$review->id}", $updateData);

    $response->assertStatus(200);
    $this->assertDatabaseHas('reviews', $updateData);
}
```

### 5. Delete
```php
public function test_can_delete_review(): void
{
    $review = Review::factory()->create();

    $response = $this->actingAs($review->user)
        ->deleteJson("/api/v1/reviews/{$review->id}");

    $response->assertStatus(204);
    $this->assertDatabaseMissing('reviews', ['id' => $review->id]);
}
```

## Authorization Tests

```php
public function test_cannot_update_others_review(): void
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
```

## Validation Tests

```php
public function test_create_requires_valid_data(): void
{
    $response = $this->actingAs($user)
        ->postJson('/api/v1/reviews', []);

    $response->assertStatus(422)
        ->assertJsonValidationErrors([
            'user_id', 'product_id', 'rating'
        ]);
}

public function test_rating_must_be_between_1_and_5(): void
{
    $response = $this->actingAs($user)
        ->postJson('/api/v1/reviews', [
            'user_id' => $user->id,
            'product_id' => $product->id,
            'rating' => 10
        ]);

    $response->assertStatus(422)
        ->assertJsonValidationErrors(['rating']);
}
```

## Test Helpers

### Custom Assertion
```php
// In tests/TestCase.php
public function assertReviewStructure($response)
{
    $response->assertJsonStructure([
        'data' => [
            'id', 'rating', 'comment', 'user', 'product'
        ]
    ]);
    return $this;
}

// Usage
$response->assertOk()
    ->assertReviewStructure();
```

### Test Helpers Class
```php
class TestHelpers
{
    public static function loginUser(?User $user = null): User
    {
        return $user ?? User::factory()->create();
    }

    public static function createReview(User $user = null): Review
    {
        return Review::factory()
            ->for($user ?? User::factory())
            ->create();
    }
}

// Usage
class ReviewTest extends TestCase
{
    public function test_something(): void
    {
        $user = TestHelpers::loginUser();
        $review = TestHelpers::createReview($user);
    }
}
```

## Test Coverage Checklist

For each endpoint, test:
- ✅ Happy path (success)
- ✅ Missing required fields
- ✅ Invalid field values
- ✅ Authorization (who can access)
- ✅ Authentication (must be logged in)
- ✅ Database changes
- ✅ Response structure
- ✅ Response status codes
- ✅ Relationships loaded
- ✅ Pagination works

## Example: Complete Review Test Suite

```php
class ReviewTest extends TestCase
{
    use RefreshDatabase;

    private User $user;
    private User $otherUser;
    private Product $product;
    private Review $review;

    protected function setUp(): void
    {
        parent::setUp();
        $this->user = User::factory()->create();
        $this->otherUser = User::factory()->create();
        $this->product = Product::factory()->create();
        $this->review = Review::factory()
            ->for($this->user)
            ->for($this->product)
            ->create();
    }

    // Tests...
}
```

## Pro Tips

1. **Use RefreshDatabase** - Start with clean database each test
2. **Test One Thing** - Each test should verify one behavior
3. **Use Descriptive Names** - test_can_update_own_review_as_owner
4. **Setup with setUp()** - Reuse common data creation
5. **Create Factories** - Use factories instead of hardcoding data
6. **Test Edge Cases** - Empty arrays, null values, boundary conditions
7. **Group Related Tests** - Use comments or test classes
8. **Run Tests Often** - Catch bugs early
