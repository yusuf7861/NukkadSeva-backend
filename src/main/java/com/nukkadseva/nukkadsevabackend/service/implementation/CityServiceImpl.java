package com.nukkadseva.nukkadsevabackend.service.implementation;

import com.nukkadseva.nukkadsevabackend.dto.request.CityWithPincodesRequest;
import com.nukkadseva.nukkadsevabackend.dto.response.CityWithPincodesResponse;
import com.nukkadseva.nukkadsevabackend.dto.response.PublicCityResponse;
import com.nukkadseva.nukkadsevabackend.entity.City;
import com.nukkadseva.nukkadsevabackend.entity.Pincode;
import com.nukkadseva.nukkadsevabackend.repository.CityRepository;
import com.nukkadseva.nukkadsevabackend.repository.PincodeRepository;
import com.nukkadseva.nukkadsevabackend.service.CityService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CityServiceImpl implements CityService {

    private final CityRepository cityRepository;
    private final PincodeRepository pincodeRepository;

    @Override
    @Transactional
    public CityWithPincodesResponse addCityWithPincodes(CityWithPincodesRequest request) {
        // Check if city already exists
        if (cityRepository.existsByCityName(request.getCityName().toUpperCase())) {
            throw new RuntimeException("City already exists: " + request.getCityName());
        }

        // Create new city
        City city = new City();
        city.setCityName(request.getCityName().toUpperCase());
        city.setState(request.getState());
        city.setIsActive(true);

        // Add pincodes to city
        for (CityWithPincodesRequest.PincodeRequest pincodeRequest : request.getPincodes()) {
            Pincode pincode = new Pincode();
            pincode.setPincode(pincodeRequest.getPincode());
            pincode.setAreaName(pincodeRequest.getAreaName());
            pincode.setIsActive(true);
            city.addPincode(pincode);
        }

        // Save city (cascades to pincodes)
        City savedCity = cityRepository.save(city);

        return mapToResponse(savedCity);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CityWithPincodesResponse> getAllCitiesWithPincodes() {
        List<City> cities = cityRepository.findAll();
        return cities.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public CityWithPincodesResponse getCityWithPincodesById(Long cityId) {
        City city = cityRepository.findById(cityId)
                .orElseThrow(() -> new RuntimeException("City not found with id: " + cityId));
        return mapToResponse(city);
    }

    @Override
    @Transactional
    public CityWithPincodesResponse addPincodesToCity(Long cityId,
            List<CityWithPincodesRequest.PincodeRequest> pincodes) {
        City city = cityRepository.findById(cityId)
                .orElseThrow(() -> new RuntimeException("City not found with id: " + cityId));

        for (CityWithPincodesRequest.PincodeRequest pincodeRequest : pincodes) {
            // Check if pincode already exists for this city
            if (!pincodeRepository.existsByPincodeAndCity(pincodeRequest.getPincode(), city)) {
                Pincode pincode = new Pincode();
                pincode.setPincode(pincodeRequest.getPincode());
                pincode.setAreaName(pincodeRequest.getAreaName());
                pincode.setIsActive(true);
                city.addPincode(pincode);
            }
        }

        City savedCity = cityRepository.save(city);
        return mapToResponse(savedCity);
    }

    @Override
    @Transactional
    public void deleteCity(Long cityId) {
        City city = cityRepository.findById(cityId)
                .orElseThrow(() -> new RuntimeException("City not found with id: " + cityId));
        cityRepository.delete(city);
    }

    @Override
    @Transactional
    public CityWithPincodesResponse toggleCityStatus(Long cityId, Boolean isActive) {
        City city = cityRepository.findById(cityId)
                .orElseThrow(() -> new RuntimeException("City not found with id: " + cityId));

        city.setIsActive(isActive);
        City updatedCity = cityRepository.save(city);

        return mapToResponse(updatedCity);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PublicCityResponse> getActiveCitiesForPublic() {
        List<City> activeCities = cityRepository.findByIsActiveTrue();
        return activeCities.stream()
                .map(city -> {
                    List<PublicCityResponse.PincodeInfo> activePincodes = city.getPincodes().stream()
                            .filter(p -> Boolean.TRUE.equals(p.getIsActive()))
                            .map(p -> new PublicCityResponse.PincodeInfo(p.getPincode(), p.getAreaName()))
                            .collect(Collectors.toList());

                    return new PublicCityResponse(city.getCityName(), city.getState(), activePincodes);
                })
                .collect(Collectors.toList());
    }

    private CityWithPincodesResponse mapToResponse(City city) {
        CityWithPincodesResponse response = new CityWithPincodesResponse();
        response.setId(city.getId());
        response.setCityName(city.getCityName());
        response.setState(city.getState());
        response.setIsActive(city.getIsActive());
        response.setCreatedAt(city.getCreatedAt());
        response.setUpdatedAt(city.getUpdatedAt());

        List<CityWithPincodesResponse.PincodeDto> pincodeDtos = city.getPincodes().stream()
                .map(pincode -> {
                    CityWithPincodesResponse.PincodeDto dto = new CityWithPincodesResponse.PincodeDto();
                    dto.setId(pincode.getId());
                    dto.setPincode(pincode.getPincode());
                    dto.setAreaName(pincode.getAreaName());
                    dto.setIsActive(pincode.getIsActive());
                    return dto;
                })
                .collect(Collectors.toList());

        response.setPincodes(pincodeDtos);
        return response;
    }
}
