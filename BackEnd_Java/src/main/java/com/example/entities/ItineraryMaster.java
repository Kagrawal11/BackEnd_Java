package com.example.entities;

import jakarta.persistence.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Table(name = "itinerary_master")
public class ItineraryMaster {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "itinerary_id", nullable = false)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "category_id", nullable = false)
    private CategoryMaster category;

    @Column(name = "day_no", nullable = false)
    private Integer dayNo;
    
    @Column(name = "itinerary_detail", nullable = false)
    private String itineraryDetail;

    // ✅ NEW COLUMN
    @Column(name = "day_wise_image")
    private String dayWiseImage;

    // ===== getters & setters =====

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public CategoryMaster getCategory() {
        return category;
    }

    public void setCategory(CategoryMaster category) {
        this.category = category;
    }

    public Integer getDayNo() {
        return dayNo;
    }

    public void setDayNo(Integer dayNo) {
        this.dayNo = dayNo;
    }

    public String getItineraryDetail() {
        return itineraryDetail;
    }

    public void setItineraryDetail(String itineraryDetail) {
        this.itineraryDetail = itineraryDetail;
    }

    public String getDayWiseImage() {
        return dayWiseImage;
    }

    public void setDayWiseImage(String dayWiseImage) {
        this.dayWiseImage = dayWiseImage;
    }
}
