---
name: laravel-add-api
description: "Add complete REST API endpoints to Laravel with model, migration, factory, controller, policy, routes, and tests. Use when: creating new data resource, adding CRUD operations, extending API functionality, building new features, scaffolding database entities."
argument-hint: "Specify the resource name (singular), e.g., 'BlogPost', 'ReviewRating', 'CommentThread'"
user-invocable: true
---

# Laravel Add API Skill

Complete workflow for adding fully functional REST API endpoints to your Laravel application. Covers database design through testing.

## When to Use

- **Creating new REST API resources** - Users, Posts, Comments, etc.
- **Extending existing functionality** - Adding new features to the show management system
- **Building CRUD operations** - Create, Read, Update, Delete operations for any entity
- **Database entity scaffolding** - New database tables with relationships
- **Feature implementation** - Complete end-to-end feature with API, auth, and tests

## What This Generates

✅ Database migration (table structure)
✅ Eloquent model with relationships
✅ Database factory for testing
✅ API controller with CRUD methods
✅ Authorization policy
✅ API routes (GET, POST, PUT, DELETE)
✅ Database seeder
✅ API tests (Feature tests)
✅ Full documentation

## Prerequisites

- Laravel application running
- Artisan CLI available
- Understanding of the resource you're creating
- Related models already exist (if foreign keys needed)

## Step-by-Step Procedure

### Phase 1: Plan & Requirements (5-10 min)

1. **Define the Resource**
   - Resource name (singular): e.g., `Review`
   - Table name (plural): e.g., `reviews`
   - Primary fields needed
   - Relationships to other models

2. **Document Relationships**
   - Foreign keys needed
   - One-to-many, many-to-many, etc.
   - Dependent relationships

3. **Define API Operations**
   - GET all resources (list with pagination)
   - GET single resource
   - POST create resource
   - PUT/PATCH update resource
   - DELETE resource
   - Custom endpoints needed?

### Phase 2: Generate Model & Migration (2-3 min)

Use the [generation script](./scripts/generate-api.sh) or manual Artisan commands:

```bash
# Generate model with migration, factory, seeder, controller, and policy
php artisan make:model ResourceName -mfsc --api

# Example for a Review resource
php artisan make:model Review -mfsc --api
```

This creates:
- `app/Models/Review.php`
- `database/migrations/YYYY_MM_DD_HHMMSS_create_reviews_table.php`
- `database/factories/ReviewFactory.php`
- `database/seeders/ReviewSeeder.php`
- `app/Http/Controllers/ReviewController.php`
- `app/Policies/ReviewPolicy.php`

### Phase 3: Define Database Schema (10-15 min)

Edit: `database/migrations/YYYY_MM_DD_HHMMSS_create_reviews_table.php`

**Use [migration template](./templates/migration.template.php) as reference**

Add fields:
```php
Schema::create('reviews', function (Blueprint $table) {
    $table->id();
    $table->unsignedBigInteger('user_id');  // Foreign key
    $table->unsignedBigInteger('product_id'); // Foreign key
    $table->integer('rating');               // 1-5 stars
    $table->text('comment')->nullable();
    $table->timestamps();
    
    // Foreign key constraints
    $table->foreign('user_id')->references('id')->on('users')->onDelete('cascade');
    $table->foreign('product_id')->references('id')->on('products')->onDelete('cascade');
    
    // Indexes for performance
    $table->index('user_id');
    $table->index('product_id');
});
```

Key considerations:
- ✅ Use appropriate column types (integer, string, text, boolean, json, etc.)
- ✅ Add nullable() for optional fields
- ✅ Add unique() for unique constraints
- ✅ Add foreign keys with onDelete('cascade') or appropriate action
- ✅ Add indexes on frequently queried columns
- ✅ Include timestamps() for created_at/updated_at

### Phase 4: Define Model & Relationships (10-15 min)

Edit: `app/Models/Review.php`

**Use [model template](./templates/model.template.php) as reference**

```php
<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Factories\HasFactory;
use Illuminate\Database\Eloquent\Model;
use Laravel\Sanctum\HasApiTokens;

class Review extends Model
{
    use HasApiTokens, HasFactory;

    protected $fillable = [
        'user_id',
        'product_id',
        'rating',
        'comment',
    ];

    protected $casts = [
        'rating' => 'integer',
        'created_at' => 'datetime',
    ];

    // Define relationships
    public function user()
    {
        return $this->belongsTo(User::class);
    }

    public function product()
    {
        return $this->belongsTo(Product::class);
    }
}
```

Add:
- ✅ `$fillable` array for mass assignment
- ✅ `$casts` for type conversion
- ✅ Relationship methods (belongsTo, hasMany, etc.)
- ✅ HasApiTokens trait (for sanctum)
- ✅ Custom accessor/mutator methods if needed
- ✅ Query scopes for filtering

### Phase 5: Create Factory (5-10 min)

Edit: `database/factories/ReviewFactory.php`

**Use [factory template](./templates/factory.template.php) as reference**

```php
<?php

namespace Database\Factories;

use Illuminate\Database\Eloquent\Factories\Factory;
use App\Models\Review;
use App\Models\User;
use App\Models\Product;

class ReviewFactory extends Factory
{
    protected $model = Review::class;

    public function definition(): array
    {
        return [
            'user_id' => User::factory(),
            'product_id' => Product::factory(),
            'rating' => $this->faker->numberBetween(1, 5),
            'comment' => $this->faker->paragraph(),
        ];
    }
}
```

Key points:
- ✅ Use faker for realistic test data
- ✅ Use factory() for related models
- ✅ Include all fillable fields
- ✅ Add state() methods for variations (featured, approved, etc.)

### Phase 6: Define Controller (15-20 min)

Edit: `app/Http/Controllers/ReviewController.php`

**Use [controller template](./templates/controller.template.php) as reference**

```php
<?php

namespace App\Http\Controllers\Api\V1;

use App\Http\Controllers\Controller;
use App\Models\Review;
use Illuminate\Http\Request;
use Illuminate\Http\Response;

class ReviewController extends Controller
{
    // Apply authorization middleware
    public function __construct()
    {
        $this->authorizeResource(Review::class);
    }

    // List all reviews with pagination
    public function index()
    {
        return Review::with(['user', 'product'])
            ->paginate(15);
    }

    // Show single review
    public function show(Review $review)
    {
        return $review->load(['user', 'product']);
    }

    // Create new review
    public function store(Request $request)
    {
        $validated = $request->validate([
            'user_id' => 'required|exists:users,id',
            'product_id' => 'required|exists:products,id',
            'rating' => 'required|integer|between:1,5',
            'comment' => 'nullable|string|max:1000',
        ]);

        return Review::create($validated)
            ->load(['user', 'product']);
    }

    // Update review
    public function update(Request $request, Review $review)
    {
        $validated = $request->validate([
            'rating' => 'sometimes|integer|between:1,5',
            'comment' => 'nullable|string|max:1000',
        ]);

        $review->update($validated);
        return $review->load(['user', 'product']);
    }

    // Delete review
    public function destroy(Review $review)
    {
        $review->delete();
        return response(null, Response::HTTP_NO_CONTENT);
    }
}
```

Implement:
- ✅ `index()` - List with pagination and eager loading
- ✅ `show($id)` - Single resource with relationships
- ✅ `store()` - Create with validation
- ✅ `update()` - Update with validation
- ✅ `destroy()` - Delete with proper response
- ✅ Request validation for each operation
- ✅ Eager load relationships to avoid N+1 queries
- ✅ Use resource responses (consistency)

### Phase 7: Create Policy (5-10 min)

Edit: `app/Policies/ReviewPolicy.php`

**Use [policy template](./templates/policy.template.php) as reference**

```php
<?php

namespace App\Policies;

use App\Models\Review;
use App\Models\User;

class ReviewPolicy
{
    // View all reviews
    public function viewAny(User $user): bool
    {
        return true; // Any authenticated user
    }

    // View single review
    public function view(User $user, Review $review): bool
    {
        return true; // Any authenticated user
    }

    // Create review
    public function create(User $user): bool
    {
        return true; // Any authenticated user
    }

    // Update own or admin
    public function update(User $user, Review $review): bool
    {
        return $user->id === $review->user_id || $user->role === 'admin';
    }

    // Delete own or admin
    public function delete(User $user, Review $review): bool
    {
        return $user->id === $review->user_id || $user->role === 'admin';
    }

    // Restore deleted reviews (admin only)
    public function restore(User $user, Review $review): bool
    {
        return $user->role === 'admin';
    }

    // Force delete (admin only)
    public function forceDelete(User $user, Review $review): bool
    {
        return $user->role === 'admin';
    }
}
```

Authorization rules:
- ✅ `viewAny()` - Who can list resources
- ✅ `view()` - Who can view single resource
- ✅ `create()` - Who can create resource
- ✅ `update()` - Who can modify resource (usually creator or admin)
- ✅ `delete()` - Who can delete resource
- ✅ `restore()` - Who can restore soft-deleted items
- ✅ `forceDelete()` - Who can permanently delete

### Phase 8: Register Routes (5 min)

Edit: `routes/api.php`

Add after imports:
```php
use App\Http\Controllers\Api\V1\ReviewController;
```

Add to protected routes group (after line ~17):
```php
Route::group([
    "prefix" => "v1",
    'namespace' => 'App\Http\Controllers\Api\V1',
    'middleware' => 'auth:sanctum'  // ⚠️ CRITICAL: Always add for protected endpoints
], function () {
    Route::apiResource("reviews", ReviewController::class);
});
```

Or add custom route for specific endpoints:
```php
Route::group([
    "prefix" => "v1",
    'namespace' => 'App\Http\Controllers\Api\V1',
    'middleware' => 'auth:sanctum'  // ⚠️ CRITICAL: Always add for protected endpoints
], function () {
    Route::get('/reviews', [ReviewController::class, 'index']);
    Route::post('/reviews', [ReviewController::class, 'store']);
    Route::get('/reviews/{review}', [ReviewController::class, 'show']);
    Route::put('/reviews/{review}', [ReviewController::class, 'update']);
    Route::delete('/reviews/{review}', [ReviewController::class, 'destroy']);
});
```

**⚠️ IMPORTANT - Authentication Middleware:**

Always include `'middleware'=> 'auth:sanctum'` for:
- ✅ Data retrieval endpoints (GET)
- ✅ Create operations (POST)
- ✅ Update operations (PUT/PATCH)
- ✅ Delete operations (DELETE)

**Do NOT** include middleware for:
- ❌ Authentication endpoints (login, register)
- ❌ Email verification (send code)
- ❌ Public information endpoints (if any)

**Why:** Without `'middleware'=> 'auth:sanctum'`, any unauthenticated user can access your API endpoints.

Generated routes:
- ✅ `GET /api/v1/reviews` - List all (requires auth token)
- ✅ `POST /api/v1/reviews` - Create (requires auth token)
- ✅ `GET /api/v1/reviews/{id}` - Show (requires auth token)
- ✅ `PUT /api/v1/reviews/{id}` - Update (requires auth token)
- ✅ `DELETE /api/v1/reviews/{id}` - Delete (requires auth token)

### Phase 9: Create Seeder (5-10 min)

Edit: `database/seeders/ReviewSeeder.php`

**Use [seeder template](./templates/seeder.template.php) as reference**

```php
<?php

namespace Database\Seeders;

use App\Models\Review;
use App\Models\User;
use App\Models\Product;
use Illuminate\Database\Seeder;

class ReviewSeeder extends Seeder
{
    public function run(): void
    {
        // Create 50 reviews with random users and products
        Review::factory()
            ->count(50)
            ->create();
    }
}
```

Update: `database/seeders/DatabaseSeeder.php`

Add this line in the run method:
```php
$this->call([
    UserSeeder::class,
    ProductSeeder::class,
    ReviewSeeder::class,
]);
```

### Phase 10: Run Migrations & Seeders (2 min)

```bash
# Run migrations
php artisan migrate

# Seed database with test data
php artisan db:seed --class=ReviewSeeder

# Or run all seeders
php artisan db:seed
```

### Phase 11: Create Tests (15-20 min)

Create: `tests/Feature/ReviewTest.php`

**Use [test template](./templates/test.template.php) as reference**

```php
<?php

namespace Tests\Feature;

use Illuminate\Foundation\Testing\RefreshDatabase;
use Tests\TestCase;
use App\Models\Review;
use App\Models\User;
use App\Models\Product;

class ReviewTest extends TestCase
{
    use RefreshDatabase;

    // Setup test data
    private User $user;
    private Product $product;
    private Review $review;

    protected function setUp(): void
    {
        parent::setUp();
        $this->user = User::factory()->create();
        $this->product = Product::factory()->create();
        $this->review = Review::factory()
            ->for($this->user)
            ->for($this->product)
            ->create();
    }

    // Test listing reviews
    public function test_can_list_reviews(): void
    {
        $response = $this->actingAs($this->user)
            ->getJson('/api/v1/reviews');

        $response->assertStatus(200)
            ->assertJsonStructure([
                'data' => [
                    '*' => ['id', 'user_id', 'product_id', 'rating', 'comment']
                ]
            ]);
    }

    // Test viewing single review
    public function test_can_view_review(): void
    {
        $response = $this->actingAs($this->user)
            ->getJson("/api/v1/reviews/{$this->review->id}");

        $response->assertStatus(200)
            ->assertJsonPath('data.id', $this->review->id)
            ->assertJsonPath('data.rating', $this->review->rating);
    }

    // Test creating review
    public function test_can_create_review(): void
    {
        $data = [
            'user_id' => $this->user->id,
            'product_id' => $this->product->id,
            'rating' => 5,
            'comment' => 'Excellent product!',
        ];

        $response = $this->actingAs($this->user)
            ->postJson('/api/v1/reviews', $data);

        $response->assertStatus(201)
            ->assertJsonPath('data.rating', 5);

        $this->assertDatabaseHas('reviews', $data);
    }

    // Test updating review
    public function test_can_update_review(): void
    {
        $data = ['rating' => 4, 'comment' => 'Updated comment'];

        $response = $this->actingAs($this->user)
            ->putJson("/api/v1/reviews/{$this->review->id}", $data);

        $response->assertStatus(200);
        $this->assertDatabaseHas('reviews', $data);
    }

    // Test deleting review
    public function test_can_delete_review(): void
    {
        $response = $this->actingAs($this->user)
            ->deleteJson("/api/v1/reviews/{$this->review->id}");

        $response->assertStatus(204);
        $this->assertDatabaseMissing('reviews', ['id' => $this->review->id]);
    }

    // Test unauthorized access
    public function test_cannot_update_others_review(): void
    {
        $otherUser = User::factory()->create();

        $response = $this->actingAs($otherUser)
            ->putJson("/api/v1/reviews/{$this->review->id}", ['rating' => 1]);

        $response->assertStatus(403);
    }

    // Test validation errors
    public function test_create_requires_validation(): void
    {
        $response = $this->actingAs($this->user)
            ->postJson('/api/v1/reviews', []);

        $response->assertStatus(422)
            ->assertJsonValidationErrors(['user_id', 'product_id', 'rating']);
    }
}
```

Test coverage:
- ✅ List resources
- ✅ View single resource
- ✅ Create resource
- ✅ Update resource
- ✅ Delete resource
- ✅ Authorization/permission checks
- ✅ Validation error handling
- ✅ Relationships loaded correctly

### Phase 12: Run Tests & Verify (5-10 min)

```bash
# Run all tests
php artisan test

# Run specific test file
php artisan test tests/Feature/ReviewTest.php

# Run tests with coverage
php artisan test --coverage
```

Expected output:
```
PASSED  Tests/Feature/ReviewTest::test_can_list_reviews
PASSED  Tests/Feature/ReviewTest::test_can_view_review
PASSED  Tests/Feature/ReviewTest::test_can_create_review
PASSED  Tests/Feature/ReviewTest::test_can_update_review
PASSED  Tests/Feature/ReviewTest::test_can_delete_review
PASSED  Tests/Feature/ReviewTest::test_cannot_update_others_review
PASSED  Tests/Feature/ReviewTest::test_create_requires_validation
```

### Phase 13: Manual API Testing (10-15 min)

Test endpoints using curl or Postman:

```bash
# Start dev server
php artisan serve

# Get auth token (login)
curl -X POST http://localhost:8000/api/v1/api_login \
  -H "Content-Type: application/json" \
  -d '{"email":"user@example.com","password":"password"}'

# Use token in requests
TOKEN="your_token_here"

# List reviews
curl -X GET http://localhost:8000/api/v1/reviews \
  -H "Authorization: Bearer $TOKEN"

# Create review
curl -X POST http://localhost:8000/api/v1/reviews \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"user_id":1,"product_id":1,"rating":5,"comment":"Great!"}'

# View single
curl -X GET http://localhost:8000/api/v1/reviews/1 \
  -H "Authorization: Bearer $TOKEN"

# Update
curl -X PUT http://localhost:8000/api/v1/reviews/1 \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"rating":4}'

# Delete
curl -X DELETE http://localhost:8000/api/v1/reviews/1 \
  -H "Authorization: Bearer $TOKEN"
```

---

## Quick Checklist

Use this checklist to ensure nothing is missed:

- [ ] **Plan Phase**
  - [ ] Resource name decided
  - [ ] Fields/columns documented
  - [ ] Relationships identified
  - [ ] API operations defined

- [ ] **Scaffolding**
  - [ ] Model generated with -mfsc flags
  - [ ] All files created (migration, factory, policy, controller)

- [ ] **Database**
  - [ ] Migration file edited with fields
  - [ ] Foreign keys defined
  - [ ] Indexes added for performance
  - [ ] Migration run successfully

- [ ] **Model**
  - [ ] $fillable array populated
  - [ ] $casts defined
  - [ ] Relationships added
  - [ ] HasApiTokens trait included

- [ ] **Factory**
  - [ ] All fields have faker data
  - [ ] Foreign key relationships use factory()
  - [ ] State methods added (if needed)

- [ ] **Controller**
  - [ ] All 5 CRUD methods implemented
  - [ ] Validation added to store() and update()
  - [ ] Eager loading in index() and show()
  - [ ] Proper HTTP responses (201, 200, 204, 422, etc.)
  - [ ] Request validation rules defined

- [ ] **Policy**
  - [ ] viewAny(), view(), create() defined
  - [ ] update() and delete() check ownership or admin
  - [ ] restore() and forceDelete() for admins only

- [ ] **Routes**
  - [ ] apiResource() registered or custom routes added
  - [ ] ⚠️ **'middleware'=> 'auth:sanctum' added for ALL data endpoints**
  - [ ] Authentication/verification endpoints have NO middleware
  - [ ] Correct URL prefix (/api/v1)

- [ ] **Seeder**
  - [ ] ReviewSeeder created with factory()
  - [ ] Registered in DatabaseSeeder
  - [ ] Run successfully with db:seed

- [ ] **Tests**
  - [ ] Tests/Feature/ReviewTest.php created
  - [ ] All CRUD operations tested
  - [ ] Authorization tests included
  - [ ] Validation tests included
  - [ ] All tests passing

- [ ] **Verification**
  - [ ] Manual API testing with curl/Postman
  - [ ] All CRUD operations working
  - [ ] Authorization working correctly
  - [ ] Validation error handling working
  - [ ] Relationships loaded correctly

---

## File References

- [Migration Template](./templates/migration.template.php)
- [Model Template](./templates/model.template.php)
- [Factory Template](./templates/factory.template.php)
- [Controller Template](./templates/controller.template.php)
- [Policy Template](./templates/policy.template.php)
- [Test Template](./templates/test.template.php)
- [Seeder Template](./templates/seeder.template.php)

## Related Documentation

- [Laravel Models & Relationships](./references/relationships.md)
- [API Response Format Guide](./references/api-responses.md)
- [Authorization & Policies](./references/policies.md)
- [Testing Best Practices](./references/testing.md)

## Time Estimates

- **Planning:** 5-10 min
- **Scaffolding:** 2-3 min (auto-generated)
- **Database:** 10-15 min (write migration, run)
- **Model:** 10-15 min (add relationships, casts)
- **Factory:** 5-10 min
- **Controller:** 15-20 min
- **Policy:** 5-10 min
- **Routes:** 5 min
- **Seeder:** 5-10 min
- **Tests:** 15-20 min
- **Verification:** 5-10 min

**Total: 1.5 - 2.5 hours** (first time), **30-45 min** (subsequent resources)

---

## Pro Tips

1. **Always add 'middleware'=> 'auth:sanctum'** - CRITICAL for API security
2. **Use artisan commands for generation** - Saves time and reduces errors
3. **Eager load relationships** - Prevents N+1 query problems in index()
4. **Add pagination** - Essential for list endpoints
5. **Validate on both sides** - Frontend + backend validation
6. **Test authorization** - Don't forget permission tests
7. **Document relationships** - Future-proof your code
8. **Use factories in tests** - Creates consistent test data
9. **Start with the happy path** - Then test error cases

---

## Example Workflow

```bash
# 1. Generate scaffolding
php artisan make:model Comment -mfsc --api

# 2. Edit migration (add fields)
# 3. Edit model (add relationships)
# 4. Edit factory (add faker data)
# 5. Edit controller (add logic)
# 6. Edit policy (add authorization)
# 7. Register routes in api.php
# 8. Edit seeder
# 9. Run migration
php artisan migrate

# 10. Run seeder
php artisan db:seed --class=CommentSeeder

# 11. Create test file
# 12. Run tests
php artisan test tests/Feature/CommentTest.php

# 13. Test API manually with curl/Postman
```

Done! Your new API is ready for use.
