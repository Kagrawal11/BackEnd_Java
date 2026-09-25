package com.example.services.impl;

import com.example.dto.ReviewCreateDTO;
import com.example.dto.ReviewDTO;
import com.example.dto.ReviewSummaryDTO;
import com.example.entities.CategoryMaster;
import com.example.entities.CustomerMaster;
import com.example.entities.ReviewMaster;
import com.example.repositories.CategoryRepository;
import com.example.repositories.CustomerRepository;
import com.example.repositories.ReviewRepository;
import com.example.services.ReviewService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final CategoryRepository categoryRepository;
    private final CustomerRepository customerRepository;

    public ReviewServiceImpl(ReviewRepository reviewRepository, CategoryRepository categoryRepository,
            CustomerRepository customerRepository) {
        this.reviewRepository = reviewRepository;
        this.categoryRepository = categoryRepository;
        this.customerRepository = customerRepository;
    }

    @Override
    public ReviewSummaryDTO getReviewsForCategory(Integer categoryId) {
        List<ReviewMaster> reviews = reviewRepository.findByCategory_IdOrderByCreatedAtDesc(categoryId);

        ReviewSummaryDTO summary = new ReviewSummaryDTO();
        Double avg = reviewRepository.findAverageRatingByCategoryId(categoryId);
        summary.setAverageRating(avg != null ? Math.round(avg * 10.0) / 10.0 : 0.0);
        summary.setReviewCount((long) reviews.size());
        summary.setReviews(reviews.stream().map(this::toDto).collect(Collectors.toList()));
        return summary;
    }

    @Override
    public void addReview(String customerEmail, ReviewCreateDTO dto) {
        if (dto.getRating() == null || dto.getRating() < 1 || dto.getRating() > 5) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Rating must be between 1 and 5");
        }

        CustomerMaster customer = customerRepository.findByEmail(customerEmail)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Customer not found"));

        CategoryMaster category = categoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Tour not found"));

        ReviewMaster review = new ReviewMaster();
        review.setCategory(category);
        review.setCustomer(customer);
        review.setRating(dto.getRating());
        review.setComment(dto.getComment());
        review.setCreatedAt(Instant.now());

        reviewRepository.save(review);
    }

    private ReviewDTO toDto(ReviewMaster r) {
        ReviewDTO dto = new ReviewDTO();
        dto.setId(r.getId());
        dto.setCategoryId(r.getCategory().getId());
        String name = (r.getCustomer().getFirstName() != null ? r.getCustomer().getFirstName() : "")
                + (r.getCustomer().getLastName() != null ? " " + r.getCustomer().getLastName() : "");
        dto.setCustomerName(name.isBlank() ? "Traveler" : name.trim());
        dto.setRating(r.getRating());
        dto.setComment(r.getComment());
        dto.setCreatedAt(r.getCreatedAt());
        return dto;
    }
}
