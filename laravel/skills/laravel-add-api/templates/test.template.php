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

    private User $user;
    private User $otherUser;
    private Product $product;
    private Review $review;

    /**
     * Set up test fixtures.
     */
    protected function setUp(): void
    {
        parent::setUp();

        $this->user = User::factory()->create();
        $this->otherUser = User::factory()->create();
        $this->product = Product::factory()->create();
        
        $this->review = Review::factory()
            ->for($this->user)
            ->for($this->product)
            ->create(['rating' => 5]);
    }

    /**
     * Test: Can list all reviews with pagination
     */
    public function test_can_list_reviews_paginated(): void
    {
        Review::factory()
            ->count(20)
            ->for($this->product)
            ->for($this->user)
            ->create();

        $response = $this->actingAs($this->user)
            ->getJson('/api/v1/reviews');

        $response->assertStatus(200)
            ->assertJsonStructure([
                'data' => [
                    '*' => ['id', 'user_id', 'product_id', 'rating', 'comment', 'created_at']
                ],
                'links',
                'meta'
            ]);
    }

    /**
     * Test: Can view single review with relationships
     */
    public function test_can_view_single_review(): void
    {
        $response = $this->actingAs($this->user)
            ->getJson("/api/v1/reviews/{$this->review->id}");

        $response->assertStatus(200)
            ->assertJsonPath('data.id', $this->review->id)
            ->assertJsonPath('data.rating', 5)
            ->assertJsonStructure([
                'data' => [
                    'id', 'user_id', 'product_id', 'rating', 'comment',
                    'user' => ['id', 'name', 'email'],
                    'product' => ['id', 'name']
                ]
            ]);
    }

    /**
     * Test: Can create new review
     */
    public function test_can_create_review(): void
    {
        $data = [
            'user_id' => $this->user->id,
            'product_id' => $this->product->id,
            'rating' => 4,
            'comment' => 'Great product, highly recommend!',
        ];

        $response = $this->actingAs($this->user)
            ->postJson('/api/v1/reviews', $data);

        $response->assertStatus(201)
            ->assertJsonPath('data.rating', 4);

        $this->assertDatabaseHas('reviews', [
            'user_id' => $this->user->id,
            'product_id' => $this->product->id,
            'rating' => 4,
        ]);
    }

    /**
     * Test: Can update own review
     */
    public function test_can_update_own_review(): void
    {
        $updateData = ['rating' => 3, 'comment' => 'Changed my mind - 3 stars'];

        $response = $this->actingAs($this->user)
            ->putJson("/api/v1/reviews/{$this->review->id}", $updateData);

        $response->assertStatus(200);
        $this->assertDatabaseHas('reviews', [
            'id' => $this->review->id,
            'rating' => 3,
        ]);
    }

    /**
     * Test: Cannot update others' review (authorization)
     */
    public function test_cannot_update_others_review(): void
    {
        $response = $this->actingAs($this->otherUser)
            ->putJson("/api/v1/reviews/{$this->review->id}", ['rating' => 1]);

        $response->assertStatus(403); // Forbidden
    }

    /**
     * Test: Can delete own review
     */
    public function test_can_delete_own_review(): void
    {
        $response = $this->actingAs($this->user)
            ->deleteJson("/api/v1/reviews/{$this->review->id}");

        $response->assertStatus(204); // No Content
        $this->assertDatabaseMissing('reviews', ['id' => $this->review->id]);
    }

    /**
     * Test: Cannot delete others' review
     */
    public function test_cannot_delete_others_review(): void
    {
        $response = $this->actingAs($this->otherUser)
            ->deleteJson("/api/v1/reviews/{$this->review->id}");

        $response->assertStatus(403); // Forbidden
        $this->assertDatabaseHas('reviews', ['id' => $this->review->id]);
    }

    /**
     * Test: Create requires valid data
     */
    public function test_create_requires_validation(): void
    {
        $response = $this->actingAs($this->user)
            ->postJson('/api/v1/reviews', []);

        $response->assertStatus(422) // Unprocessable Entity
            ->assertJsonValidationErrors(['user_id', 'product_id', 'rating']);
    }

    /**
     * Test: Rating must be 1-5
     */
    public function test_rating_must_be_between_1_and_5(): void
    {
        $response = $this->actingAs($this->user)
            ->postJson('/api/v1/reviews', [
                'user_id' => $this->user->id,
                'product_id' => $this->product->id,
                'rating' => 10, // Invalid
                'comment' => 'Too high!',
            ]);

        $response->assertStatus(422)
            ->assertJsonValidationErrors(['rating']);
    }

    /**
     * Test: User IDs must exist
     */
    public function test_user_must_exist(): void
    {
        $response = $this->actingAs($this->user)
            ->postJson('/api/v1/reviews', [
                'user_id' => 9999, // Non-existent
                'product_id' => $this->product->id,
                'rating' => 5,
            ]);

        $response->assertStatus(422)
            ->assertJsonValidationErrors(['user_id']);
    }

    /**
     * Test: Requires authentication
     */
    public function test_requires_authentication(): void
    {
        $response = $this->getJson('/api/v1/reviews');

        $response->assertStatus(401); // Unauthorized
    }

    /**
     * Test: Comment is optional
     */
    public function test_comment_is_optional(): void
    {
        $response = $this->actingAs($this->user)
            ->postJson('/api/v1/reviews', [
                'user_id' => $this->user->id,
                'product_id' => $this->product->id,
                'rating' => 5,
                // comment omitted
            ]);

        $response->assertStatus(201);
        $this->assertDatabaseHas('reviews', [
            'rating' => 5,
            'comment' => null,
        ]);
    }
}
