package com.example.repositories;

import com.example.entities.ReviewMaster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ReviewRepository extends JpaRepository<ReviewMaster, Integer> {

    List<ReviewMaster> findByCategory_IdOrderByCreatedAtDesc(Integer categoryId);

    @Query("select avg(r.rating) from ReviewMaster r where r.category.id = :categoryId")
    Double findAverageRatingByCategoryId(@Param("categoryId") Integer categoryId);

    Long countByCategory_Id(Integer categoryId);

    boolean existsByCategory_IdAndCustomer_Id(Integer categoryId, Integer customerId);
}
