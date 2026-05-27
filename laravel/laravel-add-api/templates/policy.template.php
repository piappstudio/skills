<?php

namespace App\Policies;

use App\Models\Review;
use App\Models\User;

class ReviewPolicy
{
    /**
     * Determine if the user can view any reviews.
     */
    public function viewAny(User $user): bool
    {
        return true; // Any authenticated user can list reviews
    }

    /**
     * Determine if the user can view the review.
     */
    public function view(User $user, Review $review): bool
    {
        return true; // Any authenticated user can view
    }

    /**
     * Determine if the user can create reviews.
     */
    public function create(User $user): bool
    {
        return true; // Any authenticated user can create
    }

    /**
     * Determine if the user can update the review.
     * User can update their own review or if admin
     */
    public function update(User $user, Review $review): bool
    {
        return $user->id === $review->user_id || $user->role === 'admin';
    }

    /**
     * Determine if the user can delete the review.
     * User can delete their own review or if admin
     */
    public function delete(User $user, Review $review): bool
    {
        return $user->id === $review->user_id || $user->role === 'admin';
    }

    /**
     * Determine if the user can restore the review.
     * Admin only
     */
    public function restore(User $user, Review $review): bool
    {
        return $user->role === 'admin';
    }

    /**
     * Determine if the user can permanently delete the review.
     * Admin only
     */
    public function forceDelete(User $user, Review $review): bool
    {
        return $user->role === 'admin';
    }
}
