<?php

namespace Database\Factories;

use Illuminate\Database\Eloquent\Factories\Factory;
use App\Models\Review;
use App\Models\User;
use App\Models\Product;

class ReviewFactory extends Factory
{
    /**
     * The name of the factory's corresponding model.
     */
    protected $model = Review::class;

    /**
     * Define the model's default state.
     */
    public function definition(): array
    {
        return [
            'user_id' => User::factory(),
            'product_id' => Product::factory(),
            'rating' => $this->faker->numberBetween(1, 5),
            'comment' => $this->faker->paragraph(),
        ];
    }

    /**
     * State: Create a high-rating review
     */
    public function highRating(): static
    {
        return $this->state(fn (array $attributes) => [
            'rating' => $this->faker->numberBetween(4, 5),
            'comment' => $this->faker->sentence() . ' Great product!',
        ]);
    }

    /**
     * State: Create a low-rating review
     */
    public function lowRating(): static
    {
        return $this->state(fn (array $attributes) => [
            'rating' => $this->faker->numberBetween(1, 2),
            'comment' => $this->faker->sentence() . ' Not satisfied.',
        ]);
    }

    /**
     * State: Create a review without comment
     */
    public function withoutComment(): static
    {
        return $this->state(fn (array $attributes) => [
            'comment' => null,
        ]);
    }
}
