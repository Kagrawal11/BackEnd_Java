package com.example.controller;

import com.example.dto.AdminTourRequestDTO;
import com.example.dto.ItineraryDTO;
import com.example.dto.TourDTO;
import com.example.entities.*;
import com.example.repositories.*;
import com.example.services.TourService;
import jakarta.transaction.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

// Admin CRUD for tour packages (CategoryMaster + its cost/departure/itinerary rows).
// Kept as one combined "package" shape rather than separate CRUD screens per table,
// since that's how a package is naturally created/edited and how the seed data is shaped.
@RestController
@RequestMapping("/api/admin/tours")
public class AdminTourController {

    private final CategoryRepository categoryRepository;
    private final CostRepository costRepository;
    private final DepartureRepository departureRepository;
    private final ItineraryRepository itineraryRepository;
    private final TourRepository tourRepository;
    private final TourService tourService;

    public AdminTourController(CategoryRepository categoryRepository, CostRepository costRepository,
            DepartureRepository departureRepository, ItineraryRepository itineraryRepository,
            TourRepository tourRepository, TourService tourService) {
        this.categoryRepository = categoryRepository;
        this.costRepository = costRepository;
        this.departureRepository = departureRepository;
        this.itineraryRepository = itineraryRepository;
        this.tourRepository = tourRepository;
        this.tourService = tourService;
    }

    @GetMapping
    public List<TourDTO> listAll() {
        return tourService.getAllTours();
    }

    @PostMapping
    @Transactional
    public TourDTO create(@RequestBody AdminTourRequestDTO dto) {
        CategoryMaster category = new CategoryMaster();
        applyCategoryFields(category, dto);
        category = categoryRepository.save(category);

        CostMaster cost = new CostMaster();
        cost.setCategory(category);
        applyCostFields(cost, dto);
        costRepository.save(cost);

        DepartureMaster departure = new DepartureMaster();
        departure.setCategory(category);
        applyDepartureFields(departure, dto);
        departure = departureRepository.save(departure);

        saveItineraries(category, dto.getItineraries());

        TourMaster tour = new TourMaster();
        tour.setCategory(category);
        tour.setDeparture(departure);
        tourRepository.save(tour);

        return tourService.getTourById(tour.getId());
    }

    @PutMapping("/{categoryId}")
    @Transactional
    public TourDTO update(@PathVariable Integer categoryId, @RequestBody AdminTourRequestDTO dto) {
        CategoryMaster category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Tour not found"));
        applyCategoryFields(category, dto);
        categoryRepository.save(category);

        // Replace the first cost/departure row (the common case: one package = one
        // price tier + one departure) rather than building a full sub-CRUD UI for them.
        List<CostMaster> costs = costRepository.findByCategoryId(categoryId);
        CostMaster cost = costs.isEmpty() ? new CostMaster() : costs.get(0);
        cost.setCategory(category);
        applyCostFields(cost, dto);
        costRepository.save(cost);

        List<DepartureMaster> departures = departureRepository.findByCategoryId(categoryId);
        DepartureMaster departure = departures.isEmpty() ? new DepartureMaster() : departures.get(0);
        departure.setCategory(category);
        applyDepartureFields(departure, dto);
        departureRepository.save(departure);

        if (dto.getItineraries() != null) {
            itineraryRepository.deleteAll(itineraryRepository.findByCategoryId(categoryId));
            saveItineraries(category, dto.getItineraries());
        }

        Integer anyTourId = tourRepository.findByCategory_Id(categoryId).stream()
                .findFirst().map(TourMaster::getId).orElse(null);

        return anyTourId != null ? tourService.getTourById(anyTourId) : null;
    }

    @DeleteMapping("/{categoryId}")
    @Transactional
    public ResponseEntity<Void> delete(@PathVariable Integer categoryId) {
        try {
            itineraryRepository.deleteAll(itineraryRepository.findByCategoryId(categoryId));
            tourRepository.deleteAll(tourRepository.findByCategory_Id(categoryId));
            costRepository.deleteAll(costRepository.findByCategoryId(categoryId));
            departureRepository.deleteAll(departureRepository.findByCategoryId(categoryId));
            categoryRepository.deleteById(categoryId);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            // Most likely existing bookings reference this tour - refuse rather than
            // cascading into booking history.
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Can't delete: this tour has existing bookings or related data");
        }
    }

    private void applyCategoryFields(CategoryMaster category, AdminTourRequestDTO dto) {
        category.setCategoryName(dto.getCategoryName());
        category.setCatCode(dto.getCatCode());
        category.setSubcatCode(dto.getSubcatCode() != null && !dto.getSubcatCode().isBlank()
                ? dto.getSubcatCode() : "^");
        category.setImagePath(dto.getImagePath());
        category.setJumpFlag(dto.getJumpFlag() != null ? dto.getJumpFlag() : true);
    }

    private void applyCostFields(CostMaster cost, AdminTourRequestDTO dto) {
        cost.setSinglePersonCost(dto.getSinglePersonCost());
        cost.setExtraPersonCost(dto.getExtraPersonCost());
        cost.setChildWithBedCost(dto.getChildWithBedCost());
        cost.setChildWithoutBedCost(dto.getChildWithoutBedCost());
        cost.setValidFrom(dto.getValidFrom());
        cost.setValidTo(dto.getValidTo());
    }

    private void applyDepartureFields(DepartureMaster departure, AdminTourRequestDTO dto) {
        departure.setDepartDate(dto.getDepartDate());
        departure.setEndDate(dto.getEndDate());
        departure.setNoOfDays(dto.getNoOfDays());
    }

    private void saveItineraries(CategoryMaster category, List<ItineraryDTO> itineraries) {
        if (itineraries == null) return;
        for (ItineraryDTO i : itineraries) {
            ItineraryMaster itinerary = new ItineraryMaster();
            itinerary.setCategory(category);
            itinerary.setDayNo(i.getDayNo());
            itinerary.setItineraryDetail(i.getItineraryDetail());
            itinerary.setDayWiseImage(i.getDayWiseImage());
            itineraryRepository.save(itinerary);
        }
    }
}
