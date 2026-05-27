<?php

namespace App\Http\Controllers\Api\V1;

use App\Http\Controllers\Controller;
use App\Models\Review;
use Illuminate\Http\Request;
use Illuminate\Http\Response;

class ReviewController extends Controller
{
    /**
     * Authorize all operations using ReviewPolicy
     */
    public function __construct()
    {
        $this->authorizeResource(Review::class);
    }

    /**
     * Display a listing of reviews.
     * GET /api/v1/reviews
     */
    public function index()
    {
        return Review::with(['user', 'product'])
            ->latest()
            ->paginate(15);
    }

    /**
     * Store a newly created review.
     * POST /api/v1/reviews
     */
    public function store(Request $request)
    {
        $validated = $request->validate([
            'user_id' => 'required|exists:users,id',
            'product_id' => 'required|exists:products,id',
            'rating' => 'required|integer|between:1,5',
            'comment' => 'nullable|string|max:1000',
        ]);

        $review = Review::create($validated);

        return response(
            $review->load(['user', 'product']),
            Response::HTTP_CREATED
        );
    }

    /**
     * Display a single review.
     * GET /api/v1/reviews/{id}
     */
    public function show(Review $review)
    {
        return $review->load(['user', 'product']);
    }

    /**
     * Update the specified review.
     * PUT /api/v1/reviews/{id}
     */
    public function update(Request $request, Review $review)
    {
        $validated = $request->validate([
            'rating' => 'sometimes|integer|between:1,5',
            'comment' => 'nullable|string|max:1000',
        ]);

        $review->update($validated);

        return $review->load(['user', 'product']);
    }

    /**
     * Delete the specified review.
     * DELETE /api/v1/reviews/{id}
     */
    public function destroy(Review $review)
    {
        $review->delete();

        return response(null, Response::HTTP_NO_CONTENT);
    }
}
