package com.example.dto;

import java.util.List;

public class ReviewSummaryDTO {
    private Double averageRating;
    private Long reviewCount;
    private List<ReviewDTO> reviews;

    public Double getAverageRating() { return averageRating; }
    public void setAverageRating(Double averageRating) { this.averageRating = averageRating; }

    public Long getReviewCount() { return reviewCount; }
    public void setReviewCount(Long reviewCount) { this.reviewCount = reviewCount; }

    public List<ReviewDTO> getReviews() { return reviews; }
    public void setReviews(List<ReviewDTO> reviews) { this.reviews = reviews; }
}
