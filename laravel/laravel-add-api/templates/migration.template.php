<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\Schema;

return new class extends Migration
{
    /**
     * Run the migrations.
     */
    public function up(): void
    {
        Schema::create('reviews', function (Blueprint $table) {
            $table->id();
            
            // Foreign Keys
            $table->unsignedBigInteger('user_id');
            $table->unsignedBigInteger('product_id');
            
            // Data fields
            $table->integer('rating')->comment('1-5 star rating');
            $table->text('comment')->nullable();
            
            // Timestamps
            $table->timestamps();
            
            // Foreign key constraints
            $table->foreign('user_id')
                ->references('id')
                ->on('users')
                ->onDelete('cascade');
                
            $table->foreign('product_id')
                ->references('id')
                ->on('products')
                ->onDelete('cascade');
            
            // Indexes for performance
            $table->index('user_id');
            $table->index('product_id');
            $table->index('created_at');
        });
    }

    /**
     * Reverse the migrations.
     */
    public function down(): void
    {
        Schema::dropIfExists('reviews');
    }
};
