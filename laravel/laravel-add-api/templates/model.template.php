<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Factories\HasFactory;
use Illuminate\Database\Eloquent\Model;
use Laravel\Sanctum\HasApiTokens;

class Review extends Model
{
    use HasApiTokens, HasFactory;

    /**
     * The attributes that are mass assignable.
     */
    protected $fillable = [
        'user_id',
        'product_id',
        'rating',
        'comment',
    ];

    /**
     * Get the attributes that should be cast.
     */
    protected function casts(): array
    {
        return [
            'rating' => 'integer',
            'created_at' => 'datetime',
            'updated_at' => 'datetime',
        ];
    }

    /**
     * Get the user that created the review.
     */
    public function user()
    {
        return $this->belongsTo(User::class);
    }

    /**
     * Get the product being reviewed.
     */
    public function product()
    {
        return $this->belongsTo(Product::class);
    }

    /**
     * Scope: Filter by rating
     */
    public function scopeByRating($query, $rating)
    {
        return $query->where('rating', $rating);
    }

    /**
     * Scope: Filter by minimum rating
     */
    public function scopeMinRating($query, $minRating)
    {
        return $query->where('rating', '>=', $minRating);
    }
}
