package com.example.controller;

import com.example.dto.ReviewCreateDTO;
import com.example.dto.ReviewSummaryDTO;
import com.example.services.ReviewService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reviews")
public class ReviewController {

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @GetMapping("/category/{categoryId}")
    public ReviewSummaryDTO getReviews(@PathVariable Integer categoryId) {
        return reviewService.getReviewsForCategory(categoryId);
    }

    @PostMapping
    public ResponseEntity<Void> addReview(@RequestBody ReviewCreateDTO dto, Authentication authentication) {
        reviewService.addReview(authentication.getName(), dto);
        return ResponseEntity.ok().build();
    }
}
