package com.example.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class AdminTourRequestDTO {
    private String categoryName;
    private String catCode;
    private String subcatCode;
    private String imagePath;
    private Boolean jumpFlag;

    private BigDecimal singlePersonCost;
    private BigDecimal extraPersonCost;
    private BigDecimal childWithBedCost;
    private BigDecimal childWithoutBedCost;
    private LocalDate validFrom;
    private LocalDate validTo;

    private LocalDate departDate;
    private LocalDate endDate;
    private Integer noOfDays;

    private List<ItineraryDTO> itineraries;

    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }

    public String getCatCode() { return catCode; }
    public void setCatCode(String catCode) { this.catCode = catCode; }

    public String getSubcatCode() { return subcatCode; }
    public void setSubcatCode(String subcatCode) { this.subcatCode = subcatCode; }

    public String getImagePath() { return imagePath; }
    public void setImagePath(String imagePath) { this.imagePath = imagePath; }

    public Boolean getJumpFlag() { return jumpFlag; }
    public void setJumpFlag(Boolean jumpFlag) { this.jumpFlag = jumpFlag; }

    public BigDecimal getSinglePersonCost() { return singlePersonCost; }
    public void setSinglePersonCost(BigDecimal singlePersonCost) { this.singlePersonCost = singlePersonCost; }

    public BigDecimal getExtraPersonCost() { return extraPersonCost; }
    public void setExtraPersonCost(BigDecimal extraPersonCost) { this.extraPersonCost = extraPersonCost; }

    public BigDecimal getChildWithBedCost() { return childWithBedCost; }
    public void setChildWithBedCost(BigDecimal childWithBedCost) { this.childWithBedCost = childWithBedCost; }

    public BigDecimal getChildWithoutBedCost() { return childWithoutBedCost; }
    public void setChildWithoutBedCost(BigDecimal childWithoutBedCost) { this.childWithoutBedCost = childWithoutBedCost; }

    public LocalDate getValidFrom() { return validFrom; }
    public void setValidFrom(LocalDate validFrom) { this.validFrom = validFrom; }

    public LocalDate getValidTo() { return validTo; }
    public void setValidTo(LocalDate validTo) { this.validTo = validTo; }

    public LocalDate getDepartDate() { return departDate; }
    public void setDepartDate(LocalDate departDate) { this.departDate = departDate; }

    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }

    public Integer getNoOfDays() { return noOfDays; }
    public void setNoOfDays(Integer noOfDays) { this.noOfDays = noOfDays; }

    public List<ItineraryDTO> getItineraries() { return itineraries; }
    public void setItineraries(List<ItineraryDTO> itineraries) { this.itineraries = itineraries; }
}
