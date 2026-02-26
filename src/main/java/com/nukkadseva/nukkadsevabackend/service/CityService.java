package com.nukkadseva.nukkadsevabackend.service;

import com.nukkadseva.nukkadsevabackend.dto.request.CityWithPincodesRequest;
import com.nukkadseva.nukkadsevabackend.dto.response.CityWithPincodesResponse;
import com.nukkadseva.nukkadsevabackend.dto.response.PublicCityResponse;
import com.nukkadseva.nukkadsevabackend.entity.City;

import java.util.List;

public interface CityService {

    /**
     * Add a new city with its related pincodes
     * 
     * @param request The city and pincode details
     * @return The created city with pincodes
     */
    CityWithPincodesResponse addCityWithPincodes(CityWithPincodesRequest request);

    /**
     * Get all cities with their pincodes
     * 
     * @return List of cities with pincodes
     */
    List<CityWithPincodesResponse> getAllCitiesWithPincodes();

    /**
     * Get a city by ID with its pincodes
     * 
     * @param cityId The city ID
     * @return The city with pincodes
     */
    CityWithPincodesResponse getCityWithPincodesById(Long cityId);

    /**
     * Add pincodes to an existing city
     * 
     * @param cityId   The city ID
     * @param pincodes List of pincodes to add
     * @return Updated city with pincodes
     */
    CityWithPincodesResponse addPincodesToCity(Long cityId, List<CityWithPincodesRequest.PincodeRequest> pincodes);

    /**
     * Delete a city and its pincodes
     * 
     * @param cityId The city ID
     */
    void deleteCity(Long cityId);

    /**
     * Toggle city active status
     * 
     * @param cityId   The city ID
     * @param isActive The active status
     * @return Updated city
     */
    CityWithPincodesResponse toggleCityStatus(Long cityId, Boolean isActive);

    /**
     * Get all active cities with their active pincodes (public-facing, minimal
     * fields)
     * 
     * @return List of public city responses
     */
    List<PublicCityResponse> getActiveCitiesForPublic();
}
