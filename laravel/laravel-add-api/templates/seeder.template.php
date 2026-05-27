<?php

namespace Database\Seeders;

use App\Models\Review;
use App\Models\User;
use App\Models\Product;
use Illuminate\Database\Seeder;

class ReviewSeeder extends Seeder
{
    /**
     * Run the database seeds.
     */
    public function run(): void
    {
        // Get existing users and products
        $users = User::all();
        $products = Product::all();

        // Skip if no users or products
        if ($users->isEmpty() || $products->isEmpty()) {
            $this->command->warn('Please seed Users and Products first!');
            return;
        }

        // Create 50 reviews with random users and products
        Review::factory()
            ->count(50)
            ->sequence(function ($sequence) use ($users, $products) {
                return [
                    'user_id' => $users->random()->id,
                    'product_id' => $products->random()->id,
                ];
            })
            ->create();

        $this->command->info('Reviews seeded successfully!');
    }
}
