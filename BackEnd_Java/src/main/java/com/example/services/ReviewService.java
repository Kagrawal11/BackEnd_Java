package com.example.services;

import com.example.dto.ReviewCreateDTO;
import com.example.dto.ReviewSummaryDTO;

public interface ReviewService {
    ReviewSummaryDTO getReviewsForCategory(Integer categoryId);

    void addReview(String customerEmail, ReviewCreateDTO dto);
}
